package com.ipca.sassolidaria.data.model


import com.ipca.sassolidaria.data.model.enums.GrauCurso

data class Beneficiario(
    var id_beneficiario: String = "",
    var id_utilizador: String = "",
    var nome: String = "",
    var numero_estudante: String = "",
    var curso: String = "",
    var grau_curso: GrauCurso = GrauCurso.CTESP,
    var email: String = "",
    var contacto: String = ""
)