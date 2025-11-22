package com.ipca.sassolidaria.auth

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

open class AuthViewModel : ViewModel() {

    var uiState by mutableStateOf(AuthUiState())
        protected set

    fun onEmailChange(newEmail: String) {
        uiState = uiState.copy(email = newEmail)
    }

    fun onPasswordChange(newPass: String) {
        uiState = uiState.copy(password = newPass)
    }

    fun onConfirmPasswordChange(newPass: String) {
        uiState = uiState.copy(confirmPassword = newPass)
    }

    fun clearError() {
        uiState = uiState.copy(errorMessage = null)
    }
}
