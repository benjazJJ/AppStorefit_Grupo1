package com.example.appstorefit_grupo1.data.repository

import com.example.appstorefit_grupo1.data.local.Productos.ProductosDao
import com.example.appstorefit_grupo1.data.local.Productos.ProductosEntity
import com.example.appstorefit_grupo1.data.remote.catalog.CatalogApi
import com.example.appstorefit_grupo1.data.remote.dto.catalog.ProductoDto
import com.example.appstorefit_grupo1.data.remote.dto.catalog.ProductoIdDto
import com.example.appstorefit_grupo1.data.remote.dto.catalog.StockReservaItemDto
import com.example.appstorefit_grupo1.session.SessionManager
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio remoto que trabaja contra catalog-service.
 * Sincroniza el cache local (Room) para que los flujos sigan funcionando.
 */
class ProductosRepository(
    private val dao: ProductosDao,
    private val api: CatalogApi
) {
    private val coloresPermitidos = setOf(
        "Blanco con detalles negros",
        "Negro con detalles blancos"
    )
    private val tallasPermitidas = setOf("XS", "S", "M", "L", "XL")

    // Verificacion de permisos: solo ADMIN (roleId = 2L) puede crear/editar/eliminar/ajustar stock
    private fun exigirAdmin() {
        require(SessionManager.roleId == 2L) { "Solo un ADMIN puede realizar esta accion." }
    }

    // Headers para catalog-service (requeridos por el microservicio)
    private fun headersOrThrow(): Pair<String, String> {
        val rut = SessionManager.user?.rut?.trim().orEmpty()
        require(rut.isNotBlank()) { "Sesion no disponible" }

        val roleName = SessionManager.roleName?.uppercase()
            ?: when (SessionManager.roleId) {
                1L -> "CLIENTE"
                2L -> "ADMIN"
                3L -> "SOPORTE"
                else -> "CLIENTE"
            }

        return rut to roleName
    }

    // --------- Mappers DTO <-> Entity ---------
    private fun ProductoDto.toEntity(): ProductosEntity =
        ProductosEntity(
            idCategoria = id.idCategoria,
            idProducto = id.idProducto,
            marca = marca,
            modelo = modelo,
            color = normalizarColor(color),
            talla = talla,
            precio = precio,   // Int -> Int
            stock = stock      // 💥 stock real del microservicio
        )

    private fun ProductosEntity.toDto(): ProductoDto =
        ProductoDto(
            id = ProductoIdDto(idCategoria = idCategoria, idProducto = idProducto),
            marca = marca,
            modelo = modelo,
            color = color,
            talla = talla,
            precio = precio,
            stock = stock,
            imageUrl = null
        )

    // Normaliza el nombre de color que viene del micro (ej. "Blanco" / "Negro")
    private fun normalizarColor(c: String): String {
        val v = c.lowercase()
        return when {
            "blanco" in v -> "Blanco con detalles negros"
            "negro" in v -> "Negro con detalles blancos"
            else -> c
        }
    }

    // Cachea sin borrar para no gatillar cascadas que vacíen el carrito
    private suspend fun cacheAll(productos: List<ProductosEntity>) {
        dao.upsertAll(productos) // evita cascada sobre carrito
    }

    private suspend fun cacheCategoria(idCategoria: Long, productos: List<ProductosEntity>) {
        dao.upsertAll(productos) // evita cascada sobre carrito
    }

    // Observables (cache local)
    fun observeAll(): Flow<List<ProductosEntity>> = dao.observeAll()

    fun observeByCategoria(idCategoria: Long): Flow<List<ProductosEntity>> =
        dao.observeByCategoria(idCategoria)

    fun observeVariantes(idCategoria: Long, modelo: String): Flow<List<ProductosEntity>> =
        dao.observeVariantesByCatAndModelo(idCategoria, modelo)

    // --------- Consultas remotas ----------

    suspend fun getAll(): Result<List<ProductosEntity>> = runCatching {
        val (rut, rol) = headersOrThrow()
        val remote = api.getProductos(
            headerRut = rut,
            headerRol = rol
        ).map { it.toEntity() }
        cacheAll(remote)
        remote
    }

    suspend fun getByCategoria(idCategoria: Long): Result<List<ProductosEntity>> = runCatching {
        val (rut, rol) = headersOrThrow()
        val remote = api.getProductosPorCategoria(
            categoriaId = idCategoria,
            headerRut = rut,
            headerRol = rol
        ).map { it.toEntity() }
        cacheCategoria(idCategoria, remote)
        remote
    }

    suspend fun getByIds(idCategoria: Long, idProducto: Long): Result<ProductosEntity> = runCatching {
        val (rut, rol) = headersOrThrow()
        val dto = api.getProductoPorIds(
            categoriaId = idCategoria,
            productoId = idProducto,
            headerRut = rut,
            headerRol = rol
        )
        val entity = dto.toEntity()
        dao.upsert(entity)
        entity
    }

    // --------- Operaciones de stock ----------

    suspend fun setStock(
        idCategoria: Long,
        idProducto: Long,
        nuevoStock: Int
    ): Result<Unit> {
        exigirAdmin()
        if (nuevoStock < 0) return Result.failure(IllegalArgumentException("El stock no puede ser negativo"))
        return runCatching {
            val (rut, rol) = headersOrThrow()
            val dto = api.getProductoPorIds(
                categoriaId = idCategoria,
                productoId = idProducto,
                headerRut = rut,
                headerRol = rol
            )
            val updated = api.actualizarProducto(
                categoriaId = idCategoria,
                productoId = idProducto,
                body = dto.copy(stock = nuevoStock),
                headerRut = rut,
                headerRol = rol
            )
            dao.upsert(updated.data.toEntity())
        }
    }

    suspend fun addToStock(
        idCategoria: Long,
        idProducto: Long,
        delta: Int
    ): Result<Unit> {
        exigirAdmin()
        if (delta == 0) return Result.success(Unit)

        return runCatching {
            val (rut, rol) = headersOrThrow()
            val dto = api.getProductoPorIds(
                categoriaId = idCategoria,
                productoId = idProducto,
                headerRut = rut,
                headerRol = rol
            )
            val nuevo = dto.stock + delta
            if (nuevo < 0) error("El stock no puede quedar negativo")
            val updated = api.actualizarProducto(
                categoriaId = idCategoria,
                productoId = idProducto,
                body = dto.copy(stock = nuevo),
                headerRut = rut,
                headerRol = rol
            )
            dao.upsert(updated.data.toEntity())
        }
    }

    // --------- CRUD de productos ----------

    suspend fun create(
        idCategoria: Long,
        modelo: String,
        color: String,
        talla: String,
        precio: Int,
        stock: Int,
        marca: String = "StoreFit"
    ): Result<Pair<Long, Long>> {
        if (SessionManager.roleId != 2L) {
            return Result.failure(IllegalStateException("Solo un ADMIN puede realizar esta accion."))
        }

        val modeloT = modelo.trim()
        val colorT = color.trim()
        val tallaT = talla.trim()
        val marcaT = marca.ifBlank { "StoreFit" }.trim()

        if (modeloT.isBlank()) return Result.failure(IllegalArgumentException("Modelo requerido"))
        if (colorT !in coloresPermitidos) return Result.failure(IllegalArgumentException("Color invalido"))
        if (tallaT !in tallasPermitidas) return Result.failure(IllegalArgumentException("Talla invalida"))
        if (precio < 0) return Result.failure(IllegalArgumentException("El precio no puede ser negativo"))
        if (stock < 0) return Result.failure(IllegalArgumentException("El stock no puede ser negativo"))

        val nextIdProducto = (dao.getMaxIdForCategory(idCategoria) ?: 0L) + 1L

        val dto = ProductoDto(
            id = ProductoIdDto(idCategoria = idCategoria, idProducto = nextIdProducto),
            marca = marcaT,
            modelo = modeloT,
            color = colorT,
            talla = tallaT,
            precio = precio,
            stock = stock,
            imageUrl = null
        )

        return runCatching {
            val (rut, rol) = headersOrThrow()
            val resp = api.crearProducto(
                body = dto,
                headerRut = rut,
                headerRol = rol
            )
            val saved = resp.data.toEntity()
            dao.upsert(saved)
            saved.idCategoria to saved.idProducto
        }
    }

    suspend fun update(producto: ProductosEntity): Result<Unit> {
        exigirAdmin()

        val modeloT = producto.modelo.trim()
        val colorT = producto.color.trim()
        val tallaT = producto.talla.trim()
        val marcaT = producto.marca.ifBlank { "StoreFit" }.trim()

        if (modeloT.isBlank()) return Result.failure(IllegalArgumentException("Modelo requerido"))
        if (colorT !in coloresPermitidos) return Result.failure(IllegalArgumentException("Color invalido"))
        if (tallaT !in tallasPermitidas) return Result.failure(IllegalArgumentException("Talla invalida"))
        if (producto.precio < 0) return Result.failure(IllegalArgumentException("El precio no puede ser negativo"))
        if (producto.stock < 0) return Result.failure(IllegalArgumentException("El stock no puede ser negativo"))

        return runCatching {
            val (rut, rol) = headersOrThrow()
            val updated = api.actualizarProducto(
                categoriaId = producto.idCategoria,
                productoId = producto.idProducto,
                body = producto.copy(
                    marca = marcaT,
                    modelo = modeloT,
                    color = colorT,
                    talla = tallaT
                ).toDto(),
                headerRut = rut,
                headerRol = rol
            )
            dao.upsert(updated.data.toEntity())
        }
    }

    suspend fun delete(idCategoria: Long, idProducto: Long): Result<Boolean> {
        exigirAdmin()
        return runCatching {
            val (rut, rol) = headersOrThrow()
            api.eliminarProducto(
                categoriaId = idCategoria,
                productoId = idProducto,
                headerRut = rut,
                headerRol = rol
            )
            val rows = dao.deleteByIds(idCategoria, idProducto)
            rows > 0
        }
    }

    suspend fun updateStock(idCategoria: Long, idProducto: Long, newStock: Int): Result<Unit> =
        setStock(idCategoria, idProducto, newStock)

    suspend fun comprar(idCategoria: Long, idProducto: Long, cantidad: Int): Result<Unit> {
        if (cantidad <= 0) return Result.failure(IllegalArgumentException("Cantidad invalida"))
        return runCatching {
            // /api/v1/productos/stock/reservar (no requiere headers)
            api.reservarStock(
                listOf(
                    StockReservaItemDto(
                        idProducto = idProducto,
                        cantidad = cantidad
                    )
                )
            )
            // Refrescar cache local de ese producto
            val (rut, rol) = headersOrThrow()
            val refreshed = api.getProductoPorIds(
                categoriaId = idCategoria,
                productoId = idProducto,
                headerRut = rut,
                headerRol = rol
            )
            dao.upsert(refreshed.toEntity())
        }
    }
}
