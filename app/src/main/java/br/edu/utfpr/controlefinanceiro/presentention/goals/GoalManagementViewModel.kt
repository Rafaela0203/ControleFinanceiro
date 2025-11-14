package br.edu.utfpr.controlefinanceiro.presentention.goals

// NOVO: Import do Context e do NotificationHelper
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.utfpr.controlefinanceiro.data.model.FinancialGoal
import br.edu.utfpr.controlefinanceiro.data.repository.FinancialGoalRepository
import br.edu.utfpr.controlefinanceiro.data.repository.TransactionRepository
// NOVO: Import do NotificationHelper
import br.edu.utfpr.controlefinanceiro.utils.NotificationHelper
import kotlinx.coroutines.launch
import java.util.Calendar // NOVO: Import para filtro de data
import java.util.Date

/**
 * ViewModel para Definição e Gerenciamento de Metas Financeiras (RF04) e Alertas (RF07).
 */
class GoalViewModel(
    private val goalRepository: FinancialGoalRepository,
    private val transactionRepository: TransactionRepository // Para monitorar gastos/economia
) : ViewModel() {

    private val _goals = MutableLiveData<List<FinancialGoal>>()
    val goals: LiveData<List<FinancialGoal>> = _goals

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    init {
        loadGoals()
    }

    fun loadGoals() {
        viewModelScope.launch {
            try {
                val goalList = goalRepository.getAllUserGoals()
                // AQUI: Você precisará integrar o FinancialGoal com TransactionRepository
                // para atualizar o 'savedValue' (progresso da meta) antes de postar a lista.
                _goals.postValue(goalList)
            } catch (e: Exception) {
                _message.postValue("Erro ao carregar metas: ${e.message}")
            }
        }
    }

    /**
     * Salva uma nova meta.
     */
    fun saveGoal(name: String, targetValue: Double, deadline: Date, type: String, categoryId: String? = null) {
        if (name.isBlank() || targetValue <= 0) {
            _message.postValue("Preencha todos os campos da meta corretamente.")
            return
        }

        viewModelScope.launch {
            val goalToSave = FinancialGoal(
                name = name,
                targetValue = targetValue,
                deadline = deadline,
                type = type,
                categoryId = categoryId
                // savedValue é 0.0 na criação, atualizado pelo monitoramento
            )
            try {
                goalRepository.saveGoal(goalToSave)
                _message.postValue("Meta salva com sucesso.")
                loadGoals()
            } catch (e: Exception) {
                _message.postValue("Falha ao salvar meta: ${e.message}")
            }
        }
    }

    /**
     * Lógica de Verificação de Alerta (RF07).
     * Esta função será chamada sempre que uma nova transação for registrada.
     *
     * NOVO: Agora ela recebe o Context para poder disparar a notificação.
     */
    fun checkGoalAlerts(context: Context) { // NOVO: Recebe o Context
        viewModelScope.launch {
            try {
                val transactions = transactionRepository.getAllUserTransactions()
                val goals = goalRepository.getAllUserGoals()

                // Pega o início e fim do mês atual para filtrar as transações
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                val inicioDoMes = calendar.time

                calendar.add(Calendar.MONTH, 1)
                calendar.add(Calendar.DAY_OF_MONTH, -1)
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                val fimDoMes = calendar.time

                goals.filter { it.type == "LIMITE_GASTOS" && it.categoryId != null }.forEach { limitGoal ->

                    val currentExpense = transactions
                        .filter {
                            it.type == "DESPESA" &&
                                    it.categoryId == limitGoal.categoryId &&
                                    it.date.after(inicioDoMes) && // Filtra transações deste mês
                                    it.date.before(fimDoMes)
                        }

                    val sumExpense = currentExpense.sumOf { it.value }

                    // NOVO: Verifica se a meta já foi alertada este mês (lógica simples)
                    // (Para uma lógica mais robusta, você precisaria salvar o estado do alerta na própria meta)
                    val limiteAlcancado = sumExpense >= limitGoal.targetValue
                    val limiteAnterior = (sumExpense - (transactions.lastOrNull()?.value ?: 0.0)) < limitGoal.targetValue

                    // Só dispara a notificação na *primeira vez* que o limite é ultrapassado
                    if (limiteAlcancado && limiteAnterior) {

                        // NOVO: Chama o NotificationHelper em vez de usar o _message
                        NotificationHelper.sendGoalAlertNotification(
                            context.applicationContext, // Usa o applicationContext por segurança
                            "Limite Atingido!",
                            "Você atingiu seu limite de R$${limitGoal.targetValue} para a meta '${limitGoal.name}'."
                        )
                        // Você ainda pode usar o _message se quiser um Toast na tela
                        _message.postValue("Você ultrapassou o limite para ${limitGoal.name}!")
                    }
                }
            } catch (e: Exception) {
                // Se falhar, pelo menos loga o erro
                _message.postValue("Erro ao verificar alertas: ${e.message}")
            }
        }
    }
}