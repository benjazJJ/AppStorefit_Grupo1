package com.example.appstorefit_grupo1.data.local.rol

import androidx.room.ColumnInfo

data class AdminRoleRow(
    @ColumnInfo(name = "rol_id") val id: Long,
    @ColumnInfo(name = "nombre_rol") val name: String
)