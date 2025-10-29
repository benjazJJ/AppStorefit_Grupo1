package com.example.appstorefit_grupo1.data.local.Compras

import androidx.room.*

// El DAO es la "puerta" para leer/escribir en las tablas de compra y detalle.
@Dao
interface CompraDao {

    // INSERTAR SOLO LA CABECERA (una compra)
    // Devuelve el id autogenerado (idCompra) que creó SQLite.
    @Insert
    suspend fun insertCompra(compra: CompraEntity): Long

    //INSERTAR MUCHOS DETALLES DE UNA VEZ
    @Insert
    suspend fun insertDetalles(detalles: List<CompraDetalleEntity>)

    //TRANSACCIÓN: crear compra + sus detalles juntos
    // Si algo falla, se deshace tod0 Si sale bien, quedan ambos grabados.
    @Transaction
    suspend fun insertCompraConDetalles(
        compra: CompraEntity,
        detalles: List<CompraDetalleEntity>
    ): Long {
        // 1) Inserta la cabecera y obtén el idCompra real
        val idCompra = insertCompra(compra)

        // 2) Inserta todos los detalles, ajustando su idCompra con el real
        if (detalles.isNotEmpty()) {
            insertDetalles(detalles.map { it.copy(idCompra = idCompra) })
        }

        // 3) Devuelve el id de la compra creada (útil para navegar/mostrar)
        return idCompra
    }

    // HISTORIAL POR USUARIO (RUT)
    // Trae una lista de "CompraConDetalles" (compra + sus líneas).
    // ORDER BY fechaMillis DESC = primero las compras más recientes.
    @Transaction
    @Query("SELECT * FROM compra WHERE rutUsuario = :rut ORDER BY fechaMillis DESC")
    suspend fun getComprasPorRut(rut: String): List<CompraConDetalles>

    //TOTAL GASTADO POR USUARIO
    // Suma (cantidad * precioUnitario) de todas las compras de ese RUT.
    // IFNULL evita que sea null cuando no hay compras (devuelve 0).
    @Query("""
        SELECT IFNULL(SUM(d.cantidad * d.precioUnitario), 0)
        FROM compra c
        JOIN compra_detalle d ON d.idCompra = c.idCompra
        WHERE c.rutUsuario = :rut
    """)
    suspend fun getTotalGastadoPorRut(rut: String): Int
}