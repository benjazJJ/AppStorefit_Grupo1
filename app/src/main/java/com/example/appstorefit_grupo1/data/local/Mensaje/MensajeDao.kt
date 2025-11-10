package com.example.appstorefit_grupo1.data.local.Mensaje

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MensajeDao {

    @Insert
    suspend fun insert(mensaje: MensajeEntity): Long

    // Bandeja para SOPORTE: solo mensajes originales de clientes (no respuestas)
    @Query("""
        SELECT * FROM mensajes
        WHERE targetRoleId = :rolSoporteId AND isResponse = 0
        ORDER BY createdAt DESC
    """)
    fun getForSupport(rolSoporteId: Int): Flow<List<MensajeEntity>>

    // Historial por remitente (cliente): solo lo que él envió (no respuestas)
    @Query("""
        SELECT * FROM mensajes
        WHERE senderUserId = :usuarioId AND isResponse = 0
        ORDER BY createdAt DESC
    """)
    fun getBySender(usuarioId: Long): Flow<List<MensajeEntity>>

    // Marcar como leído
    @Query("UPDATE mensajes SET read = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long): Int

    // Utilidades para responder
    @Query("SELECT * FROM mensajes WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): MensajeEntity?

    @Query("SELECT * FROM mensajes WHERE repliedToId = :originalId AND isResponse = 1 LIMIT 1")
    suspend fun getRespuestaDe(originalId: Long): MensajeEntity?

    // Outbox del cliente con su posible respuesta (orden dinámico ASC/DESC)
    @Query("""
        SELECT
          -- cliente (mensaje original)
          c.id            AS cliente_id,
          c.senderUserId  AS cliente_senderUserId,
          c.targetRoleId  AS cliente_targetRoleId,
          c.targetUserId  AS cliente_targetUserId,
          c.content       AS cliente_content,
          c.createdAt     AS cliente_createdAt,
          c.read          AS cliente_read,
          c.isResponse    AS cliente_isResponse,
          c.repliedToId   AS cliente_repliedToId,
          c.threadId      AS cliente_threadId,
          c.respondedAt   AS cliente_respondedAt,

          -- respuesta (si existe)
          r.id            AS resp_id,
          r.senderUserId  AS resp_senderUserId,
          r.targetRoleId  AS resp_targetRoleId,
          r.targetUserId  AS resp_targetUserId,
          r.content       AS resp_content,
          r.createdAt     AS resp_createdAt,
          r.read          AS resp_read,
          r.isResponse    AS resp_isResponse,
          r.repliedToId   AS resp_repliedToId,
          r.threadId      AS resp_threadId,
          r.respondedAt   AS resp_respondedAt

        FROM mensajes c
        LEFT JOIN mensajes r
          ON r.repliedToId = c.id AND r.isResponse = 1
        WHERE c.senderUserId = :usuarioId AND c.isResponse = 0
        ORDER BY
          CASE WHEN :asc = 1 THEN c.createdAt END ASC,
          CASE WHEN :asc = 0 THEN c.createdAt END DESC
    """)
    fun getOutboxConRespuesta(
        usuarioId: Long,
        asc: Boolean
    ): Flow<List<com.example.appstorefit_grupo1.data.repository.MensajeConRespuesta>>
}
