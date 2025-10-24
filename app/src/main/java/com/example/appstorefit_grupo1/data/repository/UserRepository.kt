package com.example.appstorefit_grupo1.data.repository

import androidx.room.withTransaction
import com.example.appstorefit_grupo1.data.local.database.AppDatabase
import com.example.appstorefit_grupo1.data.local.registro.RegistroDao
import com.example.appstorefit_grupo1.data.local.registro.RegistroEntity
import com.example.appstorefit_grupo1.data.local.user.UserDao
import com.example.appstorefit_grupo1.data.local.user.UserEntity
import com.example.appstorefit_grupo1.data.local.user.AdminUserRow
import com.example.appstorefit_grupo1.domain.validation.emailCanonico
import com.example.appstorefit_grupo1.session.SessionManager

class UserRepository(
    private val db: AppDatabase,
    private val userDao: UserDao,
    private val registroDao: RegistroDao,
    private val rolDao: com.example.appstorefit_grupo1.data.local.rol.RolDao
) {

    // ================== INICIO DE SESIÓN ==================
    suspend fun login(email: String, pass: String): Result<UserEntity> {
        val correoCanonico = emailCanonico(email)
        val registro = registroDao.getByUsuario(correoCanonico)
            ?: return Result.failure(IllegalArgumentException("Usuario no encontrado"))

        if (registro.contrasenia != pass) {
            return Result.failure(IllegalArgumentException("Contraseña incorrecta"))
        }

        val user = userDao.getByRut(registro.rut)
            ?: return Result.failure(IllegalStateException("Perfil no encontrado"))

        SessionManager.user = user
        SessionManager.roleId = registro.rolId
        return Result.success(user)
    }

    // ================== REGISTRO ==================
    suspend fun register(
        rut: String,
        name: String,
        email: String,
        phone: String,
        pass: String,
        address: String,
        rolId: Long = 1L,
        birthDate: String
    ): Result<Long> {
        val correoCanonico = emailCanonico(email)
        val phoneCanon = phone.filter { it.isDigit() }

        if (correoCanonico.isBlank() || pass.isBlank() || name.isBlank() || rut.isBlank()) {
            return Result.failure(IllegalArgumentException("Completa los campos obligatorios"))
        }

        if (userDao.getByRut(rut) != null) return Result.failure(IllegalArgumentException("RUT ya registrado"))
        if (registroDao.getByUsuario(correoCanonico) != null) return Result.failure(IllegalArgumentException("Correo ya registrado"))
        if (userDao.existsEmail(correoCanonico) > 0) return Result.failure(IllegalArgumentException("Correo ya registrado"))
        if (phoneCanon.isNotBlank() && userDao.existsPhone(phoneCanon) > 0) {
            return Result.failure(IllegalArgumentException("Este teléfono ya pertenece a otro usuario."))
        }

        return runCatching {
            var idRegistroCreado = -1L
            db.withTransaction {
                userDao.insertar(
                    UserEntity(
                        rut = rut,
                        name = name,
                        email = correoCanonico,
                        phone = if (phoneCanon.isBlank()) null else phoneCanon,
                        address = address,
                        birthDate = birthDate
                    )
                )
                idRegistroCreado = registroDao.insertar(
                    RegistroEntity(
                        rolId = rolId,
                        usuario = correoCanonico,
                        contrasenia = pass,
                        rut = rut,
                        address = address
                    )
                )
            }
            idRegistroCreado
        }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { error ->
                val msg =
                    if (error is android.database.sqlite.SQLiteConstraintException) {
                        val d = error.message.orEmpty().lowercase()
                        when {
                            "usuarios.correo_electronico" in d || "index_usuarios_correo_electronico" in d -> "Correo ya registrado"
                            "usuarios.telefono" in d || "index_usuarios_telefono" in d -> "Este teléfono ya pertenece a otro usuario."
                            "usuarios.rut" in d || "index_usuarios_rut" in d || "primary key" in d -> "RUT ya registrado"
                            else -> "No se pudo registrar por una restricción de unicidad."
                        }
                    } else {
                        "No se pudo registrar: ${error.message}"
                    }
                Result.failure(IllegalStateException(msg))
            }
        )
    }

    // ================== CHEQUEOS DE UNICIDAD ==================
    suspend fun isEmailTaken(email: String): Boolean {
        val canon = emailCanonico(email)
        if (canon.isBlank()) return false
        return userDao.existsEmail(canon) > 0 || registroDao.getByUsuario(canon) != null
    }

    suspend fun isRutTaken(rut: String): Boolean =
        rut.isNotBlank() && userDao.getByRut(rut) != null

    private fun phoneCanonico(raw: String) = raw.filter { it.isDigit() }

    suspend fun isPhoneTaken(phone: String): Boolean {
        val p = phoneCanonico(phone)
        if (p.isBlank()) return false
        return userDao.existsPhone(p) > 0
    }

    // ================== CONTRASEÑA ==================
    suspend fun changePassword(
        email: String,
        oldPass: String,
        newPass: String,
        confirmPass: String
    ): Result<Unit> {
        val correoCanonico = emailCanonico(email)
        val oldP = oldPass.trim()
        val newP = newPass.trim()
        val conf = confirmPass.trim()

        if (correoCanonico.isBlank() || oldP.isBlank() || newP.isBlank() || conf.isBlank()) {
            return Result.failure(IllegalArgumentException("Completa todos los campos"))
        }

        val reg = registroDao.getByUsuario(correoCanonico)
            ?: return Result.failure(IllegalStateException("Usuario no encontrado"))

        if (reg.contrasenia != oldP) return Result.failure(IllegalArgumentException("La contraseña antigua no coincide"))
        if (newP.length < 6) return Result.failure(IllegalArgumentException("La nueva contraseña debe tener al menos 6 caracteres"))
        if (newP == oldP) return Result.failure(IllegalArgumentException("La nueva contraseña no puede ser igual a la antigua"))
        if (newP != conf) return Result.failure(IllegalArgumentException("Las contraseñas no coinciden"))

        val rows = registroDao.updatePasswordByUsuario(correoCanonico, newP)
        return if (rows > 0) Result.success(Unit)
        else Result.failure(IllegalStateException("No se pudo actualizar la contraseña"))
    }

    // ================== PERFIL ==================
    suspend fun updateAddressByEmail(email: String, newAddress: String): Result<Unit> {
        val correoCanonico = emailCanonico(email)
        val user = userDao.getByEmail(correoCanonico)
            ?: return Result.failure(IllegalStateException("Usuario no encontrado"))

        val updated = userDao.actualizar(user.copy(address = newAddress))
        return if (updated > 0) {
            if (SessionManager.user?.rut == user.rut) {
                SessionManager.user = user.copy(address = newAddress)
            }
            Result.success(Unit)
        } else {
            Result.failure(IllegalStateException("No se pudo actualizar la dirección"))
        }
    }

    suspend fun getUserByEmail(email: String): UserEntity? =
        userDao.getByEmail(emailCanonico(email))

    suspend fun refreshSessionUserByEmail(email: String): UserEntity? {
        val fresh = getUserByEmail(email)
        if (fresh != null) SessionManager.user = fresh
        return fresh
    }

    //FOTO DE PERFIL
    suspend fun saveUserPhoto(email: String, uri: String): Result<Unit> {
        val canon = emailCanonico(email)
        if (canon.isBlank() || uri.isBlank()) return Result.failure(IllegalArgumentException("Datos inválidos"))
        val rows = userDao.updatePhotoByEmail(canon, uri)
        return if (rows > 0) {
            SessionManager.user?.let { if (it.email == canon) SessionManager.user = it.copy(photoUri = uri) }
            Result.success(Unit)
        } else Result.failure(IllegalStateException("No se pudo guardar la foto de perfil"))
    }

    suspend fun clearUserPhoto(email: String): Result<Unit> {
        val canon = emailCanonico(email)
        if (canon.isBlank()) return Result.failure(IllegalArgumentException("Email inválido"))
        val rows = userDao.updatePhotoByEmail(canon, null)
        return if (rows > 0) {
            SessionManager.user?.let { if (it.email == canon) SessionManager.user = it.copy(photoUri = null) }
            Result.success(Unit)
        } else Result.failure(IllegalStateException("No se pudo eliminar la foto de perfil"))
    }

    suspend fun getUserPhoto(email: String): String? =
        userDao.getPhotoUriByEmail(emailCanonico(email))

    // ADMIN – Usuarios
    suspend fun adminListUsers(): Result<List<AdminUserRow>> = runCatching {
        userDao.adminListUsers()
    }

    suspend fun adminDeleteUserByRut(rut: String): Result<Unit> = runCatching {
        db.withTransaction {
            registroDao.deleteByRut(rut)
            userDao.deleteByRut(rut)
        }
    }

    suspend fun adminCreateUser(
        user: UserEntity,
        password: String,
        roleId: Long
    ): Result<Unit> = runCatching {
        val correoCanon = emailCanonico(user.email)
        val phoneCanon  = user.phone?.filter { it.isDigit() }.orEmpty()

        if (userDao.getByRut(user.rut) != null) error("RUT ya registrado")
        if (userDao.existsEmail(correoCanon) > 0) error("Correo ya registrado")
        if (phoneCanon.isNotBlank() && userDao.existsPhone(phoneCanon) > 0) error("Este teléfono ya pertenece a otro usuario.")

        db.withTransaction {
            userDao.insertar(user.copy(email = correoCanon, phone = if (phoneCanon.isBlank()) null else phoneCanon))
            registroDao.insertar(
                RegistroEntity(
                    rolId = roleId,
                    usuario = correoCanon,
                    contrasenia = password,
                    rut = user.rut,
                    address = user.address
                )
            )
        }
    }

    suspend fun adminUpdateUser(user: UserEntity): Result<Unit> = runCatching {
        val rows = userDao.actualizar(user)
        if (rows <= 0) error("No se pudo actualizar")
    }

    suspend fun adminGetUserByRut(rut: String) = runCatching {
        userDao.getByRut(rut) ?: error("Usuario no encontrado")
    }

    //DMIN – Roles
    suspend fun adminListRoles() = runCatching {
        rolDao.adminListRoles()
    }

    //Asignar rol
    suspend fun adminAssignRoleToUser(rut: String, roleId: Long) = runCatching {
        val rows = registroDao.updateRoleByRut(rut, roleId)
        require(rows > 0) { "No se pudo actualizar el rol" }
    }
}
