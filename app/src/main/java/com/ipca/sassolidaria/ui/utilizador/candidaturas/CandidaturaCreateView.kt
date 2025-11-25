package com.ipca.sassolidaria.ui.utilizador.candidaturas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ipca.sassolidaria.ui.components.BottomBar
import com.ipca.sassolidaria.utils.ResultState

@Composable
fun CandidaturaCreateView(
    navController: NavController,
    viewModel: CandidaturaCreateViewModel = viewModel()
) {
    val submitState = viewModel.submitState

    // Quando submete com sucesso → volta à lista de candidaturas
    LaunchedEffect(submitState) {
        if (submitState is ResultState.Success) {
            navController.popBackStack()
        }
    }

    Scaffold(
        bottomBar = { BottomBar(navController = navController) }
    ) { innerPadding ->

        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Título
            Text(
                text = "Nova candidatura",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Subtítulo
            Text(
                text = "Dados do candidato",
                style = MaterialTheme.typography.bodyMedium
            )

            // Campos do formulário
            OutlinedTextField(
                value = viewModel.nome,
                onValueChange = {
                    viewModel.nome = it
                    viewModel.limparErros()
                },
                label = { Text("Nome completo") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = viewModel.email,
                onValueChange = {
                    viewModel.email = it
                    viewModel.limparErros()
                },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = viewModel.curso,
                onValueChange = {
                    viewModel.curso = it
                    viewModel.limparErros()
                },
                label = { Text("Curso") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = viewModel.numEstudante,
                onValueChange = {
                    viewModel.numEstudante = it
                    viewModel.limparErros()
                },
                label = { Text("Número de estudante") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = viewModel.anoLetivo,
                onValueChange = {
                    viewModel.anoLetivo = it
                    viewModel.limparErros()
                },
                label = { Text("Ano letivo") },
                modifier = Modifier.fillMaxWidth()
            )

            // Erros
            if (submitState is ResultState.Error) {
                Text(
                    text = submitState.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Botão Submeter
            Button(
                onClick = { viewModel.submeterCandidatura() },
                enabled = submitState !is ResultState.Loading,
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = androidx.compose.ui.graphics.Color(0xFF004B3C),
                    contentColor = androidx.compose.ui.graphics.Color.White
                )
            ) {
                if (submitState is ResultState.Loading) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(18.dp)
                                .padding(end = 8.dp),
                            strokeWidth = 2.dp
                        )
                        Text("A enviar...")
                    }
                } else {
                    Text("Enviar Candidatura")
                }
            }
        }
    }
}
