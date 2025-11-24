package com.example.appstorefit_grupo1.data.repository

import com.example.appstorefit_grupo1.data.local.rol.AdminRoleRow
import com.example.appstorefit_grupo1.data.local.user.AdminUserRow
import com.example.appstorefit_grupo1.data.local.user.UserEntity
import com.example.appstorefit_grupo1.data.remote.dto.users.AdminActualizarUsuarioRequest
import com.example.appstorefit_grupo1.data.remote.dto.users.AdminCrearUsuarioRequest
import com.example.appstorefit_grupo1.data.remote.dto.users.ChangePasswordRequest
import com.example.appstorefit_grupo1.data.remote.dto.users.LoginRequest
import com.example.appstorefit_grupo1.data.remote.dto.users.UpdateFotoRequest
import com.example.appstorefit_grupo1.data.remote.dto.users.UpdatePerfilRequest
import com.example.appstorefit_grupo1.data.remote.dto.users.UpdateRolRequest
import com.example.appstorefit_grupo1.data.remote.dto.users.UsuarioConRolDto
import com.example.appstorefit_grupo1.data.remote.dto.users.UsuarioDto
import com.example.appstorefit_grupo1.data.remote.users.UsersApi
import com.example.appstorefit_grupo1.domain.validation.emailCanonico
import com.example.appstorefit_grupo1.session.SessionManager
import retrofit2.HttpException

// Repositorio remoto (sin Room) que consume users-service.
// Mantiene nombre y apellido por separado; la app solo muestra nombre en varios lugares.

class UserRepository(
    private val api: UsersApi
) {
    // Helpers
    private fun phoneCanonico(raw: String?): String =
        raw?.filter { it.isDigit() }.orEmpty()

    // Normaliza RUT con puntos si trae guion; de lo contrario devuelve el original
    private fun dottedRut(rut: String): String {
        if (rut.contains('.')) return rut
        val cleaned = rut.replace(" ", "")
        val hyphen = cleaned.lastIndexOf('-')
        if (hyphen <= 0 || hyphen >= cleaned.length - 1) return rut
        val body = cleaned.substring(0, hyphen)
        val dv = cleaned.substring(hyphen + 1)
        val sb = StringBuilder()
        val len = body.length
        var firstGroup = len % 3
        if (firstGroup == 0) firstGroup = 3
        sb.append(body.substring(0, firstGroup))
        var i = firstGroup
        while (i < len) {
            sb.append('.').append(body.substring(i, i + 3))
            i += 3
        }
        sb.append('-').append(dv)
        return sb.toString()
    }

    private fun deriveRoleIdFromName(name: String?): Long? = name?.uppercase()?.let { role ->
        when {
            role.contains("ADMIN") -> 2L
            role.contains("SOPORTE") -> 3L
            role.contains("CLIENTE") -> 1L
            else -> null
        }
    }

    private fun deriveRoleNameFromId(id: Long?): String? = when (id) {
        1L -> "CLIENTE"
        2L -> "ADMIN"
        3L -> "SOPORTE"
        else -> null
    }

    private fun headersOrThrow(): Pair<String, String> {
        val rut = SessionManager.user?.rut ?: error("Sesion no disponible")
        val roleName = SessionManager.roleName?.uppercase()
            ?: deriveRoleNameFromId(SessionManager.roleId)
            ?: deriveRoleNameFromId(deriveRoleIdFromName(SessionManager.roleName))
        val headerRol = roleName ?: "0"
        return rut to headerRol
    }

    private fun UsuarioDto.toUserEntity(): UserEntity =
        UserEntity(
            rut = rut,
            name = nombre.trim(), // solo nombre; apellidos se guardan en SessionManager.lastName
            email = emailCanonico(correo),
            phone = telefono,
            address = direccion,
            birthDate = fechaNacimiento,
            photoUri = fotoUri
        )

    private fun UsuarioConRolDto.toUserEntity(): UserEntity =
        UserEntity(
            rut = rut,
            name = nombre.trim(),
            email = emailCanonico(correo),
            phone = telefono,
            address = direccion,
            birthDate = fechaNacimiento,
            photoUri = fotoUri
        )

    private fun UsuarioConRolDto.toAdminUserRow(): AdminUserRow =
        AdminUserRow(
            rut = rut,
            name = listOf(nombre, apellidos).joinToString(" ").trim(),
            email = emailCanonico(correo),
            phone = telefono,
            address = direccion,
            roleId = rolId,
            roleName = rolNombre
        )

    // LOGIN
    suspend fun login(email: String, pass: String): Result<UserEntity> {
        val correoCanonico = emailCanonico(email)
        if (correoCanonico.isBlank() || pass.isBlank()) {
            return Result.failure(IllegalArgumentException("Completa correo y contrasena"))
        }
        return try {
            val resp = api.login(LoginRequest(correo = correoCanonico, contrasenia = pass))
            if (!resp.success || resp.rut.isNullOrBlank()) {
                throw IllegalArgumentException(resp.message ?: "Usuario o contrasena incorrectos")
            }
            val rut = resp.rut
            val roleName = resp.rolNombre?.uppercase() ?: deriveRoleNameFromId(resp.rolId)
            val roleId = resp.rolId ?: deriveRoleIdFromName(roleName)
            val headerRol = roleName ?: "0"
            val dto = api.getUsuarioPorRut(rut, headerRut = rut, headerRol = headerRol)
            val userEntity = dto.toUserEntity()
            SessionManager.user = userEntity
            SessionManager.roleId = roleId ?: resp.rolId
            SessionManager.roleName = roleName
            SessionManager.lastName = dto.apellidos
            Result.success(userEntity)
        } catch (e: HttpException) {
            val msg = when (e.code()) {
                400, 401 -> "Contrasena invalida"
                404 -> "Correo invalido"
                else -> "No se pudo iniciar sesion. Intenta nuevamente."
            }
            Result.failure(IllegalArgumentException(msg ?: "HTTP ${e.code()}"))
        } catch (e: Exception) {
            Result.failure(IllegalStateException("No se pudo iniciar sesion: ${e.message}"))
        }
    }

    // REGISTRO
    suspend fun register(
        rut: String,
        name: String,
        lastName: String,
        email: String,
        phone: String,
        pass: String,
        address: String,
        birthDate: String
    ): Result<Long> {
        val correoCanonico = emailCanonico(email)
        val phoneCanon = phoneCanonico(phone)
        if (correoCanonico.isBlank() || pass.isBlank() || name.isBlank() || lastName.isBlank() || rut.isBlank()) {
            return Result.failure(IllegalArgumentException("Completa los campos obligatorios"))
        }
        if (isRutTaken(rut)) return Result.failure(IllegalArgumentException("RUT ya registrado"))
        if (isEmailTaken(correoCanonico)) return Result.failure(IllegalArgumentException("Correo ya registrado"))
        if (phoneCanon.isNotBlank() && isPhoneTaken(phoneCanon)) {
            return Result.failure(IllegalArgumentException("Este telefono ya pertenece a otro usuario."))
        }
        return try {
            val req = com.example.appstorefit_grupo1.data.remote.dto.users.RegistroCompletoRequest(
                rut = rut,
                nombre = name,
                apellidos = lastName,
                correo = correoCanonico,
                fechaNacimiento = birthDate,
                contrasenia = pass,
                confirmarContrasenia = pass,
                direccion = address,
                telefono = phoneCanon
            )
            val resp = api.registroCompleto(req)
            if (!resp.success) Result.failure(IllegalStateException(resp.usuario ?: "No se pudo registrar"))
            else Result.success(1L)
        } catch (e: Exception) {
            Result.failure(IllegalStateException("No se pudo registrar: ${e.message}"))
        }
    }

    // CHECKS
    suspend fun isEmailTaken(email: String): Boolean =
        emailCanonico(email).takeIf { it.isNotBlank() }?.let {
            try { !api.checkCorreo(it).available } catch (_: Exception) { false }
        } ?: false

    suspend fun isRutTaken(rut: String): Boolean =
        if (rut.isBlank()) false else try { !api.checkRut(rut).available } catch (_: Exception) { false }

    suspend fun isPhoneTaken(phone: String): Boolean {
        val p = phoneCanonico(phone)
        if (p.isBlank()) return false
        return try { !api.checkTelefono(p).available } catch (_: Exception) { false }
    }

    suspend fun emailDisponible(nuevoEmail: String, emailActual: String): Boolean {
        val canonNuevo = emailCanonico(nuevoEmail)
        val canonActual = emailCanonico(emailActual)
        if (canonNuevo.isBlank()) return true
        if (canonNuevo == canonActual) return true
        return try { api.checkCorreo(canonNuevo).available } catch (_: Exception) { true }
    }

    suspend fun telefonoDisponible(telefono: String, rut: String): Boolean {
        val p = phoneCanonico(telefono)
        if (p.isBlank() || rut.isBlank()) return true
        return try {
            val (hRut, hRol) = headersOrThrow()
            api.checkTelefonoActualizar(rut = rut, telefono = p, headerRut = hRut, headerRol = hRol).available
        } catch (_: Exception) { true }
    }

    // CONTRASENA
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
        if (newP.length < 6) return Result.failure(IllegalArgumentException("La nueva contrasena debe tener al menos 6 caracteres"))
        if (newP == oldP) return Result.failure(IllegalArgumentException("La nueva contrasena no puede ser igual a la antigua"))
        if (newP != conf) return Result.failure(IllegalArgumentException("Las contrasenas no coinciden"))

        return try {
            val req = ChangePasswordRequest(
                usuarioOCorreo = correoCanonico,
                contraseniaActual = oldP,
                nuevaContrasenia = newP,
                confirmarContrasenia = conf
            )
            val resp = api.cambiarContrasenia(req)
            if (!resp.success) Result.failure(IllegalStateException(resp.message ?: "No se pudo actualizar la contrasena"))
            else Result.success(Unit)
        } catch (e: HttpException) {
            val cuerpo = e.response()?.errorBody()?.string()?.take(200)
            val msg = when {
                e.code() == 400 || e.code() == 401 -> "La contrasena actual no es correcta."
                else -> cuerpo?.ifBlank { null } ?: e.message()
            }
            Result.failure(IllegalArgumentException(msg ?: "Error al cambiar contrasena"))
        } catch (e: Exception) {
            Result.failure(IllegalStateException("Error al cambiar contrasena: ${e.message}"))
        }
    }

    // PERFIL
    suspend fun getUserByEmail(email: String): UserEntity? {
        val canon = emailCanonico(email)
        if (canon.isBlank()) return null
        return try {
            val (hRut, hRol) = headersOrThrow()
            val dto = api.getUsuarioPorCorreo(canon, headerRut = hRut, headerRol = hRol)
            SessionManager.lastName = dto.apellidos
            dto.toUserEntity()
        } catch (_: Exception) { null }
    }

    suspend fun obtenerUsuarioPorRut(rut: String): UserEntity? {
        if (rut.isBlank()) return null
        return try {
            val (hRut, hRol) = headersOrThrow()
            val dto = api.getUsuarioPorRut(rut, headerRut = hRut, headerRol = hRol)
            SessionManager.lastName = dto.apellidos
            dto.toUserEntity()
        } catch (_: Exception) { null }
    }

    suspend fun refreshSessionUserByEmail(email: String): UserEntity? {
        val fresh = getUserByEmail(email)
        if (fresh != null) SessionManager.user = fresh
        return fresh
    }

    suspend fun updateAddressByEmail(email: String, newAddress: String): Result<Unit> {
        val canon = emailCanonico(email)
        if (canon.isBlank()) return Result.failure(IllegalArgumentException("Email inválido"))
        return try {
            val (hRut, hRol) = headersOrThrow()
            val dto = api.getUsuarioPorCorreo(canon, headerRut = hRut, headerRol = hRol)
            val req = UpdatePerfilRequest(
                nombre = dto.nombre,
                apellidos = dto.apellidos,
                correo = dto.correo,
                telefono = dto.telefono,
                direccion = newAddress,
                fechaNacimiento = dto.fechaNacimiento,
                fotoUri = dto.fotoUri
            )
            val updated = api.actualizarPerfil(dto.rut, req, headerRut = hRut, headerRol = hRol)
            if (SessionManager.user?.rut == updated.rut) {
                SessionManager.user = updated.toUserEntity()
                SessionManager.lastName = updated.apellidos
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(IllegalStateException("No se pudo actualizar la dirección: ${e.message}"))
        }
    }

    suspend fun actualizarPerfil(
        rut: String,
        nombre: String,
        apellido: String,
        telefono: String?,
        direccion: String,
        fechaNacimiento: String,
        emailNuevo: String? = null
    ): Result<Unit> = runCatching {
        if (rut.isBlank()) error("RUT inválido")
        val telefonoCanon = phoneCanonico(telefono)
        val emailCanon = emailNuevo?.let { emailCanonico(it) }
        val actual = SessionManager.user ?: error("Usuario no encontrado en sesión")
        val correoFinal = emailCanon ?: actual.email

        val (hRut, hRol) = headersOrThrow()
        val req = UpdatePerfilRequest(
            nombre = nombre.trim(),
            apellidos = apellido.trim(),
            correo = correoFinal,
            telefono = if (telefonoCanon.isBlank()) null else telefonoCanon,
            direccion = direccion.trim(),
            fechaNacimiento = fechaNacimiento.trim(),
            fotoUri = actual.photoUri
        )
        val updated = api.actualizarPerfil(rut, req, headerRut = hRut, headerRol = hRol)
        if (SessionManager.user?.rut == rut) {
            SessionManager.user = updated.toUserEntity()
            SessionManager.lastName = updated.apellidos
        }
    }

    // FOTO
    suspend fun saveUserPhoto(email: String, uri: String): Result<Unit> {
        val canon = emailCanonico(email)
        if (canon.isBlank() || uri.isBlank()) return Result.failure(IllegalArgumentException("Datos inválidos"))
        return try {
            val (hRut, hRol) = headersOrThrow()
            val dto = api.getUsuarioPorCorreo(canon, headerRut = hRut, headerRol = hRol)
            val updated = api.actualizarFoto(dto.rut, UpdateFotoRequest(fotoUri = uri), headerRut = hRut, headerRol = hRol)
            if (SessionManager.user?.rut == updated.rut) {
                SessionManager.user = updated.toUserEntity()
                SessionManager.lastName = updated.apellidos
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(IllegalStateException("No se pudo guardar la foto de perfil: ${e.message}"))
        }
    }

    suspend fun clearUserPhoto(email: String): Result<Unit> {
        val canon = emailCanonico(email)
        if (canon.isBlank()) return Result.failure(IllegalArgumentException("Email inválido"))
        return try {
            val (hRut, hRol) = headersOrThrow()
            val dto = api.getUsuarioPorCorreo(canon, headerRut = hRut, headerRol = hRol)
            val updated = api.actualizarFoto(dto.rut, UpdateFotoRequest(fotoUri = null), headerRut = hRut, headerRol = hRol)
            if (SessionManager.user?.rut == updated.rut) {
                SessionManager.user = updated.toUserEntity().copy(photoUri = null)
                SessionManager.lastName = updated.apellidos
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(IllegalStateException("No se pudo eliminar la foto de perfil: ${e.message}"))
        }
    }

    suspend fun getUserPhoto(email: String): String? {
        val canon = emailCanonico(email)
        if (canon.isBlank()) return null
        return try {
            val (hRut, hRol) = headersOrThrow()
            val dto = api.getUsuarioPorCorreo(canon, headerRut = hRut, headerRol = hRol)
            SessionManager.lastName = dto.apellidos
            dto.fotoUri
        } catch (_: Exception) { null }
    }

    // ADMIN ------------------------------------------------------------------
    suspend fun adminListUsers(): Result<List<AdminUserRow>> = runCatching {
        val (hRut, hRol) = headersOrThrow()
        api.adminListUsuarios(hRut, hRol).map { it.toAdminUserRow() }
    }

    suspend fun adminCreateUser(
        user: UserEntity,
        password: String,
        roleId: Long
    ): Result<Unit> = runCatching {
        val correoCanon = emailCanonico(user.email)
        val phoneCanon = phoneCanonico(user.phone)
        val req = AdminCrearUsuarioRequest(
            rut = user.rut,
            nombre = user.name,
            apellidos = SessionManager.lastName ?: "",
            correo = correoCanon,
            telefono = if (phoneCanon.isBlank()) null else phoneCanon,
            direccion = user.address,
            fechaNacimiento = user.birthDate,
            fotoUri = user.photoUri,
            contrasenia = password,
            rolId = roleId
        )
        val (hRut, hRol) = headersOrThrow()
        api.adminCrearUsuario(req, hRut, hRol)
    }

    suspend fun adminUpdateUser(user: UserEntity): Result<Unit> = runCatching {
        val correoCanon = emailCanonico(user.email)
        val phoneCanon = phoneCanonico(user.phone)
        val req = AdminActualizarUsuarioRequest(
            nombre = user.name,
            apellidos = SessionManager.lastName ?: "",
            correo = correoCanon,
            telefono = if (phoneCanon.isBlank()) null else phoneCanon,
            direccion = user.address,
            fechaNacimiento = user.birthDate,
            fotoUri = user.photoUri
        )
        val (hRut, hRol) = headersOrThrow()
        api.adminActualizarUsuario(user.rut, req, hRut, hRol)
    }

    suspend fun adminGetUserByRut(rut: String) = runCatching {
        val (hRut, hRol) = headersOrThrow()
        val dto: UsuarioConRolDto = api.adminGetUsuario(rut, hRut, hRol)
        dto.toUserEntity()
    }

    // ADMIN - Roles
    suspend fun adminListRoles() = runCatching {
        val (hRut, hRol) = headersOrThrow()
        api.getRoles(hRut, hRol).map { AdminRoleRow(id = it.rolId, name = it.nombreRol) }
    }

    suspend fun adminAssignRoleToUser(rut: String, roleId: Long) = runCatching {
        val (hRut, hRol) = headersOrThrow()
        api.actualizarRolUsuario(
            rut = dottedRut(rut),
            body = UpdateRolRequest(rolId = roleId),
            headerRut = hRut,
            headerRol = hRol
        )
    }
}

