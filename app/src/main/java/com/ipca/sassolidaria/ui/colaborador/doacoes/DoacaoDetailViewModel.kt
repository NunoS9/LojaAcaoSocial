package com.ipca.sassolidaria.ui.colaborador.doacoes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipca.sassolidaria.data.model.Doacao
import com.ipca.sassolidaria.data.model.Doador
import com.ipca.sassolidaria.data.model.ItemDoacao
import com.ipca.sassolidaria.data.repository.DoacoesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DoacaoDetailViewModel : ViewModel() {

    private val repo = DoacoesRepository()

    private val _doacao = MutableStateFlow<Doacao?>(null)
    val doacao: StateFlow<Doacao?> = _doacao

    private val _doador = MutableStateFlow<Doador?>(null)
    val doador: StateFlow<Doador?> = _doador

    private val _itens = MutableStateFlow<List<ItemDoacao>>(emptyList())
    val itens: StateFlow<List<ItemDoacao>> = _itens

    fun loadDoacao(id: String) {
        viewModelScope.launch {

            val doa = repo.getDoacao(id)
            _doacao.value = doa

            if (doa != null) {
                _doador.value = repo.getDoador(doa.id_doador)
                _itens.value = repo.getItensDaDoacao(doa.id_doacao)
            }
        }
    }
}
