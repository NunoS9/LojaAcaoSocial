package com.ipca.sassolidaria.ui.colaborador.beneficiarios

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
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
import com.ipca.sassolidaria.data.model.Beneficiario

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeneficiarioDetailView(
    navController: NavController,
    id: String,
    vm: BeneficiarioDetailViewModel = viewModel()
) {
    val beneficiario by vm.beneficiario.collectAsState()

    LaunchedEffect(id) { vm.loadBeneficiario(id) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Detalhes") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color.White)
        ) {
            if (beneficiario == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                BeneficiarioDetailContent(
                    beneficiario = beneficiario!!,
                    onVerPedidos = {
                        navController.navigate(
                            Routes.BENEFICIARIO_PEDIDOS.replace("{idBeneficiario}", beneficiario!!.id_beneficiario)
                        )
                    },
                    onVerApoios = {
                        navController.navigate(
                            Routes.BENEFICIARIO_APOIOS.replace("{idBeneficiario}", beneficiario!!.id_beneficiario)
                        )
                    },
                    onVerAgendamentos = {
                        navController.navigate(
                            Routes.BENEFICIARIO_AGENDAMENTOS.replace(
                                "{idBeneficiario}",
                                beneficiario!!.id_beneficiario
                            )
                        )
                    },
                    onVerEntregas = {
                        navController.navigate(
                            Routes.BENEFICIARIO_ENTREGAS.replace(
                                "{idBeneficiario}",
                                beneficiario!!.id_beneficiario
                            )
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun BeneficiarioDetailContent(
    beneficiario: Beneficiario,
    onVerPedidos: () -> Unit,
    onVerApoios: () -> Unit,
    onVerAgendamentos: () -> Unit,
    onVerEntregas: () -> Unit
) {
    val green = Color(0xFF0B3D2E)
    val radius = 12.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(radius),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFB0BEC5)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text(beneficiario.nome, style = MaterialTheme.typography.titleMedium)

                Text(
                    text = "ID: ${beneficiario.id_beneficiario}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(radius),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Informações", style = MaterialTheme.typography.titleMedium)

                Text("Email: ${beneficiario.email}", color = Color(0xFF555555))
                Text("Contacto: ${beneficiario.contacto}", color = Color(0xFF555555))
                Text("Nº Estudante: ${beneficiario.numero_estudante}", color = Color(0xFF555555))
                Text("${beneficiario.curso} (${beneficiario.grau_curso})", color = Color(0xFF555555))
            }
        }

        Button(
            onClick = onVerPedidos,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = green),
            shape = RoundedCornerShape(radius)
        ) { Text("Ver Pedidos", color = Color.White) }

        Button(
            onClick = onVerApoios,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = green),
            shape = RoundedCornerShape(radius)
        ) { Text("Ver Apoio", color = Color.White) }

        Button(
            onClick = onVerAgendamentos,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = green),
            shape = RoundedCornerShape(radius)
        ) { Text("Agendamentos", color = Color.White) }

        Button(
            onClick = onVerEntregas,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = green),
            shape = RoundedCornerShape(radius)
        ) { Text("Entregas", color = Color.White) }
    }
}
