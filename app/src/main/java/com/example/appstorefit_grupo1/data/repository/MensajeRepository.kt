package com.example.appstorefit_grupo1.data.repository


import com.example.appstorefit_grupo1.data.local.Mensaje.MensajeDao
import com.example.appstorefit_grupo1.data.local.Mensaje.MensajeEntity
import kotlinx.coroutines.flow.Flow

class MensajeRepository(
    private val dao: MensajeDao
) {

    suspend fun enviarMensaje(
        idUsuarioRemitente: Long,
        idRolDestinoSoporte: Int,
        contenido: String
    ): Long {
        require(contenido.isNotBlank()) { "El contenido no puede estar vacío" }
        val entidad = MensajeEntity(
            senderUserId = idUsuarioRemitente,
            targetRoleId = idRolDestinoSoporte,
            content = contenido.trim()
        )
        return dao.insert(entidad)
    }

    //Bandeja de soporte (targetRoleId = 3 (SOPORTE))
    fun observarBandejaSoporte(idRolSoporte: Int = 3): Flow<List<MensajeEntity>> =
        dao.getForSupport(idRolSoporte)

    //Historial por remitente (cliente)
    fun observarMensajesDe(idUsuario: Long): Flow<List<MensajeEntity>> =
        dao.getBySender(idUsuario)

    //Marcar un mensaje como leído
    suspend fun marcarComoLeido(idMensaje: Long): Int =
        dao.markAsRead(idMensaje)
}
