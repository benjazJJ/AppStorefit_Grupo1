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

    @Query("SELECT * FROM registro WHERE rut = :rut LIMIT 1")
    suspend fun getByRut(rut: String): RegistroEntity?

    @Query("SELECT COUNT(*) FROM registro")
    suspend fun count(): Int

    @Query("UPDATE registro SET contrasenia = :newPass WHERE usuario = :usuario")
    suspend fun updatePasswordByUsuario(usuario: String, newPass: String): Int

    //MÉTODOS PARA PANEL ADMIN

    /** Elimina la fila de registro asociada al RUT. */
    @Query("DELETE FROM registro WHERE rut = :rut")
    suspend fun deleteByRut(rut: String): Int

    /** Actualiza el rol (columna rol_id) del usuario asociado al RUT. */
    @Query("UPDATE registro SET rol_id = :roleId WHERE rut = :rut")
    suspend fun updateRoleByRut(rut: String, roleId: Long): Int


}
