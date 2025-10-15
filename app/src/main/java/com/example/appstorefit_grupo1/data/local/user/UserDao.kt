package com.example.appstorefit_grupo1.data.local.user

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserDao{
    //Insertar daos en la tabla
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(user: UserEntity): Long

    //obtener todos los datos de 1 usuario mediante su correo
    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun  getByEmail(email: String): UserEntity?

    //obtener los datos de tososa los usuarios
    @Query("SELECT * FROM users ORDER BY id ASC")
    suspend fun getAll(): List<UserEntity>

    //obtener cantidad de registros de la tabla
    @Query("SELECT COUNT(*) FROM users")
    suspend fun count(): Int
}