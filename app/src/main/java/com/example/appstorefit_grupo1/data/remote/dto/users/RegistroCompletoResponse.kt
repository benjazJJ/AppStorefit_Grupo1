package com.example.appstorefit_grupo1.data.remote.dto.users

// Respuesta simple del registro completo
data class RegistroCompletoResponse(
    val success: Boolean,  // true si se creó correctamente
    val usuario: String    // usuario de acceso (correo registrado)
)