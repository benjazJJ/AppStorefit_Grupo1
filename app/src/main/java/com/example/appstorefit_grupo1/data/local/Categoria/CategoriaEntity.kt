package com.example.appstorefit_grupo1.data.local.Categoria

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "categoria",
    indices = [Index(value = ["nombre"], unique = true)]
)
data class CategoriaEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,

    @ColumnInfo(name = "nombre")
    val nombre: String,

    @ColumnInfo(name = "descripcion")
    val descripcion: String? = null
)