package com.ipca.sassolidaria.ui.beneficiario.entregas

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.ipca.sassolidaria.data.model.Agendamento
import com.ipca.sassolidaria.data.model.Beneficiario
import com.ipca.sassolidaria.data.model.Entrega
import com.ipca.sassolidaria.data.model.enums.EstadoEntrega
import com.ipca.sassolidaria.utils.FirestoreCollections
import com.ipca.sassolidaria.utils.ResultState
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

data class EntregaUi(
    val id: String,
    val estado: EstadoEntrega,
    val dataHora: String
)

class EntregasListViewModel(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    var entregasState: ResultState<List<EntregaUi>> by mutableStateOf(ResultState.Loading)
        private set

    private val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault())
    private var listenerRegistration: ListenerRegistration? = null

    private val agendamentoCache = mutableMapOf<String, String>()

    init {
        carregarEntregasDoBeneficiario()
    }

    private fun carregarEntregasDoBeneficiario() {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            entregasState = ResultState.Error("Utilizador não autenticado.")
            return
        }

        entregasState = ResultState.Loading

        viewModelScope.launch {
            try {
                val benefSnap = db.collection(FirestoreCollections.BENEFICIARIOS)
                    .whereEqualTo("id_utilizador", uid)
                    .limit(1)
                    .get()
                    .await()

                val benefDoc = benefSnap.documents.firstOrNull()
                if (benefDoc == null) {
                    entregasState = ResultState.Error("Beneficiário não encontrado para este utilizador.")
                    return@launch
                }

                val benef = benefDoc.toObject(Beneficiario::class.java)
                val beneficiarioId =
                    benef?.id_beneficiario.takeUnless { it.isNullOrBlank() } ?: benefDoc.id

                startEntregasListener(beneficiarioId)
            } catch (e: Exception) {
                entregasState = ResultState.Error(e.message ?: "Erro ao carregar dados do beneficiário.")
            }
        }
    }

    private fun startEntregasListener(beneficiarioId: String) {
        listenerRegistration?.remove()

        listenerRegistration = db.collection(FirestoreCollections.ENTREGAS)
            .whereEqualTo("id_beneficiario", beneficiarioId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    entregasState = ResultState.Error("Erro ao ouvir entregas: ${e.message}")
                    return@addSnapshotListener
                }
                if (snapshot == null) {
                    entregasState = ResultState.Error("Erro ao carregar entregas.")
                    return@addSnapshotListener
                }

                val entregas = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Entrega::class.java)?.let { ent ->
                        ent.copy(id_entrega = ent.id_entrega.ifBlank { doc.id })
                    }
                }

                val uiRapido = entregas.map { ent ->
                    val idEntrega = ent.id_entrega
                    val dataHora = ent.id_agendamento
                        .takeIf { it.isNotBlank() }
                        ?.let { agendamentoCache[it].orEmpty() }
                        .orEmpty()

                    EntregaUi(
                        id = idEntrega,
                        estado = ent.estado,
                        dataHora = dataHora
                    )
                }
                entregasState = ResultState.Success(uiRapido)

                viewModelScope.launch {
                    try {
                        val agIds = entregas.mapNotNull { it.id_agendamento.takeIf { id -> id.isNotBlank() } }
                            .distinct()

                        val missing = agIds.filterNot { agendamentoCache.containsKey(it) }
                        if (missing.isNotEmpty()) {
                            // whereIn tem limite (10), por isso chunk
                            val chunks = missing.chunked(10)

                            for (chunk in chunks) {
                                val snap = db.collection(FirestoreCollections.AGENDAMENTOS)
                                    .whereIn(FieldPath.documentId(), chunk)
                                    .get()
                                    .await()

                                snap.documents.forEach { d ->
                                    val ag = d.toObject(Agendamento::class.java)
                                    val dataTxt = ag?.data_agendada?.let { dateTimeFormat.format(it) } ?: ""
                                    agendamentoCache[d.id] = dataTxt
                                }
                            }

                            val uiFinal = entregas.map { ent ->
                                val idEntrega = ent.id_entrega
                                val dataHora = ent.id_agendamento
                                    .takeIf { it.isNotBlank() }
                                    ?.let { agendamentoCache[it].orEmpty() }
                                    .orEmpty()

                                EntregaUi(
                                    id = idEntrega,
                                    estado = ent.estado,
                                    dataHora = dataHora
                                )
                            }

                            entregasState = ResultState.Success(uiFinal)
                        }
                    } catch (_: Exception) {
                    }
                }
            }
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }
}
