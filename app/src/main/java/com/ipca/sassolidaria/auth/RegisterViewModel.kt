package com.ipca.sassolidaria.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.ipca.sassolidaria.data.repository.AuthRepository

data class RegisterState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val errorMessage: String = "",
    val successMessage: String = ""
)

class RegisterViewModel : ViewModel() {

    var uiState by mutableStateOf(RegisterState())
        private set

    private val repo = AuthRepository()

    fun onNameChange(newName: String) {
        uiState = uiState.copy(name = newName)
    }

    fun onEmailChange(newEmail: String) {
        uiState = uiState.copy(email = newEmail)
    }

    fun onPasswordChange(newPass: String) {
        uiState = uiState.copy(password = newPass)
    }

    fun register(onDone: () -> Unit) {
        val name = uiState.name.trim()
        val email = uiState.email.trim()
        val password = uiState.password.trim()

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            uiState = uiState.copy(
                errorMessage = "Preencha todos os campos",
                successMessage = ""
            )
            return
        }

        repo.registerUser(name, email, password) { success, message ->
            if (success) {
                uiState = uiState.copy(
                    successMessage = message,
                    errorMessage = ""
                )
                onDone()
            } else {
                uiState = uiState.copy(
                    errorMessage = message,
                    successMessage = ""
                )
            }
        }
    }
}
