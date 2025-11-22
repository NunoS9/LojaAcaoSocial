package com.ipca.sassolidaria.data.model

import com.ipca.sassolidaria.data.model.enums.EstadoPedido
import java.util.Date

data class Pedido(
    var id_pedido: String = "",
    var id_beneficiario: String = "",
    var id_agendamento: String = "",
    var data_pedido: Date? = null,
    var descricao: String = "",
    var estado: EstadoPedido = EstadoPedido.PENDENTE
)