package com.example.appstorefit_grupo1.data.repository

import com.example.appstorefit_grupo1.data.local.registro.RegistroDao
import com.example.appstorefit_grupo1.data.local.registro.RegistroEntity
import com.example.appstorefit_grupo1.data.local.user.*

class UserRepository(
    private val userDao: UserDao,
    private val registroDao: RegistroDao
) {
    // login por 'usuario' (email) + contraseña en tabla 'registro'
    suspend fun login(email: String, pass: String): Result<UserEntity> {
        val reg = registroDao.getByUsuario(email)
        return if (reg != null && reg.contrasenia == pass) {
            val user = userDao.getByRut(reg.rut)
            if (user != null) Result.success(user)
            else Result.failure(IllegalStateException("Perfil no encontrado"))
        } else {
            Result.failure(IllegalArgumentException("Datos inválidos"))
        }
    }

    // registro: crea usuario (usuarios) + credencial (registro)
    suspend fun register(
        rut: String,
        name: String,
        email: String,
        phone: String,
        pass: String,
        rolId: Long = 1L // por defecto Cliente
    ): Result<Long> {
        // ¿ya existe un registro para este 'usuario' (email)?
        val existsReg = registroDao.getByUsuario(email) != null
        if (existsReg) {
            return Result.failure(IllegalArgumentException("Correo en uso"))
        }

        // ¿ya existe perfil por rut?
        val existsUser = userDao.getByRut(rut) != null
        if (existsUser) {
            return Result.failure(IllegalArgumentException("RUT ya registrado"))
        }

        // Crear perfil en 'usuarios'
        userDao.insertar(
            UserEntity(
                rut = rut,
                name = name,
                email = email,
                phone = phone,
                lastName = "",
                address = "",
                birthDate = "",
                registerDate = ""
            )
        )

        // Crear credenciales en 'registro'
        val regId = registroDao.insertar(
            RegistroEntity(
                rolId = rolId,
                usuario = email,   // usamos el email como 'usuario'
                contrasenia = pass,
                rut = rut
            )
        )

        return Result.success(regId)
    }
}
