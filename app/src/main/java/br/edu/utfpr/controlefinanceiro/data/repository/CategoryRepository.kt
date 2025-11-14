package br.edu.utfpr.controlefinanceiro.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import br.edu.utfpr.controlefinanceiro.data.model.Category
import kotlinx.coroutines.tasks.await

class CategoryRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val categoriesCollection = firestore.collection("categories")
    // Acesso direto à coleção de transações para verificar a regra de negócio
    private val transactionsCollection = firestore.collection("transactions")

    private fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: throw IllegalStateException("Usuário não autenticado")
    }

    suspend fun saveCategory(category: Category) {
        val categoryWithUserId = category.copy(userId = getCurrentUserId())

        try {
            // Se category.id for null, o Firestore gera um novo ID.
            if (category.id == null) {
                categoriesCollection.add(categoryWithUserId).await()
            } else {
                // Para atualizações, usamos set() no documento existente
                categoriesCollection.document(category.id).set(categoryWithUserId).await()
            }
        } catch (e: Exception) {
            println("Erro ao salvar categoria: ${e.message}")
            throw e
        }
    }

    suspend fun getAllUserCategories(): List<Category> {
        val userId = getCurrentUserId()
        return try {
            val querySnapshot = categoriesCollection
                .whereEqualTo("userId", userId)
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .await()

            querySnapshot.toObjects(Category::class.java)

        } catch (e: Exception) {
            println("Erro ao buscar categorias: ${e.message}")
            emptyList()
        }
    }

    suspend fun updateCategory(category: Category) {
        val categoryId = category.id
            ?: throw IllegalArgumentException("ID da categoria não pode ser nulo para atualização.")
        val userId = getCurrentUserId()

        // Verifica a permissão/propriedade antes de atualizar (RNF08)
        val categoryData = categoriesCollection.document(categoryId).get().await()
            .toObject(Category::class.java)
        if (categoryData?.userId != userId) {
            throw SecurityException("Usuário não tem permissão para atualizar esta categoria.")
        }

        try {
            categoriesCollection.document(categoryId).set(category).await()
        } catch (e: Exception) {
            println("Erro ao atualizar categoria: ${e.message}")
            throw e
        }
    }

    /**
     * Deleta uma categoria após verificar se não há transações vinculadas.
     */
    suspend fun deleteCategory(categoryId: String) {
        val userId = getCurrentUserId()

        // 1. VERIFICAÇÃO DE VÍNCULOS (Regra de Negócio / Exceção do Caso de Uso)
        val linkedTransactions = transactionsCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("categoryId", categoryId)
            .limit(1) // Só precisamos saber se existe pelo menos uma
            .get()
            .await()
            .documents

        if (linkedTransactions.isNotEmpty()) {
            // Exceção do Caso de Uso: Categoria vinculada não pode ser excluída.
            throw IllegalStateException("Categoria vinculada a transações. Não pode ser excluída.")
        }

        // 2. DELEÇÃO
        try {
            // (A verificação de propriedade já ocorreu, mas repetimos para garantir se o item não foi lido antes)
            val doc = categoriesCollection.document(categoryId).get().await()
            if (doc.getString("userId") != userId) {
                throw SecurityException("Tentativa de deletar categoria de outro usuário.")
            }

            categoriesCollection.document(categoryId).delete().await()
        } catch (e: Exception) {
            println("Erro ao deletar categoria: ${e.message}")
            throw e
        }
    }
}