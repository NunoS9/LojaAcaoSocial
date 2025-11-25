package com.ipca.sassolidaria.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ipca.sassolidaria.core.UserType
import com.ipca.sassolidaria.data.model.Utilizador
import com.ipca.sassolidaria.utils.FirestoreCollections

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    fun registerUser(
        name: String,
        email: String,
        password: String,
        onResult: (Boolean, String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: ""

                val utilizador = Utilizador(
                    id_utilizador = uid,
                    nome = name,
                    email = email,
                    tipo = UserType.UTILIZADOR,
                    is_beneficiario = false
                )

                db.collection(FirestoreCollections.UTILIZADORES)
                    .document(uid)
                    .set(utilizador)
                    .addOnSuccessListener {
                        onResult(true, "Registo efetuado com sucesso!")
                    }
                    .addOnFailureListener {
                        onResult(false, "Erro ao guardar utilizador no Firestore.")
                    }
            }
            .addOnFailureListener {
                onResult(false, "Erro ao registar utilizador.")
            }
    }
}
