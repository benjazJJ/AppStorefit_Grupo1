package com.example.appstorefit_grupo1.data.repository

import com.example.appstorefit_grupo1.data.local.Mensaje.MensajeDao
import com.example.appstorefit_grupo1.data.local.Mensaje.MensajeEntity
import kotlinx.coroutines.flow.Flow

class MensajeRepository(
    private val dao: MensajeDao
) {
    companion object {
        const val ROL_SOPORTE = 3
    }

    /** Cliente ⇒ Soporte (mensaje original, no respuesta). */
    suspend fun enviarMensaje(
        idUsuarioRemitente: Long,
        idRolDestinoSoporte: Int = ROL_SOPORTE,
        contenido: String
    ): Long {
        require(contenido.isNotBlank()) { "El contenido no puede estar vacío" }
        val entidad = MensajeEntity(
            senderUserId = idUsuarioRemitente,
            targetRoleId = idRolDestinoSoporte,
            targetUserId = null,
            content = contenido.trim(),
            isResponse = false,
            repliedToId = null,
            threadId = null,
            respondedAt = null
        )
        return dao.insert(entidad)
    }

    /** Bandeja de soporte: solo mensajes originales de clientes. */
    fun observarBandejaSoporte(idRolSoporte: Int = ROL_SOPORTE): Flow<List<MensajeEntity>> =
        dao.getForSupport(idRolSoporte)

    /** Historial que envió un cliente (sin respuestas). */
    fun observarMensajesDe(idUsuario: Long): Flow<List<MensajeEntity>> =
        dao.getBySender(idUsuario)

    /** Outbox del cliente + posible respuesta del soporte (orden configurable). */
    fun observarOutboxClienteConRespuesta(
        idUsuario: Long,
        asc: Boolean
    ): Flow<List<MensajeConRespuesta>> =
        dao.getOutboxConRespuesta(idUsuario, asc)

    /** Marcar como leído (lo usa quien recibe). */
    suspend fun marcarComoLeido(idMensaje: Long): Int =
        dao.markAsRead(idMensaje)

    /** Soporte ⇒ Cliente (responder una única vez al mensaje original). */
    suspend fun responderSoporteAlCliente(
        threadId: Long,             // id del mensaje original del cliente
        idUsuarioSoporte: Long,     // quien responde (soporte)
        idUsuarioCliente: Long,     // destinatario (cliente)
        contenido: String
    ): Long {
        require(contenido.isNotBlank()) { "La respuesta no puede estar vacía" }

        val original = dao.getById(threadId)
            ?: throw IllegalArgumentException("Mensaje original no encontrado")
        require(!original.isResponse) { "No se puede responder a una respuesta" }
        require(original.targetRoleId == ROL_SOPORTE) { "El mensaje no está dirigido a soporte" }
        require(idUsuarioCliente == original.senderUserId) { "El destinatario no coincide con el remitente original" }

        // Evitar múltiples respuestas sobre el mismo original
        val yaRespondida = dao.getRespuestaDe(original.id)
        require(yaRespondida == null) { "Este mensaje ya fue respondido" }

        val ahora = System.currentTimeMillis()

        val respuesta = MensajeEntity(
            senderUserId = idUsuarioSoporte,
            targetRoleId = null,                 // respuesta va a un usuario específico
            targetUserId = idUsuarioCliente,     // el cliente que recibirá
            content = contenido.trim(),
            createdAt = ahora,
            read = false,                        // el cliente aún no la lee
            isResponse = true,
            repliedToId = original.id,           // vínculo al original
            threadId = original.id,              // usamos el id del original como hilo
            respondedAt = ahora
        )

        return dao.insert(respuesta)
    }
}
