package com.ipca.sassolidaria.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun registerUser(
        name: String,
        email: String,
        password: String,
        onResult: (Boolean, String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->

                val uid = result.user?.uid ?: ""

                val userData = hashMapOf(
                    "nome" to name,
                    "email" to email,
                    "tipo" to "UTILIZADOR",
                )

                db.collection("users")
                    .document(uid)
                    .set(userData)
                    .addOnSuccessListener {
                        onResult(true, "Registo efetuado com sucesso!")
                    }
                    .addOnFailureListener {
                        onResult(false, "Erro ao guardar dados no Firestore.")
                    }
            }
            .addOnFailureListener {
                onResult(false, "Erro ao registar utilizador.")
            }
    }
}
