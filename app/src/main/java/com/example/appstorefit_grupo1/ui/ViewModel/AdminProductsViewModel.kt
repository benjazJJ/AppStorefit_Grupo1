package com.example.appstorefit_grupo1.ui.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appstorefit_grupo1.data.local.Productos.ProductosEntity
import com.example.appstorefit_grupo1.data.repository.ProductosRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Estado del formulario de crear/editar variante
data class ProductoFormState(
    // visibilidad y modo
    val showForm: Boolean = false,
    val modoEdicion: Boolean = false, // false = crear, true = editar

    // campos
    val idCategoria: String = "",
    val marca: String = "StoreFit",
    val modelo: String = "",
    val color: String = "",
    val talla: String = "",
    val precio: String = "",
    val stock: String = "",

    // errores
    val errorIdCategoria: String? = null,
    val errorMarca: String? = null,
    val errorModelo: String? = null,
    val errorColor: String? = null,
    val errorTalla: String? = null,
    val errorPrecio: String? = null,
    val errorStock: String? = null,

    // control
    val puedeGuardar: Boolean = false,
    val cargando: Boolean = false
)

class AdminProductsViewModel(
    private val repo: ProductosRepository
) : ViewModel() {

    init {
        viewModelScope.launch {
            repo.getAll().exceptionOrNull()?.message?.let {
                _error.value = it
            }
        }
    }

    // Lista reactiva de variantes
    val productos: StateFlow<List<ProductosEntity>> =
        repo.observeAll()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Error general para mostrar en UI
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Estado del formulario
    private val _form = MutableStateFlow(ProductoFormState())
    val form: StateFlow<ProductoFormState> = _form.asStateFlow()

    // Llaves de la variante en edición
    private var editingKeys: Pair<Long, Long>? = null // (idCategoria, idProducto)

    // Conjuntos permitidos (coinciden con el repositorio)
    private val coloresPermitidos = setOf(
        "Blanco con detalles negros",
        "Negro con detalles blancos"
    )
    private val tallasPermitidas = setOf("XS","S","M","L","XL")

    // ---------------- Acciones rápidas de stock ----------------

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

    // ---------------- Abrir / cerrar formulario ----------------

    fun abrirCrear() {
        editingKeys = null
        _form.value = ProductoFormState(
            showForm = true,
            modoEdicion = false,
            marca = "StoreFit"
        )
    }

    fun abrirEditarVariante(p: ProductosEntity) {
        editingKeys = p.idCategoria to p.idProducto
        _form.value = ProductoFormState(
            showForm = true,
            modoEdicion = true,
            idCategoria = p.idCategoria.toString(),
            marca = p.marca,
            modelo = p.modelo,
            color = p.color,
            talla = p.talla,
            precio = p.precio.toString(),
            stock = p.stock.toString(),
            puedeGuardar = true
        )
    }

    fun cerrarFormulario() {
        editingKeys = null
        _form.value = ProductoFormState()
    }

    // ---------------- onChange de campos ----------------

    fun onIdCategoria(s: String) { _form.value = _form.value.copy(idCategoria = s).validado() }
    fun onMarca(s: String)       { _form.value = _form.value.copy(marca = s).validado() }
    fun onModelo(s: String)      { _form.value = _form.value.copy(modelo = s).validado() }
    fun onColor(s: String)       { _form.value = _form.value.copy(color = s).validado() }
    fun onTalla(s: String)       { _form.value = _form.value.copy(talla = s).validado() }
    fun onPrecio(s: String)      { _form.value = _form.value.copy(precio = s).validado() }
    fun onStock(s: String)       { _form.value = _form.value.copy(stock = s).validado() }

    // ---------------- Guardar / Eliminar ----------------

    fun confirmarGuardar() {
        val f = _form.value.validado()
        _form.value = f
        if (!f.puedeGuardar || f.cargando) return

        viewModelScope.launch {
            _form.value = _form.value.copy(cargando = true)

            val res = if (f.modoEdicion) {
                // Update de variante
                val (catId, prodId) = editingKeys ?: (0L to 0L)
                if (catId == 0L) {
                    Result.failure(IllegalStateException("Edición inválida"))
                } else {
                    val precioInt = f.precio.toInt()
                    val stockInt  = f.stock.toInt()
                    val entidad = ProductosEntity(
                        idCategoria = catId,
                        idProducto  = prodId,
                        marca       = f.marca.trim(),
                        modelo      = f.modelo.trim(),
                        color       = f.color,
                        talla       = f.talla,
                        precio      = precioInt,
                        stock       = stockInt
                    )
                    repo.update(entidad)
                }
            } else {
                // Creación de variante (repo calcula idProducto)
                val catId    = f.idCategoria.toLong()
                val precioInt = f.precio.toInt()
                val stockInt  = f.stock.toInt()
                repo.create(
                    idCategoria = catId,
                    modelo      = f.modelo.trim(),
                    color       = f.color,
                    talla       = f.talla,
                    precio      = precioInt,
                    stock       = stockInt,
                    marca       = f.marca.trim()
                ).map { Unit }
            }

            res.fold(
                onSuccess = {
                    _error.value = null
                    cerrarFormulario()
                },
                onFailure = { e ->
                    _error.value = e.message
                    _form.value = _form.value.copy(cargando = false)
                }
            )
        }
    }

    fun confirmarEliminar() {
        val keys = editingKeys ?: return
        if (!_form.value.modoEdicion) return

        viewModelScope.launch {
            _form.value = _form.value.copy(cargando = true)
            val res = repo.delete(keys.first, keys.second)
            res.fold(
                onSuccess = {
                    _error.value = null
                    cerrarFormulario()
                },
                onFailure = { e ->
                    _error.value = e.message
                    _form.value = _form.value.copy(cargando = false)
                }
            )
        }
    }

    // ---------------- Validación local del formulario ----------------

    private fun ProductoFormState.validado(): ProductoFormState {
        var eIdCat: String? = null
        var eMarca: String? = null
        var eModelo: String? = null
        var eColor: String? = null
        var eTalla: String? = null
        var ePrecio: String? = null
        var eStock: String? = null

        // idCategoria solo se ingresa cuando crear (en editar ya viene fijo como texto informativo)
        if (!modoEdicion) {
            val idOk = idCategoria.toLongOrNull()
            if (idOk == null || idOk <= 0L) eIdCat = "Ingresa un ID de categoría válido"
        }

        if (marca.isBlank())  eMarca = "Marca requerida"
        if (modelo.isBlank()) eModelo = "Modelo requerido"

        if (color.isBlank() || color !in coloresPermitidos) {
            eColor = "Color inválido"
        }
        if (talla.isBlank() || talla !in tallasPermitidas) {
            eTalla = "Talla inválida"
        }

        val pInt = precio.toIntOrNull()
        if (pInt == null || pInt < 0) ePrecio = "Precio inválido"

        val sInt = stock.toIntOrNull()
        if (sInt == null || sInt < 0) eStock = "Stock inválido"

        val ok = listOf(eIdCat, eMarca, eModelo, eColor, eTalla, ePrecio, eStock).all { it == null }

        return copy(
            errorIdCategoria = eIdCat,
            errorMarca = eMarca,
            errorModelo = eModelo,
            errorColor = eColor,
            errorTalla = eTalla,
            errorPrecio = ePrecio,
            errorStock = eStock,
            puedeGuardar = ok
        )
    }
}
