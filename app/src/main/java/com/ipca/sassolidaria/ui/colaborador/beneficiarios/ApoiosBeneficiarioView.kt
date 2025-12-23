package com.ipca.sassolidaria.ui.colaborador.beneficiarios

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ipca.sassolidaria.data.model.Apoio
import com.ipca.sassolidaria.data.model.enums.Tipo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApoiosBeneficiarioView(
    navController: NavController,
    idBeneficiario: String,
    vm: ApoiosBeneficiarioViewModel = viewModel()
) {
    val state = vm.uiState
    LaunchedEffect(idBeneficiario) { vm.carregar(idBeneficiario) }

    val meses = listOf(
        "Janeiro","Fevereiro","Março","Abril","Maio","Junho",
        "Julho","Agosto","Setembro","Outubro","Novembro","Dezembro"
    )

    val apoiosPorMes = remember(state.apoios) { state.apoios.associateBy { it.mes } }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Apoios") },
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
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                state.error != null -> Text(state.error!!, color = Color.Red, modifier = Modifier.align(Alignment.Center))
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items((1..12).toList()) { mesNumero ->
                            val apoio: Apoio? = apoiosPorMes[mesNumero]

                            ApoioMesCard(
                                mesNome = meses[mesNumero - 1],
                                temApoio = apoio != null,
                                onMarcar = {
                                    navController.navigate(
                                        "agendamento_marcar/$idBeneficiario/${Tipo.APOIO.name}/$mesNumero"
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

@Composable
private fun ApoioMesCard(
    mesNome: String,
    temApoio: Boolean,
    onMarcar: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        androidx.compose.foundation.layout.Column(Modifier.padding(16.dp)) {
            androidx.compose.foundation.layout.Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(mesNome, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.weight(1f))

                val txt = if (temApoio) "Agendado" else "Sem Agendamento"
                val bg = if (temApoio) Color(0xFFD4EDDA) else Color(0xFFFFF3CD)
                val fg = if (temApoio) Color(0xFF2E7D32) else Color(0xFF8A6D3B)

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(bg)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(txt, color = fg, style = MaterialTheme.typography.labelSmall)
                }
            }

            if (!temApoio) {
                Spacer(Modifier.height(12.dp))
                Divider(color = Color(0xFFEAEAEA))
                Spacer(Modifier.height(8.dp))
                TextButton(
                    onClick = onMarcar,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Marcar")
                }
            }
        }
    }
}
