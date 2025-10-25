package com.example.appstorefit_grupo1.data.local.Categoria

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

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

    // QUERIE FLOW QUE SE AUTOMATIZA PARA TRAER CADA ELEMENTO PARA PODER MOSTRARLO EN EL ADMIN
    @Query("""
        SELECT 
            c.id            AS id,
            c.nombre        AS nombre,
            COUNT(p.id_producto)              AS productos,
            COUNT(DISTINCT p.modelo)          AS modelos
        FROM categoria c
        LEFT JOIN producto p ON p.id_categoria = c.id
        GROUP BY c.id, c.nombre
        ORDER BY c.id
    """)
    fun observeResumen(): Flow<List<CategoriaResumen>>

    // helpers para renombrar / eliminar solo si está vacía
    @Query("UPDATE categoria SET nombre = :nuevo WHERE id = :id")
    suspend fun renombrar(id: Long, nuevo: String): Int

    @Query("SELECT COUNT(*) FROM producto WHERE id_categoria = :id")
    suspend fun contarProductosEnCategoria(id: Long): Int

    @Query("DELETE FROM categoria WHERE id = :id")
    suspend fun deleteById(id: Long): Int
}