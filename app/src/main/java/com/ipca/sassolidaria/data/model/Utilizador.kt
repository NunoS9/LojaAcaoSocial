package com.ipca.sassolidaria.data.model
import com.ipca.sassolidaria.core.UserType

data class Utilizador(
    var id_utilizador: String = "",
    var nome: String = "",
    var email: String = "",
    var password_hash: String = "",
    var tipo: UserType = UserType.UTILIZADOR,
    var is_beneficiario: Boolean = false
)