package com.ipca.sassolidaria.ui.colaborador.beneficiarios

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ipca.sassolidaria.core.Routes
import com.ipca.sassolidaria.data.model.Beneficiario
import com.ipca.sassolidaria.ui.components.BottomBar

private val GreenSas = Color(0xFF004B3C)
private val ScreenBg = Color(0xFFF6F7FB)
private val DividerColor = Color(0xFF858585)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeneficiariosListView(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: BeneficiariosListViewModel = viewModel()
) {
    val beneficiarios by viewModel.beneficiarios.collectAsState()
    val search by viewModel.searchText.collectAsState()

    Scaffold(
        modifier = modifier,
        containerColor = ScreenBg,
        topBar = {
            Column(modifier = Modifier.background(ScreenBg)) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Beneficiários",
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
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(24.dp))

            // Barra de Pesquisa
            OutlinedTextField(
                value = search,
                onValueChange = { viewModel.updateSearch(it) },
                placeholder = { Text("Pesquisar beneficiário...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    unfocusedBorderColor = Color.LightGray,
                    focusedBorderColor = GreenSas
                )
            )

            Spacer(Modifier.height(16.dp))

            val filtered = beneficiarios.filter {
                it.nome.contains(search, ignoreCase = true) ||
                        it.email.contains(search, ignoreCase = true)
            }

            if (filtered.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Não existem beneficiários.", color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(filtered) { b ->
                        BeneficiarioCard(
                            beneficiario = b,
                            onClick = {
                                navController.navigate("${Routes.BENEFICIARIO_DETAIL}/${b.id_beneficiario}")
                            }
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun BeneficiarioCard(
    beneficiario: Beneficiario,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        elevation = CardDefaults.cardElevation(1.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(16.dp)) {

            Text(
                beneficiario.nome,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Email,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(beneficiario.email, color = Color.Gray)
            }

            Spacer(Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Phone,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(beneficiario.contacto, color = Color.Gray)
            }

            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenSas,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Ver Detalhes", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewBeneficiarioCardEmpty() {
    BeneficiarioCard(
        beneficiario = Beneficiario(),
        onClick = {}
    )
}
