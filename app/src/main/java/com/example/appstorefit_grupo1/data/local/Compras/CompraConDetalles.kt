package com.example.appstorefit_grupo1.data.local.Compras

import androidx.room.Embedded
import androidx.room.Relation

data class CompraConDetalles(
    @Embedded val compra: CompraEntity,
    @Relation(
        parentColumn = "idCompra",
        entityColumn = "idCompra",
        entity = CompraDetalleEntity::class
    )
    val detalles: List<CompraDetalleEntity>
)