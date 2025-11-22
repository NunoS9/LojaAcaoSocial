package com.ipca.sassolidaria.auth

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val errorMessage: String = "",
    val successMessage: String = ""
)
