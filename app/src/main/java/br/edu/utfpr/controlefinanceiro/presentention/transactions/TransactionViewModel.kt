package br.edu.utfpr.controlefinanceiro.presentention.transactions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.utfpr.controlefinanceiro.data.model.Category
import br.edu.utfpr.controlefinanceiro.data.model.Transaction
import br.edu.utfpr.controlefinanceiro.data.repository.CategoryRepository
import br.edu.utfpr.controlefinanceiro.data.repository.TransactionRepository
import kotlinx.coroutines.launch
import java.util.Date
import kotlin.math.round

/**
 * ViewModel central, responsável por Transações (RF02), Saldos (RF05), Busca (RF08) e Exportação (RF09).
 */
class TransactionViewModel(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository // Necessário para suporte a categorias
) : ViewModel() {

    // --- Dados Principais e Dashboard (RF05) ---
    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> = _transactions // Lista principal de todas as transações

    private val _totalBalance = MutableLiveData<Double>()
    val totalBalance: LiveData<Double> = _totalBalance

    private val _totalIncome = MutableLiveData<Double>()
    val totalIncome: LiveData<Double> = _totalIncome

    private val _totalExpense = MutableLiveData<Double>()
    val totalExpense: LiveData<Double> = _totalExpense

    // --- LiveData de Suporte ---
    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories // Para spinners de filtro/registro

    private val _filteredTransactions = MutableLiveData<List<Transaction>>()
    val filteredTransactions: LiveData<List<Transaction>> = _filteredTransactions // Para tela de Busca (RF08)

    private val _exportFileUri = MutableLiveData<String?>()
    val exportFileUri: LiveData<String?> = _exportFileUri // Para notificação de Exportação (RF09)

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    init {
        loadCategories()
        loadAndCalculateTransactions()
    }

    /**
     * Carrega todas as transações, atualiza a lista e calcula os saldos (RF05).
     */
    fun loadAndCalculateTransactions() {
        viewModelScope.launch {
            try {
                val allTransactions = transactionRepository.getAllUserTransactions()
                _transactions.postValue(allTransactions)
                calculateFinancialSummary(allTransactions)
            } catch (e: Exception) {
                _message.postValue("Erro ao carregar dados: ${e.message}")
            }
        }
    }

    /**
     * Realiza o cálculo do saldo total, receitas e despesas (RF05).
     */
    private fun calculateFinancialSummary(transactions: List<Transaction>) {
        val income = transactions.filter { it.type == "RECEITA" }.sumOf { it.value }
        val expense = transactions.filter { it.type == "DESPESA" }.sumOf { it.value }
        val balance = income - expense

        // Arredonda para duas casas decimais
        _totalIncome.postValue(round(income * 100) / 100)
        _totalExpense.postValue(round(expense * 100) / 100)
        _totalBalance.postValue(round(balance * 100) / 100)
    }

    /**
     * Carrega categorias (RF03).
     */
    fun loadCategories() {
        viewModelScope.launch {
            try {
                _categories.postValue(categoryRepository.getAllUserCategories())
            } catch (e: Exception) {
                _message.postValue("Erro ao carregar categorias: ${e.message}")
            }
        }
    }

    /**
     * Registra uma nova transação. Após o sucesso, recarrega o Dashboard. (RF02)
     */
    fun saveTransaction(value: Double, description: String, type: String, categoryId: String, date: Date) {
        if (value <= 0 || description.isBlank() || categoryId.isBlank()) {
            _message.postValue("Preencha todos os campos obrigatórios corretamente.")
            return
        }

        viewModelScope.launch {
            val newTransaction = Transaction(
                value = value,
                description = description,
                type = type,
                categoryId = categoryId,
                date = date,
            )
            try {
                transactionRepository.saveTransaction(newTransaction)
                _message.postValue("Transação registrada com sucesso!")

                loadAndCalculateTransactions() // Recarrega saldos e lista principal
                // A notificação ao GoalViewModel ficaria aqui
                // Ex: goalViewModel.checkGoalAlerts()

            } catch (e: Exception) {
                _message.postValue("Falha ao registrar transação: ${e.message}")
            }
        }
    }

    // --- Funcionalidade de Busca (RF08) ---

    fun searchTransactions(query: String?, categoryId: String?, startDate: Date?, endDate: Date?) {
        viewModelScope.launch {
            try {
                val allTransactions = transactionRepository.getAllUserTransactions()
                var filteredList = allTransactions

                // Implementa o filtro por descrição (Quadro 12)
                if (!query.isNullOrBlank()) {
                    val lowerCaseQuery = query.lowercase()
                    filteredList = filteredList.filter {
                        it.description.lowercase().contains(lowerCaseQuery)
                    }
                }

                // [PENDENTE]: Adicionar aqui a lógica de filtragem por categoryId, startDate e endDate
                // Ex: if (!categoryId.isNullOrBlank()) { ... }

                _filteredTransactions.postValue(filteredList.sortedByDescending { it.date })
            } catch (e: Exception) {
                _message.postValue("Erro na busca: ${e.message}")
            }
        }
    }

    // --- Funcionalidade de Exportação (RF09) ---

    fun exportTransactions(format: String, startDate: Date?, endDate: Date?) {
        viewModelScope.launch {
            try {
                val transactionsToExport = transactionRepository.getAllUserTransactions()
                    .filter { it.date >= (startDate ?: Date(0)) && it.date <= (endDate ?: Date()) }

                if (transactionsToExport.isEmpty()) {
                    _message.postValue("Nenhuma transação para exportar.")
                    return@launch
                }

                // [PENDENTE] Adicionar lógica para exibir o ProgressBar antes da geração

                // 2. Geração do Arquivo (Simulação)
                val generatedFilePath = when (format.uppercase()) {
                    "PDF" -> generatePdfReport(transactionsToExport)
                    "EXCEL" -> generateExcelReport(transactionsToExport)
                    else -> throw IllegalArgumentException("Formato inválido.")
                }

                // 3. Notificar a UI para iniciar o download
                _exportFileUri.postValue(generatedFilePath)
                _message.postValue("Arquivo $format gerado com sucesso!")

            } catch (e: Exception) {
                // Exceção: Falha na geração do arquivo (Quadro 13)
                _message.postValue("Falha na exportação: ${e.message}")
            }
        }
    }

    fun clearExportFileUri() {
        _exportFileUri.postValue(null)
    }

    // --- Métodos de Simulação de Geração (Placeholder) ---

    private fun generatePdfReport(transactions: List<Transaction>): String {
        // Lógica real de criação de arquivo PDF (necessita de biblioteca)
        return "Relatorio_Financeiro_${System.currentTimeMillis()}.pdf"
    }

    private fun generateExcelReport(transactions: List<Transaction>): String {
        // Lógica real de criação de arquivo Excel (necessita de biblioteca)
        return "Relatorio_Financeiro_${System.currentTimeMillis()}.xlsx"
    }
}