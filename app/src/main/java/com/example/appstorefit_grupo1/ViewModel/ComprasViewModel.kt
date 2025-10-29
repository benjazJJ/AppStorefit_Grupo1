package com.example.appstorefit_grupo1.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appstorefit_grupo1.data.local.Compras.CompraConDetalles
import com.example.appstorefit_grupo1.data.repository.CompraRepository
import com.example.appstorefit_grupo1.data.repository.ItemCarritoSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ComprasViewModel(
    private val repo: CompraRepository
) : ViewModel() {

    // Estado: lista de compras con sus detalles (para la pantalla Historial)
    private val _historial = MutableStateFlow<List<CompraConDetalles>>(emptyList())
    val historial: StateFlow<List<CompraConDetalles>> = _historial

    // Estado: total gastado por el usuario (para mostrar arriba)
    private val _totalGastado = MutableStateFlow(0)
    val totalGastado: StateFlow<Int> = _totalGastado

    fun cargar(rut: String) {
        viewModelScope.launch {
            _historial.value = repo.obtenerHistorial(rut)
            _totalGastado.value = repo.totalGastado(rut)
        }
    }
    fun registrarCompra(
        rut: String,
        items: List<ItemCarritoSnapshot>,
        onOk: (Long) -> Unit = {}
    ) {
        viewModelScope.launch {
            val id = repo.registrarCompra(
                rutUsuario = rut,
                fechaMillis = System.currentTimeMillis(),
                items = items
            )
            cargar(rut)
            onOk(id)
        }
    }
}