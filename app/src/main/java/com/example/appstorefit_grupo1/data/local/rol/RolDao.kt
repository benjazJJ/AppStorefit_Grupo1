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
    @Query("SELECT * FROM rol ORDER BY nombre_rol")
    fun getAll(): Flow<List<RolEntity>>

    @Query("SELECT * FROM rol WHERE rol_id = :id LIMIT 1")
    suspend fun getById(id: Long): RolEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rol: RolEntity): Long

    @Update
    suspend fun update(rol: RolEntity)

    @Delete
    suspend fun delete(rol: RolEntity)
}