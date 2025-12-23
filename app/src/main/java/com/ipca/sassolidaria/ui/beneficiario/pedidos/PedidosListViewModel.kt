package com.ipca.sassolidaria.ui.beneficiario.pedidos

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.ipca.sassolidaria.data.model.Pedido
import com.ipca.sassolidaria.data.model.enums.EstadoPedido
import com.ipca.sassolidaria.utils.FirestoreCollections
import com.ipca.sassolidaria.utils.ResultState
import java.text.SimpleDateFormat
import java.util.Locale

data class PedidoUi(
    val id: String,
    val data: String,
    val estado: EstadoPedido,
    val descricao: String
)

class PedidosListViewModel(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    var pedidosState: ResultState<List<PedidoUi>> by mutableStateOf(ResultState.Loading)
        private set

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    private var listenerRegistration: ListenerRegistration? = null

    init {
        observarPedidosBeneficiario()
    }

    private fun observarPedidosBeneficiario() {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            pedidosState = ResultState.Error("Utilizador não autenticado.")
            return
        }

        pedidosState = ResultState.Loading

        // Remove listener antigo se existir
        listenerRegistration?.remove()

        // 1) descobrir o id do beneficiário a partir do uid (id_utilizador)
        db.collection(FirestoreCollections.BENEFICIARIOS)
            .whereEqualTo("id_utilizador", uid)
            .limit(1)
            .get()
            .addOnSuccessListener { benefSnapshot ->

                val benefDoc = benefSnapshot.documents.firstOrNull()
                if (benefDoc == null) {
                    pedidosState = ResultState.Success(emptyList())
                    return@addOnSuccessListener
                }

                val idBeneficiario = benefDoc.getString("id_beneficiario") ?: benefDoc.id

                // 2) agora sim: ouvir pedidos do beneficiário (em realtime)
                listenerRegistration = db.collection(FirestoreCollections.PEDIDOS)
                    .whereEqualTo("id_beneficiario", idBeneficiario)
                    .addSnapshotListener { snapshot, e ->
                        if (e != null) {
                            pedidosState = ResultState.Error("Erro ao ouvir pedidos: ${e.message}")
                            return@addSnapshotListener
                        }

                        if (snapshot == null) {
                            pedidosState = ResultState.Error("Erro ao carregar pedidos.")
                            return@addSnapshotListener
                        }

                        val pedidos = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(Pedido::class.java)?.let { pedido ->
                                PedidoUi(
                                    id = pedido.id_pedido.ifBlank { doc.id },
                                    data = pedido.data_pedido?.let { dateFormat.format(it) } ?: "",
                                    estado = pedido.estado,
                                    descricao = pedido.descricao
                                )
                            }
                        }

                        pedidosState = ResultState.Success(pedidos)
                    }
            }
            .addOnFailureListener { e ->
                pedidosState = ResultState.Error("Erro ao carregar beneficiário: ${e.message}")
            }
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }
}
