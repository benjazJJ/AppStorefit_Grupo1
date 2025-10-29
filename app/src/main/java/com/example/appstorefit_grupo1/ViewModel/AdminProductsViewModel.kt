package com.example.appstorefit_grupo1.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appstorefit_grupo1.data.local.Productos.ProductosEntity
import com.example.appstorefit_grupo1.data.repository.ProductosRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AdminProductsViewModel(
    private val repo: ProductosRepository
) : ViewModel() {

    // Lista reactiva de variantes (una fila = color+talla de un modelo)
    val productos: StateFlow<List<ProductosEntity>> =
        repo.observeAll()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Estado de error simple para mostrar en UI si algo sale mal
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun setStock(idCategoria: Long, idProducto: Long, nuevoStock: Int) {
        viewModelScope.launch {
            val res = repo.setStock(idCategoria, idProducto, nuevoStock)
            _error.value = res.exceptionOrNull()?.message
        }
    }

    fun addToStock(idCategoria: Long, idProducto: Long, delta: Int) {
        viewModelScope.launch {
            val res = repo.addToStock(idCategoria, idProducto, delta)
            _error.value = res.exceptionOrNull()?.message
        }
    }
}


