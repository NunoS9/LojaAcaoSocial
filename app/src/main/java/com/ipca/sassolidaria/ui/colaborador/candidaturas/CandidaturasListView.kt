package com.ipca.sassolidaria.ui.colaborador.candidaturas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ipca.sassolidaria.core.Routes
import com.ipca.sassolidaria.ui.components.BottomBar
import com.ipca.sassolidaria.data.model.Candidatura

private val GreenPrimary = Color(0xFF004B3C)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CandidaturasListView(
    navController: NavController,
    viewModel: CandidaturasListViewModel = viewModel()
) {
    val candidaturas = viewModel.candidaturas
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.carregarCandidaturas()
    }

    val ScreenBg = Color(0xFFF6F7FB)
    val DividerColor = Color(0xFFB0B0B0)

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(ScreenBg)) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Candidaturas",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = ScreenBg
                    )
                )
                HorizontalDivider(color = DividerColor, thickness = 2.dp)
            }
        },
        bottomBar = { BottomBar(navController = navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(16.dp))
            // Barra de Pesquisa (Visual)
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Pesquisar candidatura...") },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.LightGray
                )
            )

            Spacer(Modifier.height(16.dp))

            if (viewModel.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GreenPrimary)
                }
            } else if (candidaturas.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Não existem candidaturas pendentes.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Filtra localmente pelo nome apenas para efeito visual
                    val filtered = if (searchQuery.isBlank()) candidaturas else candidaturas.filter {
                        it.nome.contains(searchQuery, ignoreCase = true)
                    }

                    items(filtered) { item ->
                        CandidaturaCard(item) {
                            // Navega para o detalhe passando o ID
                            navController.navigate("${Routes.CANDIDATURA_DETAIL_COLABORADOR}/${item.id_candidatura}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CandidaturaCard(candidatura: Candidatura, onDetalhesClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = candidatura.nome,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Email, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(candidatura.email, fontSize = 14.sp, color = Color.Gray)
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Phone, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(candidatura.telemovel.toString(), fontSize = 14.sp, color = Color.Gray)
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = onDetalhesClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = GreenPrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Ver Detalhes", fontWeight = FontWeight.Bold)
            }
        }
    }
}