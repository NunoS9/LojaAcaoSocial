package com.ipca.sassolidaria.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ipca.sassolidaria.core.UserType
import com.ipca.sassolidaria.data.model.Utilizador
import com.ipca.sassolidaria.utils.FirestoreCollections

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val errorMessage: String = "",
    val successMessage: String = ""
)

class LoginViewModel : ViewModel() {

    var uiState by mutableStateOf(LoginUiState())
        private set

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun onEmailChange(newEmail: String) {
        uiState = uiState.copy(email = newEmail)
    }

    fun onPasswordChange(newPass: String) {
        uiState = uiState.copy(password = newPass)
    }

    // Faz login e devolve o tipo de utilizador

    fun login(onSuccess: (UserType) -> Unit) {
        val email = uiState.email.trim()
        val password = uiState.password.trim()

        if (email.isBlank() || password.isBlank()) {
            uiState = uiState.copy(
                errorMessage = "Preencha todos os campos",
                successMessage = ""
            )
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: ""

                db.collection(FirestoreCollections.UTILIZADORES)
                    .document(uid)
                    .get()
                    .addOnSuccessListener { doc ->

                        val utilizador = doc.toObject(Utilizador::class.java)

                        val tipo = utilizador?.tipo ?: UserType.UTILIZADOR

                        uiState = uiState.copy(
                            successMessage = "Login efetuado com sucesso!",
                            errorMessage = ""
                        )

                        onSuccess(tipo)
                    }
                    .addOnFailureListener {
                        uiState = uiState.copy(
                            errorMessage = "Erro ao obter dados do utilizador",
                            successMessage = ""
                        )
                    }
            }
            .addOnFailureListener {
                uiState = uiState.copy(
                    errorMessage = "Email ou palavra-passe incorretos.",
                    successMessage = ""
                )
            }
    }
}
