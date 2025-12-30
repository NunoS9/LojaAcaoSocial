package com.ipca.sassolidaria.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ipca.sassolidaria.core.UserType
import com.ipca.sassolidaria.data.model.chat.ChatMessage
import java.text.SimpleDateFormat
import java.util.*

private val Green = Color(0xFF004B3C)
private val MyMessage = Color(0xFFDCF8C6)
private val OtherMessage = Color(0xFFEDEDED)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatView(
    chatId: String,
    userType: UserType,
    navController: NavController? = null,
    viewModel: ChatViewModel = viewModel()
) {
    var messages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var text by remember { mutableStateOf("") }

    val listState = rememberLazyListState()

    /* -------------------- LISTEN + MARK AS READ -------------------- */
    LaunchedEffect(Unit) {
        viewModel.createChatIfNeeded(chatId)

        viewModel.listenMessages(chatId) {
            messages = it
        }

        // Marcar como lidas conforme o userType
        if (userType == UserType.COLABORADOR) {
            viewModel.marcarMensagensComoLidasColaborador(chatId)
        } else if (userType == UserType.BENEFICIARIO) {
            viewModel.marcarMensagensComoLidasBeneficiario(chatId)
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Suporte") },
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
                .padding(8.dp)
        ) {

            /* ---------- MENSAGENS ---------- */
            LazyColumn(
                modifier = Modifier.weight(1f),
                state = listState
            ) {
                items(messages) { msg ->

                    val isMe = msg.senderTipo == userType.name
                    val senderName =
                        if (msg.senderTipo == UserType.BENEFICIARIO.name)
                            "Beneficiário"
                        else
                            "Colaborador"

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            if (isMe) Arrangement.End else Arrangement.Start
                    ) {
                        Column(
                            modifier = Modifier
                                .background(
                                    if (isMe) MyMessage else OtherMessage,
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(10.dp)
                                .widthIn(max = 260.dp)
                        ) {

                            Text(
                                senderName,
                                style = MaterialTheme.typography.labelSmall,
                                color = Green
                            )

                            Spacer(Modifier.height(2.dp))

                            Text(msg.text)

                            Spacer(Modifier.height(4.dp))

                            Text(
                                formatTime(msg.timestamp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray,
                                modifier = Modifier.align(Alignment.End)
                            )
                        }
                    }

                    Spacer(Modifier.height(6.dp))
                }
            }

            /* ---------- INPUT ---------- */
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Escreve uma mensagem…") }
                )

                Spacer(Modifier.width(8.dp))

                Button(
                    onClick = {
                        if (text.isNotBlank()) {
                            viewModel.sendMessage(chatId, text, userType)
                            text = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Green)
                ) {
                    Text("Enviar", color = Color.White)
                }
            }
        }
    }
}

/* ---------- UTILS ---------- */

private fun formatTime(time: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(time))
}

/* ---------- PREVIEW ---------- */

@Preview(showBackground = true)
@Composable
fun ChatViewPreview() {
    ChatView(
        chatId = "",
        userType = UserType.BENEFICIARIO
    )
}
