package com.example.appstorefit_grupo1.data.remote.dto.users

// DTO usado en los endpoints admin (usuario + rol)
data class UsuarioConRolDto(
    val rut: String,              // rut
    val nombre: String,           // nombre
    val apellidos: String,        // apellidos
    val correo: String,           // correo
    val telefono: String?,        // teléfono
    val direccion: String,        // dirección
    val fechaNacimiento: String,  // fecha nacimiento
    val fotoUri: String?,         // foto de perfil
    val rolId: Long?,             // id del rol
    val rolNombre: String?        // nombre del rol
)
