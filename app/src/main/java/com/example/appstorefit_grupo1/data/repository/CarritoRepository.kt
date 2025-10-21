package com.example.appstorefit_grupo1.data.repository

import com.example.appstorefit_grupo1.data.local.Carrito.CarritoDao
import com.example.appstorefit_grupo1.data.local.Carrito.CarritoEntity
import kotlinx.coroutines.flow.Flow

class CarritoRepository(private val dao: CarritoDao) {

    // --- Observables (para badge/total en vivo) ---
    fun observarItems(): Flow<List<CarritoEntity>> = dao.observarCarrito()
    fun observarCantidadTotal(): Flow<Int> = dao.observarUnidades()
    fun observarTotalCLP(): Flow<Int> = dao.observarTotalCLP()

    // --- Consultas simples (suspend) ---
    suspend fun getAll(): Result<List<CarritoEntity>> = Result.success(dao.getAll())
    suspend fun countUnidades(): Result<Int> = Result.success(dao.countUnidades())
    suspend fun totalCLP(): Result<Int> = Result.success(dao.totalCLP())

    // --- Reglas iguales a ProductosRepository ---
    private val coloresPermitidos = setOf(
        "Blanco con detalles negros",
        "Negro con detalles blancos"
    )
    private val tallasPermitidas = setOf("XS", "S", "M", "L", "XL")

    /** Agrega 1 unidad de un producto+variante. Si ya existe, incrementa cantidad. */
    suspend fun agregar(
        idCategoria: Long,
        idProducto: Long,
        modelo: String,
        color: String,
        talla: String,
        precioUnitario: Int
    ): Result<Unit> {
        if (modelo.isBlank()) return Result.failure(IllegalArgumentException("Modelo requerido"))
        if (color !in coloresPermitidos) return Result.failure(IllegalArgumentException("Color inválido"))
        if (talla !in tallasPermitidas) return Result.failure(IllegalArgumentException("Talla inválida"))
        if (precioUnitario < 0) return Result.failure(IllegalArgumentException("Precio inválido"))

        val existente = dao.findByProductoYVariante(idCategoria, idProducto, color, talla)
        if (existente == null) {
            dao.insert(
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
            dao.update(existente.copy(cantidad = existente.cantidad + 1))
        }
        return Result.success(Unit)
    }

    /** Disminuye 1 unidad; si queda en 0, elimina el ítem. */
    suspend fun disminuir(
        idCategoria: Long,
        idProducto: Long,
        color: String,
        talla: String
    ): Result<Unit> {
        val it = dao.findByProductoYVariante(idCategoria, idProducto, color, talla)
            ?: return Result.failure(IllegalArgumentException("Ítem no encontrado"))
        val nueva = it.cantidad - 1
        if (nueva <= 0) dao.delete(it) else dao.update(it.copy(cantidad = nueva))
        return Result.success(Unit)
    }

    /** Elimina el ítem completo (todas sus unidades). */
    suspend fun eliminar(
        idCategoria: Long,
        idProducto: Long,
        color: String,
        talla: String
    ): Result<Boolean> {
        val it = dao.findByProductoYVariante(idCategoria, idProducto, color, talla)
            ?: return Result.failure(IllegalArgumentException("Ítem no encontrado"))
        val rows = dao.delete(it)
        return if (rows > 0) Result.success(true)
        else Result.failure(IllegalStateException("No se pudo eliminar"))
    }

    /** Vacía el carrito. */
    suspend fun limpiar(): Result<Unit> {
        dao.clear()
        return Result.success(Unit)
    }
}