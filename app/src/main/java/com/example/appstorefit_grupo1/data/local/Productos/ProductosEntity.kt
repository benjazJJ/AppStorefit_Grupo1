package com.example.appstorefit_grupo1.data.local.Productos

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.example.appstorefit_grupo1.data.local.Categoria.CategoriaEntity

@Entity(
    tableName = "producto",
    primaryKeys = ["id_categoria", "id_producto"],
    foreignKeys = [
        ForeignKey(
            entity = CategoriaEntity::class,
            parentColumns = ["id"],
            childColumns = ["id_categoria"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["id_categoria"])]
)
data class ProductosEntity(
    @ColumnInfo(name = "id_categoria")
    val idCategoria: Long,

    @ColumnInfo(name = "id_producto")
    val idProducto: Long,

    @ColumnInfo(name = "marca", defaultValue = "'StoreFit'")
    val marca: String = "StoreFit",

    @ColumnInfo(name = "modelo")
    val modelo: String, // nombre libre por categoría

    @ColumnInfo(name = "color")
    val color: String,  // dos opciones válidas

    @ColumnInfo(name = "talla")
    val talla: String,  // XS..XL

    @ColumnInfo(name = "precio")
    val precio: Int,    // CLP SIN decimales

    @ColumnInfo(name = "stock", defaultValue = "0")
    val stock: Int
)