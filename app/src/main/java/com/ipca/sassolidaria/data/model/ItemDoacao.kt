package com.ipca.sassolidaria.data.model

import com.ipca.sassolidaria.data.model.enums.TipoProduto
import java.util.Date

data class ItemDoacao(
    var id_item_doacao: String = "",
    var id_doacao: String = "",
    var nome: String = "",
    var tipo_produto: TipoProduto = TipoProduto.HIGIENE,
    var quantidade: Int = 0,
    var data_validade: Date? = null
)