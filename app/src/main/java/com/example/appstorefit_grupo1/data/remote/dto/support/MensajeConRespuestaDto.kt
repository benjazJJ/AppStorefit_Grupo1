package com.example.appstorefit_grupo1.data.remote.dto.support

data class MensajeConRespuestaDto (
    val clienteMensaje: SupportDto,  // Mensaje original del cliente
    val respuesta: SupportDto?       // Respuesta de soporte (puede ser null si aún no hay)
)
