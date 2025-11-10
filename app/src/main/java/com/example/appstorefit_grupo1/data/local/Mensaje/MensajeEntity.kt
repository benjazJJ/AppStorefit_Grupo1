package com.example.appstorefit_grupo1.data.local.Mensaje

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mensajes")
data class MensajeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,

    val senderUserId: Long,              // RUT estable del que envía
    val targetRoleId: Int? = null,       // 3 = SOPORTE cuando cliente envía; null en respuestas
    val targetUserId: Long? = null,      // id del cliente al que va dirigida la respuesta

    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val read: Boolean = false,

    // Campos para manejar respuestas del soporte
    val isResponse: Boolean = false,     // true si es respuesta del soporte
    val repliedToId: Long? = null,       // id del mensaje original del cliente
    val threadId: Long? = null,          // id del hilo (usa el id del mensaje original)
    val respondedAt: Long? = null        // timestamp de la respuesta
)