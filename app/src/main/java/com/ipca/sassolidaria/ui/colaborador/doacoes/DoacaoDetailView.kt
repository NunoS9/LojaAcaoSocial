package com.ipca.sassolidaria.ui.colaborador.doacoes

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ipca.sassolidaria.R
import com.ipca.sassolidaria.data.model.ItemDoacao
import java.text.SimpleDateFormat
import java.util.*


private val VerdeClaro = Color(0xFFB2E8C8)
private val RosaUser = Color(0xFFD938A0)

@Composable
fun DoacaoDetailView(
    navController: NavController,
    id: String,
    vm: DoacaoDetailViewModel = viewModel()
) {
    LaunchedEffect(Unit) { vm.loadDoacao(id) }

    val doacao by vm.doacao.collectAsState()
    val doador by vm.doador.collectAsState()
    val itens by vm.itens.collectAsState()

    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    Column(Modifier.fillMaxSize().padding(16.dp)) {

        Row(
            Modifier.fillMaxWidth().height(56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, null)
            }

            Box(Modifier.weight(1f), Alignment.Center) {
                Text("Detalhes da Doação", style = MaterialTheme.typography.titleLarge)
            }

            Spacer(Modifier.width(48.dp))
        }

        Divider()
        Spacer(Modifier.height(16.dp))

        doacao?.let { d ->
            DoacaoHeaderCardFull(
                idDoacao = d.id_doacao,
                nome = doador?.nome ?: "",
                contacto = doador?.contacto ?: "",
                data = d.data_doacao?.let { sdf.format(it) } ?: "",
                totalItens = itens.size
            )
        }

        Spacer(Modifier.height(25.dp))

        Text("Itens Doados", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))

        /* ---------------- LISTA DE ITENS ---------------- */
        LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            items(itens) { item ->
                ItemDoadoCard(item)
            }
        }
    }
}

@Composable
fun DoacaoHeaderCardFull(
    idDoacao: String,
    nome: String,
    contacto: String,
    data: String,
    totalItens: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(20.dp)) {

            Text("Doação $idDoacao", style = MaterialTheme.typography.titleMedium, color = Verde)

            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {

                Box(
                    modifier = Modifier.size(48.dp).background(RosaUser, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, null, tint = Color.White)
                }

                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Text(nome, style = MaterialTheme.typography.bodyLarge)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Phone, null, tint = Verde)
                        Spacer(Modifier.width(4.dp))
                        Text(contacto, style = MaterialTheme.typography.bodySmall)
                    }
                }

                AssistChip(
                    onClick = {},
                    label = { Text("Concluída") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = VerdeClaro,
                        labelColor = Verde
                    )
                )
            }

            Spacer(Modifier.height(16.dp))
            Divider()
            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarMonth, null, tint = Verde)
                Spacer(Modifier.width(8.dp))
                Text("Data da Doação: $data")
            }

            Spacer(Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {

                Icon(
                    painter = painterResource(id = R.drawable.ic_produtos),
                    contentDescription = null,
                    tint = Verde
                )

                Spacer(Modifier.width(8.dp))
                Text("Total de Itens: $totalItens")
            }
        }
    }
}

@Composable
fun ItemDoadoCard(item: ItemDoacao) {

    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {

            Icon(
                painter = painterResource(id = R.drawable.ic_produtos),
                contentDescription = null,
                tint = Verde
            )

            Spacer(Modifier.width(12.dp))

            Column {
                Text(item.nome, style = MaterialTheme.typography.titleMedium)
                Text("Quantidade: ${item.quantidade}")
                Text("Tipo: ${item.tipo_produto}")
                Text("Validade: ${item.data_validade?.let { sdf.format(it) }}")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewDoacaoDetailView() {
    DoacaoDetailView(rememberNavController(), id = "ABC123")
}
