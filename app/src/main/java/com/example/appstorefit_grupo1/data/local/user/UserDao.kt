package com.example.appstorefit_grupo1.data.local.user

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(user: UserEntity): Long

    @Query("SELECT * FROM usuarios WHERE correo_electronico = :email LIMIT 1")
    suspend fun getByEmail(email: String): UserEntity?

    @Query("SELECT COUNT(*) FROM usuarios WHERE correo_electronico = :email")
    suspend fun existsEmail(email: String): Int

    @Query("SELECT * FROM usuarios WHERE rut = :rut LIMIT 1")
    suspend fun getByRut(rut: String): UserEntity?

    @Query("SELECT * FROM usuarios WHERE telefono = :phone LIMIT 1")
    suspend fun getByPhone(phone: String): UserEntity?

    @Query("SELECT * FROM usuarios ORDER BY rut ASC")
    suspend fun getAll(): List<UserEntity>

    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM usuarios WHERE telefono = :phone")
    suspend fun existsPhone(phone: String): Int

    @Update
    suspend fun actualizar(user: UserEntity): Int

}