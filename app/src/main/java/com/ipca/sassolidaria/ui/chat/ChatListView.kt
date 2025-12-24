package com.ipca.sassolidaria.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.ipca.sassolidaria.core.Routes

/* ---------- MODELO ---------- */

data class ChatListItem(
    val chatId: String,
    val nome: String
)

/* ---------- FILTRO ---------- */

enum class ChatFiltro {
    NAO_LIDAS,
    LIDAS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListView(
    navController: NavController? = null
) {
    val db = FirebaseFirestore.getInstance()

    var chatsNaoLidos by remember { mutableStateOf<List<ChatListItem>>(emptyList()) }
    var chatsLidos by remember { mutableStateOf<List<ChatListItem>>(emptyList()) }

    var filtroSelecionado by remember { mutableStateOf(ChatFiltro.NAO_LIDAS) }

    /* ---------- FIRESTORE ---------- */

    LaunchedEffect(Unit) {
        db.collection("chats")
            .whereEqualTo("ativo", true)
            .addSnapshotListener { snap, _ ->

                if (snap == null) return@addSnapshotListener

                val naoLidosTemp = mutableListOf<ChatListItem>()
                val lidosTemp = mutableListOf<ChatListItem>()

                snap.documents.forEach { chatDoc ->
                    val chatId = chatDoc.id

                    db.collection("users")
                        .document(chatId)
                        .get()
                        .addOnSuccessListener { userDoc ->

                            val nome = userDoc.getString("nome") ?: "Beneficiário"
                            val item = ChatListItem(chatId, nome)

                            db.collection("chats")
                                .document(chatId)
                                .collection("messages")
                                .whereEqualTo("senderTipo", "BENEFICIARIO")
                                .whereEqualTo("lidaPeloColaborador", false)
                                .limit(1)
                                .get()
                                .addOnSuccessListener { msgSnap ->

                                    if (msgSnap.isEmpty) {
                                        lidosTemp.add(item)
                                    } else {
                                        naoLidosTemp.add(item)
                                    }

                                    chatsNaoLidos = naoLidosTemp.toList()
                                    chatsLidos = lidosTemp.toList()
                                }
                        }
                }
            }
    }

    /* ---------- LISTA FILTRADA ---------- */

    val chatsVisiveis =
        if (filtroSelecionado == ChatFiltro.NAO_LIDAS)
            chatsNaoLidos
        else
            chatsLidos

    /* ---------- UI ---------- */

    Scaffold(
        containerColor = Color.White,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Chats de Suporte") },
                navigationIcon = {
                    IconButton(onClick = { navController?.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
        ) {

            /* ---------- CHIPS (COMO MARCAÇÕES) ---------- */

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filtroSelecionado == ChatFiltro.NAO_LIDAS,
                    onClick = { filtroSelecionado = ChatFiltro.NAO_LIDAS },
                    label = { Text("Não Lidas") }
                )

                FilterChip(
                    selected = filtroSelecionado == ChatFiltro.LIDAS,
                    onClick = { filtroSelecionado = ChatFiltro.LIDAS },
                    label = { Text("Lidas") }
                )
            }

            /* ---------- LISTA ---------- */

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                if (chatsVisiveis.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Sem chats")
                        }
                    }
                } else {
                    items(chatsVisiveis) { chat ->
                        ChatCard(chat, navController)
                    }
                }
            }
        }
    }
}

/* ---------- CARD ---------- */

@Composable
fun ChatCard(chat: ChatListItem, navController: NavController?) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable {
                navController?.navigate(
                    Routes.CHAT_COLABORADOR.replace("{chatId}", chat.chatId)
                )
            }
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(chat.nome, style = MaterialTheme.typography.titleMedium)
            Text("Pedido de Suporte", style = MaterialTheme.typography.bodySmall)
        }
    }
}

/* ---------- PREVIEW ---------- */

@Preview(showBackground = true)
@Composable
fun ChatListViewPreview() {
    ChatListView(navController = null)
}
