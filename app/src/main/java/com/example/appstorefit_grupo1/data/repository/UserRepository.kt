package com.example.appstorefit_grupo1.data.repository

import androidx.room.withTransaction
import com.example.appstorefit_grupo1.data.local.database.AppDatabase
import com.example.appstorefit_grupo1.data.local.registro.RegistroDao
import com.example.appstorefit_grupo1.data.local.registro.RegistroEntity
import com.example.appstorefit_grupo1.data.local.user.UserDao
import com.example.appstorefit_grupo1.data.local.user.UserEntity
import com.example.appstorefit_grupo1.session.SessionManager

class UserRepository(
    private val db: AppDatabase,
    private val userDao: UserDao,
    private val registroDao: RegistroDao
) {
    // -------------------- AUTENTICACIÓN --------------------

    suspend fun login(email: String, pass: String): Result<UserEntity> {
        val emailNorm = email.trim().lowercase()
        val passNorm  = pass.trim()

        val reg = registroDao.getByUsuario(emailNorm)
            ?: return Result.failure(IllegalArgumentException("Usuario no encontrado"))

        if (reg.contrasenia != passNorm) {
            return Result.failure(IllegalArgumentException("Contraseña incorrecta"))
        }

        val user = userDao.getByRut(reg.rut)
            ?: return Result.failure(IllegalStateException("Perfil no encontrado"))

        // Mantén sesión simple en memoria
        SessionManager.user = user
        SessionManager.roleId = reg.rolId

        return Result.success(user)
    }

    suspend fun register(
        rut: String,
        name: String,
        email: String,
        phone: String,
        pass: String,
        address: String,
        rolId: Long = 1L
    ): Result<Long> {
        val emailNorm = email.trim().lowercase()
        val passNorm  = pass.trim()

        if (emailNorm.isBlank() || passNorm.isBlank() || name.isBlank() || rut.isBlank()) {
            return Result.failure(IllegalArgumentException("Completa los campos obligatorios"))
        }

        // Validaciones previas
        if (registroDao.getByUsuario(emailNorm) != null) {
            return Result.failure(IllegalArgumentException("Correo ya registrado"))
        }
        if (userDao.getByRut(rut) != null) {
            return Result.failure(IllegalArgumentException("RUT ya registrado"))
        }

        // Transacción: inserta usuario + registro atómicamente
        return runCatching {
            var regId: Long = -1L
            db.withTransaction {
                userDao.insertar(
                    UserEntity(
                        rut = rut,
                        name = name,
                        email = emailNorm,
                        phone = phone,
                        lastName = "",
                        address = address,
                        birthDate = ""
                    )
                )
                regId = registroDao.insertar(
                    RegistroEntity(
                        rolId = rolId,
                        usuario = emailNorm,
                        contrasenia = passNorm,
                        rut = rut,
                        address = address   // usa el campo consistente
                    )
                )
            }
            regId
        }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { e -> Result.failure(IllegalStateException("No se pudo registrar: ${e.message}")) }
        )
    }

    suspend fun getRegistroByUsuario(email: String): RegistroEntity? =
        registroDao.getByUsuario(email.trim().lowercase())

    // -------------------- CONTRASEÑA --------------------

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

        val rows = registroDao.updatePasswordByUsuario(emailNorm, newP)
        return if (rows > 0) Result.success(Unit)
        else Result.failure(IllegalStateException("No se pudo actualizar la contraseña"))
    }

    // -------------------- PERFIL --------------------

    suspend fun updateAddressByEmail(email: String, newAddress: String): Result<Unit> {
        val emailNorm = email.trim().lowercase()
        val user = userDao.getByEmail(emailNorm)
            ?: return Result.failure(IllegalStateException("Usuario no encontrado"))

        val updated = userDao.actualizar(user.copy(address = newAddress))
        return if (updated > 0) {
            // si el usuario de sesión es el mismo, refresca en memoria
            if (SessionManager.user?.rut == user.rut) {
                SessionManager.user = user.copy(address = newAddress)
            }
            Result.success(Unit)
        } else {
            Result.failure(IllegalStateException("No se pudo actualizar la dirección"))
        }
    }

    suspend fun getUserByEmail(email: String): UserEntity? =
        userDao.getByEmail(email.trim().lowercase())

    suspend fun refreshSessionUserByEmail(email: String): UserEntity? {
        val fresh = getUserByEmail(email)
        if (fresh != null) {
            SessionManager.user = fresh
        }
        return fresh
    }
}
