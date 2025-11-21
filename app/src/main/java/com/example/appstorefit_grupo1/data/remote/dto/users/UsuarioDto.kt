package com.example.appstorefit_grupo1.data.remote.dto.users

// Representa al modelo Usuario del backend
// Se usa para el perfil y consultas de usuario
data class UsuarioDto(
    val rut: String,             // rut del usuario
    val nombre: String,          // nombre
    val apellidos: String,       // apellidos
    val correo: String,          // correo
    val telefono: String,        // teléfono
    val direccion: String,       // dirección
    val fechaNacimiento: String, // fecha de nacimiento yyyy-mm-dd
    val fotoUri: String          // ruta o url de la foto de perfil
)