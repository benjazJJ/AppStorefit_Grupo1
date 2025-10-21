package com.example.appstorefit_grupo1.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.appstorefit_grupo1.data.local.Carrito.CarritoDao
import com.example.appstorefit_grupo1.data.local.Carrito.CarritoEntity
import com.example.appstorefit_grupo1.data.repository.CarritoRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Estado que la UI del carrito va a observar
data class CarritoUiState(
    val items: List<CarritoEntity> = emptyList(),
    val cantidadTotal: Int = 0,
    val totalCLP: Int = 0
)

class CarritoViewModel(
    private val repo: CarritoRepository
) : ViewModel() {

    // Flujos del repositorio
    private val itemsFlow = repo.observarItems()
    private val cantFlow  = repo.observarCantidadTotal()
    private val totalFlow = repo.observarTotalCLP()

    // Estado combinado listo para la UI
    val uiState: StateFlow<CarritoUiState> =
        combine(itemsFlow, cantFlow, totalFlow) { items, cant, total ->
            CarritoUiState(items = items, cantidadTotal = cant, totalCLP = total)
        }.stateIn(viewModelScope, SharingStarted.Lazily, CarritoUiState())

    // Acciones
    fun agregar(
        idCat: Long,
        idProd: Long,
        modelo: String,
        color: String,
        talla: String,
        precioUnit: Int
    ) = viewModelScope.launch {
        repo.agregar(idCat, idProd, modelo, color, talla, precioUnit)
    }

    fun disminuir(idCat: Long, idProd: Long, color: String, talla: String) =
        viewModelScope.launch { repo.disminuir(idCat, idProd, color, talla) }

    fun eliminar(idCat: Long, idProd: Long, color: String, talla: String) =
        viewModelScope.launch { repo.eliminar(idCat, idProd, color, talla) }

    fun limpiar() = viewModelScope.launch { repo.limpiar() }
}

/** Factory simple (si no usas Hilt) */
class CarritoViewModelFactory(
    dao: CarritoDao
) : ViewModelProvider.Factory {
    private val repo = CarritoRepository(dao)
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        CarritoViewModel(repo) as T
}