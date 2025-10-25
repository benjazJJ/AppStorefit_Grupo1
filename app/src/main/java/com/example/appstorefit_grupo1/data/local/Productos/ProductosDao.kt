package com.example.appstorefit_grupo1.data.local.Productos

import androidx.room.*

@Dao
interface ProductosDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(product: ProductosEntity): Long

    // NUEVO: inserción masiva idempotente (evita duplicados)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(productos: List<ProductosEntity>): List<Long>

    @Update
    suspend fun update(product: ProductosEntity): Int

    @Delete
    suspend fun delete(product: ProductosEntity): Int

    @Query("""
        SELECT * FROM producto
        WHERE id_categoria = :idCategoria AND id_producto = :idProducto
        LIMIT 1
    """)
    suspend fun getByIds(idCategoria: Long, idProducto: Long): ProductosEntity?

    @Query("SELECT * FROM producto ORDER BY id_categoria, id_producto")
    suspend fun getAll(): List<ProductosEntity>

    @Query("SELECT * FROM producto WHERE id_categoria = :idCategoria ORDER BY id_producto")
    suspend fun getByCategoria(idCategoria: Long): List<ProductosEntity>

    @Query("SELECT COUNT(*) FROM producto")
    suspend fun count(): Int

    @Query("SELECT MAX(id_producto) FROM producto WHERE id_categoria = :idCategoria")
    suspend fun getMaxIdForCategory(idCategoria: Long): Long?

    @Query("""
        UPDATE producto
        SET stock = :newStock
        WHERE id_categoria = :idCategoria AND id_producto = :idProducto
    """)
    suspend fun updateStock(idCategoria: Long, idProducto: Long, newStock: Int): Int

    @Query("""
        UPDATE producto
        SET stock = stock - :cantidad
        WHERE id_categoria = :idCategoria
          AND id_producto  = :idProducto
          AND stock >= :cantidad
    """)
    suspend fun descontarStock(
        idCategoria: Long,
        idProducto: Long,
        cantidad: Int
    ): Int

    @Query("""
        SELECT * FROM producto
        WHERE id_categoria = :idCategoria AND modelo = :modelo
        ORDER BY talla, color
    """)
    suspend fun getByCategoriaYModelo(
        idCategoria: Long,
        modelo: String
    ): List<ProductosEntity>

    // NUEVO: trae 1 variante exacta por modelo+color+talla
    @Query("""
        SELECT * FROM producto
        WHERE id_categoria = :idCategoria
          AND modelo = :modelo
          AND color  = :color
          AND talla  = :talla
        LIMIT 1
    """)
    suspend fun getByCatModeloColorTalla(
        idCategoria: Long,
        modelo: String,
        color: String,
        talla: String
    ): ProductosEntity?

    // NUEVO: contar si existe esa variante (para evitar duplicados en seed)
    @Query("""
        SELECT COUNT(*) FROM producto
        WHERE id_categoria = :idCategoria
          AND modelo = :modelo
          AND color  = :color
          AND talla  = :talla
    """)
    suspend fun countByCatModeloColorTalla(
        idCategoria: Long,
        modelo: String,
        color: String,
        talla: String
    ): Int
}
