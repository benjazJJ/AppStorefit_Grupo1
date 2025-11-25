package com.example.appstorefit_grupo1.data.repository

import com.example.appstorefit_grupo1.data.local.Productos.ProductosDao
import com.example.appstorefit_grupo1.data.remote.catalog.CatalogApi
import com.example.appstorefit_grupo1.data.remote.dto.catalog.ProductoDto
import com.example.appstorefit_grupo1.data.remote.dto.catalog.ProductoIdDto
import com.example.appstorefit_grupo1.data.remote.dto.catalog.ProductoResponseDto
import com.example.appstorefit_grupo1.session.SessionManager
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ProductosRepositoryTest {

    @RelaxedMockK lateinit var dao: ProductosDao
    @RelaxedMockK lateinit var api: CatalogApi
    private lateinit var repo: ProductosRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        SessionManager.user = com.example.appstorefit_grupo1.data.local.user.UserEntity(
            rut = "11.111.111-1",
            name = "Admin",
            email = "a@a.cl",
            phone = "",
            address = "",
            birthDate = ""
        )
        SessionManager.roleId = 2L
        SessionManager.roleName = "ADMIN"
        repo = ProductosRepository(dao, api)
    }

    @After
    fun tearDown() {
        SessionManager.clear()
    }

    @Test
    fun create_falla_si_no_es_admin() = runBlocking {
        SessionManager.roleId = 1L

        val res = repo.create(1, "M", "Negro con detalles blancos", "M", 1000, 1)

        assertTrue(res.isFailure)
    }

    @Test
    fun create_falla_por_color_invalido() = runBlocking {
        val res = repo.create(1, "Mod", "Rojo", "M", 1000, 1)

        assertTrue(res.isFailure)
    }

    @Test
    fun create_ok_llama_api_y_cachea() = runBlocking {
        coEvery { dao.getMaxIdForCategory(1) } returns 0L
        val dto = productoDto(idCat = 1, idProd = 1)
        coEvery { api.crearProducto(any(), any(), any()) } returns ProductoResponseDto(
            message = "ok",
            data = dto
        )
        coEvery { dao.upsert(any()) } returns Unit

        val res = repo.create(1, "Mod", "Negro con detalles blancos", "M", 1000, 1)

        assertTrue(res.isSuccess)
        coVerify { dao.upsert(any()) }
    }

    @Test
    fun addToStock_falla_si_queda_negativo() = runBlocking {
        coEvery { api.getProductoPorIds(any(), any(), any(), any()) } returns productoDto(stock = 0)

        val res = repo.addToStock(1, 1, -5)

        assertTrue(res.isFailure)
    }

    private fun productoDto(idCat: Long = 1, idProd: Long = 1, stock: Int = 5) = ProductoDto(
        id = ProductoIdDto(idCategoria = idCat, idProducto = idProd),
        marca = "M",
        modelo = "X",
        color = "Negro con detalles blancos",
        talla = "M",
        precio = 1000,
        stock = stock,
        imageUrl = null
    )
}
