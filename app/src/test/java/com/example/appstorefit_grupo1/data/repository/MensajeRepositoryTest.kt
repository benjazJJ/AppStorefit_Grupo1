package com.example.appstorefit_grupo1.data.repository

import com.example.appstorefit_grupo1.data.remote.dto.support.MensajeConRespuestaDto
import com.example.appstorefit_grupo1.data.remote.dto.support.ResponderMensajeRequest
import com.example.appstorefit_grupo1.data.remote.dto.support.SupportDto
import com.example.appstorefit_grupo1.data.remote.support.SupportApi
import com.example.appstorefit_grupo1.session.SessionManager
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MensajeRepositoryTest {

    @RelaxedMockK lateinit var api: SupportApi
    private lateinit var repo: MensajeRepository

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
        repo = MensajeRepository(api, SessionManager)
    }

    @After
    fun tearDown() {
        SessionManager.clear()
    }

    @Test
    fun enviarMensajeCliente_falla_por_contenido_vacio() = runBlocking {
        val result = kotlin.runCatching { repo.enviarMensajeCliente("   ") }

        assertTrue(result.isFailure)
    }

    @Test
    fun enviarMensajeCliente_ok_envia_headers() = runBlocking {
        val dto = SupportDto(1, "11.111.111-1", null, null, "hola", 0, false, false, null, null, null)
        coEvery { api.enviarMensajeCliente(any(), any(), any()) } returns dto

        val res = repo.enviarMensajeCliente(" hola ")

        assertEquals(dto, res)
        coVerify { api.enviarMensajeCliente("11.111.111-1", "ADMIN", any()) }
    }

    @Test
    fun obtenerBandejaSoporte_ok() = runBlocking {
        val esperado = listOf<MensajeConRespuestaDto>()
        coEvery { api.getBandejaSoporte(any(), any(), any()) } returns esperado

        val res = repo.obtenerBandejaSoporte()

        assertEquals(esperado, res)
    }

    @Test
    fun responderSoporteAlCliente_ok() = runBlocking {
        val dto = SupportDto(2, "11.111.111-1", null, "22.222.222-2", "respuesta", 0, false, true, 1, 1, null)
        coEvery { api.responderMensajeCliente(any(), any(), any(), any()) } returns dto

        val res = repo.responderSoporteAlCliente(1, " ok ")

        assertEquals(dto, res)
        coVerify { api.responderMensajeCliente(1, "11.111.111-1", "ADMIN", any<ResponderMensajeRequest>()) }
    }
}
