package com.example.appstorefit_grupo1.session

import android.content.Context
import com.example.appstorefit_grupo1.data.local.user.UserEntity

// Sesión simple en memoria + helpers para persistir con DataStore

object SessionManager {
    @Volatile var user: UserEntity? = null
    @Volatile var roleId: Long? = null

    //Limpia solo memoria
    fun clear() {
        user = null
        roleId = null
    }

    //Restaura sesión desde DataStore (llamar al iniciar la app)
    suspend fun restoreFromStore(context: Context) {
        val s = SessionStore.load(context) ?: return
        user = UserEntity(
            rut = s.rut,
            name = s.name,
            email = s.email,
            phone = s.phone,
            address = s.address,
            birthDate = s.birthDate ?: "",
            photoUri = null
        )
        roleId = s.roleId
    }

    // Persiste la sesión actual en DataStore (llamar tras login OK)
    suspend fun persistToStore(context: Context) {
        val u = user ?: return
        SessionStore.save(
            context = context,
            email = u.email,
            rut = u.rut,
            name = u.name,
            address = u.address,
            phone = u.phone,
            birthDate = u.birthDate.ifBlank { null },
            roleId = roleId ?: 0L
        )
    }

    //Limpia memoria + DataStore (usar en "Cerrar sesión")
    suspend fun clearEverywhere(context: Context) {
        clear()
        SessionStore.clear(context)
    }
}
