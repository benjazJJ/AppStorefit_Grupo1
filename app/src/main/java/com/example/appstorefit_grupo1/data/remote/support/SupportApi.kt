package com.example.appstorefit_grupo1.data.remote.support

import com.example.appstorefit_grupo1.data.remote.dto.support.EnviarMensajeRequest
import com.example.appstorefit_grupo1.data.remote.dto.support.MensajeConRespuestaDto
import com.example.appstorefit_grupo1.data.remote.dto.support.ResponderMensajeRequest
import com.example.appstorefit_grupo1.data.remote.dto.support.SupportDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface SupportApi {

    // ---------- CRUD básico ----------

    // Listar todos los mensajes (solo SOPORTE)
    @GET("api/v1/mensajes")
    suspend fun listarMensajes(
        @Header("X-User-Rut") headerRut: String,
        @Header("X-User-Rol") headerRol: String
    ): List<SupportDto>

    // Obtener un mensaje por id (soporte o participante del mensaje)
    @GET("api/v1/mensajes/{id}")
    suspend fun getMensajePorId(
        @Path("id") id: Long,
        @Header("X-User-Rut") headerRut: String,
        @Header("X-User-Rol") headerRol: String
    ): SupportDto

    // Eliminar un mensaje (solo SOPORTE)
    @DELETE("api/v1/mensajes/{id}")
    suspend fun eliminarMensaje(
        @Path("id") id: Long,
        @Header("X-User-Rut") headerRut: String,
        @Header("X-User-Rol") headerRol: String
    )

    // Marcar un mensaje como leído (soporte o participante del mensaje)
    @PATCH("api/v1/mensajes/{id}/leido")
    suspend fun marcarMensajeLeido(
        @Path("id") id: Long,
        @Header("X-User-Rut") headerRut: String,
        @Header("X-User-Rol") headerRol: String
    ): SupportDto

    // ---------- Operaciones de negocio ----------

    // Cliente envía mensaje a soporte
    @POST("api/v1/mensajes/cliente")
    suspend fun enviarMensajeCliente(
        @Header("X-User-Rut") headerRut: String,
        @Header("X-User-Rol") headerRol: String,
        @Body body: EnviarMensajeRequest
    ): SupportDto

    // Soporte responde a mensaje de cliente
    @POST("api/v1/mensajes/soporte/{originalId}/respuesta")
    suspend fun responderMensajeCliente(
        @Path("originalId") originalId: Long,
        @Header("X-User-Rut") headerRut: String,
        @Header("X-User-Rol") headerRol: String,
        @Body body: ResponderMensajeRequest
    ): SupportDto

    // Bandeja de soporte (solo SOPORTE)
    @GET("api/v1/mensajes/soporte/bandeja")
    suspend fun getBandejaSoporte(
        @Header("X-User-Rut") headerRut: String,
        @Header("X-User-Rol") headerRol: String,
        @Query("asc") asc: Boolean = false
    ): List<MensajeConRespuestaDto>

    // Bandeja de un usuario (cliente o soporte)
    @GET("api/v1/mensajes/usuario/{rut}/bandeja")
    suspend fun getBandejaUsuario(
        @Path("rut") rut: String,
        @Header("X-User-Rut") headerRut: String,
        @Header("X-User-Rol") headerRol: String,
        @Query("asc") asc: Boolean = false
    ): List<MensajeConRespuestaDto>

    // Obtener un hilo completo por idHilo
    @GET("api/v1/mensajes/hilos/{idHilo}")
    suspend fun getMensajesPorHilo(
        @Path("idHilo") idHilo: Long,
        @Header("X-User-Rut") headerRut: String,
        @Header("X-User-Rol") headerRol: String
    ): List<SupportDto>
}