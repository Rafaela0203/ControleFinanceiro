package br.edu.utfpr.controlefinanceiro.data.model

import com.google.firebase.firestore.DocumentId

data class Category(
    @DocumentId
    val id: String? = null,
    val userId: String = "",
    val name: String = "",
    val type: String = "DESPESA",
    val icon: String? = null
)