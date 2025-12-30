package com.ipca.sassolidaria.ui.beneficiario.entregas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ipca.sassolidaria.utils.ResultState

private val DarkGreen = Color(0xFF004B3C)
private val CardBg = Color(0xFFF6F7FB)

@Composable
fun EntregaDetalhesDialogView(
    detalhesState: ResultState<List<String>>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp),

        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Itens da entrega",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DarkGreen
                )
                Text(
                    text = "Detalhes dos produtos incluídos",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        },

        text = {
            when (detalhesState) {
                is ResultState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is ResultState.Error -> {
                    Surface(
                        color = Color(0xFFFFEBEE),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = detalhesState.message,
                            color = Color(0xFFC62828),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                is ResultState.Success -> {
                    val itens = detalhesState.data

                    if (itens.isEmpty()) {
                        Surface(
                            color = CardBg,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Nenhum item associado a esta entrega.",
                                color = Color.DarkGray,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 280.dp) // ✅ evita dialog gigante
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            itens.forEachIndexed { index, linha ->
                                Surface(
                                    color = CardBg,
                                    shape = RoundedCornerShape(12.dp),
                                    tonalElevation = 1.dp
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(26.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(DarkGreen.copy(alpha = 0.12f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = (index + 1).toString(),
                                                color = DarkGreen,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Spacer(Modifier.width(10.dp))

                                        Text(
                                            text = linha,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color(0xFF2B2B2B)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                ResultState.Idle -> {
                    Surface(
                        color = CardBg,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Carregue em 'Ver Detalhes' para ver os itens.",
                            color = Color.DarkGray,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        },

        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkGreen,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Fechar")
            }
        }
    )
}
