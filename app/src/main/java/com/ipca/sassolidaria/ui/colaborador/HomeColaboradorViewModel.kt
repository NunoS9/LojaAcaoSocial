package com.ipca.sassolidaria.ui.colaborador

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ipca.sassolidaria.data.model.Agendamento
import com.ipca.sassolidaria.data.model.Beneficiario
import com.ipca.sassolidaria.data.model.Entrega
import com.ipca.sassolidaria.data.model.enums.EstadoEntrega
import com.ipca.sassolidaria.utils.FirestoreCollections
import com.ipca.sassolidaria.utils.ResultState
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale

data class EntregaCalendarioUi(
    val idEntrega: String,
    val dia: LocalDate,
    val dataHoraLabel: String,
    val nomeBeneficiario: String,
    val estado: EstadoEntrega,
    val estadoLabel: String,
    val estadoColorBg: androidx.compose.ui.graphics.Color,
    val estadoColorFg: androidx.compose.ui.graphics.Color
)

class HomeColaboradorViewModel(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    var entregasState: ResultState<List<EntregaCalendarioUi>> by mutableStateOf(ResultState.Loading)
        private set

    var unseenAlertasCount by mutableIntStateOf(0)
        private set

    var alertasCountLoading by mutableStateOf(true)
        private set

    private val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault())

    @RequiresApi(Build.VERSION_CODES.O)
    fun start() {
        carregarEntregasPendentesProntas()
        carregarAlertasUnseenCount()
    }

    private fun agoraMais14Dias(): Date {
        val ms = System.currentTimeMillis() + 14L * 24L * 60L * 60L * 1000L
        return Date(ms)
    }

    private fun carregarAlertasUnseenCount() {
        viewModelScope.launch {
            alertasCountLoading = true
            try {
                val uid = auth.currentUser?.uid ?: run {
                    unseenAlertasCount = 0
                    alertasCountLoading = false
                    return@launch
                }

                val now = Date()
                val limite = agoraMais14Dias()

                // Produtos que expiram <= 14 dias
                val produtosSnap = db.collection(FirestoreCollections.PRODUTOS)
                    .whereGreaterThanOrEqualTo("data_validade", now)
                    .whereLessThanOrEqualTo("data_validade", limite)
                    .get()
                    .await()

                val alertIds = produtosSnap.documents.map { it.id }.toSet()

                // Doc de vistos
                val vistosDoc = db.collection("alertas_validade_vistos")
                    .document(uid)
                    .get()
                    .await()

                val vistosIds = (vistosDoc.get("seen_ids") as? List<*>)?.mapNotNull { it as? String }?.toSet()
                    ?: emptySet()

                unseenAlertasCount = (alertIds - vistosIds).size
            } catch (_: Exception) {
                unseenAlertasCount = 0
            } finally {
                alertasCountLoading = false
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun carregarEntregasPendentesProntas() {
        viewModelScope.launch {
            entregasState = ResultState.Loading
            try {
                // 1) buscar entregas com estado PENDENTE ou PRONTA
                val entregasSnap = db.collection(FirestoreCollections.ENTREGAS)
                    .whereIn("estado", listOf(EstadoEntrega.PENDENTE.name, EstadoEntrega.PRONTA.name))
                    .get()
                    .await()

                val entregas = entregasSnap.documents.mapNotNull { doc ->
                    doc.toObject(Entrega::class.java)?.copy(id_entrega = doc.id)
                }

                if (entregas.isEmpty()) {
                    entregasState = ResultState.Success(emptyList())
                    return@launch
                }

                // 2) carregar agendamentos (batch por ids)
                val agIds = entregas.mapNotNull { it.id_agendamento.takeIf { id -> id.isNotBlank() } }.distinct()
                val agMap = mutableMapOf<String, Agendamento>()

                for (chunk in agIds.chunked(10)) {
                    val snap = db.collection(FirestoreCollections.AGENDAMENTOS)
                        .whereIn("id_agendamento", chunk) // se não existir este campo, usa document(id)
                        .get()
                        .await()

                    snap.documents.forEach { d ->
                        val ag = d.toObject(Agendamento::class.java)
                        if (ag != null) agMap[ag.id_agendamento.ifBlank { d.id }] = ag
                    }
                }

                // 3) carregar beneficiários (batch por ids)
                val benIds = entregas.mapNotNull { it.id_beneficiario.takeIf { id -> id.isNotBlank() } }.distinct()
                val benMap = mutableMapOf<String, Beneficiario>()

                for (chunk in benIds.chunked(10)) {
                    val snap = db.collection(FirestoreCollections.BENEFICIARIOS)
                        .whereIn("id_beneficiario", chunk) // se não existir este campo, usa document(id)
                        .get()
                        .await()

                    snap.documents.forEach { d ->
                        val b = d.toObject(Beneficiario::class.java)
                        if (b != null) benMap[b.id_beneficiario.ifBlank { d.id }] = b
                    }
                }

                // 4) map UI
                val ui = entregas.mapNotNull { ent ->
                    val ag = agMap[ent.id_agendamento]
                    val date = ag?.data_agendada ?: return@mapNotNull null

                    val localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                    val labelData = dateTimeFormat.format(date)

                    val ben = benMap[ent.id_beneficiario]
                    val nome = ben?.nome ?: ""

                    val (lbl, bg, fg) = when (ent.estado) {
                        EstadoEntrega.PENDENTE ->
                            Triple(
                                "Pendente",
                                androidx.compose.ui.graphics.Color(0xFFFFF3CD),
                                androidx.compose.ui.graphics.Color(0xFF8A6D3B)
                            )

                        EstadoEntrega.PRONTA ->
                            Triple(
                                "Pronta",
                                androidx.compose.ui.graphics.Color(0xFFE0E7FF),
                                androidx.compose.ui.graphics.Color(0xFF1E40AF)
                            )

                        else ->
                            Triple(
                                ent.estado.name,
                                androidx.compose.ui.graphics.Color.LightGray,
                                androidx.compose.ui.graphics.Color.DarkGray
                            )
                    }

                    EntregaCalendarioUi(
                        idEntrega = ent.id_entrega,
                        dia = localDate,
                        dataHoraLabel = labelData,
                        nomeBeneficiario = nome,
                        estado = ent.estado,
                        estadoLabel = lbl,
                        estadoColorBg = bg,
                        estadoColorFg = fg
                    )
                }
                    // ordena por dia (mais próximo primeiro)
                    .sortedBy { it.dia }

                entregasState = ResultState.Success(ui)
            } catch (e: Exception) {
                entregasState = ResultState.Error(e.message ?: "Erro ao carregar entregas.")
            }
        }
    }
}
