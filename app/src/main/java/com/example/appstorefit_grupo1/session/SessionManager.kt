package com.example.appstorefit_grupo1.session


import com.example.appstorefit_grupo1.data.local.user.UserEntity

/**
Sesión simple en memoria para el usuario autenticado.
 */
object SessionManager {
    @Volatile var user: UserEntity? = null
    @Volatile var roleId: Long? = null

    fun clear() {
        user = null
        roleId = null
    }
}
