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

    // Últimos N usuarios registrados, con nombre/email/rol, ordenados por registro.id DESC
    @Query("""
    SELECT 
        u.rut                    AS rut,
        u.nombre                 AS name,
        u.correo_electronico     AS email,
        u.telefono               AS phone,
        u.direccion              AS address,
        r.rol_id                 AS roleId,
        rl.nombre_rol            AS roleName
    FROM registro r
    JOIN usuarios u ON u.rut = r.rut
    LEFT JOIN rol rl ON rl.rol_id = r.rol_id
    ORDER BY r.id DESC
    LIMIT :limitRows
""")
    fun observeUltimosAdminUsers(limitRows: Int = 20): kotlinx.coroutines.flow.Flow<
            List<com.example.appstorefit_grupo1.data.local.user.AdminUserRow>
            >


}
