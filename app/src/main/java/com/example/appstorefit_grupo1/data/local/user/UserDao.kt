package com.example.appstorefit_grupo1.data.local.user

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    // Crear usuario
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(user: UserEntity): Long

    // Lecturas básicas por email, rut, teléfono y listados
    @Query("SELECT * FROM usuarios WHERE correo_electronico = :email LIMIT 1")
    suspend fun getByEmail(email: String): UserEntity?

    @Query("SELECT * FROM usuarios WHERE rut = :rut LIMIT 1")
    suspend fun getByRut(rut: String): UserEntity?

    @Query("SELECT * FROM usuarios WHERE telefono = :phone LIMIT 1")
    suspend fun getByPhone(phone: String): UserEntity?

    @Query("SELECT * FROM usuarios ORDER BY rut ASC")
    suspend fun getAll(): List<UserEntity>

    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun count(): Int

    // Verificaciones de existencia directas
    @Query("SELECT COUNT(*) FROM usuarios WHERE correo_electronico = :email")
    suspend fun existsEmail(email: String): Int

    @Query("SELECT COUNT(*) FROM usuarios WHERE telefono = :phone")
    suspend fun existsPhone(phone: String): Int

    // Verificaciones de unicidad excluyendo al propio usuario por RUT
    @Query("SELECT COUNT(*) FROM usuarios WHERE correo_electronico = :email AND rut <> :rut")
    suspend fun existsEmailExceptRut(email: String, rut: String): Int

    @Query("SELECT COUNT(*) FROM usuarios WHERE telefono = :phone AND rut <> :rut")
    suspend fun existsPhoneExceptRut(phone: String, rut: String): Int

    // Actualización completa usando la entidad
    @Update
    suspend fun actualizar(user: UserEntity): Int

    // Actualización parcial editable por ADMIN (nombre, email y teléfono)
    @Query("""
        UPDATE usuarios
        SET nombre = :nombre,
            correo_electronico = :email,
            telefono = :telefono
        WHERE rut = :rut
    """)
    suspend fun adminUpdateEditable(
        rut: String,
        nombre: String,
        email: String,
        telefono: String?
    ): Int


    @Query("""
        UPDATE usuarios
        SET nombre      = :nombre,
            telefono    = :telefono,
            direccion   = :direccion,
            fec_nac     = :fechaNacimiento
        WHERE rut = :rut
    """)
    suspend fun actualizarPerfilPorRut(
        rut: String,
        nombre: String,
        telefono: String?,
        direccion: String,
        fechaNacimiento: String
    ): Int

    @Query("UPDATE usuarios SET correo_electronico = :email WHERE rut = :rut")
    suspend fun updateEmailByRut(rut: String, email: String): Int

    // Foto de perfil: mantener flujo existente por email
    @Query("UPDATE usuarios SET foto_uri = :uri WHERE correo_electronico = :email")
    suspend fun updatePhotoByEmail(email: String, uri: String?): Int

    @Query("SELECT foto_uri FROM usuarios WHERE correo_electronico = :email LIMIT 1")
    suspend fun getPhotoUriByEmail(email: String): String?

    // Eliminación por RUT
    @Query("DELETE FROM usuarios WHERE rut = :rut")
    suspend fun deleteByRut(rut: String): Int

    // Listado para el panel de administración con rol
    @Query("""
        SELECT 
            u.rut                    AS rut,
            u.nombre                 AS name,
            u.correo_electronico     AS email,
            u.telefono               AS phone,
            u.direccion              AS address,
            r.rol_id                 AS roleId,
            rl.nombre_rol            AS roleName
        FROM usuarios u
        LEFT JOIN registro r ON r.rut = u.rut
        LEFT JOIN rol rl      ON rl.rol_id = r.rol_id
        ORDER BY u.nombre COLLATE NOCASE ASC
    """)
    suspend fun adminListUsers(): List<AdminUserRow>

    // Observación reactiva del total de usuarios
    @Query("SELECT COUNT(*) FROM usuarios")
    fun observeCount(): Flow<Int>
}
