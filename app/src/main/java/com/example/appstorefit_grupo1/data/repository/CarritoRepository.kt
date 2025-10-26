package com.example.appstorefit_grupo1.data.repository

import com.example.appstorefit_grupo1.data.local.Carrito.CarritoDao
import com.example.appstorefit_grupo1.data.local.Carrito.CarritoEntity
import com.example.appstorefit_grupo1.data.local.Productos.ProductosDao
import kotlinx.coroutines.flow.Flow

class CarritoRepository(
    private val carritoDao: CarritoDao,
    private val productosDao: ProductosDao
) {
    // Observables para la UI
    fun observarItems(): Flow<List<CarritoEntity>> = carritoDao.observarCarrito()
    fun observarCantidadTotal(): Flow<Int> = carritoDao.observarUnidades()
    fun observarTotalCLP(): Flow<Int> = carritoDao.observarTotalCLP()

    // Consultas puntuales
    suspend fun getAll() = Result.success(carritoDao.getAll())
    suspend fun countUnidades() = Result.success(carritoDao.countUnidades())
    suspend fun totalCLP() = Result.success(carritoDao.totalCLP())

    //  Agrega 1 unidad de la variante indicada, validando que no supere el stock disponible.
    //  Devuelve Result.success(Unit) si agregó; Result.failure con mensaje si no.

    suspend fun agregar(
        idCategoria: Long,
        idProducto: Long,
        modelo: String,
        color: String,
        talla: String,
        precioUnitario: Int
    ): Result<Unit> {
        if (modelo.isBlank()) return Result.failure(IllegalArgumentException("Modelo requerido"))
        if (color !in COLORES_PERMITIDOS) return Result.failure(IllegalArgumentException("Color inválido"))
        if (talla !in TALLAS_PERMITIDAS) return Result.failure(IllegalArgumentException("Talla inválida"))
        if (precioUnitario < 0) return Result.failure(IllegalArgumentException("Precio inválido"))

        // Cantidad actual en carrito para esta variante
        val existente = carritoDao.findByProductoYVariante(idCategoria, idProducto, color, talla)
        val cantidadActual = existente?.cantidad ?: 0

        // Stock real del producto
        val producto = productosDao.getByIds(idCategoria, idProducto)
            ?: return Result.failure(IllegalStateException("Producto no encontrado"))
        val stock = producto.stock

        // No permitir superar stock
        if (cantidadActual + 1 > stock) {
            return Result.failure(IllegalStateException("Sin stock suficiente para $modelo ($color/$talla)"))
        }

        // Insertar o incrementar
        if (existente == null) {
            carritoDao.insert(
                CarritoEntity(
                    idCategoria = idCategoria,
                    idProducto = idProducto,
                    color = color,
                    talla = talla,
                    modelo = modelo.trim(),
                    precioUnitario = precioUnitario,
                    cantidad = 1
                )
            )
        } else {
            carritoDao.update(existente.copy(cantidad = cantidadActual + 1))
        }
        return Result.success(Unit)
    }

    // Disminuye 1 unidad; si queda 0, elimina la variante.
    suspend fun disminuir(
        idCategoria: Long,
        idProducto: Long,
        color: String,
        talla: String
    ): Result<Unit> {
        val it = carritoDao.findByProductoYVariante(idCategoria, idProducto, color, talla)
            ?: return Result.failure(IllegalArgumentException("Ítem no encontrado"))
        val nueva = it.cantidad - 1
        if (nueva <= 0) carritoDao.delete(it) else carritoDao.update(it.copy(cantidad = nueva))
        return Result.success(Unit)
    }

    // Elimina completamente la variante
    suspend fun eliminar(
        idCategoria: Long,
        idProducto: Long,
        color: String,
        talla: String
    ): Result<Boolean> {
        val it = carritoDao.findByProductoYVariante(idCategoria, idProducto, color, talla)
            ?: return Result.failure(IllegalArgumentException("Ítem no encontrado"))
        val rows = carritoDao.delete(it)
        return if (rows > 0) Result.success(true)
        else Result.failure(IllegalStateException("No se pudo eliminar"))
    }

    //Vacia el carrito
    suspend fun limpiar(): Result<Unit> {
        carritoDao.clear()
        return Result.success(Unit)
    }

    companion object {
        // Constantes permitidas
        const val COLOR_BLANCO: String = "Blanco con detalles negros"
        const val COLOR_NEGRO: String  = "Negro con detalles blancos"

        val COLORES_PERMITIDOS: Set<String> = setOf(
            COLOR_BLANCO,
            COLOR_NEGRO
        )

        val TALLAS_PERMITIDAS: Set<String> = setOf("XS", "S", "M", "L", "XL")
    }
}
