package com.example.appstorefit_grupo1.data.local.Productos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductosDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: ProductosEntity): Long

    // InserciИn masiva idempotente (evita duplicados)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(productos: List<ProductosEntity>): List<Long>

    @androidx.room.Transaction
    suspend fun upsert(product: ProductosEntity) {
        val updated = update(product)
        if (updated == 0) insert(product)
    }

    @androidx.room.Transaction
    suspend fun upsertAll(productos: List<ProductosEntity>) {
        productos.forEach { upsert(it) }
    }

    @Update
    suspend fun update(product: ProductosEntity): Int

    @Delete
    suspend fun delete(product: ProductosEntity): Int

    @Query("DELETE FROM producto WHERE id_categoria = :idCategoria AND id_producto = :idProducto")
    suspend fun deleteByIds(idCategoria: Long, idProducto: Long): Int

    @Query("DELETE FROM producto WHERE id_categoria = :idCategoria")
    suspend fun deleteByCategoria(idCategoria: Long): Int

    @Query("DELETE FROM producto")
    suspend fun clear(): Int

    @Query(
        """
        SELECT * FROM producto
        WHERE id_categoria = :idCategoria AND id_producto = :idProducto
        LIMIT 1
    """
    )
    suspend fun getByIds(idCategoria: Long, idProducto: Long): ProductosEntity?

    @Query("SELECT * FROM producto ORDER BY id_categoria, id_producto")
    suspend fun getAll(): List<ProductosEntity>

    @Query("SELECT * FROM producto WHERE id_categoria = :idCategoria ORDER BY id_producto")
    suspend fun getByCategoria(idCategoria: Long): List<ProductosEntity>

    @Query("SELECT COUNT(*) FROM producto")
    suspend fun count(): Int

    @Query("SELECT MAX(id_producto) FROM producto WHERE id_categoria = :idCategoria")
    suspend fun getMaxIdForCategory(idCategoria: Long): Long?

    @Query(
        """
        UPDATE producto
        SET stock = :newStock
        WHERE id_categoria = :idCategoria AND id_producto = :idProducto
    """
    )
    suspend fun updateStock(idCategoria: Long, idProducto: Long, newStock: Int): Int

    @Query(
        """
        UPDATE producto
        SET stock = stock - :cantidad
        WHERE id_categoria = :idCategoria
          AND id_producto  = :idProducto
          AND stock >= :cantidad
    """
    )
    suspend fun descontarStock(
        idCategoria: Long,
        idProducto: Long,
        cantidad: Int
    ): Int

    @Query(
        """
        SELECT * FROM producto
        WHERE id_categoria = :idCategoria AND modelo = :modelo
        ORDER BY talla, color
    """
    )
    suspend fun getByCategoriaYModelo(
        idCategoria: Long,
        modelo: String
    ): List<ProductosEntity>

    // Trae 1 variante exacta por modelo+color+talla
    @Query(
        """
        SELECT * FROM producto
        WHERE id_categoria = :idCategoria
          AND modelo = :modelo
          AND color  = :color
          AND talla  = :talla
        LIMIT 1
    """
    )
    suspend fun getByCatModeloColorTalla(
        idCategoria: Long,
        modelo: String,
        color: String,
        talla: String
    ): ProductosEntity?

    // contar si existe esa variante (para evitar duplicados en seed)
    @Query(
        """
        SELECT COUNT(*) FROM producto
        WHERE id_categoria = :idCategoria
          AND modelo = :modelo
          AND color  = :color
          AND talla  = :talla
    """
    )
    suspend fun countByCatModeloColorTalla(
        idCategoria: Long,
        modelo: String,
        color: String,
        talla: String
    ): Int

    // Evitar duplicados al editar (excluyendo el propio id_categoria + id_producto)
    @Query(
        """
    SELECT COUNT(*) FROM producto
    WHERE id_categoria = :idCategoria
      AND modelo = :modelo
      AND color  = :color
      AND talla  = :talla
      AND (id_categoria <> :idCategoria OR id_producto <> :idProducto)
"""
    )
    suspend fun countByCatModeloColorTallaExceptoId(
        idCategoria: Long,
        idProducto: Long,
        modelo: String,
        color: String,
        talla: String
    ): Int

    // Streams reactivos (Room -> Flow)
    @Query("SELECT * FROM producto ORDER BY id_categoria, id_producto")
    fun observeAll(): Flow<List<ProductosEntity>>

    @Query("SELECT * FROM producto WHERE id_categoria = :idCategoria ORDER BY id_producto")
    fun observeByCategoria(idCategoria: Long): Flow<List<ProductosEntity>>

    @Query(
        """
        SELECT * FROM producto
        WHERE id_categoria = :idCategoria AND modelo = :modelo
        ORDER BY color, talla
    """
    )
    fun observeVariantesByCatAndModelo(
        idCategoria: Long,
        modelo: String
    ): Flow<List<ProductosEntity>>

    //helpers de stock (set y delta)
    @Query(
        """
        UPDATE producto
        SET stock = :nuevoStock
        WHERE id_categoria = :idCategoria AND id_producto = :idProducto
    """
    )
    suspend fun setStock(
        idCategoria: Long,
        idProducto: Long,
        nuevoStock: Int
    ): Int

    @Query(
        """
        UPDATE producto
        SET stock = stock + :delta
        WHERE id_categoria = :idCategoria AND id_producto = :idProducto
    """
    )
    suspend fun addToStock(
        idCategoria: Long,
        idProducto: Long,
        delta: Int
    ): Int
}
