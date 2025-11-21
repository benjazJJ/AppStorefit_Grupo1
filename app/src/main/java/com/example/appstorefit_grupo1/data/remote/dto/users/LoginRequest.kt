package com.example.appstorefit_grupo1.data.remote.dto.users

// Body que envía la app al hacer login en users-service
data class LoginRequest(
    val correo: String,        // correo usado para iniciar sesión
    val contrasenia: String    // contraseña en texto plano que se valida en el backend
)