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

    // Obtener un usuario por email
    @Query("SELECT * FROM usuarios WHERE correo_electronico = :email LIMIT 1")
    suspend fun getByEmail(email: String): UserEntity?

    // Obtener un usuario por rut
    @Query("SELECT * FROM usuarios WHERE rut = :rut LIMIT 1")
    suspend fun getByRut(rut: String): UserEntity?

    // Obtener todos los usuarios
    @Query("SELECT * FROM usuarios ORDER BY rut ASC")
    suspend fun getAll(): List<UserEntity>

    // Cantidad de registros
    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun count(): Int

    @Update
    suspend fun actualizar(user: UserEntity): Int


}
