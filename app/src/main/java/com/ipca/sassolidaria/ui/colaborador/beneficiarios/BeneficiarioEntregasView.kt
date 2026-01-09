package com.ipca.sassolidaria.ui.colaborador.beneficiarios

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ipca.sassolidaria.core.Routes
import com.ipca.sassolidaria.data.model.enums.EstadoEntrega
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeneficiarioEntregasView(
    navController: NavController,
    idBeneficiario: String,
    beneficiarioNome: String = "Beneficiário",
    vm: BeneficiarioEntregasViewModel = viewModel()
) {
    val state by vm.uiState

    LaunchedEffect(idBeneficiario) { vm.start(idBeneficiario) }

    val proximas = state.entregas.filter { it.entrega.estado != EstadoEntrega.ENTREGUE }
    val historico = state.entregas.filter { it.entrega.estado == EstadoEntrega.ENTREGUE }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Entregas") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { pad ->
        Box(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
                .background(Color(0xFFF6F7FB))
        ) {
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))

                state.error != null -> Text(
                    state.error!!,
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.Center)
                )

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        item {
                            Text("Próximas Entregas", fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(6.dp))
                        }

                        if (proximas.isEmpty()) {
                            item { Text("Sem entregas pendentes.", color = Color.Gray) }
                        } else {
                            items(items = proximas, key = { ui -> ui.entrega.id_entrega }) { ui ->
                                EntregaCard(
                                    entregaUi = ui,
                                    beneficiarioNome = beneficiarioNome,
                                    onProcessarOuEditar = {
                                        navController.navigate(
                                            Routes.ENTREGA_PROCESSAR
                                                .replace("{idEntrega}", ui.entrega.id_entrega)
                                                .replace("{idBeneficiario}", idBeneficiario)
                                        )
                                    },
                                    onConfirmar = {
                                        vm.confirmarEntregaEAtualizarStock(ui.entrega.id_entrega)
                                    }
                                )
                            }
                        }

                        item {
                            Spacer(Modifier.height(12.dp))
                            Text("Histórico de Entregas", fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(6.dp))
                        }

                        if (historico.isEmpty()) {
                            item { Text("Sem entregas entregues.", color = Color.Gray) }
                        } else {
                            items(items = historico, key = { ui -> ui.entrega.id_entrega }) { ui ->
                                EntregaCard(
                                    entregaUi = ui,
                                    beneficiarioNome = beneficiarioNome,
                                    onProcessarOuEditar = { },
                                    onConfirmar = { }
                                )
                            }
                        }

                        item { Spacer(Modifier.height(30.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun EntregaCard(
    entregaUi: EntregaUi,
    beneficiarioNome: String,
    onProcessarOuEditar: () -> Unit,
    onConfirmar: () -> Unit
) {
    val entrega = entregaUi.entrega

    val (chipTxt, chipBg, chipFg) = when (entrega.estado) {
        EstadoEntrega.PENDENTE -> Triple("Pendente", Color(0xFFFFF3CD), Color(0xFF8A6D3B))
        EstadoEntrega.PRONTA -> Triple("Pronta", Color(0xFFE3F2FD), Color(0xFF1565C0))
        EstadoEntrega.ENTREGUE -> Triple("Entregue", Color(0xFFD4EDDA), Color(0xFF2E7D32))
        EstadoEntrega.CANCELADA -> Triple("Cancelada", Color(0xFFF8D7DA), Color(0xFFD32F2F))
    }

    val sdf = androidx.compose.runtime.remember {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    }
    val dataTxt = entregaUi.dataAgendada?.let { sdf.format(it) } ?: "-"

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {

                Text(
                    beneficiarioNome.ifBlank { "Beneficiário" },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.weight(1f))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(chipBg)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) { Text(chipTxt, color = chipFg, style = MaterialTheme.typography.labelSmall) }
            }

            Spacer(Modifier.height(10.dp))

            Text("Tipo: ${entrega.tipo.name}", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
            Text("Data do Agendamento: $dataTxt", color = Color.Gray, style = MaterialTheme.typography.bodySmall)

            if (entregaUi.itensResumo.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Itens: ${entregaUi.itensResumo}",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (entrega.estado == EstadoEntrega.PENDENTE || entrega.estado == EstadoEntrega.PRONTA) {
                Spacer(Modifier.height(12.dp))
                Divider(color = Color(0xFFEAEAEA))
                Spacer(Modifier.height(10.dp))

                val label = if (entrega.estado == EstadoEntrega.PENDENTE) "Processar Entrega" else "Editar Entrega"

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Button(
                        onClick = onProcessarOuEditar,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B5E45)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) { Text(label, color = Color.White) }

                    Spacer(Modifier.weight(0.08f))

                    Button(
                        onClick = onConfirmar,
                        enabled = entrega.estado == EstadoEntrega.PRONTA,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B5E45)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) { Text("Confirmar", color = Color.White) }
                }
            }
        }
    }
}
