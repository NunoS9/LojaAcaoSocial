package com.ipca.sassolidaria.ui.beneficiario.pedidos

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ipca.sassolidaria.data.model.Pedido
import com.ipca.sassolidaria.data.model.enums.EstadoPedido
import com.ipca.sassolidaria.utils.FirestoreCollections
import com.ipca.sassolidaria.utils.ResultState
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

data class PedidoUi(
    val id: String,
    val data: String,
    val estado: EstadoPedido

)

class PedidosListViewModel(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    var pedidosState: ResultState<List<PedidoUi>> by mutableStateOf(ResultState.Loading)
        private set

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    init {
        carregarPedidosBeneficiario()
    }

    fun carregarPedidosBeneficiario() {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            pedidosState = ResultState.Error("Utilizador não autenticado.")
            return
        }

        pedidosState = ResultState.Loading

        viewModelScope.launch {
            try {
                val snapshot = db.collection(FirestoreCollections.PEDIDOS)
                    .whereEqualTo("id_beneficiario", uid)  //só pedidos DO BENEFICIÁRIO
                    .get()
                    .await()

                val pedidos = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Pedido::class.java)?.let { pedido ->
                        PedidoUi(
                            id = pedido.id_pedido.ifBlank { doc.id },
                            data = pedido.data_pedido?.let { dateFormat.format(it) } ?: "",
                            estado = pedido.estado

                        )
                    }
                }

                pedidosState = ResultState.Success(pedidos)
            } catch (e: Exception) {
                pedidosState = ResultState.Error(e.message ?: "Erro ao carregar pedidos.")
            }
        }
    }
}
