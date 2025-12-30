package com.ipca.sassolidaria.ui.colaborador.candidaturas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

private val GreenPrimary = Color(0xFF004B3C)
private val RedAction = Color(0xFFE57373)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CandidaturaDetailView(
    navController: NavController,
    candidaturaId: String,
    viewModel: CandidaturaDetailViewModel = viewModel()
) {
    // Carrega os dados ao entrar
    LaunchedEffect(candidaturaId) {
        viewModel.carregarCandidatura(candidaturaId)
    }

    val item = viewModel.candidatura
    var showMore by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Detalhes", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { innerPadding ->

        if (viewModel.isLoading || item == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GreenPrimary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- CARD PERFIL ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Avatar Placeholder
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF9EA4AA)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(item.nome, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        // Mostra número de estudante
                        Text("Estudante: ${item.num_estudante}", color = Color.Gray, fontSize = 14.sp)
                    }
                }

                Spacer(Modifier.height(16.dp))

                // --- CARD INFORMAÇÕES ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Informações", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.height(16.dp))

                        InfoRow(Icons.Outlined.Email, item.email)
                        Spacer(Modifier.height(8.dp))
                        InfoRow(Icons.Outlined.Phone, item.telemovel.toString())
                        Spacer(Modifier.height(8.dp))

                        // CORREÇÃO 1: grauCurso -> grau_curso
                        InfoRow(Icons.Outlined.School, "${item.grau_curso.name} - ${item.curso}")

                        Spacer(Modifier.height(8.dp))
                        InfoRow(Icons.Outlined.Badge, "CC: ${item.cc}")

                        AnimatedVisibility(visible = showMore) {
                            Column(Modifier.padding(top = 8.dp)) {

                                HorizontalDivider(Modifier.padding(vertical = 8.dp))

                                Text("Detalhes Financeiros:", fontWeight = FontWeight.SemiBold)
                                InfoText("Bolsa: ${if (item.bolsa) "Sim" else "Não"}")

                                // CORREÇÃO 2: obsBolsa -> obs_bolsa
                                if(item.bolsa) InfoText("Obs: ${item.obs_bolsa}")

                                InfoText("FAES: ${if (item.faes) "Sim" else "Não"}")

                                Spacer(Modifier.height(8.dp))
                                Text("Apoios Solicitados:", fontWeight = FontWeight.SemiBold)
                                if(item.apoio_alimentacao) InfoText("- Alimentação")
                                if(item.apoio_higiene) InfoText("- Higiene")
                                if(item.apoio_limpeza) InfoText("- Limpeza")
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Botão Toggle "Mostrar mais"
                        OutlinedButton(
                            onClick = { showMore = !showMore },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray)
                        ) {
                            Text(if (showMore) "Mostrar menos" else "Mostrar mais")
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                if(showMore) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // AÇÕES DO COLABORADOR
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Ações do Colaborador", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.height(16.dp))

                        // Botão APROVAR
                        Button(
                            onClick = {
                                viewModel.aprovarCandidatura { navController.popBackStack() }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                        ) {
                            Text("Aprovar Beneficiário", fontWeight = FontWeight.Bold)
                        }

                        Spacer(Modifier.height(12.dp))

                        // Botão RECUSAR
                        Button(
                            onClick = {
                                viewModel.recusarCandidatura { navController.popBackStack() }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RedAction)
                        ) {
                            Text("Recusar Beneficiário", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(text, fontSize = 15.sp, color = Color(0xFF424242))
    }
}

@Composable
fun InfoText(text: String) {
    Text(text, fontSize = 14.sp, color = Color(0xFF424242), modifier = Modifier.padding(start = 32.dp, top = 2.dp))
}