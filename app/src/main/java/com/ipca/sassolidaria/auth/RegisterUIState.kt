package com.ipca.sassolidaria.auth

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val errorMessage: String = "",
    val successMessage: String = ""
)
