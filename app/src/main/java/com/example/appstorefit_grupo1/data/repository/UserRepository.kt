package com.example.appstorefit_grupo1.data.repository

import com.example.appstorefit_grupo1.data.local.user.UserDao
import com.example.appstorefit_grupo1.data.local.user.UserEntity


class UserRepository (
    private val userDao: UserDao
){
    //login
    suspend fun login(email: String, pass: String): Result<UserEntity> {
        val user = userDao.getByEmail(email)
        return if (user != null && user.pass == pass) {
            Result.success(user)
        }
        else{
            Result.failure(IllegalArgumentException("Datos Inválidos"))
        }
    }

    //registro

    //suspend fun register(name: String, email: String, phone: String, pass: String): Result<>
    //val exist = userDao.getByEmail(email) != null
    //if(exist){
    //    return
    }
 //TERMINAR, COPIAR Y PEGAR EL DEL PROFE
