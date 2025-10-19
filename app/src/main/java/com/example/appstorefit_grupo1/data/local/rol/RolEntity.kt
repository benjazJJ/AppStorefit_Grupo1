package com.example.appstorefit_grupo1.data.local.rol

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rol")
data class RolEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "rol_id")
    val rolId: Long = 0L,

    @ColumnInfo(name = "nombre_rol")
    val nombreRol: String //Cliente, Administrador, Soporte
)
