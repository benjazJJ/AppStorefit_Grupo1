package com.example.appstorefit_grupo1.data.local.user

data class AdminUserRow(

    val rut: String,
    val name: String,
    val email: String,
    val phone: String?,
    val address: String,
    val roleId: Long?,
    val roleName: String?
)