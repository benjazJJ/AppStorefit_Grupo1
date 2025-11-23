package com.example.appstorefit_grupo1.data.remote.dto.users

// Body para POST /api/v1/admin/usuarios
data class AdminCrearUsuarioRequest(
    val rut: String,              // rut del usuario
    val nombre: String,           // nombre
    val apellidos: String,        // apellidos
    val correo: String,           // correo
    val telefono: String?,        // teléfono opcional
    val direccion: String,        // dirección
    val fechaNacimiento: String,  // fecha nacimiento
    val fotoUri: String?,         // foto de perfil opcional
    val contrasenia: String,      // contraseña inicial
    val rolId: Long               // rol a asignar (CLIENTE, ADMIN, SOPORTE)
)
