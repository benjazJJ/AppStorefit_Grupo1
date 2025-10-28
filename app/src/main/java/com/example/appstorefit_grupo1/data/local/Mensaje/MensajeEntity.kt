package com.example.appstorefit_grupo1.data.local.Mensaje


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mensajes")
data class MensajeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val senderUserId: Long,          // ID estable del remitente (usaremos el RUT en Long)
    val targetRoleId: Int,           // 3 = SOPORTE
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val read: Boolean = false
)
