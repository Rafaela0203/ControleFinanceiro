package br.edu.utfpr.controlefinanceiro.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import br.edu.utfpr.controlefinanceiro.data.model.Recurrence
import kotlinx.coroutines.tasks.await

/**
 * Repositório responsável pelo CRUD das Transações Recorrentes (RF06).
 */
class RecurrenceRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val recurrencesCollection = firestore.collection("recurrences")

    private fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: throw IllegalStateException("Usuário não autenticado")
    }

    /**
     * Salva ou atualiza uma transação recorrente (RF06).
     */
    suspend fun saveRecurrence(recurrence: Recurrence) {
        val recurrenceWithUserId = recurrence.copy(userId = getCurrentUserId())

        try {
            if (recurrence.id == null) {
                // Novo registro
                recurrencesCollection.add(recurrenceWithUserId).await()
            } else {
                // Atualização
                recurrencesCollection.document(recurrence.id).set(recurrenceWithUserId).await()
            }
        } catch (e: Exception) {
            println("Erro ao salvar recorrência: ${e.message}")
            throw e
        }
    }

    /**
     * Busca todas as recorrências para o usuário logado (RF06).
     */
    suspend fun getAllUserRecurrences(): List<Recurrence> {
        val userId = getCurrentUserId()
        return try {
            val querySnapshot = recurrencesCollection
                .whereEqualTo("userId", userId)
                .orderBy("nextExecutionDate", com.google.firebase.firestore.Query.Direction.ASCENDING)
                .get()
                .await()

            querySnapshot.toObjects(Recurrence::class.java)

        } catch (e: Exception) {
            println("Erro ao buscar recorrências: ${e.message}")
            emptyList()
        }
    }

    /**
     * Deleta uma recorrência (RF06).
     */
    suspend fun deleteRecurrence(recurrenceId: String) {
        val userId = getCurrentUserId()
        try {
            val doc = recurrencesCollection.document(recurrenceId).get().await()
            if (doc.getString("userId") == userId) {
                recurrencesCollection.document(recurrenceId).delete().await()
            } else {
                throw SecurityException("Tentativa de deletar recorrência de outro usuário.")
            }
        } catch (e: Exception) {
            println("Erro ao deletar recorrência: ${e.message}")
            throw e
        }
    }
}