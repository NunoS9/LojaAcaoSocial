package com.ipca.sassolidaria.ui.colaborador.doacoes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipca.sassolidaria.data.model.Doacao
import com.ipca.sassolidaria.data.model.ItemDoacao
import com.ipca.sassolidaria.data.repository.DoacoesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DoacoesListViewModel : ViewModel() {

    private val repo = DoacoesRepository()

    private val _doacoes = MutableStateFlow<List<Doacao>>(emptyList())
    val doacoes: StateFlow<List<Doacao>> = _doacoes

    private val _itensPorDoacao = MutableStateFlow<Map<String, List<ItemDoacao>>>(emptyMap())
    val itensPorDoacao: StateFlow<Map<String, List<ItemDoacao>>> = _itensPorDoacao

    fun loadDoacoes() {
        viewModelScope.launch {

            val lista = repo.getAllDoacoes()
            _doacoes.value = lista

            val mapa = mutableMapOf<String, List<ItemDoacao>>()

            lista.forEach { d ->
                val itens = repo.getItensDaDoacao(d.id_doacao)
                mapa[d.id_doacao] = itens
            }

            _itensPorDoacao.value = mapa
        }
    }
}
