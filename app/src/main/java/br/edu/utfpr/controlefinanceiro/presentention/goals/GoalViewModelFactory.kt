package br.edu.utfpr.controlefinanceiro.presentention.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import br.edu.utfpr.controlefinanceiro.data.repository.FinancialGoalRepository
import br.edu.utfpr.controlefinanceiro.data.repository.TransactionRepository

/**
 * Factory para criar instâncias de GoalViewModel com seus repositórios.
 */
class GoalViewModelFactory(
    private val financialGoalRepository: FinancialGoalRepository,
    private val transactionRepository: TransactionRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GoalViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GoalViewModel(financialGoalRepository, transactionRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}