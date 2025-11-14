package br.edu.utfpr.controlefinanceiro.data.repository

import br.edu.utfpr.controlefinanceiro.data.model.Transaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await


class TransactionRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val transactionsCollection = firestore.collection("transactions")

    private fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: throw IllegalStateException("Usuário não autenticado")
    }

    suspend fun saveTransaction(transaction: Transaction) {
        val transactionWithUserId = transaction.copy(userId = getCurrentUserId())

        try {
            transactionsCollection.add(transactionWithUserId).await()
        } catch (e: Exception) {
            println("Erro ao salvar transação: ${e.message}")
            throw e
        }
    }

    suspend fun getAllUserTransactions(): List<Transaction> {
        val userId = getCurrentUserId()
        return try {
            val querySnapshot = transactionsCollection
                .whereEqualTo("userId", userId)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .await()

            querySnapshot.toObjects(Transaction::class.java)

        } catch (e: Exception) {
            println("Erro ao buscar transações: ${e.message}")
            emptyList()
        }
    }

    suspend fun deleteTransaction(transactionId: String) {
        val userId = getCurrentUserId()
        try {
            val doc = transactionsCollection.document(transactionId).get().await()
            if (doc.getString("userId") == userId) {
                transactionsCollection.document(transactionId).delete().await()
            } else {
                throw SecurityException("Tentativa de deletar transação de outro usuário.")
            }
        } catch (e: Exception) {
            println("Erro ao deletar transação: ${e.message}")
            throw e
        }
    }
}