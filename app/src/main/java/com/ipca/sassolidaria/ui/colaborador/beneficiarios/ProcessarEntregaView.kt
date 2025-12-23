package com.ipca.sassolidaria.ui.colaborador.beneficiarios

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.ipca.sassolidaria.data.model.Produto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProcessarEntregaView(
    navController: NavController,
    idEntrega: String,
    idBeneficiario: String,
    vm: ProcessarEntregaViewModel = viewModel()
) {
    val state = vm.uiState

    // ✅ Carrega produtos + itens já existentes da entrega
    LaunchedEffect(idEntrega) { vm.carregarTudo(idEntrega) }

    var search by remember { mutableStateOf("") }
    var selecoes by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }

    // ✅ Preenche seleções quando o VM carregar as seleções atuais (edição)
    LaunchedEffect(state.selecoesAtuais) {
        if (state.selecoesAtuais.isNotEmpty() && selecoes.isEmpty()) {
            selecoes = state.selecoesAtuais
        }
    }

    val produtosFiltrados = remember(state.produtos, search) {
        val s = search.trim()
        if (s.isBlank()) state.produtos
        else state.produtos.filter { it.nome.contains(s, ignoreCase = true) }
    }

    val podeConfirmar = selecoes.values.any { it > 0 }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Processar Entrega") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
                .background(Color(0xFFF6F7FB))
        ) {

            Card(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Produtos Disponíveis", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = search,
                        onValueChange = { search = it },
                        placeholder = { Text("Pesquisar produto...") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    state.error?.let {
                        Spacer(Modifier.height(10.dp))
                        Text(it, color = Color.Red)
                    }
                }
            }

            when {
                state.isLoading && state.produtos.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(produtosFiltrados, key = { it.id_produto }) { p ->
                            ProdutoSelecionavelCard(
                                produto = p,
                                quantidadeSelecionada = selecoes[p.id_produto] ?: 0,
                                onAlterar = { novaQtd ->
                                    val max = p.quantidade_disponivel
                                    val fixed = novaQtd.coerceIn(0, max)

                                    selecoes = if (fixed == 0) {
                                        selecoes - p.id_produto
                                    } else {
                                        selecoes + (p.id_produto to fixed)
                                    }
                                }
                            )
                        }

                        item { Spacer(Modifier.height(90.dp)) }
                    }
                }
            }

            // Botão fixo no fundo
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Button(
                        onClick = {
                            vm.processarEntrega(
                                idEntrega = idEntrega,
                                selecoes = selecoes,
                                onOk = { navController.popBackStack() }
                            )
                        },
                        enabled = podeConfirmar && !state.isLoading,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(10.dp))
                            Text("A processar...")
                        } else {
                            Text("Confirmar Entrega")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProdutoSelecionavelCard(
    produto: Produto,
    quantidadeSelecionada: Int,
    onAlterar: (Int) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(produto.nome, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Stock: ${produto.quantidade_disponivel} unidades",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = { onAlterar(quantidadeSelecionada - 1) },
                        enabled = quantidadeSelecionada > 0,
                        modifier = Modifier.size(36.dp),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) { Text("−") }

                    Spacer(Modifier.width(10.dp))

                    Text(quantidadeSelecionada.toString(), fontWeight = FontWeight.SemiBold)

                    Spacer(Modifier.width(10.dp))

                    Button(
                        onClick = { onAlterar(quantidadeSelecionada + 1) },
                        enabled = quantidadeSelecionada < produto.quantidade_disponivel,
                        modifier = Modifier.size(36.dp),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) { Text("+") }
                }
            }

            Spacer(Modifier.height(10.dp))
            Divider(color = Color(0xFFEAEAEA))
        }
    }
}
