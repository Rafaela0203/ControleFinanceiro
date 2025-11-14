package br.edu.utfpr.controlefinanceiro.data.model

import com.google.firebase.firestore.DocumentId
import java.util.Date

data class User(
    @DocumentId
    val id: String? = null,
    val name: String = "",
    val email: String = "",
    val creationDate: Date = Date()
)