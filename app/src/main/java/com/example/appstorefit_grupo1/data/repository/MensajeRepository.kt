package com.example.appstorefit_grupo1.data.repository
import com.example.appstorefit_grupo1.data.remote.dto.support.EnviarMensajeRequest
import com.example.appstorefit_grupo1.data.remote.dto.support.MensajeConRespuestaDto
import com.example.appstorefit_grupo1.data.remote.dto.support.ResponderMensajeRequest
import com.example.appstorefit_grupo1.data.remote.dto.support.SupportDto
import com.example.appstorefit_grupo1.data.remote.support.SupportApi
import com.example.appstorefit_grupo1.session.SessionManager

class MensajeRepository(
    private val api: SupportApi,
    private val session: SessionManager
) {
    companion object {
        const val ROL_SOPORTE = 3
    }

    //Obtiene rut y rol actuales desde la sesión para armar los headers.
    private fun headers(): Pair<String, String> {
        val user = session.user ?: throw IllegalStateException("Inicia sesion para enviar mensajes.")
        val roleId = session.roleId ?: throw IllegalStateException("Rol no disponible en la sesion.")
        val rut = user.rut.trim()
        require(rut.isNotBlank()) { "RUT no disponible en la sesion." }

        // El servicio de soporte exige el nombre del rol (CLIENTE/ADMIN/SOPORTE), no el id numAcrico.
        val rolHeader = when (roleId) {
            1L -> "CLIENTE"
            2L -> "ADMIN"
            3L -> "SOPORTE"
            else -> roleId.toString()
        }
        return rut to rolHeader
    }

    //Cliente ⇒ Soporte (mensaje original, no respuesta).
    suspend fun enviarMensajeCliente(contenido: String): SupportDto {
        require(contenido.isNotBlank()) { "El contenido no puede estar vacío" }

        val (rut, rol) = headers()

        val body = EnviarMensajeRequest(
            rutRemitente = rut,
            contenido = contenido.trim()
        )

        return api.enviarMensajeCliente(
            headerRut = rut,
            headerRol = rol,
            body = body
        )
    }

    //Bandeja del cliente (sus mensajes + posible respuesta).
    suspend fun obtenerBandejaUsuario(): List<MensajeConRespuestaDto> {
        val (rut, rol) = headers()
        return api.getBandejaUsuario(
            rut = rut,
            headerRut = rut,
            headerRol = rol,
            asc = false
        )
    }

    //Bandeja de soporte: lista de hilos cliente ↔ soporte.
    suspend fun obtenerBandejaSoporte(): List<MensajeConRespuestaDto> {
        val (rut, rol) = headers()
        return api.getBandejaSoporte(
            headerRut = rut,
            headerRol = rol,
            asc = false
        )
    }

    //Marcar mensaje como leído (lo usa quien recibe).
    suspend fun marcarComoLeido(idMensaje: Long): SupportDto {
        val (rut, rol) = headers()
        return api.marcarMensajeLeido(
            id = idMensaje,
            headerRut = rut,
            headerRol = rol
        )
    }

    //Soporte ⇒ Cliente (responder al mensaje original).
    suspend fun responderSoporteAlCliente(
        originalId: Long,
        contenido: String
    ): SupportDto {
        require(contenido.isNotBlank()) { "La respuesta no puede estar vacía" }

        val (rut, rol) = headers()

        val body = ResponderMensajeRequest(
            rutSoporte = rut,
            contenido = contenido.trim()
        )

        return api.responderMensajeCliente(
            originalId = originalId,
            headerRut = rut,
            headerRol = rol,
            body = body
        )
    }

    //Obtener todos los mensajes de un hilo por idHilo.
    suspend fun obtenerHilo(idHilo: Long): List<SupportDto> {
        val (rut, rol) = headers()
        return api.getMensajesPorHilo(
            idHilo = idHilo,
            headerRut = rut,
            headerRol = rol
        )
    }
}
