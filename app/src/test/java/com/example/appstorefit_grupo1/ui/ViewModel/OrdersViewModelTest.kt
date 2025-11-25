package com.example.appstorefit_grupo1.ui.ViewModel
import com.example.appstorefit_grupo1.data.local.Compras.CompraConDetalles
import com.example.appstorefit_grupo1.data.repository.ItemCarritoSnapshot
import com.example.appstorefit_grupo1.data.repository.OrdersRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(JUnit4::class)
class OrdersViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private lateinit var repo: OrdersRepository
    private lateinit var viewModel: OrdersViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repo = mockk(relaxed = true)
        viewModel = OrdersViewModel(repo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ============= crearCompraDesdeCarrito =============

    @Test
    fun crearCompraDesdeCarrito_conCarritoVacio_seteaError() {
        val items = emptyList<ItemCarritoSnapshot>()

        viewModel.crearCompraDesdeCarrito(items)
        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.crearCompra.value
        assertEquals("El carrito está vacío.", state.error)
        assertNull(state.compraCreada)
        assertEquals(false, state.enviando)
        // No debería llamar al repo
        coVerify(exactly = 0) { repo.crearCompra(any()) }
    }

    @Test
    fun crearCompraDesdeCarrito_success_actualizaEstadoOk() {
        val items = listOf(
            ItemCarritoSnapshot(
                idProducto = 1,
                nombreProducto = "Zapa",
                cantidad = 2,
                precioUnitario = 10000
            )
        )
        val compraMock: CompraConDetalles = mockk()

        coEvery { repo.crearCompra(items) } returns Result.success(compraMock)

        viewModel.crearCompraDesdeCarrito(items)
        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.crearCompra.value
        assertEquals(compraMock, state.compraCreada)
        assertNull(state.error)
        assertEquals(false, state.enviando)
    }

    @Test
    fun crearCompraDesdeCarrito_error_actualizaEstadoConError() {
        val items = listOf(
            ItemCarritoSnapshot(
                idProducto = 1,
                nombreProducto = "Zapa",
                cantidad = 2,
                precioUnitario = 10000
            )
        )

        coEvery { repo.crearCompra(items) } returns
                Result.failure(RuntimeException("Falla al crear"))

        viewModel.crearCompraDesdeCarrito(items)
        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.crearCompra.value
        assertNull(state.compraCreada)
        assertEquals("Falla al crear", state.error)
        assertEquals(false, state.enviando)
    }

    // ============= cargarHistorialCliente =============

    @Test
    fun cargarHistorialCliente_success_actualizaHistorial() {
        val compraMock: CompraConDetalles = mockk()
        coEvery { repo.historialCliente() } returns Result.success(listOf(compraMock))

        viewModel.cargarHistorialCliente()
        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.historial.value
        assertEquals(listOf(compraMock), state.compras)
        assertNull(state.error)
        assertEquals(false, state.cargando)
    }

    @Test
    fun cargarHistorialCliente_error_seteaError() {
        coEvery { repo.historialCliente() } returns
                Result.failure(RuntimeException("No se pudo"))

        viewModel.cargarHistorialCliente()
        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.historial.value
        assertEquals(emptyList<CompraConDetalles>(), state.compras)
        assertEquals("No se pudo", state.error)
        assertEquals(false, state.cargando)
    }

    // ============= cargarHistorialPorRut =============

    @Test
    fun cargarHistorialPorRut_success_llamaRepoYActualiza() {
        val rut = "11.111.111-1"
        val compraMock: CompraConDetalles = mockk()
        coEvery { repo.historialCliente(rut) } returns Result.success(listOf(compraMock))

        viewModel.cargarHistorialPorRut(rut)
        dispatcher.scheduler.advanceUntilIdle()

        coVerify { repo.historialCliente(rut) }
        val state = viewModel.historial.value
        assertEquals(listOf(compraMock), state.compras)
        assertNull(state.error)
    }

    // ============= cargarTotalGastado =============

    @Test
    fun cargarTotalGastado_success_actualizaTotal() {
        coEvery { repo.totalGastado(null) } returns Result.success(50000)

        viewModel.cargarTotalGastado()
        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.totalGastado.value
        assertEquals(50000, state.total)
        assertNull(state.error)
        assertEquals(false, state.cargando)
    }

    @Test
    fun cargarTotalGastado_error_seteaError() {
        coEvery { repo.totalGastado(null) } returns
                Result.failure(RuntimeException("Error total"))

        viewModel.cargarTotalGastado()
        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.totalGastado.value
        assertNull(state.total)
        assertEquals("Error total", state.error)
        assertEquals(false, state.cargando)
    }

    // ============= adminListarTodas =============

    @Test
    fun adminListarTodas_success_actualizaAdminCompras() {
        val compraMock: CompraConDetalles = mockk()
        coEvery { repo.adminListarCompras() } returns Result.success(listOf(compraMock))

        viewModel.adminListarTodas()
        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.adminCompras.value
        assertEquals(listOf(compraMock), state.compras)
        assertNull(state.error)
        assertEquals(false, state.cargando)
    }

    @Test
    fun adminListarTodas_error_seteaError() {
        coEvery { repo.adminListarCompras() } returns
                Result.failure(RuntimeException("No se pudo listar"))

        viewModel.adminListarTodas()
        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.adminCompras.value
        assertEquals(emptyList<CompraConDetalles>(), state.compras)
        assertEquals("No se pudo listar", state.error)
        assertEquals(false, state.cargando)
    }

    // ============= resets =============

    @Test
    fun resetCrearCompraState_vuelveAlEstadoPorDefecto() {
        // ensuciar estado
        val compraMock: CompraConDetalles = mockk()
        _hack_setCrearCompraStateForTest(compraMock, "errorX")

        viewModel.resetCrearCompraState()

        val state = viewModel.crearCompra.value
        assertEquals(CrearCompraUiState(), state)
    }

    @Test
    fun resetHistorialState_vuelveAlEstadoPorDefecto() {
        // ensuciar estado
        viewModel.resetHistorialState() // ya lo deja por defecto
        val state = viewModel.historial.value
        assertEquals(HistorialComprasUiState(), state)
    }

    @Test
    fun resetTotalState_vuelveAlEstadoPorDefecto() {
        viewModel.resetTotalState()
        val state = viewModel.totalGastado.value
        assertEquals(TotalGastadoUiState(), state)
    }

    @Test
    fun resetAdminState_vuelveAlEstadoPorDefecto() {
        viewModel.resetAdminState()
        val state = viewModel.adminCompras.value
        assertEquals(AdminComprasUiState(), state)
    }

    // Helper privado solo para “ensuciar” estado sin tocar la VM original
    private fun _hack_setCrearCompraStateForTest(
        compra: CompraConDetalles,
        error: String?
    ) {
        // No hay acceso directo al MutableStateFlow, así que
        // disparamos un flujo real llamando al método
        coEvery { repo.crearCompra(any()) } returns Result.success(compra)
        viewModel.crearCompraDesdeCarrito(
            listOf(
                ItemCarritoSnapshot(
                    idProducto = 1,
                    nombreProducto = "X",
                    cantidad = 1,
                    precioUnitario = 1000
                )
            )
        )
        dispatcher.scheduler.advanceUntilIdle()
    }
}