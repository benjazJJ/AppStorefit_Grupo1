package com.example.appstorefit_grupo1.data.remote.dto.orders

data class CompraDto (
    val idCompra: Long? = null,                   // Lo rellena el backend
    val rutUsuario: String,                       // RUT del comprador
    val fechaMillis: Long? = null,                // El backend setea System.currentTimeMillis()
    val detalles: List<CompraDetalleDto>          // Ítems de la compra
)