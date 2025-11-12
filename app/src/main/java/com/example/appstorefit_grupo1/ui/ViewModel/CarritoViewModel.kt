package com.example.appstorefit_grupo1.ui.ViewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.appstorefit_grupo1.data.local.Carrito.CarritoDao
import com.example.appstorefit_grupo1.data.local.Carrito.CarritoEntity
import com.example.appstorefit_grupo1.data.repository.CarritoRepository
import com.example.appstorefit_grupo1.data.local.database.AppDatabase
import com.example.appstorefit_grupo1.data.repository.CheckoutRepository
import com.example.appstorefit_grupo1.data.repository.CheckoutResult
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// Estado que consumirá la UI del carrito.
// Se combinan: lista de items, cantidad total y total en CLP.
data class CarritoUiState(
    val items: List<CarritoEntity> = emptyList(),
    val cantidadTotal: Int = 0,
    val totalCLP: Int = 0
)

// ViewModel del carrito.
// Orquesta los flujos del repositorio del carrito y expone una acción de compra
// que delega en el CheckoutRepository para procesar el descuento de stock y limpieza del carrito.
class CarritoViewModel(
    private val repo: CarritoRepository,
    private val checkoutRepo: CheckoutRepository
) : ViewModel() {

    // Flujos reactivos del repositorio del carrito.
    // Estos flujos reflejan cambios en la base de datos sin que la UI tenga que volver a consultar manualmente.
    private val itemsFlow = repo.observarItems()
    private val cantFlow  = repo.observarCantidadTotal()
    private val totalFlow = repo.observarTotalCLP()

    // Estado combinado para que la UI tenga todos los datos necesarios en un único StateFlow.
    // SharingStarted.WhileSubscribed evita trabajo innecesario cuando la pantalla no está visible.
    val uiState: StateFlow<CarritoUiState> =
        combine(itemsFlow, cantFlow, totalFlow) { items, cant, total ->
            CarritoUiState(items = items, cantidadTotal = cant, totalCLP = total)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CarritoUiState()
        )

    // Canal para eventos de una sola vez (mensajes a mostrar en Snackbar/Toast).
    // Se usa en lugar de StateFlow para evitar reconsumos al rotar la pantalla.
    private val _eventos = Channel<String>(Channel.BUFFERED)
    val eventos = _eventos.receiveAsFlow()

    // Acciones básicas del carrito: agregar, disminuir, eliminar y limpiar.
    // Estas delegan en el CarritoRepository, que a su vez llama al DAO correspondiente.

    fun agregar(
        idCat: Long,
        idProd: Long,
        modelo: String,
        color: String,
        talla: String,
        precioUnit: Int
    ) = viewModelScope.launch {
        val r = repo.agregar(idCat, idProd, modelo, color, talla, precioUnit)
        if (r.isFailure) {
            _eventos.send(r.exceptionOrNull()?.message ?: "Sin stock suficiente")
        }else{
            _eventos.send("Agregado al carrito")
        }
    }

    fun disminuir(idCat: Long, idProd: Long, color: String, talla: String) =
        viewModelScope.launch { repo.disminuir(idCat, idProd, color, talla) }

    fun eliminar(idCat: Long, idProd: Long, color: String, talla: String) =
        viewModelScope.launch { repo.eliminar(idCat, idProd, color, talla) }

    fun limpiar() = viewModelScope.launch { repo.limpiar() }

    // Acción de compra.
    // Llama a CheckoutRepository.confirmarCompra(), que realiza una transacción en Room:
    // 1) Valida stock por cada item del carrito.
    // 2) Descuenta stock por cada item (si alguno falla, revierte tod0).
    // 3) Limpia el carrito si todo0 fue exitoso.
    // Emite un mensaje acorde al resultado para que la UI lo muestre.
    fun onComprar() = viewModelScope.launch {
        when (val r = checkoutRepo.confirmarCompra()) {
            is CheckoutResult.Ok       -> _eventos.send("Compra confirmada. Gracias por tu compra.")
            is CheckoutResult.SinStock -> _eventos.send(r.msg)
            is CheckoutResult.Error    -> _eventos.send("Ocurrió un error al procesar la compra. Intenta nuevamente.")
        }
    }
}

// Factory que construye el ViewModel sin usar Hilt.
// Crea el AppDatabase, los DAOs y los repositorios necesarios.
// Permite inyectar un CarritoDao ya creado si fuera necesario.
class CarritoViewModelFactory(
    private val appContext: Context,
    private val carritoDao: CarritoDao? = null
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val db = AppDatabase.getInstance(appContext)
        val dao = carritoDao ?: db.carritoDao()

        val carritoRepo = CarritoRepository(
            carritoDao = dao,
            productosDao = db.productosDao()
        )
        val checkoutRepo = CheckoutRepository(
            db = db,
            productosDao = db.productosDao(),
            carritoDao = db.carritoDao()
        )
        return CarritoViewModel(carritoRepo, checkoutRepo) as T
    }
}