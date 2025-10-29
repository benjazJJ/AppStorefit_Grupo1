package com.example.appstorefit_grupo1.data.repository

import com.example.appstorefit_grupo1.data.local.Compras.CompraConDetalles
import com.example.appstorefit_grupo1.data.local.Compras.CompraDao
import com.example.appstorefit_grupo1.data.local.Compras.CompraDetalleEntity
import com.example.appstorefit_grupo1.data.local.Compras.CompraEntity


//ItemCarritoSnapshot: “foto” de lo que había en el carrito al pagar.
// Lo usamos para grabar el detalle de la compra SIN depender de que luego cambie el nombre o precio del producto
data class ItemCarritoSnapshot(
    val idProducto: Long,
    val nombreProducto: String,
    val cantidad: Int,
    val precioUnitario: Int
)

// Repository de Compras:
// RegistrarCompra: crea la cabecera y los detalles (transacción)
// ObtenerHistorial: compras + detalles por RUT
// TotalGastado: suma de (cantidad * precioUnitario) por RUT
class CompraRepository(
    private val compraDao: CompraDao
) {

    //Registra una compra completa:
    // Crea CompraEntity (rut + fecha)
    // Inserta todos los detalles (mapeados desde ItemCarritoSnapshot)
    // Devuelve el idCompra creado
    suspend fun registrarCompra(
        rutUsuario: String,
        fechaMillis: Long,
        items: List<ItemCarritoSnapshot>
    ): Long {
        val compra = CompraEntity(
            rutUsuario = rutUsuario,
            fechaMillis = fechaMillis
        )
        val detalles = items.map {
            CompraDetalleEntity(
                idDetalle = 0,               // autogenerado por Room
                idCompra = 0,                // lo completa insertCompraConDetalles
                idProducto = it.idProducto,
                nombreProducto = it.nombreProducto,
                cantidad = it.cantidad,
                precioUnitario = it.precioUnitario
            )
        }
        return compraDao.insertCompraConDetalles(compra, detalles)
    }

    // Devuelve la lista de compras (cada una con su lista de detalles)
    suspend fun obtenerHistorial(rut: String): List<CompraConDetalles> =
        compraDao.getComprasPorRut(rut)

    // Devuelve el total gastado por ese RUT (en CLP)
    suspend fun totalGastado(rut: String): Int =
        compraDao.getTotalGastadoPorRut(rut)
}