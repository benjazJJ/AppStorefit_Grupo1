package com.example.appstorefit_grupo1.data.remote.dto.support

data class SupportDto(
    val id: Long,              // ID del mensaje
    val rutRemitente: String,  // RUT del remitente (cliente o soporte)
    val idRolDestino: Int?,    // ID del rol destino (cuando cliente escribe a SOPORTE)
    val rutDestino: String?,   // RUT destino cuando soporte responde a un cliente específico
    val contenido: String,     // Contenido del mensaje
    val creadoEn: Long,        // Timestamp de creación (millis)
    val leido: Boolean,        // Marcado como leído
    val esRespuesta: Boolean,  // true si es una respuesta de soporte
    val respondeAId: Long?,    // ID del mensaje original al que se responde
    val idHilo: Long?,         // ID del hilo de conversación
    val respondidoEn: Long?    // Timestamp de respuesta (millis), si aplica
)