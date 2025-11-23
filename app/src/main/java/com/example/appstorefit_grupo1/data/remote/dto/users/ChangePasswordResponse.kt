package com.example.appstorefit_grupo1.data.remote.dto.users

// Respuesta para cambio de contraseña
data class ChangePasswordResponse(
    val success: Boolean,     // true si se cambió bien
    val message: String?      // mensaje de éxito o error
)
