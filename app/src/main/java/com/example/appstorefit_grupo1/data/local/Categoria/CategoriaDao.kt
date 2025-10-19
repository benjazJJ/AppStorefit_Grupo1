package com.example.appstorefit_grupo1.data.local.Categoria

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete
import androidx.room.Update

@Dao
interface CategoriaDao {

    // Inserta una categoría. ABORT si ya existe PK; para nombre duplicado lo controlamos en repo.
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(categoria: CategoriaEntity): Long

    // Obtiene por id
    @Query("SELECT * FROM categoria WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): CategoriaEntity?

    // Obtiene por nombre (útil para validar duplicados)
    @Query("SELECT * FROM categoria WHERE nombre = :nombre LIMIT 1")
    suspend fun getByNombre(nombre: String): CategoriaEntity?

    // Cantidad total (útil para seed)
    @Query("SELECT COUNT(*) FROM categoria")
    suspend fun count(): Int

    // Lista completa (orden asc por id)
    @Query("SELECT * FROM categoria ORDER BY id ASC")
    suspend fun getAll(): List<CategoriaEntity>

    @Update
    suspend fun update(categoria: CategoriaEntity): Int

    @Delete
    suspend fun delete(categoria: CategoriaEntity): Int
}