package br.edu.utfpr.controlefinanceiro.data.model

import com.google.firebase.firestore.DocumentId
import java.util.Date

data class FinancialGoal(
    @DocumentId
    val id: String? = null,
    val userId: String = "",
    val name: String = "",
    val targetValue: Double = 0.0,
    val savedValue: Double = 0.0,
    val deadline: Date = Date(),
    val type: String = "",
    val categoryId: String? = null
)