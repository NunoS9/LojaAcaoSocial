package com.ipca.sassolidaria.data.model

data class Utilizador(
    var id_utilizador: String = "",
    var nome: String = "",
    var email: String = "",
    var password_hash: String = "",
    var is_beneficiario: Boolean = false
)