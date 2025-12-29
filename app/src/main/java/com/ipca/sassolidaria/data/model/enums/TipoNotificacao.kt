package com.ipca.sassolidaria.data.model.enums

enum class TipoNotificacao {
    // Utilizador / Beneficiário
    CANDIDATURA_ATUALIZADA,

    // Colaborador
    NOVA_CANDIDATURA,
    NOVO_PEDIDO,
    AGENDAMENTO_ATUALIZADO,

    // Beneficiário / Entregas
    NOVO_AGENDAMENTO,
    PEDIDO_ATUALIZADO,
    ENTREGA_ATUALIZADA,
    LEMBRETE_ENTREGA,

    // Geral
    GERAL
}
