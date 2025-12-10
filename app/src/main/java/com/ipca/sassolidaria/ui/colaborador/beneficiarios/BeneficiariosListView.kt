package com.ipca.sassolidaria.ui.colaborador.beneficiarios

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ipca.sassolidaria.core.Routes
import com.ipca.sassolidaria.data.model.Beneficiario


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeneficiariosListView(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: BeneficiariosListViewModel = viewModel()
) {
    val beneficiarios by viewModel.beneficiarios.collectAsState()
    val search by viewModel.searchText.collectAsState()

    Column(
        modifier = modifier.padding(16.dp)
    ) {

        OutlinedTextField(
            value = search,
            onValueChange = { viewModel.updateSearch(it) },
            placeholder = { Text("Pesquisar beneficiário...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(14.dp))

        val filtered = beneficiarios.filter {
            it.nome.contains(search, ignoreCase = true) ||
                    it.email.contains(search, ignoreCase = true)
        }

        LazyColumn {
            items(filtered) { b ->
                BeneficiarioCard(
                    beneficiario = b,
                    onClick = {
                        navController.navigate("${Routes.BENEFICIARIO_DETAIL}/${b.id_beneficiario}")
                    }
                )
                Spacer(Modifier.height(12.dp))
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

            DetailsButton(
                text = "Ver Detalhes",
                onClick = onClick
            )
        }
    }
}


@Composable
fun DetailsButton(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentWidth(Alignment.End)
            .height(34.dp)
            .border(
                width = 1.dp,
                color = Color(0xFF0B5E45),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 14.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color(0xFF0B5E45),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewBeneficiarioCardEmpty() {
    BeneficiarioCard(
        beneficiario = Beneficiario(),  // Sem dados
        onClick = {}
    )
}
