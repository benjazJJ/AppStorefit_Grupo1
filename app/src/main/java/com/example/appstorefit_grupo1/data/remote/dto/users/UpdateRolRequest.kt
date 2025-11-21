package com.example.appstorefit_grupo1.data.remote.dto.users

// Body para actualizar el rol de un usuario desde PUT /api/v1/usuarios/{rut}
data class UpdateRolRequest(
    val rolId: Long   // nuevo id de rol que se quiere asignar
)