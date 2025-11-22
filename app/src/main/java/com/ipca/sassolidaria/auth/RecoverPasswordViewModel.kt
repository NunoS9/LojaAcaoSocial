package com.ipca.sassolidaria.auth

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RecoverPasswordViewModel : AuthViewModel() {

    fun recover(onSuccess: () -> Unit) {
        clearError()

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)

            delay(1000) // simula Firebase

            onSuccess()

            uiState = uiState.copy(isLoading = false)
        }
    }
}
