package br.edu.utfpr.controlefinanceiro.presentention.recurrences

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.utfpr.controlefinanceiro.data.model.Recurrence
import br.edu.utfpr.controlefinanceiro.data.model.Transaction
import br.edu.utfpr.controlefinanceiro.data.repository.RecurrenceRepository
import br.edu.utfpr.controlefinanceiro.data.repository.TransactionRepository
import kotlinx.coroutines.launch
import java.util.Date

/**
 * ViewModel para Gerenciar Transações Recorrentes (RF06 / Quadro 10).
 */
class RecurrenceViewModel(
    private val recurrenceRepository: RecurrenceRepository,
    private val transactionRepository: TransactionRepository // Para registrar a transação gerada
) : ViewModel() {

    private val _recurrences = MutableLiveData<List<Recurrence>>()
    val recurrences: LiveData<List<Recurrence>> = _recurrences

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    init {
        loadRecurrences()
    }

    fun loadRecurrences() {
        viewModelScope.launch {
            try {
                val list = recurrenceRepository.getAllUserRecurrences()
                _recurrences.postValue(list)
            } catch (e: Exception) {
                _message.postValue("Erro ao carregar recorrências: ${e.message}")
            }
        }
    }

    fun saveRecurrence(recurrence: Recurrence) {
        // Implementar validações (Exceção: Frequência não informada - Quadro 10)
        viewModelScope.launch {
            try {
                recurrenceRepository.saveRecurrence(recurrence)
                _message.postValue("Recorrência salva com sucesso.")
                loadRecurrences()
            } catch (e: Exception) {
                _message.postValue("Falha ao salvar recorrência: ${e.message}")
            }
        }
    }

    /**
     * Lógica essencial: Gera a transação real a partir da recorrência quando a data de execução chega.
     */
    fun processRecurrence(recurrence: Recurrence) {
        viewModelScope.launch {
            if (recurrence.nextExecutionDate.before(Date())) {
                val newTransaction = Transaction(
                    value = recurrence.value,
                    description = recurrence.description + " (Recorrente)",
                    type = "DESPESA", // Tipo precisa ser definido no modelo de Recurrence
                    categoryId = recurrence.categoryId,
                    date = recurrence.nextExecutionDate,
                    recurrenceId = recurrence.id
                )

                try {
                    transactionRepository.saveTransaction(newTransaction)

                    // IMPORTANTE: Atualizar a próxima data de execução no objeto Recurrence
                    // nextExecutionDate = calcularProximaData(recurrence.frequency)
                    // recurrenceRepository.updateRecurrence(recurrence com nova data)

                } catch (e: Exception) {
                    _message.postValue("Erro ao processar lançamento recorrente: ${recurrence.description}")
                }
            }
        }
    }
}