package com.example.appstorefit_grupo1.data.local.Carrito

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CarritoDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(item: CarritoEntity): Long

    @Update
    suspend fun update(item: CarritoEntity): Int

    @Delete
    suspend fun delete(item: CarritoEntity): Int

    @Query("DELETE FROM carrito")
    suspend fun clear(): Int

    @Query("SELECT * FROM carrito ORDER BY id DESC")
    suspend fun getAll(): List<CarritoEntity>

    @Query("""
        SELECT * FROM carrito
        WHERE id_categoria = :idCategoria
          AND id_producto  = :idProducto
          AND color        = :color
          AND talla        = :talla
        LIMIT 1
    """)
    suspend fun findByProductoYVariante(
        idCategoria: Long,
        idProducto: Long,
        color: String,
        talla: String
    ): CarritoEntity?

    @Query("SELECT COALESCE(SUM(cantidad), 0) FROM carrito")
    suspend fun countUnidades(): Int

    @Query("SELECT COALESCE(SUM(cantidad * precio_unitario), 0) FROM carrito")
    suspend fun totalCLP(): Int

    // Observables (para badge/actualización en vivo)
    @Query("SELECT * FROM carrito ORDER BY id DESC")
    fun observarCarrito(): Flow<List<CarritoEntity>>

    @Query("SELECT COALESCE(SUM(cantidad), 0) FROM carrito")
    fun observarUnidades(): Flow<Int>

    @Query("SELECT COALESCE(SUM(cantidad * precio_unitario), 0) FROM carrito")
    fun observarTotalCLP(): Flow<Int>
}