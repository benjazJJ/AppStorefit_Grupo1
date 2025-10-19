package com.example.appstorefit_grupo1.data.repository

import com.example.appstorefit_grupo1.data.local.rol.RolEntity
import kotlinx.coroutines.flow.Flow

interface RolRepository {
    fun getAll(): Flow<List<RolEntity>>
    suspend fun getById(id: Long): RolEntity?
    suspend fun add(rol: RolEntity): Long
    suspend fun update(rol: RolEntity)
    suspend fun delete(rol: RolEntity)
}
