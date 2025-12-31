package com.ipca.sassolidaria.ui.utilizador.candidaturas

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ipca.sassolidaria.data.model.DocumentoCandidatura
import com.ipca.sassolidaria.data.model.enums.GrauCurso
import com.ipca.sassolidaria.ui.components.BottomBar
import com.ipca.sassolidaria.utils.ResultState
import com.ipca.sassolidaria.utils.uriToBase64
import java.util.Date

private val GreenDark = Color(0xFF004B3C)
private val ScreenBg = Color(0xFFF6F7FB)
private val DividerColor = Color(0xFFB0B0B0)

private const val DOCUMENTOS_INFO = """
Nota: os candidatos abrangidos pelo Fundo de Apoio de Emergência Social (FAES) dispensam entrega
de documentos e avaliação da situação socioeconómica.

a) Fotocópia de recibos de despesas permanentes (ex.: despesas referentes a habitação,
despesas com saúde e/ou transporte) do agregado familiar;
b) Fotocópia dos 3 últimos recibos de vencimentos/pensões/subsídios/prestações sociais;
c) Extratos bancários dos 3 últimos meses dos elementos do agregado familiar;
d) Outros rendimentos recebidos, a qualquer título, pelos elementos do agregado familiar;
e) No caso de o requerente ser originário de países terceiros (...) deve ser apresentado o título
de residência e os meios de prova autenticados:
   i. Comprovativo dos apoios recebidos do Estado do país de origem;
   ii. Declaração do Município/Poder Local com composição do agregado e condição socioeconómica.
"""

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CandidaturaCreateView(
    navController: NavController,
    viewModel: CandidaturaCreateViewModel = viewModel()
) {
    val submitState = viewModel.submitState
    val ctx = LocalContext.current

    var dataNascimento by remember { mutableStateOf("") }


    var showInfo by remember { mutableStateOf(false) }


    val docsPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNullOrEmpty()) return@rememberLauncherForActivityResult

        uris.forEach { uri ->
            runCatching {
                val nome = getFileName(ctx, uri) ?: (uri.lastPathSegment ?: "documento")
                val mime = ctx.contentResolver.getType(uri) ?: "application/octet-stream"
                val base64 = uriToBase64(ctx, uri)


                val tempId = "${System.currentTimeMillis()}_${nome.hashCode()}"

                viewModel.adicionarDocumento(
                    DocumentoCandidatura(
                        id_documento = tempId,
                        nome = nome,
                        tipoMime = mime,
                        base64 = base64,
                        data_upload = Date()
                    )
                )
            }
        }
    }

    LaunchedEffect(submitState) {
        if (submitState is ResultState.Success) {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(ScreenBg)) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Submeter Candidatura",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Voltar")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = ScreenBg,
                        scrolledContainerColor = ScreenBg
                    )
                )
                Divider(color = DividerColor, thickness = 1.dp)
            }
        },
        bottomBar = { BottomBar(navController = navController) },
        containerColor = ScreenBg
    ) { innerPadding ->

        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Detalhes da Candidatura",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
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

            Text("Identificação do candidato", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = viewModel.nome,
                onValueChange = {
                    viewModel.nome = it
                    viewModel.limparErros()
                },
                label = { Text("Nome") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = dataNascimento,
                    onValueChange = { dataNascimento = it },
                    label = { Text("Data de nascimento") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = viewModel.cc,
                    onValueChange = {
                        viewModel.cc = it
                        viewModel.limparErros()
                    },
                    label = { Text("CC/Passaporte") },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = viewModel.telemovel,
                    onValueChange = {
                        viewModel.telemovel = it
                        viewModel.limparErros()
                    },
                    label = { Text("Telemóvel") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = viewModel.email,
                    onValueChange = {
                        viewModel.email = it
                        viewModel.limparErros()
                    },
                    label = { Text("Email") },
                    modifier = Modifier.weight(1f)
                )
            }

            Text("Dados académicos", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GrauCheckbox(
                    label = "Licenciatura",
                    selected = viewModel.grauCurso == GrauCurso.LICENCIATURA,
                    onSelected = { viewModel.grauCurso = GrauCurso.LICENCIATURA }
                )

                Spacer(modifier = Modifier.width(16.dp))

                GrauCheckbox(
                    label = "Mestrado",
                    selected = viewModel.grauCurso == GrauCurso.MESTRADO,
                    onSelected = { viewModel.grauCurso = GrauCurso.MESTRADO }
                )

                Spacer(modifier = Modifier.width(16.dp))

                GrauCheckbox(
                    label = "CTeSP",
                    selected = viewModel.grauCurso == GrauCurso.CTESP,
                    onSelected = { viewModel.grauCurso = GrauCurso.CTESP }
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = viewModel.curso,
                    onValueChange = {
                        viewModel.curso = it
                        viewModel.limparErros()
                    },
                    label = { Text("Curso") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = viewModel.numEstudante,
                    onValueChange = {
                        viewModel.numEstudante = it
                        viewModel.limparErros()
                    },
                    label = { Text("N.º estudante") },
                    modifier = Modifier.weight(1f)
                )
            }

            Text("Tipologia do pedido", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                ApoioCheckbox("Produtos alimentares", viewModel.apoioAlimentacao) { viewModel.apoioAlimentacao = it }
                ApoioCheckbox("Produtos de higiene pessoal", viewModel.apoioHigiene) { viewModel.apoioHigiene = it }
                ApoioCheckbox("Produtos de limpeza", viewModel.apoioLimpeza) { viewModel.apoioLimpeza = it }
            }

            Text("Outros apoios", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

            ApoioCheckbox(
                label = "É apoiado(a) pelo Fundo de Apoio de Emergência Social (FAES)?",
                checked = viewModel.faes
            ) {
                viewModel.faes = it
                if (it) viewModel.limparDocs()
            }

            ApoioCheckbox(
                label = "É beneficiário(a) de alguma bolsa de estudo ou apoio?",
                checked = viewModel.bolsa
            ) { viewModel.bolsa = it }

            if (viewModel.bolsa) {
                OutlinedTextField(
                    value = viewModel.obsBolsa,
                    onValueChange = { viewModel.obsBolsa = it },
                    label = { Text("Se sim, indique a entidade e o valor") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ---------------------- UPLOAD + INFO ----------------------
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Upload de documentos",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = { showInfo = !showInfo }) {
                    Icon(Icons.Outlined.Info, contentDescription = "Info", tint = GreenDark)
                }
            }

            if (showInfo) {
                Surface(
                    color = Color.White,
                    shadowElevation = 2.dp,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = DOCUMENTOS_INFO.trim(),
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF444444)
                    )
                }
            }

            if (viewModel.faes) {
                Surface(
                    color = Color(0xFFE3F3ED),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "FAES ativo: dispensa entrega de documentos.",
                        modifier = Modifier.padding(12.dp),
                        color = GreenDark,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Button(
                onClick = { docsPickerLauncher.launch(arrayOf("*/*")) },
                enabled = !viewModel.faes,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = GreenDark,
                    disabledContainerColor = Color.Transparent,
                    disabledContentColor = Color.Gray
                )
            ) {
                Text(if (viewModel.faes) "Upload desativado (FAES)" else "Clique para selecionar documentos (vários)")
            }

            // Lista dos documentos escolhidos
            if (viewModel.documentos.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    viewModel.documentos.forEach { doc ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White,
                            shadowElevation = 1.dp
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth().padding(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Outlined.Description, contentDescription = "Documento", tint = Color.Gray)
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = doc.nome,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Text(
                                    text = "Remover",
                                    color = Color.Red,
                                    modifier = Modifier
                                        .clickable { viewModel.removerDocumento(doc.id_documento) }
                                        .padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Erros
            if (submitState is ResultState.Error) {
                Text(
                    text = submitState.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { viewModel.submeterCandidatura() },
                enabled = submitState !is ResultState.Loading,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GreenDark,
                    contentColor = Color.White
                )
            ) {
                if (submitState is ResultState.Loading) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp).padding(end = 8.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
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

// ---------------------- UI helpers ----------------------

@Composable
private fun ApoioCheckbox(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label)
    }
}

@Composable
private fun GrauCheckbox(
    label: String,
    selected: Boolean,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Checkbox(
            checked = selected,
            onCheckedChange = { onSelected() },
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label)
    }
}


private fun getFileName(context: Context, uri: Uri): String? {
    val cr = context.contentResolver
    val cursor = cr.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)
        ?: return null

    return cursor.use {
        if (!it.moveToFirst()) return null
        val index = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
        if (index >= 0) it.getString(index) else null
    }
}
