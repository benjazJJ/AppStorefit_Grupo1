package com.example.appstorefit_grupo1.data.remote.dto.users

// Body para PATCH /api/v1/usuarios/{rut}/foto
data class UpdateFotoRequest(
    val fotoUri: String?      // nueva foto, null o vacío para eliminar
)
