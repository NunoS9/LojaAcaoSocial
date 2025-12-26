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
import java.util.Date

class PedidoCreateViewModel(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    var submitState by mutableStateOf<ResultState<Unit>>(ResultState.Idle)
        private set

    fun criarPedido(descricao: String) {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            submitState = ResultState.Error("Utilizador não autenticado.")
            return
        }

        if (descricao.isBlank()) {
            submitState = ResultState.Error("A descrição do pedido não pode estar vazia.")
            return
        }

        submitState = ResultState.Loading

        viewModelScope.launch {
            try {
                // ✅ 1) descobrir o ID do beneficiário através do id_utilizador = uid
                val benefSnapshot = db.collection(FirestoreCollections.BENEFICIARIOS)
                    .whereEqualTo("id_utilizador", uid)
                    .limit(1)
                    .get()
                    .await()

                val benefDoc = benefSnapshot.documents.firstOrNull()
                if (benefDoc == null) {
                    submitState = ResultState.Error("Este utilizador ainda não tem perfil de beneficiário.")
                    return@launch
                }

                // ✅ usar campo id_beneficiario se existir, senão usar o id do documento
                val idBeneficiario = benefDoc.getString("id_beneficiario") ?: benefDoc.id

                val pedido = Pedido(
                    id_pedido = "",
                    id_beneficiario = idBeneficiario,
                    id_agendamento = "",
                    data_pedido = Date(),
                    descricao = descricao,
                    estado = EstadoPedido.PENDENTE
                )

                val docRef = db.collection(FirestoreCollections.PEDIDOS)
                    .add(pedido)
                    .await()

                docRef.update("id_pedido", docRef.id).await()

                submitState = ResultState.Success(Unit)
            } catch (e: Exception) {
                submitState = ResultState.Error(e.message ?: "Erro ao criar pedido.")
            }
        }
    }

    fun limparErros() {
        if (submitState is ResultState.Error) {
            submitState = ResultState.Idle
        }
    }
}
