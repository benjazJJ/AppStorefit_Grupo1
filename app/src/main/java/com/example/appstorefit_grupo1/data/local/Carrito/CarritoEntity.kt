package com.example.appstorefit_grupo1.data.local.Carrito

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.appstorefit_grupo1.data.local.Productos.ProductosEntity

@Entity(
    tableName = "carrito",
    foreignKeys = [
        ForeignKey(
            entity = ProductosEntity::class,
            parentColumns = ["id_categoria", "id_producto"],
            childColumns  = ["id_categoria", "id_producto"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["id_categoria", "id_producto"]),
        Index(value = ["id_categoria", "id_producto", "color", "talla"], unique = true)
    ]
)
data class CarritoEntity(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,

    // se llenará cuando generes el pedido (opcional)
    @ColumnInfo(name = "id_pedido")
    val idPedido: Long? = null,

    @ColumnInfo(name = "id_categoria")
    val idCategoria: Long,

    @ColumnInfo(name = "id_producto")
    val idProducto: Long,

    // variante
    @ColumnInfo(name = "color")
    val color: String,

    @ColumnInfo(name = "talla")
    val talla: String,

    // snapshot para UI rápida
    @ColumnInfo(name = "modelo")
    val modelo: String,

    // CLP, mismo tipo que producto.precio
    @ColumnInfo(name = "precio_unitario")
    val precioUnitario: Int,

    @ColumnInfo(name = "cantidad")
    val cantidad: Int = 1
)