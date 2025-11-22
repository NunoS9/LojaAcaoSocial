package com.ipca.sassolidaria.data.model

data class PreferenciaAgendamento(
    var id_preferencia: String = "",
    var id_beneficiario: String = "",
    var dia_semana: Int = 0,
    var hora_inicio: String = "",
    var hora_fim: String = "",
    var observacoes: String = ""
)