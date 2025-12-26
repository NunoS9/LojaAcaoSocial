package com.ipca.sassolidaria.ui.beneficiario

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Soap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
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

    private val db = FirebaseFirestore.getInstance()

    init {
        carregarProdutos()
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
