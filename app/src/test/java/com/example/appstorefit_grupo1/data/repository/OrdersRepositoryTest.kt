package com.example.appstorefit_grupo1.data.repository

import com.example.appstorefit_grupo1.data.remote.dto.orders.CompraDetalleDto
import com.example.appstorefit_grupo1.data.remote.dto.orders.CompraDto
import com.example.appstorefit_grupo1.data.remote.orders.OrdersApi
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
class OrdersRepositoryTest {

    @RelaxedMockK lateinit var api: OrdersApi
    private lateinit var repo: OrdersRepository

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
        repo = OrdersRepository(api)
    }

    @After
    fun tearDown() {
        SessionManager.clear()
    }

    @Test
    fun crearCompra_falla_si_carrito_vacio() = runBlocking {
        val res = repo.crearCompra(emptyList())

        assertTrue(res.isFailure)
    }

    @Test
    fun crearCompra_ok_mapea_resultado() = runBlocking {
        val dto = CompraDto(
            idCompra = 10,
            rutUsuario = "11.111.111-1",
            fechaMillis = 1L,
            detalles = listOf(CompraDetalleDto(null, 1, "Prod", 1, 1000))
        )
        coEvery { api.crearCompra(any(), any(), any()) } returns dto

        val res = repo.crearCompra(listOf(ItemCarritoSnapshot(1, "Prod", 1, 1000)))

        assertTrue(res.isSuccess)
        coVerify { api.crearCompra(headerRut = "11.111.111-1", headerRol = "ADMIN", compra = any()) }
    }

    @Test
    fun adminListarCompras_falla_si_no_es_admin() = runBlocking {
        SessionManager.roleId = 1L
        val res = repo.adminListarCompras()

        assertTrue(res.isFailure)
    }

    @Test
    fun historialCliente_ok() = runBlocking {
        val dto = CompraDto(
            idCompra = 10,
            rutUsuario = "11.111.111-1",
            fechaMillis = 1L,
            detalles = listOf(CompraDetalleDto(1, 1, "Prod", 1, 1000))
        )
        coEvery { api.getCompraByRut(any(), any(), any()) } returns listOf(dto)

        val res = repo.historialCliente()

        assertTrue(res.isSuccess)
    }
}
