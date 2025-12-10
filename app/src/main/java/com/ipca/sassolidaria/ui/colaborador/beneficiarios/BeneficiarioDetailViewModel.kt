package com.ipca.sassolidaria.ui.colaborador.beneficiarios

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.ipca.sassolidaria.data.model.Beneficiario
import com.ipca.sassolidaria.utils.FirestoreCollections
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BeneficiarioDetailViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _beneficiario = MutableStateFlow<Beneficiario?>(null)
    val beneficiario: StateFlow<Beneficiario?> = _beneficiario.asStateFlow()

    fun loadBeneficiario(id: String) {
        if (id.isBlank()) return

        db.collection(FirestoreCollections.BENEFICIARIOS)
            .document(id)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener

                val b = snapshot.toObject(Beneficiario::class.java)
                if (b != null) {
                    _beneficiario.value = b.copy(id_beneficiario = snapshot.id)
                }
            }
    }
}
