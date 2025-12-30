package com.ipca.sassolidaria.ui.colaborador.produtos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ipca.sassolidaria.data.model.enums.TipoProduto

private val GreenSas = Color(0xFF004B3C)
private val RedAction = Color(0xFFE57373)
private val ScreenBg = Color(0xFFF6F7FB)
private val DividerColor = Color(0xFF858585)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProdutoEditView(
    navController: NavController,
    viewModel: ProdutoFormViewModel = viewModel()
) {
    val state = viewModel.uiState

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(ScreenBg)) {
                CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Editar Produto",
                        style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = ScreenBg
                    )
                )
                HorizontalDivider(color = DividerColor, thickness = 2.dp)
            }
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
                modifier = Modifier.fillMaxWidth()
            )

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
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    }
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
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.validade,
                onValueChange = { viewModel.onValidadeChange(it) },
                label = { Text("Validade (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth()
            )

            state.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(8.dp))

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                onClick = {
                    viewModel.guardar {
                        navController.popBackStack()
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenSas)
            ) {
                Text("Salvar Alterações", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RedAction),
                shape = RoundedCornerShape(12.dp),
                onClick = {
                    viewModel.eliminar {
                        navController.popBackStack()
                    }
                }
            ) {
                Text("Eliminar Produto", fontWeight = FontWeight.Bold)
            }
        }
    }
}
