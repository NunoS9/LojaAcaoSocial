package com.ipca.sassolidaria.ui.colaborador.beneficiarios

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
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
                BeneficiarioDetailContent(beneficiario!!)
            }
        }
    }
}

@Composable
private fun BeneficiarioDetailContent(beneficiario: Beneficiario) {

    val green = Color(0xFF0B3D2E)
    val radius = 12.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // CARD SUPERIOR
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


        // CARD DE INFORMAÇÕES
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

                InfoRow(Icons.Default.Email, beneficiario.email)
                InfoRow(Icons.Default.Phone, beneficiario.contacto)
                InfoRow(Icons.Default.Badge, "Nº Estudante: ${beneficiario.numero_estudante}")
                InfoRow(Icons.Default.School, "${beneficiario.curso} (${beneficiario.grau_curso})")
            }
        }


        // BOTÕES RETANGULARES (dp = 12)
        listOf(
            "Ver Pedidos",
            "Ver Apoio",
            "Agendamentos",
            "Entregas"
        ).forEach { label ->
            Button(
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = green),
                shape = RoundedCornerShape(radius)
            ) {
                Text(label, color = Color.White)
            }
        }
    }
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {

        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF555555),
            modifier = Modifier.size(18.dp)
        )

        Spacer(Modifier.width(8.dp))

        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}


@Preview(showBackground = true)
@Composable
fun BeneficiarioDetailPreview() {
    BeneficiarioDetailContent(
        beneficiario = Beneficiario()
    )
}
