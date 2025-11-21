package com.example.appstorefit_grupo1.data.remote.dto.users

// Representa al modelo Rol del backend
data class RolDto(
    val rolId: Long,        // id del rol
    val nombreRol: String   // nombre del rol: CLIENTE, ADMIN, SOPORTE
)