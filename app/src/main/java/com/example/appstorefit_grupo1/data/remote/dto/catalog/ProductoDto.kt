package com.example.appstorefit_grupo1.data.remote.dto.catalog

import com.google.gson.annotations.SerializedName

data class ProductoDto(
    @SerializedName("id")
    val id: ProductoIdDto,
    @SerializedName("marca")
    val marca: String,
    @SerializedName("modelo")
    val modelo: String,
    @SerializedName("color")
    val color: String,
    @SerializedName("talla")
    val talla: String,
    @SerializedName("precio")
    val precio: Int,
    @SerializedName("stock")
    val stock: Int,
    @SerializedName("imageUrl")
    val imageUrl: String? = null
)
