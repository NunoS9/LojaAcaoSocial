package com.ipca.sassolidaria.data.model

import java.util.Date

data class Doacao(
    var id_doacao: String = "",
    var id_doador: String = "",
    var id_colaborador: String = "",
    var data_doacao: Date? = null,
    var descricao: String = ""
)