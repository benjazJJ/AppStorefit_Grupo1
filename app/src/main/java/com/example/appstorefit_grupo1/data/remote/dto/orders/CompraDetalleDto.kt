package com.example.appstorefit_grupo1.data.remote.dto.orders

data class CompraDetalleDto (
    val idDetalle: Long? = null,      // Lo rellena el backend
    val idProducto: Long,             // id_producto del catálogo
    val nombreProducto: String,       // nombre snapshot
    val cantidad: Int,                // cantidad comprada
    val precioUnitario: Int           // precio unitario CLP
)