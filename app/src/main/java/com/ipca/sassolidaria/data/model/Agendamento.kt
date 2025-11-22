package com.ipca.sassolidaria.data.model

import com.ipca.sassolidaria.data.model.enums.EstadoAgendamento
import com.ipca.sassolidaria.data.model.enums.Tipo
import java.util.Date

data class Agendamento(
    var id_agendamento: String = "",
    var id_beneficiario: String = "",
    var data_agendada: Date? = null,
    var estado: EstadoAgendamento = EstadoAgendamento.PENDENTE,
    var tipo: Tipo = Tipo.PEDIDO
)