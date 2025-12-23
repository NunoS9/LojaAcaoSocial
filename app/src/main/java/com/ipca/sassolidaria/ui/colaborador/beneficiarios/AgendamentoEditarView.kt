package com.ipca.sassolidaria.ui.colaborador.beneficiarios

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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
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
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendamentoEditarView(
    navController: NavController,
    idBeneficiario: String,
    idAgendamento: String,
    vm: AgendamentoEditarViewModel = viewModel()
) {
    val state = vm.uiState

    LaunchedEffect(idBeneficiario, idAgendamento) {
        vm.carregar(idBeneficiario, idAgendamento)
    }

    val datePickerState = rememberDatePickerState()
    var hora by remember { mutableStateOf("09:00") }

    // sincronizar hora quando carrega
    LaunchedEffect(state.agendamento?.data_agendada) {
        val d = state.agendamento?.data_agendada ?: return@LaunchedEffect
        val cal = Calendar.getInstance().apply { time = d }
        hora = String.format("%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Editar Agendamento") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        bottomBar = {
            // ✅ BOTÃO FIXO
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

                    vm.confirmarEdicao(idAgendamento, cal.time) {
                        navController.popBackStack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(48.dp),
                enabled = !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Text("Guardar Alterações")
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

            // Preferências
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Agendamento preferencial", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))

                    val p = state.preferencia
                    if (p == null) {
                        Text("Sem preferências registadas", color = Color.Gray)
                    } else {
                        Text("Dia: ${p.dia_semana} (1=Dom..7=Sáb)", color = Color.Gray)
                        Text("Hora: ${p.hora_inicio} até ${p.hora_fim}", color = Color.Gray)
                        if (p.observacoes.isNotBlank()) {
                            Text("Obs: ${p.observacoes}", color = Color.Gray)
                        }
                    }
                }
            }

            Text("Marcar", fontWeight = FontWeight.SemiBold)

            // ✅ DatePicker SEM height fixa
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
                modifier = Modifier.fillMaxWidth()
            )

            state.error?.let {
                Text(it, color = Color.Red)
            }

            // espaço para o bottom bar
            Spacer(Modifier.height(70.dp))
        }
    }
}
