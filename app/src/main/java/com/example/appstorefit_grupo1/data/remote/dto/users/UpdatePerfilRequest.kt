package com.example.appstorefit_grupo1.data.remote.dto.users

// Body para PUT /api/v1/usuarios/{rut}/perfil
data class UpdatePerfilRequest(
    val nombre: String,           // nombre
    val apellidos: String,        // apellidos
    val correo: String,           // correo
    val telefono: String?,        // teléfono (puede ser null)
    val direccion: String,        // dirección
    val fechaNacimiento: String,  // fecha nacimiento
    val fotoUri: String?          // foto (opcional)
)
