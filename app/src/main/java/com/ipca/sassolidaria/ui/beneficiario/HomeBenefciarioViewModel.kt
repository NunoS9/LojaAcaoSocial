package com.ipca.sassolidaria.ui.beneficiario

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
import com.ipca.sassolidaria.data.model.Beneficiario
import com.ipca.sassolidaria.data.model.Produto
import com.ipca.sassolidaria.data.model.enums.TipoProduto
import com.ipca.sassolidaria.utils.FirestoreCollections
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class HomeBeneficiarioState(
    val produtos: List<Produto> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class HomeBeneficiarioViewModel : ViewModel() {

    var uiState by mutableStateOf(HomeBeneficiarioState())
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
                uiState = uiState.copy(isLoading = true, errorMessage = null)

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
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = "Erro ao carregar produtos."
                )
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

                // 1) descobrir id_beneficiario pelo uid
                val benSnap = db.collection(FirestoreCollections.BENEFICIARIOS)
                    .whereEqualTo("id_utilizador", uid)
                    .limit(1)
                    .get()
                    .await()

                val benDoc = benSnap.documents.firstOrNull() ?: run {
                    unseenNotificacoesCount = 0
                    notificacoesCountLoading = false
                    return@launch
                }

                val ben = benDoc.toObject(Beneficiario::class.java)
                val idBeneficiario = ben?.id_beneficiario?.takeIf { it.isNotBlank() } ?: benDoc.id

                // 2) contar notificações não lidas
                val notifSnap = db.collection(FirestoreCollections.NOTIFICACOES)
                    .whereEqualTo("destinatario", idBeneficiario)
                    .whereEqualTo("lida", false)
                    .get()
                    .await()

                unseenNotificacoesCount = notifSnap.size()

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
