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

    @Query("UPDATE usuarios SET foto_uri = :uri WHERE correo_electronico = :email")
    suspend fun updatePhotoByEmail(email: String, uri: String?): Int

    @Query("SELECT foto_uri FROM usuarios WHERE correo_electronico = :email LIMIT 1")
    suspend fun getPhotoUriByEmail(email: String): String?

    /* ---------- MÉTODOS PARA PANEL ADMIN ---------- */

    @Query("""
        SELECT 
            u.rut                    AS rut,
            u.nombre                 AS name,
            u.correo_electronico     AS email,
            u.telefono               AS phone,
            u.direccion              AS address,
            r.rol_id                 AS roleId,
            rl.nombre_rol                AS roleName
        FROM usuarios u
        LEFT JOIN registro r ON r.rut = u.rut
        LEFT JOIN rol rl      ON rl.rol_id = r.rol_id
        ORDER BY u.nombre COLLATE NOCASE ASC
    """)
    suspend fun adminListUsers(): List<AdminUserRow>

    // Eliminar usuario por RUT
    @Query("DELETE FROM usuarios WHERE rut = :rut")
    suspend fun deleteByRut(rut: String): Int
}
