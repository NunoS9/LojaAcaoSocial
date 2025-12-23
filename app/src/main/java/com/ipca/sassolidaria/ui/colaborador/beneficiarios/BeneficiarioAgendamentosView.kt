package com.ipca.sassolidaria.ui.colaborador.beneficiarios

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ipca.sassolidaria.data.model.Agendamento
import com.ipca.sassolidaria.data.model.enums.EstadoAgendamento
import com.ipca.sassolidaria.data.model.enums.Tipo
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeneficiarioAgendamentosView(
    navController: NavController,
    idBeneficiario: String,
    beneficiarioNome: String = "Beneficiário",
    vm: BeneficiarioAgendamentosViewModel = viewModel()
) {
    val state = vm.uiState

    LaunchedEffect(idBeneficiario) {
        vm.start(idBeneficiario)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Agendamentos") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
                .background(Color(0xFFF6F7FB))
        ) {

            FiltrosAgendamentos(
                filtroTipo = state.filtroTipo,
                filtroEstado = state.filtroEstado,
                onTipo = { vm.alterarFiltroTipo(it) },
                onEstado = { vm.alterarFiltroEstado(it) }
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 4.dp)
            ) {
                when {
                    state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))

                    state.error != null -> Text(
                        state.error!!,
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center)
                    )

                    else -> {
                        val listaFiltrada = state.agendamentos
                            .filter { ag -> state.filtroTipo == null || ag.tipo == state.filtroTipo }
                            .filter { ag -> state.filtroEstado == null || ag.estado == state.filtroEstado }

                        if (listaFiltrada.isEmpty()) {
                            Text(
                                "Sem agendamentos",
                                color = Color.Gray,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(
                                    items = listaFiltrada,
                                    key = { it.id_agendamento }
                                ) { ag ->
                                    AgendamentoCardColaborador(
                                        agendamento = ag,
                                        beneficiarioNome = beneficiarioNome,
                                        onEditarRecusado = {
                                            navController.navigate(
                                                "agendamento_editar/$idBeneficiario/${ag.id_agendamento}"
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FiltrosAgendamentos(
    filtroTipo: Tipo?,
    filtroEstado: EstadoAgendamento?,
    onTipo: (Tipo?) -> Unit,
    onEstado: (EstadoAgendamento?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = filtroTipo == null, onClick = { onTipo(null) }, label = { Text("Todos") })
            FilterChip(selected = filtroTipo == Tipo.PEDIDO, onClick = { onTipo(Tipo.PEDIDO) }, label = { Text("Pedidos") })
            FilterChip(selected = filtroTipo == Tipo.APOIO, onClick = { onTipo(Tipo.APOIO) }, label = { Text("Apoios") })
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = filtroEstado == null, onClick = { onEstado(null) }, label = { Text("Todos") })
            FilterChip(selected = filtroEstado == EstadoAgendamento.PENDENTE, onClick = { onEstado(EstadoAgendamento.PENDENTE) }, label = { Text("Pendentes") })
            FilterChip(selected = filtroEstado == EstadoAgendamento.ACEITE, onClick = { onEstado(EstadoAgendamento.ACEITE) }, label = { Text("Aceites") })
            FilterChip(selected = filtroEstado == EstadoAgendamento.RECUSADO, onClick = { onEstado(EstadoAgendamento.RECUSADO) }, label = { Text("Recusados") })
        }
    }
}

@Composable
private fun AgendamentoCardColaborador(
    agendamento: Agendamento,
    beneficiarioNome: String,
    onEditarRecusado: () -> Unit
) {
    // ✅ agora inclui hora
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
    val dataTxt = agendamento.data_agendada?.let { sdf.format(it) } ?: "-"

    val (estadoTxt, estadoCor) = when (agendamento.estado) {
        EstadoAgendamento.ACEITE -> "Aceite" to Color(0xFF2E7D32)
        EstadoAgendamento.PENDENTE -> "Pendente" to Color(0xFFF9A825)
        EstadoAgendamento.RECUSADO -> "Recusado" to Color(0xFFD32F2F)
    }

    val tipoTxt = when (agendamento.tipo) {
        Tipo.PEDIDO -> "Pedido"
        Tipo.APOIO -> "Apoio"
    }

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ID do Agendamento: #${agendamento.id_agendamento}",
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(Modifier.weight(1f))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(estadoCor)
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(estadoTxt, color = Color.White, style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = beneficiarioNome,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
            )

            Spacer(Modifier.height(4.dp))

            Text("Tipo: $tipoTxt", style = MaterialTheme.typography.bodySmall, color = Color(0xFF6F6F6F))
            Text("Data do Agendamento: $dataTxt", style = MaterialTheme.typography.bodySmall, color = Color(0xFF6F6F6F))

            if (agendamento.estado == EstadoAgendamento.RECUSADO) {
                Spacer(Modifier.height(12.dp))
                Divider(color = Color(0xFFEAEAEA))
                Spacer(Modifier.height(10.dp))

                Button(
                    onClick = onEditarRecusado,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B5E45)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Editar Agendamento", color = Color.White)
                }
            }
        }
    }
}
