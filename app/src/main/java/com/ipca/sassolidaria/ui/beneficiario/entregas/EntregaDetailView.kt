package com.ipca.sassolidaria.ui.beneficiario.entregas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ipca.sassolidaria.utils.ResultState

@Composable
fun EntregaDetalhesDialogView(
    detalhesState: ResultState<List<String>>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Fechar") }
        },
        title = { Text("Itens da entrega") },
        text = {
            when (detalhesState) {
                is ResultState.Loading -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) { CircularProgressIndicator() }
                }

                is ResultState.Error -> {
                    Text(
                        text = detalhesState.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                is ResultState.Success -> {
                    val itens = detalhesState.data
                    if (itens.isEmpty()) {
                        Text("Nenhum item associado a esta entrega.")
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            itens.forEach { linha -> Text("• $linha") }
                        }
                    }
                }

                ResultState.Idle -> Text("Carregue em 'Ver Detalhes' para ver os itens.")
            }
        }
    )
}
