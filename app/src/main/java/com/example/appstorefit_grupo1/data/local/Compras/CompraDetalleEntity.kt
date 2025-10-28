package com.example.appstorefit_grupo1.data.local.Compras

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "compra_detalle",
    indices = [Index("idCompra"), Index("idProducto")]
)
data class CompraDetalleEntity(
    @PrimaryKey(autoGenerate = true) val idDetalle: Long = 0,
    val idCompra: Long,
    val idProducto: Long,
    val nombreProducto: String,
    val cantidad: Int,
    val precioUnitario: Int
)