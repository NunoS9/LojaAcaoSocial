package com.ipca.sassolidaria.ui.colaborador.pedidos

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ipca.sassolidaria.data.model.enums.EstadoPedido
import com.ipca.sassolidaria.ui.components.BottomBar
import com.ipca.sassolidaria.core.Routes


private val GreenDark = Color(0xFF2E7D32)

data class PedidoUi(
    val id: String,
    val data: String,
    val estado: EstadoPedido
)

@Composable
fun PedidosColaboradorListView(
    navController: NavController
) {
    // Por agora dados mockados só para o layout
    val pedidos = remember {
        listOf(
            PedidoUi("Pedido 2023001", "26/10/2023", EstadoPedido.ENTREGUE),
            PedidoUi("Pedido 2023002", "15/09/2023", EstadoPedido.PENDENTE),
            PedidoUi("Pedido 2023003", "01/08/2023", EstadoPedido.CANCELADO)
        )
    }

    Scaffold(
        bottomBar = { BottomBar(navController = navController) }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {

            // Top bar tipo mockup
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
                Text(
                    text = "Pedidos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                IconButton(
                    onClick = {
                        navController.navigate(Routes.PEDIDO_COLABORADOR_CREATE)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Criar pedido"
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(pedidos) { pedido ->
                    PedidoCard(
                        pedido = pedido,
                        onClickDetalhes = {
                            // TODO: navegar para detalhes do pedido do colaborador
                        }
                    )
                }
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
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

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

            EstadoPedidoChip(estado = pedido.estado)

            Spacer(modifier = Modifier.height(12.dp))

            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp),
                color = Color(0xFFE0E0E0)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Ver Detalhes",
                style = MaterialTheme.typography.bodyMedium,
                color = GreenDark,
                modifier = Modifier.align(Alignment.End),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun EstadoPedidoChip(estado: EstadoPedido) {
    val (texto, bg, fg) = when (estado) {
        EstadoPedido.ENTREGUE -> Triple("Entregue", GreenDark, Color.White)
        EstadoPedido.PENDENTE -> Triple("Pendente", Color(0xFFFFECB3), Color(0xFF8D6E63))
        EstadoPedido.CANCELADO -> Triple("Cancelado", Color(0xFFFFCDD2), Color(0xFFC62828))
        else -> Triple(estado.name, Color.LightGray, Color.DarkGray)
    }

    Box(
        modifier = Modifier
            .background(color = bg, shape = MaterialTheme.shapes.large)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = texto,
            color = fg,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1
        )
    }
}
