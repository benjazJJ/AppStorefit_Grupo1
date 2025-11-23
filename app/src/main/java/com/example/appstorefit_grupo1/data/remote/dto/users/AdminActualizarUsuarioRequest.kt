package com.example.appstorefit_grupo1.data.remote.dto.users

// Body para PUT /api/v1/admin/usuarios/{rut}
data class AdminActualizarUsuarioRequest(
    val nombre: String,           // nombre
    val apellidos: String,        // apellidos
    val correo: String,           // correo
    val telefono: String?,        // teléfono
    val direccion: String,        // dirección
    val fechaNacimiento: String,  // fecha nacimiento
    val fotoUri: String?          // foto de perfil opcional
)
