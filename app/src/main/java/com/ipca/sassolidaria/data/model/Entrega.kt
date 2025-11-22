package com.ipca.sassolidaria.data.model

import com.ipca.sassolidaria.data.model.enums.EstadoEntrega
import com.ipca.sassolidaria.data.model.enums.Tipo

data class Entrega(
    var id_entrega: String = "",
    var tipo: Tipo = Tipo.PEDIDO,
    var id_beneficiario: String = "",
    var id_agendamento: String = "",
    var id_colaborador: String = "",
    var estado: EstadoEntrega = EstadoEntrega.PENDENTE
)