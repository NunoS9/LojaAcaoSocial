package com.ipca.sassolidaria.ui.utilizador.candidaturas

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ipca.sassolidaria.data.model.enums.GrauCurso
import com.ipca.sassolidaria.ui.components.BottomBar
import com.ipca.sassolidaria.utils.ResultState

private val GreenDark = Color(0xFF004B3C)
private val ScreenBg = Color(0xFFF6F7FB)
private val DividerColor = Color(0xFFB0B0B0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CandidaturaCreateView(
    navController: NavController,
    viewModel: CandidaturaCreateViewModel = viewModel()
) {
    val submitState = viewModel.submitState

    var dataNascimento by remember { mutableStateOf("") }
    var selectedFileName by remember { mutableStateOf<String?>(null) }

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedFileName = uri?.lastPathSegment
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

            Text(
                text = "Identificação do candidato",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = viewModel.nome,
                onValueChange = {
                    viewModel.nome = it
                    viewModel.limparErros()
                },
                label = { Text("Nome") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
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

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
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

            Text(
                text = "Dados académicos",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

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

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
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

            Text(
                text = "Tipologia do pedido",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                ApoioCheckbox(
                    label = "Produtos alimentares",
                    checked = viewModel.apoioAlimentacao,
                    onCheckedChange = { viewModel.apoioAlimentacao = it }
                )
                ApoioCheckbox(
                    label = "Produtos de higiene pessoal",
                    checked = viewModel.apoioHigiene,
                    onCheckedChange = { viewModel.apoioHigiene = it }
                )
                ApoioCheckbox(
                    label = "Produtos de limpeza",
                    checked = viewModel.apoioLimpeza,
                    onCheckedChange = { viewModel.apoioLimpeza = it }
                )
            }

            Text(
                text = "Outros apoios",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            ApoioCheckbox(
                label = "É apoiado(a) pelo Fundo de Apoio de Emergência Social (FAES)?",
                checked = viewModel.faes,
                onCheckedChange = { viewModel.faes = it }
            )

            ApoioCheckbox(
                label = "É beneficiário(a) de alguma bolsa de estudo ou apoio?",
                checked = viewModel.bolsa,
                onCheckedChange = { viewModel.bolsa = it }
            )

            if (viewModel.bolsa) {
                OutlinedTextField(
                    value = viewModel.obsBolsa,
                    onValueChange = { viewModel.obsBolsa = it },
                    label = { Text("Se sim, indique a entidade e o valor") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Text(
                text = "Upload de documentos",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = { documentPickerLauncher.launch("*/*") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = GreenDark
                )
            ) {
                Text("Arraste e solte ou clique para fazer upload")
            }

            if (selectedFileName != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Description,
                            contentDescription = "Documento",
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = selectedFileName ?: "",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = "Remover",
                        color = Color.Red,
                        modifier = Modifier
                            .clickable { selectedFileName = null }
                            .padding(start = 8.dp)
                    )
                }
            }

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
                            modifier = Modifier
                                .size(18.dp)
                                .padding(end = 8.dp),
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

@Composable
private fun ApoioCheckbox(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
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
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Checkbox(
            checked = selected,
            onCheckedChange = { onSelected() },
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label)
    }
}
