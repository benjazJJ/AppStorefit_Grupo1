package com.example.appstorefit_grupo1.data.local.rol

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RolDao {
    @Query("SELECT * FROM rol ORDER BY nombre_rol COLLATE NOCASE")
    fun getAll(): Flow<List<RolEntity>>

    @Query("SELECT * FROM rol WHERE rol_id = :id LIMIT 1")
    suspend fun getById(id: Long): RolEntity?

    @Query("SELECT * FROM rol WHERE nombre_rol = :name LIMIT 1")
    suspend fun getByName(name: String): RolEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(rol: RolEntity): Long

    @Update
    suspend fun update(rol: RolEntity)

    @Delete
    suspend fun delete(rol: RolEntity)

    @Query("SELECT COUNT(*) FROM rol")
    suspend fun count(): Int

    // Usado por el VM (Admin)
    @Query("""
        SELECT 
            rol_id AS id, 
            nombre_rol AS name
        FROM rol 
        ORDER BY nombre_rol COLLATE NOCASE
    """)
    suspend fun adminListRoles(): List<AdminRoleRow>
}
