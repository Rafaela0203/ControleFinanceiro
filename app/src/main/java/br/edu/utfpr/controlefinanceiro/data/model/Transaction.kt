package br.edu.utfpr.controlefinanceiro.data.model

import com.google.firebase.firestore.DocumentId
import java.util.Date

data class Transaction(
    @DocumentId
    val id: String? = null,
    val userId: String = "",
    val type: String = "", // "RECEITA" ou "DESPESA"
    val value: Double = 0.0,
    val description: String = "",
    val categoryId: String = "",
    val date: Date = Date(),
    val recurrenceId: String? = null
)