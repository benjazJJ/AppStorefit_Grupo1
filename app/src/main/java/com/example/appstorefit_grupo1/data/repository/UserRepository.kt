package com.example.appstorefit_grupo1.data.repository

import com.example.appstorefit_grupo1.data.local.registro.RegistroDao
import com.example.appstorefit_grupo1.data.local.registro.RegistroEntity
import com.example.appstorefit_grupo1.data.local.user.UserDao
import com.example.appstorefit_grupo1.data.local.user.UserEntity

class UserRepository(
    private val userDao: UserDao,
    private val registroDao: RegistroDao
) {
    // -------------------- AUTENTICACIÓN --------------------

    suspend fun login(email: String, pass: String): Result<UserEntity> {
        val emailNorm = email.trim().lowercase()   // <-- ahora siempre lowercase
        val passNorm  = pass.trim()

        val reg = registroDao.getByUsuario(emailNorm)
        return if (reg != null && reg.contrasenia == passNorm) {
            val user = userDao.getByRut(reg.rut)
            if (user != null) Result.success(user)
            else Result.failure(IllegalStateException("Perfil no encontrado"))
        } else {
            Result.failure(IllegalArgumentException("Datos inválidos"))
        }
    }

    suspend fun register(
        rut: String,
        name: String,
        email: String,
        phone: String,
        pass: String,
        address: String,
        photoUri: String?,
        rolId: Long = 1L
    ): Result<Long> {
        val emailNorm = email.trim().lowercase()   // guardamos consistente
        if (registroDao.getByUsuario(emailNorm) != null) {
            return Result.failure(IllegalArgumentException("Correo en uso"))
        }
        if (userDao.getByRut(rut) != null) {
            return Result.failure(IllegalArgumentException("RUT ya registrado"))
        }

        userDao.insertar(
            UserEntity(
                rut = rut,
                name = name,
                email = emailNorm,
                phone = phone,
                lastName = "",
                address = address,
                birthDate = "",
                photoUri = photoUri
            )
        )

        val regId = registroDao.insertar(
            RegistroEntity(
                rolId = rolId,
                usuario = emailNorm,
                contrasenia = pass.trim(),
                rut = rut
            )
        )
        return Result.success(regId)
    }

    suspend fun getRegistroByUsuario(email: String): RegistroEntity? =
        registroDao.getByUsuario(email.trim().lowercase())

    // -------------------- CONTRASEÑA --------------------


    //Cambia la contraseña del usuario.
    //Valida: no vacíos, antigua coincide, nueva >= 6, nueva != antigua, nueva == confirmar.
    //Devuelve Result.success(Unit) si se actualizó 1+ filas; failure con mensaje si no.

    suspend fun changePassword(
        email: String,
        oldPass: String,
        newPass: String,
        confirmPass: String
    ): Result<Unit> {
        val emailNorm = email.trim().lowercase()
        val oldP = oldPass.trim()
        val newP = newPass.trim()
        val conf = confirmPass.trim()

        if (emailNorm.isBlank() || oldP.isBlank() || newP.isBlank() || conf.isBlank()) {
            return Result.failure(IllegalArgumentException("Completa todos los campos"))
        }
        val reg = registroDao.getByUsuario(emailNorm)
            ?: return Result.failure(IllegalStateException("Usuario no encontrado"))

        if (reg.contrasenia != oldP) {
            return Result.failure(IllegalArgumentException("La contraseña antigua no coincide"))
        }
        if (newP.length < 6) {
            return Result.failure(IllegalArgumentException("La nueva contraseña debe tener al menos 6 caracteres"))
        }
        if (newP == oldP) {
            return Result.failure(IllegalArgumentException("La nueva contraseña no puede ser igual a la antigua"))
        }
        if (newP != conf) {
            return Result.failure(IllegalArgumentException("Las contraseñas no coinciden"))
        }

        val rows = updatePasswordByUsuario(emailNorm, newP)
        return if (rows > 0) Result.success(Unit)
        else Result.failure(IllegalStateException("No se pudo actualizar la contraseña"))
    }

    //Actualiza la contraseña en la tabla Registro por 'usuario' (email)
    private suspend fun updatePasswordByUsuario(email: String, newPass: String): Int {
        return registroDao.updatePasswordByUsuario(email, newPass)
    }

    //FUNCIÓN PARA ACTUALIZAR LA DIRECCIÓN DEL USUARIO SI ES NECESARIO
    suspend fun updateAddressByEmail(email: String, newAddress: String): Result<Unit> {
        val emailNorm = email.trim().lowercase()
        val user = userDao.getByEmail(emailNorm)
            ?: return Result.failure(IllegalStateException("Usuario no encontrado"))

        val updated = userDao.actualizar(user.copy(address = newAddress))
        return if (updated > 0) Result.success(Unit)
        else Result.failure(IllegalStateException("No se pudo actualizar la dirección"))
    }


    suspend fun getUserByEmail(email: String): UserEntity? =
        userDao.getByEmail(email.trim().lowercase())


    suspend fun refreshSessionUserByEmail(email: String): UserEntity? {
        val fresh = getUserByEmail(email)
        if (fresh != null) {
            // sincroniza la sesión en memoria
            com.example.appstorefit_grupo1.session.SessionManager.user = fresh
        }
        return fresh
    }

    suspend fun updatePhotoByEmail(email: String, uri: String): Result<Unit> {
        val rows = userDao.updatePhotoByEmail(email.trim().lowercase(), uri)
        return if (rows > 0) Result.success(Unit)
        else Result.failure(IllegalStateException("No se pudo guardar la foto"))
    }

}
