package com.ipca.sassolidaria.ui.utilizador

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Soap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ipca.sassolidaria.data.model.Produto
import com.ipca.sassolidaria.data.model.enums.TipoProduto
import com.ipca.sassolidaria.utils.FirestoreCollections
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class HomeUtilizadorState(
    val produtos: List<Produto> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class HomeUtilizadorViewModel : ViewModel() {

    var uiState by mutableStateOf(HomeUtilizadorState())
        private set

    // ✅ Badge Notificações
    var unseenNotificacoesCount by mutableIntStateOf(0)
        private set

    var notificacoesCountLoading by mutableStateOf(true)
        private set

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    init {
        carregarProdutos()
        carregarNotificacoesUnseenCount()
    }

    fun carregarProdutos() {
        viewModelScope.launch {
            try {
                uiState = uiState.copy(isLoading = true)

                val snapshot = db.collection(FirestoreCollections.PRODUTOS)
                    .get()
                    .await()

                val lista = snapshot.toObjects(Produto::class.java)

                uiState = uiState.copy(
                    produtos = lista,
                    isLoading = false,
                    errorMessage = null
                )
            } catch (_: Exception) {
                uiState = uiState.copy(isLoading = false, errorMessage = "Erro ao carregar produtos.")
            }
        }
    }

    fun carregarNotificacoesUnseenCount() {
        viewModelScope.launch {
            notificacoesCountLoading = true
            try {
                val uid = auth.currentUser?.uid ?: run {
                    unseenNotificacoesCount = 0
                    notificacoesCountLoading = false
                    return@launch
                }

                val snap = db.collection(FirestoreCollections.NOTIFICACOES)
                    .whereEqualTo("destinatario", uid)
                    .whereEqualTo("lida", false)
                    .get()
                    .await()

                unseenNotificacoesCount = snap.size()

            } catch (_: Exception) {
                unseenNotificacoesCount = 0
            } finally {
                notificacoesCountLoading = false
            }
        }
    }

    fun getIconForTipo(tipo: TipoProduto): ImageVector =
        when (tipo) {
            TipoProduto.ALIMENTACAO -> Icons.Outlined.Restaurant
            TipoProduto.HIGIENE -> Icons.Outlined.Soap
            TipoProduto.LIMPEZA -> Icons.Outlined.CleaningServices
        }

    fun tipoToLabel(tipo: TipoProduto): String =
        when (tipo) {
            TipoProduto.ALIMENTACAO -> "Alimentação"
            TipoProduto.HIGIENE -> "Higiene"
            TipoProduto.LIMPEZA -> "Limpeza"
        }
}
