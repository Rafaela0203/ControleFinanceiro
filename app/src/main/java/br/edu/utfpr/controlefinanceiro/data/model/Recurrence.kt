package br.edu.utfpr.controlefinanceiro.data.model

import com.google.firebase.firestore.DocumentId
import java.util.Date

data class Recurrence(
    @DocumentId
    val id: String? = null,
    val userId: String = "",
    val description: String = "",
    val value: Double = 0.0,
    val categoryId: String = "",
    val frequency: String = "",
    val nextExecutionDate: Date = Date()
)