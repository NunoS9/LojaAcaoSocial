package com.ipca.sassolidaria.ui.beneficiario.pedidos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ipca.sassolidaria.ui.components.BottomBar
import com.ipca.sassolidaria.utils.ResultState

private val GreenDark = Color(0xFF2E7D32)

@Composable
fun PedidoCreateView(
    navController: NavController,
    viewModel: PedidoCreateViewModel = viewModel()
) {
    var detalhes by remember { mutableStateOf("") }
    val submitState = viewModel.submitState

    LaunchedEffect(submitState) {
        if (submitState is ResultState.Success) {
            navController.popBackStack()
        }
    }

    Scaffold(
        bottomBar = { BottomBar(navController = navController) }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Voltar"
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Criar Pedido",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "Detalhes do Pedido",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = detalhes,
                onValueChange = {
                    detalhes = it
                    viewModel.limparErros()
                },
                label = { Text("Adicione quaisquer informações relevantes para o pedido") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )

            if (submitState is ResultState.Error) {
                Text(
                    text = submitState.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { viewModel.criarPedido(detalhes) },
                enabled = submitState !is ResultState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GreenDark,
                    contentColor = Color.White
                )
            ) {
                if (submitState is ResultState.Loading) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(18.dp)
                                .padding(end = 8.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        Text("A enviar...")
                    }
                } else {
                    Text("Finalizar Pedido")
                }
            }
        }
    }
}
