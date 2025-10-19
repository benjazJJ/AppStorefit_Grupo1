package com.example.appstorefit_grupo1.data.local.registro

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.appstorefit_grupo1.data.local.user.UserEntity

@Entity(
    tableName = "registro",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["rut"],
            childColumns = ["rut"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("rut"), Index("rol_id")]
)
data class RegistroEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "rol_id")
    val rolId: Long,                  // 1=Cliente, 2=Admin, 3=Soporte

    @ColumnInfo(name = "usuario")
    val usuario: String,

    @ColumnInfo(name = "contrasenia")
    val contrasenia: String,

    @ColumnInfo(name = "rut")
    val rut: String,                  // FK a usuarios.rut

    @ColumnInfo(name = "direccion")
    val adress: String = ""
)
