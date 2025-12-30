@file:OptIn(ExperimentalMaterial3Api::class)

package com.ipca.sassolidaria.ui.colaborador.doacoes
import androidx.compose.foundation.shape.RoundedCornerShape
import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ipca.sassolidaria.core.Routes
import com.ipca.sassolidaria.data.model.ItemDoacao
import com.ipca.sassolidaria.data.model.enums.TipoProduto
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*



val Verde = Color(0xFF0B5D47)
val Vermelho = Color(0xFFE53935)


@Composable
fun BotaoAdicionarProduto(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(55.dp),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(2.dp, Verde),
        contentPadding = PaddingValues(vertical = 16.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Verde)
    ) {
        Icon(Icons.Default.Add, null, tint = Verde)
        Spacer(Modifier.width(10.dp))
        Text("Adicionar Produto")
    }
}


@Composable
fun BotaoRegistarDoacao(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(55.dp),
        shape = RectangleShape,
        contentPadding = PaddingValues(vertical = 16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Verde,
            contentColor = Color.White
        )
    ) {
        Text("Registar Doação")
    }
}


@Composable
fun IconEditar(onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.size(42.dp)) {
        Icon(Icons.Default.Edit, null, tint = Verde)
    }
}

@Composable
fun IconRemover(onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.size(42.dp)) {
        Icon(Icons.Default.Delete, null, tint = Vermelho)
    }
}


@Composable
fun DateField(label: String, date: Date?, onDateSelected: (Date) -> Unit) {

    val context = LocalContext.current
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val display = date?.let { sdf.format(it) } ?: ""

    Box(
        modifier = Modifier.fillMaxWidth().clickable {
            val cal = Calendar.getInstance()
            if (date != null) cal.time = date

            DatePickerDialog(
                context,
                { _, y, m, d ->
                    val c = Calendar.getInstance()
                    c.set(y, m, d)
                    onDateSelected(c.time)
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    ) {
        OutlinedTextField(
            value = display,
            onValueChange = {},
            label = { Text(label) },
            enabled = false,
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                disabledTextColor = Color.Black,
                disabledContainerColor = Color.Transparent,
                disabledIndicatorColor = Color.Gray,
                disabledLabelColor = Color.Gray
            )
        )
    }
}


@Composable
fun PrettyConfirmDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.CheckCircle, null, tint = Verde) },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, modifier = Modifier.weight(1f))
                IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null) }
            }
        },
        text = { Text(message) },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Sim") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Não") } }
    )
}


@Composable
fun PrettyMessageDialog(
    title: String,
    message: String,
    isError: Boolean = false,
    autoClose: Boolean = false,
    onClose: () -> Unit
) {
    val scope = rememberCoroutineScope()

    LaunchedEffect(autoClose) {
        if (autoClose) {
            scope.launch {
                delay(2000)
                onClose()
            }
        }
    }

    AlertDialog(
        onDismissRequest = onClose,
        icon = {
            Icon(
                if (isError) Icons.Default.Error else Icons.Default.CheckCircle,
                null,
                tint = if (isError) Vermelho else Verde
            )
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, modifier = Modifier.weight(1f))
                IconButton(onClick = onClose) { Icon(Icons.Default.Close, null) }
            }
        },
        text = { Text(message) },
        confirmButton = {
            if (!autoClose)
                TextButton(onClick = onClose) { Text("OK") }
        }
    )
}


@Composable
fun ProdutoDoadoCard(
    item: ItemDoacao,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {

    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {

        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(Modifier.weight(1f)) {
                Text(item.nome, style = MaterialTheme.typography.titleMedium)
                Text("Quantidade: ${item.quantidade}")
                Text("Tipo: ${item.tipo_produto}")
                item.data_validade?.let { Text("Validade: ${sdf.format(it)}") }
            }

            IconEditar(onEdit)
            IconRemover(onDelete)
        }
    }
}


@Composable
fun AddItemDialog(
    itemOriginal: ItemDoacao? = null,
    onDismiss: () -> Unit,
    onConfirm: (ItemDoacao) -> Unit
) {

    var nome by remember { mutableStateOf(itemOriginal?.nome ?: "") }
    var quantidade by remember { mutableStateOf(itemOriginal?.quantidade?.toString() ?: "") }
    var tipo by remember { mutableStateOf(itemOriginal?.tipo_produto ?: TipoProduto.ALIMENTACAO) }
    var validade by remember { mutableStateOf<Date?>(itemOriginal?.data_validade) }
    var expanded by remember { mutableStateOf(false) }
    var erro by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,

        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (itemOriginal == null) "Adicionar Produto"
                    else "Editar Produto",
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null) }
            }
        },

        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                if (erro.isNotEmpty())
                    Text(erro, color = Vermelho)

                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = quantidade,
                    onValueChange = { quantidade = it },
                    label = { Text("Quantidade") },
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {

                    OutlinedTextField(
                        value = tipo.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo de Produto") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        TipoProduto.values().forEach { t ->
                            DropdownMenuItem(
                                text = { Text(t.name) },
                                onClick = {
                                    tipo = t
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                // DATA
                DateField("Validade", validade) { validade = it }
            }
        },

        confirmButton = {
            Button(
                onClick = {

                    val qtd = quantidade.toIntOrNull()

                    if (nome.isBlank() || qtd == null || qtd <= 0 || validade == null) {
                        erro = "Preencha todos os campos."
                        return@Button
                    }

                    onConfirm(
                        ItemDoacao(
                            id_item_doacao = itemOriginal?.id_item_doacao ?: "",
                            id_doacao = itemOriginal?.id_doacao ?: "",
                            nome = nome,
                            quantidade = qtd,
                            tipo_produto = tipo,
                            data_validade = validade
                        )
                    )

                    onDismiss()
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Verde,
                    contentColor = Color.White
                )
            ) {
                Text(if (itemOriginal == null) "Adicionar" else "Guardar")
            }
        },

        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}


@Composable
fun DoacaoCreateView(
    navController: NavController,
    vm: DoacaoCreateViewModel = viewModel()
) {

    val itens by vm.itens.collectAsState()

    var nome by remember { mutableStateOf("") }
    var telefone by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    var dataDoacao by remember { mutableStateOf(Date()) }

    var erroForm by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()   // PARA AUTO CLEAR DE ERRO

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog: ItemDoacao? by remember { mutableStateOf(null) }
    var itemToDelete: ItemDoacao? by remember { mutableStateOf(null) }

    var showConfirm by remember { mutableStateOf(false) }
    var success by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {

        // TOP BAR
        Row(
            Modifier.fillMaxWidth().height(56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, null)
            }
            Box(Modifier.weight(1f), Alignment.Center) {
                Text("Registar Doação", style = MaterialTheme.typography.titleLarge)
            }
            Spacer(Modifier.width(48.dp))
        }

        Divider()
        Spacer(Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {

            item {
                Text("Informações do Doador", style = MaterialTheme.typography.titleMedium)

                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome Completo") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = telefone,
                    onValueChange = { telefone = it },
                    label = { Text("Contacto") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))
                Text("Detalhes da Doação", style = MaterialTheme.typography.titleMedium)

                DateField("Data da Doação", dataDoacao) { dataDoacao = it }

                OutlinedTextField(
                    value = descricao,
                    onValueChange = { descricao = it },
                    label = { Text("Descrição") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp)
                )

                Spacer(Modifier.height(16.dp))
                Text("Produtos Doados", style = MaterialTheme.typography.titleMedium)
            }

            items(itens) { item ->
                ProdutoDoadoCard(
                    item,
                    onEdit = { showEditDialog = item },
                    onDelete = { itemToDelete = item }
                )
            }

            item {

                // BOTÃO ADICIONAR PRODUTO
                BotaoAdicionarProduto { showAddDialog = true }

                Spacer(Modifier.height(12.dp))

                if (erroForm.isNotEmpty())
                    Text(erroForm, color = Vermelho)

                // BOTÃO REGISTAR DOAÇÃO
                BotaoRegistarDoacao {

                    erroForm = ""   // limpar antes de validar

                    if (nome.isBlank() || telefone.isBlank() || itens.isEmpty()) {

                        erroForm = "Preencha todos os campos."

                        // AUTO-remove depois de 2 segundos
                        scope.launch {
                            delay(2000)
                            erroForm = ""
                        }

                        return@BotaoRegistarDoacao
                    }

                    showConfirm = true
                }
            }
        }
    }

    /* ----------------------- DIALOGS ----------------------- */

    if (showAddDialog)
        AddItemDialog(
            itemOriginal = null,
            onDismiss = { showAddDialog = false },
            onConfirm = { vm.addItem(it) }
        )

    showEditDialog?.let { item ->
        AddItemDialog(
            itemOriginal = item,
            onDismiss = { showEditDialog = null },
            onConfirm = { vm.updateItem(item, it) }
        )
    }

    itemToDelete?.let { item ->
        PrettyConfirmDialog(
            "Eliminar Produto",
            "Deseja eliminar este item?",
            onConfirm = {
                vm.removeItem(item)
                itemToDelete = null
            },
            onDismiss = { itemToDelete = null }
        )
    }

    if (showConfirm) {
        PrettyConfirmDialog(
            "Registar Doação",
            "Confirmar registo?",
            onConfirm = {
                showConfirm = false

                vm.criarDoacao(
                    nome,
                    telefone,
                    dataDoacao,
                    descricao,
                    onSuccess = { id ->
                        success = true
                        navController.navigate("${Routes.DOACAO_DETAIL}/$id")
                    },
                    onError = { error = true }
                )
            },
            onDismiss = { showConfirm = false }
        )
    }

    if (success)
        PrettyMessageDialog(
            "Sucesso",
            "Doação registada com sucesso!",
            autoClose = true
        ) { success = false }

    if (error)
        PrettyMessageDialog(
            "Erro",
            "Ocorreu um erro ao registar.",
            isError = true
        ) { error = false }
}

@Preview(showBackground = true)
@Composable
fun PreviewDoacaoCreateView() {
    DoacaoCreateView(navController = rememberNavController())
}
