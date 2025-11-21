package com.example.appstorefit_grupo1.data.remote.dto.users

// Respuesta que devuelve el endpoint POST /api/v1/registros/login
data class LoginResponse(
    val success: Boolean,   // true si las credenciales son válidas
    val usuario: String,    // nombre de usuario de acceso
    val rut: String,        // rut del usuario autenticado
    val nombre: String,     // nombre del usuario
    val correo: String,     // correo del usuario
    val rolId: Long,        // id numérico del rol
    val rolNombre: String   // nombre del rol: CLIENTE, ADMIN, SOPORTE
)