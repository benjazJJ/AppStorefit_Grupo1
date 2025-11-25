package com.example.appstorefit_grupo1.ui.ViewModel

import com.example.appstorefit_grupo1.data.repository.CarritoRepository
import com.example.appstorefit_grupo1.data.repository.CheckoutRepository
import com.example.appstorefit_grupo1.data.repository.CheckoutResult
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CarritoViewModelTest {

    @RelaxedMockK
    lateinit var repo: CarritoRepository

    @RelaxedMockK
    lateinit var checkoutRepo: CheckoutRepository

    private lateinit var viewModel: CarritoViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        // Necesario porque el ViewModel usa viewModelScope (Dispatchers.Main)
        Dispatchers.setMain(testDispatcher)
        MockKAnnotations.init(this)

        // Flujos simples para que el ViewModel pueda inicializarse
        every { repo.observarItems() } returns MutableStateFlow(emptyList())
        every { repo.observarCantidadTotal() } returns MutableStateFlow(0)
        every { repo.observarTotalCLP() } returns MutableStateFlow(0)

        viewModel = CarritoViewModel(repo, checkoutRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun agregar_envia_mensaje_exito_cuando_repo_success() = runTest(testDispatcher) {
        coEvery {
            repo.agregar(any(), any(), any(), any(), any(), any())
        } returns Result.success(Unit)

        viewModel.agregar(
            idCat = 1L,
            idProd = 10L,
            modelo = "Modelo X",
            color = "Negro",
            talla = "42",
            precioUnit = 5000
        )

        val msg = viewModel.eventos.first()
        assertEquals("Agregado al carrito", msg)
    }

    @Test
    fun agregar_envia_mensaje_error_cuando_repo_failure() = runTest(testDispatcher) {
        coEvery {
            repo.agregar(any(), any(), any(), any(), any(), any())
        } returns Result.failure(IllegalStateException("No hay stock"))

        viewModel.agregar(
            idCat = 1L,
            idProd = 10L,
            modelo = "Modelo X",
            color = "Negro",
            talla = "42",
            precioUnit = 5000
        )

        val msg = viewModel.eventos.first()
        assertEquals("No hay stock", msg)
    }

    @Test
    fun disminuir_delega_en_repo() = runTest(testDispatcher) {
        viewModel.disminuir(1L, 10L, "Negro", "42")

        coVerify { repo.disminuir(1L, 10L, "Negro", "42") }
    }

    @Test
    fun eliminar_delega_en_repo() = runTest(testDispatcher) {
        viewModel.eliminar(1L, 10L, "Negro", "42")

        coVerify { repo.eliminar(1L, 10L, "Negro", "42") }
    }

    @Test
    fun limpiar_delega_en_repo() = runTest(testDispatcher) {
        viewModel.limpiar()

        coVerify { repo.limpiar() }
    }

    @Test
    fun onComprar_emite_mensaje_ok_cuando_resultado_es_Ok() = runTest(testDispatcher) {
        coEvery { checkoutRepo.confirmarCompra() } returns CheckoutResult.Ok

        viewModel.onComprar()

        val msg = viewModel.eventos.first()
        assertEquals("Compra confirmada. Gracias por tu compra.", msg)
    }
}