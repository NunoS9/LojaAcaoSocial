package com.ipca.sassolidaria.ui.beneficiario.pedidos

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ipca.sassolidaria.core.Routes
import com.ipca.sassolidaria.data.model.enums.EstadoPedido
import com.ipca.sassolidaria.ui.components.BottomBar
import com.ipca.sassolidaria.utils.ResultState
import java.text.SimpleDateFormat
import java.util.Locale

private val GreenDark = Color(0xFF004B3C)

private val ChipGreen = Color(0xFFE0F2F1)
private val ChipGreenText = Color(0xFF2E7D32)
private val ChipYellow = Color(0xFFFFF3CD)
private val ChipYellowText = Color(0xFF8D6E63)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PedidosListView(
    navController: NavController,
    viewModel: PedidosListViewModel = viewModel()
) {
    val state = viewModel.pedidosState

    val ScreenBg = Color(0xFFF6F7FB)
    val DividerColor = Color(0xFFB0B0B0)

    // --------- CONTROLO DO POPUP ----------
    var showDetailsDialog by remember { mutableStateOf(false) }
    var selectedPedidoId by remember { mutableStateOf<String?>(null) }

    fun openDetails(pedidoId: String) {
        selectedPedidoId = pedidoId
        showDetailsDialog = true
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(ScreenBg)) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Pedidos",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate(Routes.PEDIDOS_CREATE) }) {
                            Icon(Icons.Filled.Add, contentDescription = "Criar pedido")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = ScreenBg
                    )
                )
                HorizontalDivider(color = DividerColor, thickness = 2.dp)
            }
        },
        bottomBar = { BottomBar(navController = navController) }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            when (state) {
                is ResultState.Loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }

                is ResultState.Error -> Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error
                )

                is ResultState.Success -> {
                    val pedidos = state.data

                    if (pedidos.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Ainda não existem pedidos.")
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(pedidos) { pedido ->
                                PedidoCard(
                                    pedido = pedido,
                                    onClickDetalhes = { openDetails(pedido.id) }
                                )
                            }
                        }
                    }
                }

                ResultState.Idle -> Unit
            }
        }
    }

    // --------- POPUP DETALHES ----------
    if (showDetailsDialog && selectedPedidoId != null) {
        PedidoDetailsDialog(
            pedidoId = selectedPedidoId!!,
            onDismiss = {
                showDetailsDialog = false
                selectedPedidoId = null
            }
        )
    }
}

@Composable
private fun PedidoCard(
    pedido: PedidoUi,
    onClickDetalhes: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClickDetalhes() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pedido",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = pedido.data,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            EstadoPedidoChip(pedido.estado)

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(color = Color(0xFFE0E0E0))

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = onClickDetalhes,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenDark,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Ver Detalhes")
                }
            }
        }
    }
}

@Composable
private fun EstadoPedidoChip(estado: EstadoPedido) {
    val (texto, bg, fg) = when (estado) {
        EstadoPedido.ACEITE -> Triple("Aceite", ChipGreen, ChipGreenText)
        EstadoPedido.PENDENTE -> Triple("Pendente", ChipYellow, ChipYellowText)
        EstadoPedido.CANCELADO -> Triple("Cancelado", Color(0xFFFFCDD2), Color(0xFFC62828))
        else -> Triple(estado.name, Color.LightGray, Color.DarkGray)
    }

    Box(
        modifier = Modifier
            .background(bg, shape = MaterialTheme.shapes.large)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(texto, color = fg, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun PedidoDetailsDialog(
    pedidoId: String,
    onDismiss: () -> Unit,
    viewModel: PedidoDetailsViewModel = viewModel(key = "pedido_details_$pedidoId")
) {
    val state = viewModel.state

    LaunchedEffect(pedidoId) {
        viewModel.carregarPedido(pedidoId)
    }

    val df = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = Color(0xFFE9E9EF),
        title = {
            Text(
                text = "Detalhes do Pedido",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            when (val s = state) {
                is ResultState.Loading -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
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

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                        // Estado (chip)
                        val estadoStr = pedido.estado.name
                        val estado = try {
                            EstadoPedido.valueOf(estadoStr)
                        } catch (_: Exception) {
                            EstadoPedido.PENDENTE
                        }
                        EstadoPedidoChip(estado)

                        // Data
                        val dataStr = pedido.data_pedido?.let { df.format(it) } ?: "—"
                        Text(
                            text = "Data: $dataStr",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodySmall
                        )

                        Spacer(Modifier.height(6.dp))

                        Text(
                            text = "Mensagem",
                            fontWeight = FontWeight.SemiBold
                        )

                        // "Card" da mensagem (bonito como nos mockups)
                        Surface(
                            color = Color(0xFFF6F7FB),
                            shape = RoundedCornerShape(12.dp),
                            tonalElevation = 0.dp
                        ) {
                            Text(
                                text = if (pedido.descricao.isNotBlank())
                                    pedido.descricao
                                else
                                    "Sem mensagem associada.",
                                modifier = Modifier.padding(12.dp),
                                color = Color.DarkGray
                            )
                        }
                    }
                }

                ResultState.Idle -> Text("Carregue para ver detalhes.")
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = GreenDark,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Fechar")
            }
        },
    )
}
