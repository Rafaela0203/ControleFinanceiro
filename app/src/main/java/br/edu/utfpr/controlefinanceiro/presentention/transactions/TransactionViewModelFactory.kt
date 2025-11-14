package br.edu.utfpr.controlefinanceiro.presentention.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import br.edu.utfpr.controlefinanceiro.data.repository.CategoryRepository
import br.edu.utfpr.controlefinanceiro.data.repository.TransactionRepository

class TransactionViewModelFactory(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransactionViewModel::class.java)) {
            return TransactionViewModel(
                transactionRepository,
                categoryRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}