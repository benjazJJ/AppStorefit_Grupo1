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

    //INICIO DE SESIÓN
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

        // Mantén sesión simple en memoria
        SessionManager.user = user
        SessionManager.roleId = registro.rolId

        return Result.success(user)
    }

    //REGISTRO
    suspend fun register(
        rut: String,
        name: String,
        email: String,
        phone: String,
        pass: String,
        address: String,
        rolId: Long = 1L
    ): Result<Long> {
        val correoCanonico = emailCanonico(email)
        val passIngresada  = pass

        if (correoCanonico.isBlank() || passIngresada.isBlank() || name.isBlank() || rut.isBlank()) {
            return Result.failure(IllegalArgumentException("Completa los campos obligatorios"))
        }

        // Pre-chequeos RUT y CORREO y TELEFONO
        val yaExisteRut = userDao.getByRut(rut) != null
        if (yaExisteRut) return Result.failure(IllegalArgumentException("RUT ya registrado"))

        val existeEnRegistro = registroDao.getByUsuario(correoCanonico) != null
        if (existeEnRegistro) return Result.failure(IllegalArgumentException("Correo ya registrado"))

        val existeEnUsuarios = userDao.existsEmail(correoCanonico) > 0
        if (existeEnUsuarios) return Result.failure(IllegalArgumentException("Correo ya registrado"))

        if (phone.isNotBlank()) {
            val yaExisteTelefono = isPhoneTaken(phone)
            if (yaExisteTelefono) {
                return Result.failure(IllegalArgumentException("Este teléfono ya pertenece a otro usuario."))
            }
        }


        // insertar usuario + registro con el MISMO correo canónico
        return runCatching {
            var idRegistroCreado: Long = -1L
            db.withTransaction {
                userDao.insertar(
                    UserEntity(
                        rut = rut,
                        name = name,
                        email = correoCanonico,
                        phone = if (phone.isBlank()) null else phone,
                        lastName = "",
                        address = address,
                        birthDate = ""
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
                        val detalle = error.message.orEmpty()
                        val d = detalle.lowercase()

                        when {
                            // correo único
                            "usuarios.correo_electronico" in d || "index_usuarios_correo_electronico" in d ->
                                "Correo ya registrado"

                            // teléfono único
                            "usuarios.telefono" in d || "index_usuarios_telefono" in d ->
                                "Este teléfono ya pertenece a otro usuario."

                            // rut único
                            "usuarios.rut" in d || "index_usuarios_rut" in d || "primary key" in d ->
                                "RUT ya registrado"

                            else ->
                                "No se pudo registrar por una restricción de unicidad."
                        }
                    } else {
                        "No se pudo registrar: ${error.message}"
                    }
                Result.failure(IllegalStateException(mensajeParaUsuario))
            }
        )
    }

    //CHEQUEOS DE UNICIDAD EN TIEMPO REAL
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

    suspend fun isPhoneTaken(phone: String): Boolean {
        if (phone.isBlank()) return false
        return userDao.existsPhone(phone) > 0
    }

    suspend fun getRegistroByUsuario(email: String): RegistroEntity? =
        registroDao.getByUsuario(emailCanonico(email))

    //CONTRASEÑA
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

    //PERFIL
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
}