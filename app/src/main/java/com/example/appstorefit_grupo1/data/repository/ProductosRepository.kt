package com.example.appstorefit_grupo1.data.repository

import com.example.appstorefit_grupo1.data.local.Productos.ProductosDao
import com.example.appstorefit_grupo1.data.local.Productos.ProductosEntity
import com.example.appstorefit_grupo1.session.SessionManager
import kotlinx.coroutines.flow.Flow

class ProductosRepository(
    private val dao: ProductosDao
) {
    // Colores y tallas permitidos en tu dominio actual
    private val coloresPermitidos = setOf(
        "Blanco con detalles negros",
        "Negro con detalles blancos"
    )
    private val tallasPermitidas = setOf("XS", "S", "M", "L", "XL")

    // Verificación de permisos: solo ADMIN (roleId = 2L) puede crear/editar/eliminar/ajustar stock
    private fun exigirAdmin() {
        val esAdmin = SessionManager.roleId == 2L ||
                SessionManager.roleName?.uppercase() == "ADMIN"
        require(esAdmin) { "Solo un ADMIN puede realizar esta acción." }
    }

    // Observables (no requieren permisos)
    fun observeAll(): Flow<List<ProductosEntity>> = dao.observeAll()

    fun observeByCategoria(idCategoria: Long): Flow<List<ProductosEntity>> =
        dao.observeByCategoria(idCategoria)

    fun observeVariantes(idCategoria: Long, modelo: String): Flow<List<ProductosEntity>> =
        dao.observeVariantesByCatAndModelo(idCategoria, modelo)

    // Set de stock directo (ADMIN)
    suspend fun setStock(
        idCategoria: Long,
        idProducto: Long,
        nuevoStock: Int
    ): Result<Unit> {
        exigirAdmin()
        if (nuevoStock < 0) return Result.failure(IllegalArgumentException("El stock no puede ser negativo"))
        val rows = dao.setStock(idCategoria, idProducto, nuevoStock)
        return if (rows > 0) Result.success(Unit)
        else Result.failure(IllegalStateException("No se pudo actualizar el stock"))
    }

    // Sumar/restar stock por delta (ADMIN). Evita dejar el stock en negativo.
    suspend fun addToStock(
        idCategoria: Long,
        idProducto: Long,
        delta: Int
    ): Result<Unit> {
        exigirAdmin()
        if (delta == 0) return Result.success(Unit) // No hay cambio

        val actual = dao.getByIds(idCategoria, idProducto)
            ?: return Result.failure(IllegalArgumentException("Producto no encontrado"))

        val nuevo = actual.stock + delta
        if (nuevo < 0) return Result.failure(IllegalArgumentException("El stock no puede quedar negativo"))

        val rows = dao.setStock(idCategoria, idProducto, nuevo)
        return if (rows > 0) Result.success(Unit)
        else Result.failure(IllegalStateException("No se pudo actualizar el stock"))
    }

    // Crear variante (ADMIN). Evita duplicados por (modelo + color + talla) dentro de la categoría.
    suspend fun create(
        idCategoria: Long,
        modelo: String,
        color: String,
        talla: String,
        precio: Int,
        stock: Int,
        marca: String = "StoreFit"
    ): Result<Pair<Long, Long>> {
        exigirAdmin()

        val modeloT = modelo.trim()
        val colorT  = color.trim()
        val tallaT  = talla.trim()
        val marcaT  = marca.ifBlank { "StoreFit" }.trim()

        if (modeloT.isBlank()) return Result.failure(IllegalArgumentException("Modelo requerido"))
        if (colorT !in coloresPermitidos) return Result.failure(IllegalArgumentException("Color inválido"))
        if (tallaT !in tallasPermitidas) return Result.failure(IllegalArgumentException("Talla inválida"))
        if (precio < 0) return Result.failure(IllegalArgumentException("El precio no puede ser negativo"))
        // Si deseas obligar a precio > 0: if (precio <= 0) return Result.failure(IllegalArgumentException("El precio debe ser mayor a 0"))
        if (stock < 0) return Result.failure(IllegalArgumentException("El stock no puede ser negativo"))

        // Evitar duplicado exacto de variante dentro de la misma categoría
        val dup = dao.countByCatModeloColorTalla(idCategoria, modeloT, colorT, tallaT) > 0
        if (dup) return Result.failure(IllegalArgumentException("Ya existe una variante con ese modelo, color y talla en la categoría"))

        // Calcular el siguiente id_producto secuencial por categoría
        val nextIdProducto = (dao.getMaxIdForCategory(idCategoria) ?: 0L) + 1L

        val entity = ProductosEntity(
            idCategoria = idCategoria,
            idProducto  = nextIdProducto,
            marca       = marcaT,
            modelo      = modeloT,
            color       = colorT,
            talla       = tallaT,
            precio      = precio,
            stock       = stock
        )
        dao.insert(entity)
        return Result.success(idCategoria to nextIdProducto)
    }

    // Lecturas directas (no requieren permisos)
    suspend fun getAll() = Result.success(dao.getAll())

    suspend fun getByCategoria(idCategoria: Long) =
        Result.success(dao.getByCategoria(idCategoria))

    suspend fun getByIds(idCategoria: Long, idProducto: Long): Result<ProductosEntity> {
        val p = dao.getByIds(idCategoria, idProducto)
            ?: return Result.failure(IllegalArgumentException("Producto no encontrado"))
        return Result.success(p)
    }

    // Editar variante (ADMIN). Valida duplicados contra otra fila distinta a la actual.
    suspend fun update(producto: ProductosEntity): Result<Unit> {
        exigirAdmin()

        val modeloT = producto.modelo.trim()
        val colorT  = producto.color.trim()
        val tallaT  = producto.talla.trim()
        val marcaT  = producto.marca.ifBlank { "StoreFit" }.trim()

        if (modeloT.isBlank()) return Result.failure(IllegalArgumentException("Modelo requerido"))
        if (colorT !in coloresPermitidos) return Result.failure(IllegalArgumentException("Color inválido"))
        if (tallaT !in tallasPermitidas) return Result.failure(IllegalArgumentException("Talla inválida"))
        if (producto.precio < 0) return Result.failure(IllegalArgumentException("El precio no puede ser negativo"))
        if (producto.stock  < 0) return Result.failure(IllegalArgumentException("El stock no puede ser negativo"))

        // Verificar si existe otra fila con la misma combinación (modelo, color, talla)
        // Si hay una y no es esta misma PK, entonces es duplicado.
        val posibleConflicto = dao.getByCatModeloColorTalla(
            idCategoria = producto.idCategoria,
            modelo = modeloT,
            color  = colorT,
            talla  = tallaT
        )
        if (posibleConflicto != null &&
            (posibleConflicto.idCategoria != producto.idCategoria ||
                    posibleConflicto.idProducto  != producto.idProducto)
        ) {
            return Result.failure(IllegalArgumentException("Ya existe otra variante con ese modelo, color y talla"))
        }

        val filas = dao.update(
            producto.copy(
                marca  = marcaT,
                modelo = modeloT,
                color  = colorT,
                talla  = tallaT
            )
        )
        return if (filas > 0) Result.success(Unit)
        else Result.failure(IllegalStateException("No se pudo actualizar el producto"))
    }

    // Eliminar variante (ADMIN)
    suspend fun delete(idCategoria: Long, idProducto: Long): Result<Boolean> {
        exigirAdmin()
        val actual = dao.getByIds(idCategoria, idProducto)
            ?: return Result.failure(IllegalArgumentException("Producto no encontrado"))
        val rows = dao.delete(actual)
        return if (rows > 0) Result.success(true)
        else Result.failure(IllegalStateException("No se pudo eliminar el producto"))
    }

    // Alias para mantener compatibilidad con lo que ya tenías (ADMIN)
    suspend fun updateStock(idCategoria: Long, idProducto: Long, newStock: Int): Result<Unit> =
        setStock(idCategoria, idProducto, newStock)

    // Flujo de compra (no exige ADMIN). Descuenta stock si hay suficiente.
    suspend fun comprar(idCategoria: Long, idProducto: Long, cantidad: Int): Result<Unit> {
        if (cantidad <= 0) return Result.failure(IllegalArgumentException("Cantidad inválida"))
        val rows = dao.descontarStock(idCategoria, idProducto, cantidad)
        return if (rows > 0) Result.success(Unit)
        else Result.failure(IllegalStateException("Stock insuficiente"))
    }
}
