package com.ipca.sassolidaria.data.repository


import com.ipca.sassolidaria.data.firebase.FirestoreSource
import com.ipca.sassolidaria.data.model.Utilizador

class UserRepository(
    private val firestoreSource: FirestoreSource = FirestoreSource()
) {

    suspend fun getUtilizador(id: String): Utilizador? {
        return firestoreSource.getUtilizadorById(id)
    }

    suspend fun createUtilizador(utilizador: Utilizador) {
        firestoreSource.saveUtilizador(utilizador)
    }
}