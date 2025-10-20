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

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE rut = :rut LIMIT 1")
    suspend fun getByRut(rut: String): UserEntity?

    @Query("SELECT * FROM users ORDER BY rut ASC")
    suspend fun getAll(): List<UserEntity>

    @Query("SELECT COUNT(*) FROM users")
    suspend fun count(): Int

    @Update
    suspend fun actualizar(user: UserEntity): Int

    @Query("UPDATE users SET photoUri = :uri WHERE email = :email")
    suspend fun updatePhotoByEmail(email: String, uri: String): Int
}
