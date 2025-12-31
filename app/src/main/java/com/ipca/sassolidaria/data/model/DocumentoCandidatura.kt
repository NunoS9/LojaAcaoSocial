package com.ipca.sassolidaria.data.model

import java.util.Date

data class DocumentoCandidatura(
    var id_documento: String = "",
    var nome: String = "",
    var tipoMime: String = "",
    var base64: String = "",
    var data_upload: Date? = null
)
