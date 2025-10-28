package com.example.appstorefit_grupo1.data.local.Compras

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "compra",
    indices = [
        Index(value = ["rutUsuario"]), // para consultas por usuario
        Index(value = ["fechaMillis"]) // para ordenar por fecha
    ]
)
data class CompraEntity(
    @PrimaryKey(autoGenerate = true) val idCompra: Long = 0,
    val rutUsuario: String,          // RUT snapshot del comprador
    val fechaMillis: Long            // System.currentTimeMillis()
)