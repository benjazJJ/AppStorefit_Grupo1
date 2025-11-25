package com.example.appstorefit_grupo1.ui.ViewModel

import com.example.appstorefit_grupo1.data.repository.MensajeRepository
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
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(JUnit4::class)
class MensajesViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private lateinit var repo: MensajeRepository
    private lateinit var viewModel: MensajesViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)

        // mock relajado: si no stubbeamos algo, no lanza error
        repo = mockk(relaxed = true)

        viewModel = MensajesViewModel(repo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun enviarMensaje_ok_actualiza_estado_y_llama_repo() {
        // NO necesitamos coEvery aquí: el mock relajado no lanza excepción

        viewModel.enviarMensaje(
            idUsuarioRemitente = 1L,
            idRolDestinoSoporte = 3,
            contenido = "Hola"
        )
        dispatcher.scheduler.advanceUntilIdle()

        coVerify { repo.enviarMensajeCliente("Hola") }
        val estado = viewModel.envio.value
        assertEquals(true, estado.ok)
        assertEquals(false, estado.enviando)
        assertEquals(null, estado.error)
    }

    @Test
    fun enviarMensaje_error_actualiza_estado_con_error() {
        coEvery { repo.enviarMensajeCliente("Hola") } throws RuntimeException("Falla X")

        viewModel.enviarMensaje(
            idUsuarioRemitente = 1L,
            idRolDestinoSoporte = 3,
            contenido = "Hola"
        )
        dispatcher.scheduler.advanceUntilIdle()

        val estado = viewModel.envio.value
        assertEquals(false, estado.ok)
        assertEquals("Falla X", estado.error)
    }

    @Test
    fun responderMensaje_ok_actualiza_estado_y_llama_repo() {
        // Igual: no hace falta stub, mock relajado no lanza excepción

        viewModel.responderMensaje(
            threadId = 10L,
            idUsuarioSoporte = 2L,
            idUsuarioCliente = 1L,
            contenido = "Respuesta"
        )
        dispatcher.scheduler.advanceUntilIdle()

        coVerify { repo.responderSoporteAlCliente(10L, "Respuesta") }
        val estado = viewModel.respuesta.value
        assertEquals(true, estado.ok)
        assertEquals(false, estado.enviando)
        assertEquals(null, estado.error)
    }

    @Test
    fun responderMensaje_error_actualiza_estado_con_error() {
        coEvery { repo.responderSoporteAlCliente(any(), any()) } throws RuntimeException("No se pudo")

        viewModel.responderMensaje(
            threadId = 10L,
            idUsuarioSoporte = 2L,
            idUsuarioCliente = 1L,
            contenido = "Respuesta"
        )
        dispatcher.scheduler.advanceUntilIdle()

        val estado = viewModel.respuesta.value
        assertEquals(false, estado.ok)
        assertEquals("No se pudo", estado.error)
    }

    @Test
    fun limpiarEstadoEnvio_resetea_estado_envio() {
        coEvery { repo.enviarMensajeCliente(any()) } throws RuntimeException("Error X")
        viewModel.enviarMensaje(1L, 3, "Hola")
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.limpiarEstadoEnvio()

        val estado = viewModel.envio.value
        assertEquals(EnvioMensajeUi(), estado)
    }

    @Test
    fun limpiarEstadoRespuesta_resetea_estado_respuesta() {
        coEvery { repo.responderSoporteAlCliente(any(), any()) } throws RuntimeException("Error Y")
        viewModel.responderMensaje(10L, 2L, 1L, "Resp")
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.limpiarEstadoRespuesta()

        val estado = viewModel.respuesta.value
        assertEquals(RespuestaUi(), estado)
    }
}