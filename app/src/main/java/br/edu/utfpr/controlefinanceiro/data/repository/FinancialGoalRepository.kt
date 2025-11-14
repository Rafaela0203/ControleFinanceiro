package br.edu.utfpr.controlefinanceiro.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import br.edu.utfpr.controlefinanceiro.data.model.FinancialGoal
import kotlinx.coroutines.tasks.await

/**
 * Repositório responsável pelo CRUD das Metas Financeiras (RF04).
 */
class FinancialGoalRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val goalsCollection = firestore.collection("financialGoals")

    private fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: throw IllegalStateException("Usuário não autenticado")
    }

    /**
     * Salva ou atualiza uma meta financeira (RF04).
     */
    suspend fun saveGoal(goal: FinancialGoal) {
        val goalWithUserId = goal.copy(userId = getCurrentUserId())

        try {
            if (goal.id == null) {
                // Nova meta
                goalsCollection.add(goalWithUserId).await()
            } else {
                // Atualização de meta existente
                goalsCollection.document(goal.id).set(goalWithUserId).await()
            }
        } catch (e: Exception) {
            println("Erro ao salvar meta: ${e.message}")
            throw e
        }
    }

    /**
     * Busca todas as metas para o usuário logado (RF04).
     */
    suspend fun getAllUserGoals(): List<FinancialGoal> {
        val userId = getCurrentUserId()
        return try {
            val querySnapshot = goalsCollection
                .whereEqualTo("userId", userId)
                .orderBy("deadline", com.google.firebase.firestore.Query.Direction.ASCENDING)
                .get()
                .await()

            querySnapshot.toObjects(FinancialGoal::class.java)

        } catch (e: Exception) {
            println("Erro ao buscar metas: ${e.message}")
            emptyList()
        }
    }

    /**
     * Deleta uma meta (RF04).
     */
    suspend fun deleteGoal(goalId: String) {
        val userId = getCurrentUserId()
        try {
            // Verifica a propriedade do usuário antes de deletar (RNF08)
            val doc = goalsCollection.document(goalId).get().await()
            if (doc.getString("userId") == userId) {
                goalsCollection.document(goalId).delete().await()
            } else {
                throw SecurityException("Tentativa de deletar meta de outro usuário.")
            }
        } catch (e: Exception) {
            println("Erro ao deletar meta: ${e.message}")
            throw e
        }
    }
}