package com.ipca.sassolidaria.ui.beneficiario.pedidos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ipca.sassolidaria.ui.components.BottomBar
import com.ipca.sassolidaria.utils.ResultState
import java.text.SimpleDateFormat
import java.util.Locale

private val DarkGreen = Color(0xFF004B3C)
private val ScreenBg = Color(0xFFF6F7FB)
private val CardBg = Color.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PedidoDetailsView(
    navController: NavController,
    pedidoId: String,
    viewModel: PedidoDetailsViewModel = viewModel()
) {
    val state = viewModel.state

    LaunchedEffect(pedidoId) {
        viewModel.carregarPedido(pedidoId)
    }

    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(ScreenBg)) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Detalhes do Pedido",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Voltar"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = ScreenBg
                    )
                )
                HorizontalDivider(color = Color(0xFFB0B0B0), thickness = 2.dp)
            }
        },
        bottomBar = { BottomBar(navController = navController) }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(ScreenBg)
                .padding(16.dp)
        ) {

            when (val s = state) {
                is ResultState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is ResultState.Error -> {
                    Surface(
                        color = Color(0xFFFFEBEE),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = s.message,
                            color = Color(0xFFC62828),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                is ResultState.Success -> {
                    val pedido = s.data

                    // --- chip do estado (bg + fg)
                    val estadoStr = pedido.estado.toString()

                    // se o teu pedido.estado for String, isto funciona igual
                    val (estadoTexto, estadoBg, estadoFg) = when (estadoStr.uppercase()) {
                        "ACEITE" -> Triple("Aceite", Color(0xFFE8F5E9), Color(0xFF2E7D32))
                        "PENDENTE" -> Triple("Pendente", Color(0xFFFFF8E1), Color(0xFFF9A825))
                        "CANCELADO" -> Triple("Cancelado", Color(0xFFFFCDD2), Color(0xFFC62828))
                        else -> Triple(estadoStr, Color.LightGray, Color.DarkGray)
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                        // ---- Card Resumo
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = CardBg,
                            shape = RoundedCornerShape(12.dp),
                            tonalElevation = 1.dp
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Estado",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                    Spacer(Modifier.weight(1f))

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(estadoBg)
                                            .padding(horizontal = 12.dp, vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = estadoTexto,
                                            color = estadoFg,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                Spacer(Modifier.height(10.dp))

                                Text(
                                    text = "Data do pedido",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                                Text(
                                    text = pedido.data_pedido?.let { dateFormat.format(it) } ?: "-",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // ---- Card Mensagem
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = CardBg,
                            shape = RoundedCornerShape(12.dp),
                            tonalElevation = 1.dp
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Mensagem do pedido",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkGreen
                                )
                                Spacer(Modifier.height(8.dp))

                                val msg = pedido.descricao.trim()
                                Text(
                                    text = if (msg.isNotBlank()) msg else "Sem mensagem associada.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF2B2B2B)
                                )
                            }
                        }
                    }
                }

                ResultState.Idle -> Unit
            }
        }
    }
}
