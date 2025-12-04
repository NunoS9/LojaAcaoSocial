package com.ipca.sassolidaria.ui.colaborador.produtos

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ipca.sassolidaria.data.model.enums.TipoProduto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProdutoCreateView(
    navController: NavController,
    viewModel: ProdutoFormViewModel = viewModel()
) {
    val state = viewModel.uiState

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Adicionar Produto",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            OutlinedTextField(
                value = state.nome,
                onValueChange = { viewModel.onNomeChange(it) },
                label = { Text("Nome do Produto") },
                placeholder = { Text("Ex: Arroz, Leite, Bolachas") },
                modifier = Modifier.fillMaxWidth()
            )

            // Dropdown Tipo
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    value = when (state.tipo) {
                        TipoProduto.HIGIENE -> "Higiene"
                        TipoProduto.LIMPEZA -> "Limpeza"
                        TipoProduto.ALIMENTACAO -> "Alimentação"
                    },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tipo") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Higiene") },
                        onClick = {
                            viewModel.onTipoChange(TipoProduto.HIGIENE)
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Limpeza") },
                        onClick = {
                            viewModel.onTipoChange(TipoProduto.LIMPEZA)
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Alimentação") },
                        onClick = {
                            viewModel.onTipoChange(TipoProduto.ALIMENTACAO)
                            expanded = false
                        }
                    )
                }
            }

            OutlinedTextField(
                value = state.quantidade,
                onValueChange = { viewModel.onQuantidadeChange(it) },
                label = { Text("Quantidade") },
                placeholder = { Text("Ex: 100") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.validade,
                onValueChange = { viewModel.onValidadeChange(it) },
                label = { Text("Validade (YYYY-MM-DD)") },
                placeholder = { Text("Ex: 2024-12-31") },
                modifier = Modifier.fillMaxWidth()
            )

            state.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = { navController.popBackStack() }
                ) {
                    Text("Cancelar")
                }

                Button(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        viewModel.guardar {
                            navController.popBackStack()
                        }
                    }
                ) {
                    Text("Salvar Produto")
                }
            }
        }
    }
}
