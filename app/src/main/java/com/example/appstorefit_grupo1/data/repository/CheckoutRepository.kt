package com.example.appstorefit_grupo1.data.repository

import androidx.room.withTransaction
import com.example.appstorefit_grupo1.data.local.Carrito.CarritoDao
import com.example.appstorefit_grupo1.data.local.Productos.ProductosDao
import com.example.appstorefit_grupo1.data.local.database.AppDatabase

sealed class CheckoutResult {
    data object Ok : CheckoutResult()
    data class SinStock(val msg: String) : CheckoutResult()
    data class Error(val e: Throwable) : CheckoutResult()
}

class CheckoutRepository(
    private val db: AppDatabase,
    private val productosDao: ProductosDao,
    private val carritoDao: CarritoDao
) {
    /** Descuenta stock de todos los items del carrito y limpia el carrito en una sola transacción. */
    suspend fun confirmarCompra(): CheckoutResult {
        return try {
            db.withTransaction {
                val items = carritoDao.getAllOnce()
                if (items.isEmpty()) return@withTransaction CheckoutResult.SinStock("Tu carrito está vacío")

                // Validación previa (mejor mensaje)
                for (it in items) {
                    val prod = productosDao.getByIds(it.idCategoria, it.idProducto)
                        ?: return@withTransaction CheckoutResult.SinStock("Producto no encontrado")
                    if (prod.stock < it.cantidad) {
                        return@withTransaction CheckoutResult.SinStock(
                            "Sin stock de ${prod.modelo} (${prod.color}/${prod.talla})"
                        )
                    }
                }

                // Descuento atómico; si alguno falla => excepción -> rollback
                for (it in items) {
                    val rows = productosDao.descontarStock(it.idCategoria, it.idProducto, it.cantidad)
                    if (rows == 0) throw IllegalStateException(
                        "Stock insuficiente al confirmar ${it.modelo} (${it.color}/${it.talla})"
                    )
                }

                carritoDao.clear()
                CheckoutResult.Ok
            }
        } catch (e: IllegalStateException) {
            CheckoutResult.SinStock(e.message ?: "Sin stock")
        } catch (t: Throwable) {
            CheckoutResult.Error(t)
        }
    }
}