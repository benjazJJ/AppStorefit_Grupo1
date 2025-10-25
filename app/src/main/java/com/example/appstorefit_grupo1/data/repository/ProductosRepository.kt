package com.example.appstorefit_grupo1.data.repository

import com.example.appstorefit_grupo1.data.local.Productos.ProductosDao
import com.example.appstorefit_grupo1.data.local.Productos.ProductosEntity
import kotlinx.coroutines.flow.Flow

class ProductosRepository(
    private val dao: ProductosDao
) {
    private val coloresPermitidos = setOf(
        "Blanco con detalles negros",
        "Negro con detalles blancos"
    )
    private val tallasPermitidas = setOf("XS","S","M","L","XL")

    fun observeAll(): Flow<List<ProductosEntity>> = dao.observeAll()

    fun observeByCategoria(idCategoria: Long): Flow<List<ProductosEntity>> =
        dao.observeByCategoria(idCategoria)

    fun observeVariantes(idCategoria: Long, modelo: String): Flow<List<ProductosEntity>> =
        dao.observeVariantesByCatAndModelo(idCategoria, modelo)


    suspend fun setStock(
        idCategoria: Long,
        idProducto: Long,
        nuevoStock: Int
    ): Result<Unit> {
        if (nuevoStock < 0) return Result.failure(IllegalArgumentException("El stock no puede ser negativo"))
        val rows = dao.setStock(idCategoria, idProducto, nuevoStock)
        return if (rows > 0) Result.success(Unit)
        else Result.failure(IllegalStateException("No se pudo actualizar el stock"))
    }

    suspend fun addToStock(
        idCategoria: Long,
        idProducto: Long,
        delta: Int
    ): Result<Unit> {
        val rows = dao.addToStock(idCategoria, idProducto, delta)
        return if (rows > 0) Result.success(Unit)
        else Result.failure(IllegalStateException("No se pudo actualizar el stock"))
    }

    suspend fun create(
        idCategoria: Long,
        modelo: String,
        color: String,
        talla: String,
        precio: Int,   // <- Int ahora
        stock: Int,
        marca: String = "StoreFit"
    ): Result<Pair<Long, Long>> {
        if (modelo.isBlank()) return Result.failure(IllegalArgumentException("Modelo requerido"))
        if (color !in coloresPermitidos) return Result.failure(IllegalArgumentException("Color inválido"))
        if (talla !in tallasPermitidas) return Result.failure(IllegalArgumentException("Talla inválida"))
        if (precio < 0) return Result.failure(IllegalArgumentException("El precio no puede ser negativo"))
        if (stock  < 0) return Result.failure(IllegalArgumentException("El stock no puede ser negativo"))

        val nextIdProducto = (dao.getMaxIdForCategory(idCategoria) ?: 0L) + 1L

        val entity = ProductosEntity(
            idCategoria = idCategoria,
            idProducto  = nextIdProducto,
            marca       = marca.ifBlank { "StoreFit" }.trim(),
            modelo      = modelo.trim(),
            color       = color,
            talla       = talla,
            precio      = precio,
            stock       = stock
        )
        dao.insert(entity)
        return Result.success(idCategoria to nextIdProducto)
    }

    suspend fun getAll() = Result.success(dao.getAll())
    suspend fun getByCategoria(idCategoria: Long) = Result.success(dao.getByCategoria(idCategoria))

    suspend fun getByIds(idCategoria: Long, idProducto: Long): Result<ProductosEntity> {
        val p = dao.getByIds(idCategoria, idProducto)
            ?: return Result.failure(IllegalArgumentException("Producto no encontrado"))
        return Result.success(p)
    }

    suspend fun update(producto: ProductosEntity): Result<Unit> {
        if (producto.color !in coloresPermitidos) return Result.failure(IllegalArgumentException("Color inválido"))
        if (producto.talla !in tallasPermitidas) return Result.failure(IllegalArgumentException("Talla inválida"))
        if (producto.precio < 0) return Result.failure(IllegalArgumentException("El precio no puede ser negativo"))
        if (producto.stock  < 0) return Result.failure(IllegalArgumentException("El stock no puede ser negativo"))

        val rows = dao.update(producto)
        return if (rows > 0) Result.success(Unit)
        else Result.failure(IllegalStateException("No se pudo actualizar el producto"))
    }

    suspend fun delete(idCategoria: Long, idProducto: Long): Result<Boolean> {
        val actual = dao.getByIds(idCategoria, idProducto)
            ?: return Result.failure(IllegalArgumentException("Producto no encontrado"))
        val rows = dao.delete(actual)
        return if (rows > 0) Result.success(true)
        else Result.failure(IllegalStateException("No se pudo eliminar el producto"))
    }

    suspend fun updateStock(idCategoria: Long, idProducto: Long, newStock: Int): Result<Unit> {
        if (newStock < 0) return Result.failure(IllegalArgumentException("El stock no puede ser negativo"))
        val rows = dao.updateStock(idCategoria, idProducto, newStock)
        return if (rows > 0) Result.success(Unit)
        else Result.failure(IllegalStateException("No se pudo actualizar el stock"))
    }

    suspend fun comprar(idCategoria: Long, idProducto: Long, cantidad: Int): Result<Unit> {
        if (cantidad <= 0) return Result.failure(IllegalArgumentException("Cantidad inválida"))
        val rows = dao.descontarStock(idCategoria, idProducto, cantidad)
        return if (rows > 0) Result.success(Unit)
        else Result.failure(IllegalStateException("Stock insuficiente"))
    }
}
