package com.ipca.sassolidaria.ui.colaborador.beneficiarios

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.ipca.sassolidaria.data.model.Beneficiario
import com.ipca.sassolidaria.utils.FirestoreCollections
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BeneficiariosListViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _beneficiarios = MutableStateFlow<List<Beneficiario>>(emptyList())
    val beneficiarios: StateFlow<List<Beneficiario>> = _beneficiarios.asStateFlow()

    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    init {
        loadBeneficiarios()
    }

    fun updateSearch(text: String) {
        _searchText.value = text
    }

    private fun loadBeneficiarios() {
        db.collection(FirestoreCollections.BENEFICIARIOS)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener

                val list = snapshot.documents.mapNotNull {
                    it.toObject(Beneficiario::class.java)?.copy(id_beneficiario = it.id)
                }

                _beneficiarios.value = list
            }
    }
}
