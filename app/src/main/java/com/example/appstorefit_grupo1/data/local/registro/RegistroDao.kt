package com.example.appstorefit_grupo1.data.local.registro

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RegistroDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(registro: RegistroEntity): Long

    @Query("SELECT * FROM registro WHERE usuario = :usuario LIMIT 1")
    suspend fun getByUsuario(usuario: String): RegistroEntity?

    @Query("SELECT COUNT(*) FROM registro")
    suspend fun count(): Int

    @Query("UPDATE registro SET contrasenia = :newPass WHERE usuario = :usuario")
    suspend fun updatePasswordByUsuario(usuario: String, newPass: String): Int
}
