package com.ipca.sassolidaria.auth

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.ipca.sassolidaria.data.repository.AuthRepository

class RegisterViewModel : ViewModel() {

    var uiState = mutableStateOf(RegisterState())
    private val repo = AuthRepository()

    fun register(onDone: () -> Unit) {

        val name = uiState.value.name
        val email = uiState.value.email
        val password = uiState.value.password

        repo.registerUser(name, email, password) { success, message ->
            if (success) {
                uiState.value = uiState.value.copy(successMessage = message, errorMessage = "")
                onDone()
            } else {
                uiState.value = uiState.value.copy(errorMessage = message, successMessage = "")
            }
        }
    }
}

data class RegisterState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val errorMessage: String = "",
    val successMessage: String = ""
)
