package com.example.appstorefit_grupo1.ViewModel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appstorefit_grupo1.data.local.Categoria.CategoriaResumen
import com.example.appstorefit_grupo1.data.repository.CategoriaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AdminCategoriasViewModel(
    private val repo: CategoriaRepository
) : ViewModel() {

    // Flujo en vivo con el resumen de categorías (id, nombre, #productos, #modelos)
    val resumen: StateFlow<List<CategoriaResumen>> =
        repo.observeResumen()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Donde guardamos el último error para mostrarlo en pantalla
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Renombrar por id (usado por el diálogo)
    fun renombrar(id: Long, nuevo: String) {
        viewModelScope.launch {
            val res = repo.renombrar(id, nuevo)
            _error.value = res.exceptionOrNull()?.message
        }
    }
}
