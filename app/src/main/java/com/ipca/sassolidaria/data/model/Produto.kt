package com.ipca.sassolidaria.data.model

import com.ipca.sassolidaria.data.model.enums.TipoProduto
import java.util.Date

data class Produto(
    var id_produto: String = "",
    var nome: String = "",
    var tipo_produto: TipoProduto = TipoProduto.ALIMENTACAO,
    var quantidade: Int = 0,
    var quantidade_disponivel: Int = 0,
    var data_validade: Date? = null
)