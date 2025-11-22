package com.ipca.sassolidaria.auth

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RegisterViewModel : AuthViewModel() {

    fun register(onSuccess: () -> Unit) {
        clearError()

        if (uiState.password != uiState.confirmPassword) {
            uiState = uiState.copy(errorMessage = "As palavras-passe não coincidem")
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)

            delay(1000) // simula Firebase

            onSuccess()

            uiState = uiState.copy(isLoading = false)
        }
    }
}
