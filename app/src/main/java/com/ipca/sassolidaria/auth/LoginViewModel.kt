package com.ipca.sassolidaria.auth

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class LoginViewModel : ViewModel() {

    var uiState = mutableStateOf(LoginUiState())

    private val auth = FirebaseAuth.getInstance()

    fun login(onSuccess: () -> Unit) {

        val email = uiState.value.email
        val password = uiState.value.password

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                uiState.value = uiState.value.copy(
                    successMessage = "Login efetuado com sucesso!",
                    errorMessage = ""
                )
                onSuccess()
            }
            .addOnFailureListener {
                uiState.value = uiState.value.copy(
                    errorMessage = "Email ou palavra-passe incorretos.",
                    successMessage = ""
                )
            }
    }

}
