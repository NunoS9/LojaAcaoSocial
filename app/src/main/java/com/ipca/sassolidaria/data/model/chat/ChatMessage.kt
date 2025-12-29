package com.ipca.sassolidaria.data.model.chat

data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val senderTipo: String = "", // BENEFICIARIO | COLABORADOR
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis(),

    val lidaPeloColaborador: Boolean = false,
    val lidaPeloBeneficiario: Boolean = false
)
