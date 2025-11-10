package com.example.appstorefit_grupo1.data.local.user

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "usuarios",
    indices = [
        Index(value = ["correo_electronico"], unique = true),
        Index(value = ["telefono"], unique = true),
    ]
)
data class UserEntity(
    @PrimaryKey
    @ColumnInfo(name = "rut")
    val rut: String,

    @ColumnInfo(name = "nombre")
    val name: String,


    @ColumnInfo(name = "correo_electronico")
    val email: String,

    @ColumnInfo(name = "telefono")
    val phone: String? = null,

    @ColumnInfo(name = "direccion")
    val address: String = "",

    @ColumnInfo(name = "fec_nac")
    val birthDate: String = "",

    @ColumnInfo(name = "foto_uri")
    val photoUri: String? = null
)