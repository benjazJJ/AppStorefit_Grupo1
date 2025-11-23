package com.example.appstorefit_grupo1.data.remote.dto.users

data class LoginResponse(
    val success: Boolean,      // true si el login fue correcto
    val message: String?,      // mensaje opcional (error o info)
    val rut: String?,          // rut del usuario autenticado (puede venir null en error)
    val nombre: String?,       // nombre del usuario
    val correo: String?,       // correo del usuario
    val rolId: Long?,          // id del rol
    val rolNombre: String?     // nombre del rol (CLIENTE, ADMIN, SOPORTE)
)
