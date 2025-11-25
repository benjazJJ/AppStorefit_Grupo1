package com.example.appstorefit_grupo1.data.repository

import com.example.appstorefit_grupo1.data.local.Carrito.CarritoDao
import com.example.appstorefit_grupo1.data.local.Carrito.CarritoEntity
import com.example.appstorefit_grupo1.data.local.Productos.ProductosDao
import com.example.appstorefit_grupo1.data.local.Productos.ProductosEntity
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CarritoRepositoryTest {

    @RelaxedMockK lateinit var carritoDao: CarritoDao
    @RelaxedMockK lateinit var productosDao: ProductosDao
    private lateinit var repo: CarritoRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        repo = CarritoRepository(carritoDao, productosDao)
    }

    @Test
    fun agregar_inserta_cuando_no_existe() = runBlocking {
        coEvery { carritoDao.findByProductoYVariante(1, 10, CarritoRepository.COLOR_BLANCO, "M") } returns null
        coEvery { productosDao.getByIds(1, 10) } returns producto(stock = 3)
        coEvery { carritoDao.insert(any()) } returns 1

        val result = repo.agregar(1, 10, "Modelo X", CarritoRepository.COLOR_BLANCO, "M", 25000)

        assertTrue(result.isSuccess)
        coVerify { carritoDao.insert(match { it.cantidad == 1 && it.modelo == "Modelo X" && it.precioUnitario == 25000 }) }
        coVerify(exactly = 0) { carritoDao.update(any()) }
    }

    @Test
    fun agregar_incrementa_cuando_ya_existe_y_hay_stock() = runBlocking {
        val existente = item(cantidad = 1)
        coEvery { carritoDao.findByProductoYVariante(1, 10, CarritoRepository.COLOR_BLANCO, "M") } returns existente
        coEvery { productosDao.getByIds(1, 10) } returns producto(stock = 5)
        coEvery { carritoDao.update(any()) } returns 1

        val result = repo.agregar(1, 10, "Modelo X", CarritoRepository.COLOR_BLANCO, "M", 25000)

        assertTrue(result.isSuccess)
        coVerify { carritoDao.update(existente.copy(cantidad = 2)) }
        coVerify(exactly = 0) { carritoDao.insert(any()) }
    }

    @Test
    fun agregar_falla_si_sobrepasa_stock() = runBlocking {
        val existente = item(cantidad = 3)
        coEvery { carritoDao.findByProductoYVariante(any(), any(), any(), any()) } returns existente
        coEvery { productosDao.getByIds(any(), any()) } returns producto(stock = 3)

        val result = repo.agregar(1, 10, "Modelo X", CarritoRepository.COLOR_BLANCO, "M", 25000)

        assertTrue(result.isFailure)
        assertEquals("Sin stock suficiente para Modelo X (${CarritoRepository.COLOR_BLANCO}/M)", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { carritoDao.update(any()) }
        coVerify(exactly = 0) { carritoDao.insert(any()) }
    }

    @Test
    fun disminuir_borra_cuando_queda_en_cero() = runBlocking {
        val existente = item(cantidad = 1)
        coEvery { carritoDao.findByProductoYVariante(1, 10, CarritoRepository.COLOR_NEGRO, "S") } returns existente
        coEvery { carritoDao.delete(existente) } returns 1

        val result = repo.disminuir(1, 10, CarritoRepository.COLOR_NEGRO, "S")

        assertTrue(result.isSuccess)
        coVerify { carritoDao.delete(existente) }
    }

    @Test
    fun eliminar_devuelve_failure_si_no_existe() = runBlocking {
        coEvery { carritoDao.findByProductoYVariante(any(), any(), any(), any()) } returns null

        val result = repo.eliminar(1, 10, CarritoRepository.COLOR_BLANCO, "M")

        assertTrue(result.isFailure)
    }

    @Test
    fun limpiar_llama_clear() = runBlocking {
        coEvery { carritoDao.clear() } returns 1

        val result = repo.limpiar()

        assertTrue(result.isSuccess)
        coVerify { carritoDao.clear() }
    }

    private fun producto(stock: Int) = ProductosEntity(
        idCategoria = 1,
        idProducto = 10,
        modelo = "Modelo X",
        color = CarritoRepository.COLOR_BLANCO,
        talla = "M",
        precio = 25000,
        stock = stock
    )

    private fun item(cantidad: Int) = CarritoEntity(
        idCategoria = 1,
        idProducto = 10,
        color = CarritoRepository.COLOR_BLANCO,
        talla = "M",
        modelo = "Modelo X",
        precioUnitario = 25000,
        cantidad = cantidad
    )
}