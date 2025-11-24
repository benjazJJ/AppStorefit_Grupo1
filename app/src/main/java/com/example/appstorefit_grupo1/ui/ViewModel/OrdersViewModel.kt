package com.example.appstorefit_grupo1.ui.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appstorefit_grupo1.data.local.Compras.CompraConDetalles
import com.example.appstorefit_grupo1.data.repository.ItemCarritoSnapshot
import com.example.appstorefit_grupo1.data.repository.OrdersRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ===== UI STATES =====

data class CrearCompraUiState(
    val enviando: Boolean = false,
    val compraCreada: CompraConDetalles? = null,
    val error: String? = null
)

data class HistorialComprasUiState(
    val cargando: Boolean = false,
    val compras: List<CompraConDetalles> = emptyList(),
    val error: String? = null
)

data class TotalGastadoUiState(
    val cargando: Boolean = false,
    val total: Int? = null,
    val error: String? = null
)

data class AdminComprasUiState(
    val cargando: Boolean = false,
    val compras: List<CompraConDetalles> = emptyList(),
    val error: String? = null
)

/**
 * ViewModel para consumir orders-service a través de OrdersRepository.
 *
 * Casos de uso:
 *  - Cliente:
 *      - crearCompra(itemsCarrito)
 *      - cargarHistorialCliente()
 *      - cargarTotalGastado()
 *  - Admin:
 *      - adminListarTodas()
 *      - cargarHistorialPorRut(rut)
 *      - cargarTotalPorRut(rut)
 */
class OrdersViewModel(
    private val repo: OrdersRepository
) : ViewModel() {

    // ===== State: crear compra =====
    private val _crearCompra = MutableStateFlow(CrearCompraUiState())
    val crearCompra: StateFlow<CrearCompraUiState> = _crearCompra.asStateFlow()

    // ===== State: historial (cliente / admin) =====
    private val _historial = MutableStateFlow(HistorialComprasUiState())
    val historial: StateFlow<HistorialComprasUiState> = _historial.asStateFlow()

    // ===== State: total gastado =====
    private val _totalGastado = MutableStateFlow(TotalGastadoUiState())
    val totalGastado: StateFlow<TotalGastadoUiState> = _totalGastado.asStateFlow()

    // ===== State: admin - todas las compras =====
    private val _adminCompras = MutableStateFlow(AdminComprasUiState())
    val adminCompras: StateFlow<AdminComprasUiState> = _adminCompras.asStateFlow()

    // =========================================================
    // CLIENTE → Crear compra desde el carrito
    // =========================================================
    fun crearCompraDesdeCarrito(items: List<ItemCarritoSnapshot>) {
        viewModelScope.launch {
            if (items.isEmpty()) {
                _crearCompra.value = CrearCompraUiState(
                    enviando = false,
                    compraCreada = null,
                    error = "El carrito está vacío."
                )
                return@launch
            }

            _crearCompra.value = CrearCompraUiState(
                enviando = true,
                compraCreada = null,
                error = null
            )

            val result = repo.crearCompra(items)

            _crearCompra.value = result.fold(
                onSuccess = { compra ->
                    CrearCompraUiState(
                        enviando = false,
                        compraCreada = compra,
                        error = null
                    )
                },
                onFailure = { e ->
                    CrearCompraUiState(
                        enviando = false,
                        compraCreada = null,
                        error = e.message ?: "No se pudo crear la compra."
                    )
                }
            )
        }
    }

    // =========================================================
    // CLIENTE → Historial de compras del usuario actual
    // =========================================================
    fun cargarHistorialCliente() {
        viewModelScope.launch {
            _historial.value = _historial.value.copy(
                cargando = true,
                error = null
            )

            val result = repo.historialCliente()

            _historial.value = result.fold(
                onSuccess = { lista ->
                    HistorialComprasUiState(
                        cargando = false,
                        compras = lista,
                        error = null
                    )
                },
                onFailure = { e ->
                    HistorialComprasUiState(
                        cargando = false,
                        compras = emptyList(),
                        error = e.message ?: "No se pudo cargar el historial."
                    )
                }
            )
        }
    }

    // =========================================================
    // CLIENTE / ADMIN → Historial por RUT específico
    // =========================================================
    fun cargarHistorialPorRut(rut: String) {
        viewModelScope.launch {
            _historial.value = _historial.value.copy(
                cargando = true,
                error = null
            )

            val result = repo.historialCliente(rut)

            _historial.value = result.fold(
                onSuccess = { lista ->
                    HistorialComprasUiState(
                        cargando = false,
                        compras = lista,
                        error = null
                    )
                },
                onFailure = { e ->
                    HistorialComprasUiState(
                        cargando = false,
                        compras = emptyList(),
                        error = e.message ?: "No se pudo cargar el historial del RUT."
                    )
                }
            )
        }
    }

    // =========================================================
    // CLIENTE / ADMIN → Total gastado (usuario actual o por RUT)
    // =========================================================
    fun cargarTotalGastado(rut: String? = null) {
        viewModelScope.launch {
            _totalGastado.value = _totalGastado.value.copy(
                cargando = true,
                error = null
            )

            val result = repo.totalGastado(rut)

            _totalGastado.value = result.fold(
                onSuccess = { total ->
                    TotalGastadoUiState(
                        cargando = false,
                        total = total,
                        error = null
                    )
                },
                onFailure = { e ->
                    TotalGastadoUiState(
                        cargando = false,
                        total = null,
                        error = e.message ?: "No se pudo cargar el total gastado."
                    )
                }
            )
        }
    }

    // =========================================================
    // ADMIN → Listar todas las compras
    // =========================================================
    fun adminListarTodas() {
        viewModelScope.launch {
            _adminCompras.value = _adminCompras.value.copy(
                cargando = true,
                error = null
            )

            val result = repo.adminListarCompras()

            _adminCompras.value = result.fold(
                onSuccess = { lista ->
                    AdminComprasUiState(
                        cargando = false,
                        compras = lista,
                        error = null
                    )
                },
                onFailure = { e ->
                    AdminComprasUiState(
                        cargando = false,
                        compras = emptyList(),
                        error = e.message ?: "No se pudo cargar la lista de compras."
                    )
                }
            )
        }
    }

    // =========================================================
    // Helpers para limpiar estados (útiles en la UI)
    // =========================================================
    fun resetCrearCompraState() {
        _crearCompra.value = CrearCompraUiState()
    }

    fun resetHistorialState() {
        _historial.value = HistorialComprasUiState()
    }

    fun resetTotalState() {
        _totalGastado.value = TotalGastadoUiState()
    }

    fun resetAdminState() {
        _adminCompras.value = AdminComprasUiState()
    }
}