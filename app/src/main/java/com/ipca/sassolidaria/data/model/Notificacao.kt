package com.ipca.sassolidaria.data.model

import com.ipca.sassolidaria.data.model.enums.TipoNotificacao
import java.util.Date

data class Notificacao(
    var id_notificacao: String = "",
    var destinatario: String = "",          // uid do colaborador/beneficiário/utilizador
    var tipo: TipoNotificacao = TipoNotificacao.GERAL,
    var titulo: String = "",
    var conteudo: String = "",
    var data_envio: Date? = null,
    var lida: Boolean = false
)
