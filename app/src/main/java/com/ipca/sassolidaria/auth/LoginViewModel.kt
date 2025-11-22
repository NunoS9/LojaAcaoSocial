package com.ipca.sassolidaria.auth

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val errorMessage: String = "",
    val successMessage: String = ""
)

class LoginViewModel : ViewModel() {

    val uiState = mutableStateOf(LoginUiState())

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun login(onSuccess: (String) -> Unit) {
        val email = uiState.value.email
        val password = uiState.value.password

        if (email.isBlank() || password.isBlank()) {
            uiState.value = uiState.value.copy(
                errorMessage = "Preencha todos os campos",
                successMessage = ""
            )
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: ""

                db.collection("users").document(uid)
                    .get()
                    .addOnSuccessListener { doc ->
                        val tipo = doc.getString("tipo") ?: "UTILIZADOR"

                        uiState.value = uiState.value.copy(
                            successMessage = "Login efetuado com sucesso!",
                            errorMessage = ""
                        )

                        onSuccess(tipo)
                    }
                    .addOnFailureListener {
                        uiState.value = uiState.value.copy(
                            errorMessage = "Erro ao obter dados do utilizador",
                            successMessage = ""
                        )
                    }
            }
            .addOnFailureListener {
                uiState.value = uiState.value.copy(
                    errorMessage = "Email ou password incorretos",
                    successMessage = ""
                )
            }
    }
}
