package com.example.appstorefit_grupo1.data.repository

import androidx.room.withTransaction
import com.example.appstorefit_grupo1.data.local.database.AppDatabase
import com.example.appstorefit_grupo1.data.local.registro.RegistroDao
import com.example.appstorefit_grupo1.data.local.registro.RegistroEntity
import com.example.appstorefit_grupo1.data.local.user.UserDao
import com.example.appstorefit_grupo1.data.local.user.UserEntity
import com.example.appstorefit_grupo1.domain.validation.emailCanonico
import com.example.appstorefit_grupo1.session.SessionManager

class UserRepository(
    private val db: AppDatabase,
    private val userDao: UserDao,
    private val registroDao: RegistroDao
) {

    // ================== INICIO DE SESIÓN ==================
    suspend fun login(email: String, pass: String): Result<UserEntity> {
        val correoCanonico = emailCanonico(email)
        val passIngresada  = pass

        val registro = registroDao.getByUsuario(correoCanonico)
            ?: return Result.failure(IllegalArgumentException("Usuario no encontrado"))

        if (registro.contrasenia != passIngresada) {
            return Result.failure(IllegalArgumentException("Contraseña incorrecta"))
        }

        val user = userDao.getByRut(registro.rut)
            ?: return Result.failure(IllegalStateException("Perfil no encontrado"))

        // Sesión en memoria
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
        val passIngresada  = pass
        val phoneCanon     = phoneCanonico(phone)

        if (correoCanonico.isBlank() || passIngresada.isBlank() || name.isBlank() || rut.isBlank()) {
            return Result.failure(IllegalArgumentException("Completa los campos obligatorios"))
        }

        // Pre-chequeos
        val yaExisteRut = userDao.getByRut(rut) != null
        if (yaExisteRut) return Result.failure(IllegalArgumentException("RUT ya registrado"))

        val existeEnRegistro = registroDao.getByUsuario(correoCanonico) != null
        if (existeEnRegistro) return Result.failure(IllegalArgumentException("Correo ya registrado"))

        val existeEnUsuarios = userDao.existsEmail(correoCanonico) > 0
        if (existeEnUsuarios) return Result.failure(IllegalArgumentException("Correo ya registrado"))

        if (phoneCanon.isNotBlank()) {
            val yaExisteTelefono = isPhoneTaken(phoneCanon)
            if (yaExisteTelefono) {
                return Result.failure(IllegalArgumentException("Este teléfono ya pertenece a otro usuario."))
            }
        }

        // Insertar usuario + registro con el MISMO correo canónico
        return runCatching {
            var idRegistroCreado: Long = -1L
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
                        contrasenia = passIngresada,
                        rut = rut,
                        address = address
                    )
                )
            }
            idRegistroCreado
        }.fold(
            onSuccess = { id -> Result.success(id) },
            onFailure = { error ->
                val mensajeParaUsuario =
                    if (error is android.database.sqlite.SQLiteConstraintException) {
                        val detalle = error.message.orEmpty().lowercase()
                        when {
                            "usuarios.correo_electronico" in detalle || "index_usuarios_correo_electronico" in detalle ->
                                "Correo ya registrado"
                            "usuarios.telefono" in detalle || "index_usuarios_telefono" in detalle ->
                                "Este teléfono ya pertenece a otro usuario."
                            "usuarios.rut" in detalle || "index_usuarios_rut" in detalle || "primary key" in detalle ->
                                "RUT ya registrado"
                            else -> "No se pudo registrar por una restricción de unicidad."
                        }
                    } else {
                        "No se pudo registrar: ${error.message}"
                    }
                Result.failure(IllegalStateException(mensajeParaUsuario))
            }
        )
    }

    // ================== CHEQUEOS DE UNICIDAD ==================
    suspend fun isEmailTaken(email: String): Boolean {
        val canon = emailCanonico(email)
        if (canon.isBlank()) return false
        val existeEnUsuarios = userDao.existsEmail(canon) > 0
        val existeEnRegistro = registroDao.getByUsuario(canon) != null
        return existeEnUsuarios || existeEnRegistro
    }

    suspend fun isRutTaken(rut: String): Boolean {
        if (rut.isBlank()) return false
        return userDao.getByRut(rut) != null
    }

    private fun phoneCanonico(raw: String): String =
        raw.filter { it.isDigit() }
    suspend fun isPhoneTaken(phone: String): Boolean {
        val p = phoneCanonico(phone)
        if (p.isBlank()) return false
        return userDao.existsPhone(p) > 0
    }

    suspend fun getRegistroByUsuario(email: String): com.example.appstorefit_grupo1.data.local.registro.RegistroEntity? =
        registroDao.getByUsuario(emailCanonico(email))

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
        if (fresh != null) {
            SessionManager.user = fresh
        }
        return fresh
    }

    //FOTO DE PERFIL

    /** Guarda/actualiza la foto de perfil (URI en texto). */
    suspend fun saveUserPhoto(email: String, uri: String): Result<Unit> {
        val canon = emailCanonico(email)
        if (canon.isBlank() || uri.isBlank()) {
            return Result.failure(IllegalArgumentException("Datos inválidos"))
        }
        val rows = userDao.updatePhotoByEmail(canon, uri)
        return if (rows > 0) {
            // refresca sesión si corresponde
            SessionManager.user?.let { current ->
                if (current.email == canon) {
                    SessionManager.user = current.copy(photoUri = uri)
                }
            }
            Result.success(Unit)
        } else {
            Result.failure(IllegalStateException("No se pudo guardar la foto de perfil"))
        }
    }

    /** Borra la foto de perfil (deja NULL). */
    suspend fun clearUserPhoto(email: String): Result<Unit> {
        val canon = emailCanonico(email)
        if (canon.isBlank()) {
            return Result.failure(IllegalArgumentException("Email inválido"))
        }
        val rows = userDao.updatePhotoByEmail(canon, null)
        return if (rows > 0) {
            SessionManager.user?.let { current ->
                if (current.email == canon) {
                    SessionManager.user = current.copy(photoUri = null)
                }
            }
            Result.success(Unit)
        } else {
            Result.failure(IllegalStateException("No se pudo eliminar la foto de perfil"))
        }
    }

    /** Devuelve la URI (String) de la foto de perfil, o null si no hay. */
    suspend fun getUserPhoto(email: String): String? =
        userDao.getPhotoUriByEmail(emailCanonico(email))
}
