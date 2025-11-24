package com.example.appstorefit_grupo1.data.remote.dto.catalog

data class ProductoDto(
    val id: ProductoIdDto,      // coincide con tu `ProductoId` embebido en Java
    val marca: String,
    val modelo: String,
    val color: String,
    val talla: String,
    val precio: Double,
    val cantidadPolera: Int
)