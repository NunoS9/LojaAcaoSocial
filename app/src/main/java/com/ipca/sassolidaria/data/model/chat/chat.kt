package com.ipca.sassolidaria.data.model.chat

data class Chat(
    val id: String = "",
    val beneficiarioId: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val ativo: Boolean = true
)
