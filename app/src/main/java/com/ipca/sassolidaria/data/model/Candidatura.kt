package com.ipca.sassolidaria.data.model

import com.ipca.sassolidaria.data.model.enums.EstadoCandidatura
import com.ipca.sassolidaria.data.model.enums.GrauCurso
import java.util.Date

data class Candidatura(
    var id_candidatura: String = "",
    var id_utilizador: String = "",
    var nome: String = "",
    var data_nascimento: Date? = null,
    var cc: String = "",
    var email: String = "",
    var telemovel: Int=0,
    var ano_letivo: String = "",
    var grau_curso: GrauCurso = GrauCurso.CTESP,
    var curso: String = "",
    var num_estudante: Int = 0,
    var apoio_alimentacao: Boolean = false,
    var apoio_limpeza: Boolean = false,
    var apoio_higiene: Boolean = false,
    var faes: Boolean = false,
    var bolsa: Boolean = false,
    var obs_bolsa: String = "",
    var assinatura: String = "",
    var data_submissao: Date? = null,
    var estado: EstadoCandidatura = EstadoCandidatura.EM_ANALISE,
)