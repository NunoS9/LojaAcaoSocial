package com.ipca.sassolidaria.ui.beneficiario.pedidos

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.ipca.sassolidaria.data.model.Pedido
import com.ipca.sassolidaria.utils.FirestoreCollections
import com.ipca.sassolidaria.utils.ResultState
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PedidoDetailsViewModel(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    var state: ResultState<Pedido> by mutableStateOf(ResultState.Loading)
        private set

    fun carregarPedido(pedidoId: String) {
        if (pedidoId.isBlank()) {
            state = ResultState.Error("ID do pedido inválido.")
            return
        }

        state = ResultState.Loading

        viewModelScope.launch {
            try {
                val snapshot = db.collection(FirestoreCollections.PEDIDOS)
                    .document(pedidoId)
                    .get()
                    .await()

                val pedido = snapshot.toObject(Pedido::class.java)

                state = if (pedido != null) {
                    ResultState.Success(pedido)
                } else {
                    ResultState.Error("Pedido não encontrado.")
                }
            } catch (e: Exception) {
                state = ResultState.Error(e.message ?: "Erro ao carregar pedido.")
            }
        }
    }
}
