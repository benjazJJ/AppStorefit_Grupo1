package com.example.appstorefit_grupo1.data.repository

import com.example.appstorefit_grupo1.data.local.registro.RegistroDao
import com.example.appstorefit_grupo1.data.local.registro.RegistroEntity
import com.example.appstorefit_grupo1.data.local.user.*

class UserRepository(
    private val userDao: UserDao,
    private val registroDao: RegistroDao
) {
    suspend fun login(email: String, pass: String): Result<UserEntity> {
        val emailNorm = email.trim()          // <- normalizo entrada
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
        rolId: Long = 1L
    ): Result<Long> {
        val emailNorm = email.trim().lowercase()  // guardamos consistente
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
                address = "",
                birthDate = ""
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
        registroDao.getByUsuario(email.trim())
}
