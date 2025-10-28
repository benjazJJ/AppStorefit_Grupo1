package com.example.appstorefit_grupo1.data.local.Mensaje

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MensajeDao {

    @Insert
    suspend fun insert(mensaje: MensajeEntity): Long

    // Bandeja para soporte (solo mensajes dirigidos al rol 3, ordenados por fecha desc)
    @Query("""
        SELECT * FROM mensajes 
        WHERE targetRoleId = :rolSoporteId 
        ORDER BY createdAt DESC
    """)
    fun getForSupport(rolSoporteId: Int): Flow<List<MensajeEntity>>

    // Historial por remitente
    @Query("""
        SELECT * FROM mensajes 
        WHERE senderUserId = :usuarioId 
        ORDER BY createdAt DESC
    """)
    fun getBySender(usuarioId: Long): Flow<List<MensajeEntity>>

    // Marcar como leído
    @Query("UPDATE mensajes SET read = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long): Int
}
