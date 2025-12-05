package com.ipca.sassolidaria.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.ipca.sassolidaria.data.model.Doacao
import com.ipca.sassolidaria.data.model.Doador
import com.ipca.sassolidaria.data.model.ItemDoacao
import com.ipca.sassolidaria.utils.FirestoreCollections
import kotlinx.coroutines.tasks.await

class DoacoesRepository {

    private val db = FirebaseFirestore.getInstance()

    suspend fun getAllDoacoes(): List<Doacao> {
        return db.collection(FirestoreCollections.DOACOES)
            .get()
            .await()
            .toObjects(Doacao::class.java)
    }

    suspend fun getItensDaDoacao(idDoacao: String): List<ItemDoacao> {
        return db.collection(FirestoreCollections.ITENS_DOACAO)
            .whereEqualTo("id_doacao", idDoacao)
            .get()
            .await()
            .toObjects(ItemDoacao::class.java)
    }

    suspend fun getDoador(idDoador: String): Doador? {
        return db.collection(FirestoreCollections.DOADORES)
            .document(idDoador)
            .get()
            .await()
            .toObject(Doador::class.java)
    }

    suspend fun getDoacao(id: String): Doacao? {
        return db.collection(FirestoreCollections.DOACOES)
            .document(id)
            .get()
            .await()
            .toObject(Doacao::class.java)
    }
}
