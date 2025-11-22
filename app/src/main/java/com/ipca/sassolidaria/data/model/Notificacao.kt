package com.ipca.sassolidaria.data.model

import com.ipca.sassolidaria.data.model.enums.TipoNotificacao
import java.util.Date

data class Notificacao(
    var id_notificacao: String = "",
    var destinatario: String = "",          // id do utilizador/beneficiário
    var tipo: TipoNotificacao = TipoNotificacao.CONFIRMACAO,
    var titulo: String = "",
    var conteudo: String = "",
    var data_envio: Date? = null
)