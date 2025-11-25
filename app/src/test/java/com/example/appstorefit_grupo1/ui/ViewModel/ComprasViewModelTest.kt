package com.example.appstorefit_grupo1.ui.ViewModel

import com.example.appstorefit_grupo1.data.local.Compras.CompraConDetalles
import com.example.appstorefit_grupo1.data.local.Compras.CompraDetalleEntity
import com.example.appstorefit_grupo1.data.local.Compras.CompraEntity
import com.example.appstorefit_grupo1.data.repository.CompraRepository
import com.example.appstorefit_grupo1.data.repository.ItemCarritoSnapshot
import io.mockk.coEvery
import io.mockk.coVerifySequence
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(JUnit4::class)
class ComprasViewModelTest {

    // Dispatcher de prueba para controlar las corutinas
    private val dispatcher = StandardTestDispatcher()

    private lateinit var repo: CompraRepository
    private lateinit var viewModel: ComprasViewModel

    @Before
    fun setUp() {
        // MUY IMPORTANTE: conectar Main al dispatcher de test
        Dispatchers.setMain(dispatcher)

        // mockk relajado para no fallar por métodos no stubbeados
        repo = mockk(relaxed = true)
        viewModel = ComprasViewModel(repo)
    }

    @After
    fun tearDown() {
        // Dejar Main como estaba
        Dispatchers.resetMain()
    }

    @Test
    fun cargar_actualizaEstadosConDatosDelRepositorio() {
        val rut = "12345678-9"
        val compra = CompraEntity(
            idCompra = 1,
            rutUsuario = rut,
            fechaMillis = 1_670_000_000_000
        )
        val detalles = listOf(
            CompraDetalleEntity(
                idDetalle = 1,
                idCompra = 1,
                idProducto = 42,
                nombreProducto = "Pulsera",
                cantidad = 2,
                precioUnitario = 1_000
            )
        )
        val historial = listOf(CompraConDetalles(compra, detalles))

        coEvery { repo.obtenerHistorial(rut) } returns historial
        coEvery { repo.totalGastado(rut) } returns 2_000

        // Lanza la corutina del ViewModel
        viewModel.cargar(rut)
        // Avanzar todas las tareas pendientes del dispatcher de test
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(historial, viewModel.historial.value)
        assertEquals(2_000, viewModel.totalGastado.value)
        coVerifySequence {
            repo.obtenerHistorial(rut)
            repo.totalGastado(rut)
        }
    }

    @Test
    fun registrarCompra_notificaRepositorioYCallback() {
        val rut = "87654321-K"
        val items = listOf(
            ItemCarritoSnapshot(
                idProducto = 10,
                nombreProducto = "Anillo",
                cantidad = 1,
                precioUnitario = 500
            )
        )
        val generatedId = 54L

        coEvery { repo.registrarCompra(rut, any(), items) } returns generatedId
        coEvery { repo.obtenerHistorial(rut) } returns emptyList()
        coEvery { repo.totalGastado(rut) } returns 0

        var callbackResult: Long? = null

        // Lanza la corutina del ViewModel
        viewModel.registrarCompra(rut, items) { callbackResult = it }
        // Procesar las corutinas lanzadas
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(generatedId, callbackResult)
        coVerifySequence {
            repo.registrarCompra(rut, any(), items)
            repo.obtenerHistorial(rut)
            repo.totalGastado(rut)
        }
    }
}
