package com.ipca.sassolidaria.ui.colaborador.beneficiarios

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ipca.sassolidaria.data.model.Pedido
import com.ipca.sassolidaria.data.model.enums.EstadoPedido
import com.ipca.sassolidaria.data.model.enums.Tipo
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PedidosBeneficiarioView(
    navController: NavController,
    idBeneficiario: String,
    beneficiarioNome: String,
    vm: PedidosBeneficiarioViewModel = viewModel()
) {
    val state = vm.uiState
    LaunchedEffect(idBeneficiario) { vm.carregar(idBeneficiario) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Pedidos") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { pad ->
        Box(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .background(Color(0xFFF6F7FB))
        ) {

            when {
                state.isLoading -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }

                state.error != null -> {
                    Text(
                        state.error!!,
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                state.pedidos.isEmpty() -> {
                    Text(
                        "Sem pedidos",
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.pedidos) { pedido ->
                            PedidoCard(
                                pedido = pedido,
                                beneficiarioNome = beneficiarioNome,
                                onMarcar = {
                                    navController.navigate(
                                        "agendamento_marcar/$idBeneficiario/${Tipo.PEDIDO.name}/${pedido.id_pedido}"
                                    )
                                },
                                onCancelar = { vm.cancelar(pedido.id_pedido, idBeneficiario) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PedidoCard(
    pedido: Pedido,
    beneficiarioNome: String,
    onMarcar: () -> Unit,
    onCancelar: () -> Unit
) {
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val dataTxt = pedido.data_pedido?.let { sdf.format(it) } ?: "-"

    val (chipTxt, chipBg, chipFg) = when (pedido.estado) {
        EstadoPedido.PENDENTE -> Triple("Pendente", Color(0xFFFFF3CD), Color(0xFF8A6D3B))
        EstadoPedido.ACEITE -> Triple("Aceite", Color(0xFFD4EDDA), Color(0xFF2E7D32))
        EstadoPedido.CANCELADO -> Triple("Cancelado", Color(0xFFF8D7DA), Color(0xFFD32F2F))
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(Modifier.padding(16.dp)) {

            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = beneficiarioNome.ifBlank { "Beneficiário" },
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.weight(1f))
                Text(dataTxt, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(chipBg)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(chipTxt, color = chipFg, style = MaterialTheme.typography.labelSmall)
            }

            Spacer(Modifier.height(10.dp))

            var expanded by remember { mutableStateOf(false) }
            OutlinedButton(
                onClick = { expanded = !expanded },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(if (expanded) "Mostrar menos" else "Mostrar mais")
            }
            if (expanded) {
                Spacer(Modifier.height(8.dp))
                Text(pedido.descricao.ifBlank { "Sem descrição" }, color = Color.Gray)
            }

            if (pedido.estado == EstadoPedido.PENDENTE) {
                Spacer(Modifier.height(12.dp))
                Divider(color = Color(0xFFEAEAEA))
                Spacer(Modifier.height(8.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onMarcar) { Text("Marcar") }
                    TextButton(onClick = onCancelar) { Text("Cancelar") }
                }
            }
        }
    }
}
