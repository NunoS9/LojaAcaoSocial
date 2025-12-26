package com.ipca.sassolidaria.ui.colaborador

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ipca.sassolidaria.data.model.Produto
import com.ipca.sassolidaria.utils.FirestoreCollections
import com.ipca.sassolidaria.utils.ResultState
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AlertaProdutoUi(
    val idProduto: String,
    val nome: String,
    val validadeLabel: String,
    val quantidadeDisponivel: Int
)

class AlertasValidadeViewModel(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    var alertasState: ResultState<List<AlertaProdutoUi>> by mutableStateOf(ResultState.Loading)
        private set

    private var rawList: List<AlertaProdutoUi> = emptyList()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    fun start() {
        carregarAlertas()
    }

    fun updateQuery(q: String) {
        val s = q.trim()
        alertasState = ResultState.Success(
            if (s.isBlank()) rawList
            else rawList.filter {
                it.nome.contains(s, ignoreCase = true) || it.validadeLabel.contains(s, ignoreCase = true)
            }
        )
    }

    fun marcarComoVistos() {
        viewModelScope.launch {
            try {
                val uid = auth.currentUser?.uid ?: return@launch
                val ids = rawList.map { it.idProduto }
                db.collection("alertas_validade_vistos")
                    .document(uid)
                    .set(mapOf("seen_ids" to ids, "seen_at" to System.currentTimeMillis()))
                    .await()
            } catch (_: Exception) {
            }
        }
    }

    private fun agoraMais14Dias(): Date {
        val ms = System.currentTimeMillis() + 14L * 24L * 60L * 60L * 1000L
        return Date(ms)
    }

    private fun carregarAlertas() {
        viewModelScope.launch {
            alertasState = ResultState.Loading
            try {
                val now = Date()
                val limite = agoraMais14Dias()

                val snap = db.collection(FirestoreCollections.PRODUTOS)
                    .whereGreaterThanOrEqualTo("data_validade", now)
                    .whereLessThanOrEqualTo("data_validade", limite)
                    .get()
                    .await()

                val list = snap.documents.mapNotNull { d ->
                    val p = d.toObject(Produto::class.java) ?: return@mapNotNull null
                    val validade = p.data_validade ?: return@mapNotNull null
                    AlertaProdutoUi(
                        idProduto = d.id,
                        nome = p.nome,
                        validadeLabel = dateFormat.format(validade),
                        quantidadeDisponivel = p.quantidade_disponivel
                    )
                }.sortedBy { it.validadeLabel }

                rawList = list
                alertasState = ResultState.Success(list)
            } catch (e: Exception) {
                alertasState = ResultState.Error(e.message ?: "Erro ao carregar alertas.")
            }
        }
    }
}
