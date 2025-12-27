package com.ipca.sassolidaria.ui.beneficiario.pedidos

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ipca.sassolidaria.core.Routes
import com.ipca.sassolidaria.ui.components.BottomBar
import com.ipca.sassolidaria.data.model.enums.EstadoPedido
import com.ipca.sassolidaria.utils.ResultState
import androidx.compose.foundation.shape.RoundedCornerShape

private val GreenDark = Color(0xFF2E7D32)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PedidosListView(
    navController: NavController,
    viewModel: PedidosListViewModel = viewModel()
) {
    val state = viewModel.pedidosState

    val ScreenBg = Color(0xFFF6F7FB)
    val DividerColor = Color(0xFFB0B0B0)

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
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            navController.navigate(Routes.PEDIDOS_CREATE)
                        }) {
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
        bottomBar = {
            BottomBar(navController = navController)
        }
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
                ) {
                    CircularProgressIndicator()
                }

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
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(pedidos) { pedido ->
                                PedidoCard(
                                    pedido = pedido,
                                    onClickDetalhes = {
                                        navController.navigate("${Routes.PEDIDO_DETAILS_BENEFICIARIO}/${pedido.id}")
                                    }
                                )
                            }
                        }
                    }
                }

                ResultState.Idle -> {}
            }
        }
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
                    text = pedido.id,
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

            Divider(color = Color(0xFFE0E0E0))

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
        EstadoPedido.ACEITE -> Triple("Entregue", GreenDark, Color.White)
        EstadoPedido.PENDENTE -> Triple("Pendente", Color(0xFFFFF3CD), Color(0xFF8D6E63))
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
