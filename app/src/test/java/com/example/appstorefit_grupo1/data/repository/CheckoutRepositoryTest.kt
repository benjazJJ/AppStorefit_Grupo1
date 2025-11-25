package com.example.appstorefit_grupo1.data.repository

import androidx.room.withTransaction
import com.example.appstorefit_grupo1.data.local.Carrito.CarritoDao
import com.example.appstorefit_grupo1.data.local.Carrito.CarritoEntity
import com.example.appstorefit_grupo1.data.local.Productos.ProductosDao
import com.example.appstorefit_grupo1.data.local.Productos.ProductosEntity
import com.example.appstorefit_grupo1.data.local.database.AppDatabase
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CheckoutRepositoryTest {

    @RelaxedMockK lateinit var db: AppDatabase
    @RelaxedMockK lateinit var productosDao: ProductosDao
    @RelaxedMockK lateinit var carritoDao: CarritoDao
    private lateinit var repo: CheckoutRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        repo = CheckoutRepository(db, productosDao, carritoDao)
        coEvery { db.withTransaction<Any?>(any()) } coAnswers {
            val block = arg<suspend () -> Any?>(0); block.invoke()
        }
    }

    @Test
    fun confirmarCompra_devuelve_sin_stock_si_carrito_vacio() = runBlocking {
        coEvery { carritoDao.getAllOnce() } returns emptyList()

        val result = repo.confirmarCompra()

        assertTrue(result is CheckoutResult.SinStock)
        coVerify(exactly = 0) { carritoDao.clear() }
    }

    @Test
    fun confirmarCompra_devuelve_sin_stock_si_producto_no_existe() = runBlocking {
        val item = item(cantidad = 1)
        coEvery { carritoDao.getAllOnce() } returns listOf(item)
        coEvery { productosDao.getByIds(any(), any()) } returns null

        val result = repo.confirmarCompra()

        assertTrue(result is CheckoutResult.SinStock)
    }

    @Test
    fun confirmarCompra_devuelve_sin_stock_si_stock_insuficiente() = runBlocking {
        val item = item(cantidad = 2)
        coEvery { carritoDao.getAllOnce() } returns listOf(item)
        coEvery { productosDao.getByIds(any(), any()) } returns producto(stock = 1)

        val result = repo.confirmarCompra()

        assertTrue(result is CheckoutResult.SinStock)
        coVerify(exactly = 0) { carritoDao.clear() }
    }

    @Test
    fun confirmarCompra_ok_descuenta_y_limpia() = runBlocking {
        val item = item(cantidad = 1)
        coEvery { carritoDao.getAllOnce() } returns listOf(item)
        coEvery { productosDao.getByIds(any(), any()) } returns producto(stock = 5)
        coEvery { productosDao.descontarStock(any(), any(), any()) } returns 1
        coEvery { carritoDao.clear() } returns 1

        val result = repo.confirmarCompra()

        assertTrue(result is CheckoutResult.Ok)
        coVerify { productosDao.descontarStock(item.idCategoria, item.idProducto, 1) }
        coVerify { carritoDao.clear() }
    }

    private fun item(cantidad: Int) = CarritoEntity(
        idCategoria = 1,
        idProducto = 10,
        color = "Negro con detalles blancos",
        talla = "M",
        modelo = "X",
        precioUnitario = 1000,
        cantidad = cantidad
    )

    private fun producto(stock: Int) = ProductosEntity(
        idCategoria = 1,
        idProducto = 10,
        modelo = "X",
        marca = "M",
        color = "Negro con detalles blancos",
        talla = "M",
        precio = 1000,
        stock = stock
    )
}
