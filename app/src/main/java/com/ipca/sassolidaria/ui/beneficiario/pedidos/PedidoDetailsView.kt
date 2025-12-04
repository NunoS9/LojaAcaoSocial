package com.ipca.sassolidaria.ui.beneficiario.pedidos

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.ipca.sassolidaria.data.model.Pedido
import com.ipca.sassolidaria.ui.components.BottomBar
import com.ipca.sassolidaria.utils.FirestoreCollections
import com.ipca.sassolidaria.utils.ResultState
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun PedidoDetailsView(
    navController: NavController,
    pedidoId: String,
    db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    var state by remember { mutableStateOf<ResultState<Pedido>>(ResultState.Loading) }

    val dateFormat = remember {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    }

    LaunchedEffect(pedidoId) {
        try {
            val snapshot = db.collection(FirestoreCollections.PEDIDOS)
                .document(pedidoId)
                .get()
                .await()

            val pedido = snapshot.toObject(Pedido::class.java)
            if (pedido != null) {
                state = ResultState.Success(pedido)
            } else {
                state = ResultState.Error("Pedido não encontrado.")
            }
        } catch (e: Exception) {
            state = ResultState.Error(e.message ?: "Erro ao carregar pedido.")
        }
    }

    Scaffold(
        bottomBar = { BottomBar(navController = navController) }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {

            Column(
                modifier = Modifier.fillMaxSize()
            ) {

                // Top bar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Detalhes do Pedido",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (val s = state) {
                    is ResultState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    is ResultState.Error -> {
                        Text(
                            text = s.message,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    is ResultState.Success -> {
                        val pedido = s.data

                        Text(
                            text = "Estado: ${pedido.estado}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Data do pedido: " +
                                    (pedido.data_pedido?.let { dateFormat.format(it) } ?: "-"),
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Mensagem do pedido",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (pedido.descricao.isNotBlank())
                                pedido.descricao
                            else
                                "Sem mensagem associada.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    ResultState.Idle -> Unit
                }
            }
        }
    }
}
