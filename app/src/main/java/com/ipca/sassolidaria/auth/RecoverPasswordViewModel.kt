package com.ipca.sassolidaria.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class RecoverPasswordState(
    val email: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class RecoverPasswordViewModel : ViewModel() {

    var uiState by mutableStateOf(RecoverPasswordState())
        private set

    fun onEmailChange(newEmail: String) {
        uiState = uiState.copy(email = newEmail, errorMessage = null, successMessage = null)
    }

    fun recover(onSuccess: () -> Unit) {
        val email = uiState.email.trim()

        if (email.isEmpty()) {
            uiState = uiState.copy(
                errorMessage = "Introduza o email associado à conta",
                successMessage = null
            )
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null, successMessage = null)

            // TODO: aqui mais tarde colocas o FirebaseAuth.sendPasswordResetEmail(email)
            delay(1000)

            uiState = uiState.copy(
                isLoading = false,
                successMessage = "Foram enviadas instruções para o email.",
                errorMessage = null
            )

            onSuccess()
        }
    }
}
