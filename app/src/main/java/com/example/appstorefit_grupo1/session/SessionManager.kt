package com.example.appstorefit_grupo1.session

import android.content.Context
import com.example.appstorefit_grupo1.data.local.user.UserEntity

// Sesión simple en memoria + helpers para persistir con DataStore

object SessionManager {
    private fun deriveRoleIdFromName(name: String?): Long? = name?.uppercase()?.let {
        when {
            it.contains("ADMIN") -> 2L
            it.contains("SOPORTE") -> 3L
            it.contains("CLIENTE") -> 1L
            else -> null
        }
    }
    @Volatile var user: UserEntity? = null
    @Volatile var roleId: Long? = null
    @Volatile var roleName: String? = null
    @Volatile var lastName: String? = null

    //Limpia solo memoria
    fun clear() {
        user = null
        roleId = null
        roleName = null
        lastName = null
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
        roleId = (if (s.roleId > 0) s.roleId else deriveRoleIdFromName(s.roleName))
        roleName = s.roleName
        lastName = s.lastName
    }

    // Persiste la sesión actual en DataStore (llamar tras login OK)
    suspend fun persistToStore(context: Context) {
        val u = user ?: return
        SessionStore.save(
            context = context,
            email = u.email,
            rut = u.rut,
            name = u.name,
            lastName = lastName,
            address = u.address,
            phone = u.phone,
            birthDate = u.birthDate.ifBlank { null },
            roleId = roleId ?: 0L,
            roleName = roleName
        )
    }

    //Limpia memoria + DataStore (usar en "Cerrar sesión")
    suspend fun clearEverywhere(context: Context) {
        clear()
        SessionStore.clear(context)
    }
}




