package com.example.appstorefit_grupo1.data.repository

import com.example.appstorefit_grupo1.data.local.Compras.CompraConDetalles
import com.example.appstorefit_grupo1.data.local.Compras.CompraDao
import com.example.appstorefit_grupo1.data.local.Compras.CompraDetalleEntity
import com.example.appstorefit_grupo1.data.local.Compras.CompraEntity
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
class CompraRepositoryTest {

    @RelaxedMockK lateinit var dao: CompraDao
    private lateinit var repo: CompraRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        repo = CompraRepository(dao)
    }

    @Test
    fun registrarCompra_inserta_y_devuelve_id() = runBlocking {
        val items = listOf(ItemCarritoSnapshot(1, "Prod", 2, 5000))
        coEvery { dao.insertCompraConDetalles(any(), any()) } returns 42L

        val id = repo.registrarCompra("1-9", 123L, items)

        assertEquals(42L, id)
        coVerify {
            dao.insertCompraConDetalles(
                match<CompraEntity> { it.rutUsuario == "1-9" },
                match<List<CompraDetalleEntity>> { it.size == 1 && it.first().cantidad == 2 }
            )
        }
    }

    @Test
    fun obtenerHistorial_devuelve_lista() = runBlocking {
        val esperado = listOf(
            CompraConDetalles(CompraEntity(1, "1-9", 0), emptyList())
        )
        coEvery { dao.getComprasPorRut("1-9") } returns esperado

        val res = repo.obtenerHistorial("1-9")

        assertEquals(esperado, res)
    }

    @Test
    fun totalGastado_passthrough() = runBlocking {
        coEvery { dao.getTotalGastadoPorRut("1-9") } returns 9000

        val total = repo.totalGastado("1-9")

        assertEquals(9000, total)
    }
}
