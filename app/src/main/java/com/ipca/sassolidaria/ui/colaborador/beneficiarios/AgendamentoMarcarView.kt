package com.ipca.sassolidaria.ui.colaborador.beneficiarios

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ipca.sassolidaria.data.model.PreferenciaAgendamento
import com.ipca.sassolidaria.data.model.enums.Tipo
import java.util.Calendar

private val GreenSas = Color(0xFF004B3C)
private val ScreenBg = Color(0xFFF6F7FB)
private val DividerColor = Color(0xFF858585)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendamentoMarcarView(
    navController: NavController,
    idBeneficiario: String,
    tipoStr: String,
    ref: String,
    vm: AgendamentoMarcarViewModel = viewModel()
) {
    val state = vm.uiState
    val tipo = remember(tipoStr) { Tipo.valueOf(tipoStr) }

    LaunchedEffect(idBeneficiario) { vm.carregarPreferencias(idBeneficiario) }

    val datePickerState = rememberDatePickerState()
    var hora by remember { mutableStateOf("09:00") }

    Scaffold(
        containerColor = ScreenBg,
        topBar = {
            Column(modifier = Modifier.background(ScreenBg)) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Agendamento",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = ScreenBg
                    )
                )
                HorizontalDivider(color = DividerColor, thickness = 2.dp)
            }
        },
        bottomBar = {
            Button(
                onClick = {
                    val millis = datePickerState.selectedDateMillis ?: return@Button

                    val parts = hora.split(":")
                    val hh = parts.getOrNull(0)?.toIntOrNull() ?: 9
                    val mm = parts.getOrNull(1)?.toIntOrNull() ?: 0

                    val cal = Calendar.getInstance().apply {
                        timeInMillis = millis
                        set(Calendar.HOUR_OF_DAY, hh)
                        set(Calendar.MINUTE, mm)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }

                    vm.confirmar(
                        idBeneficiario = idBeneficiario,
                        tipo = tipo,
                        ref = ref,
                        data = cal.time
                    ) { navController.popBackStack() }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(48.dp),
                enabled = !state.isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = GreenSas),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp),
                        color = Color.White
                    )
                } else {
                    Text("Marcar Agendamento", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
                .background(Color(0xFFF6F7FB))
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Agendamento preferencial", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))

                    val p: PreferenciaAgendamento? = state.preferencia
                    if (p == null) {
                        Text("Sem preferências registadas", color = Color.Gray)
                    } else {
                        Text("Dia: ${diaSemanaExtenso(p.dia_semana)}", color = Color.Gray)
                        Text("Hora: ${p.hora_inicio} até ${p.hora_fim}", color = Color.Gray)
                        if (p.observacoes.isNotBlank()) Text("Obs: ${p.observacoes}", color = Color.Gray)
                    }
                }
            }

            Text("Marcar", fontWeight = FontWeight.SemiBold)

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(Modifier.padding(12.dp)) {
                    DatePicker(state = datePickerState)
                }
            }

            OutlinedTextField(
                value = hora,
                onValueChange = { hora = it },
                label = { Text("Hora (HH:mm)") },
                placeholder = { Text("09:00") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = GreenSas
                )
            )

            state.error?.let { Text(it, color = Color.Red) }

            Spacer(Modifier.height(70.dp))
        }
    }
}

private fun diaSemanaExtenso(dia: Int): String {
    return when (dia) {
        1 -> "Domingo"
        2 -> "Segunda-feira"
        3 -> "Terça-feira"
        4 -> "Quarta-feira"
        5 -> "Quinta-feira"
        6 -> "Sexta-feira"
        7 -> "Sábado"
        else -> "Desconhecido ($dia)"
    }
}