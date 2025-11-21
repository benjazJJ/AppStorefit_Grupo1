package com.example.appstorefit_grupo1.data.remote.dto.users

// Body para el endpoint POST /api/v1/registros/registro-completo
// Crea perfil de Usuario + credenciales de Registro
data class RegistroCompletoRequest(
    val rut: String,               // rut del usuario, PK lógica
    val nombre: String,            // nombre
    val apellidos: String,         // apellidos
    val correo: String,            // correo de contacto y login
    val fechaNacimiento: String,   // fecha en formato yyyy-mm-dd
    val contrasenia: String,       // contraseña elegida
    val confirmarContrasenia: String, // confirmación de contraseña
    val direccion: String,         // dirección registrada
    val telefono: String           // teléfono de contacto
)