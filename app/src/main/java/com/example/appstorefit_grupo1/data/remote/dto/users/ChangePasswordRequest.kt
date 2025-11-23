package com.example.appstorefit_grupo1.data.remote.dto.users

// Body para POST /api/v1/registros/cambiar-contrasenia
data class ChangePasswordRequest(
    val usuarioOCorreo: String,   // puede ser usuario o correo
    val contraseniaActual: String,// contraseña actual
    val nuevaContrasenia: String, // nueva contraseña
    val confirmarContrasenia: String // confirmación
)
