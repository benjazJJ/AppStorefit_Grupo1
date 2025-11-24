package com.example.appstorefit_grupo1.data.remote.dto.catalog

import com.google.gson.annotations.SerializedName

data class ProductoIdDto(
    @SerializedName("idCategoria")
    val idCategoria: Long,
    @SerializedName("idProducto")
    val idProducto: Long
)
