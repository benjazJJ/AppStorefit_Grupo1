package com.example.appstorefit_grupo1.data.repository

import com.example.appstorefit_grupo1.data.local.user.UserDao
import com.example.appstorefit_grupo1.data.local.user.UserEntity


class UserRepository (
    private val userDao: UserDao
){
    //login
    suspend fun login(email:String, pass: String): Result<UserEntity>{
        val user = userDao.getByEmail(email)
        return if(user != null && user.pass == pass){
            Result.success(user)
        }
        else{
            Result.failure(IllegalArgumentException("Datos Inválidos"))
        }
    }

    //registro
    suspend fun register(name: String, email: String, phone: String, pass: String): Result<Long>{
        val exists = userDao.getByEmail(email) != null
        if(exists){
            return Result.failure(IllegalArgumentException("Correo en uso"))
        }
        else{
            val id = userDao.insertar(
                UserEntity(
                    name = name,
                    email = email,
                    phone = phone,
                    pass = pass
                )
            )
            return Result.success(id)
        }
    }

}