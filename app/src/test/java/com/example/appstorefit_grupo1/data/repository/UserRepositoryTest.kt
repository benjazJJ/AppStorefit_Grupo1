package com.example.appstorefit_grupo1.data.repository

import com.example.appstorefit_grupo1.data.local.user.UserEntity
import com.example.appstorefit_grupo1.data.remote.dto.users.UpdatePerfilRequest
import com.example.appstorefit_grupo1.data.remote.dto.users.UsuarioDto
import com.example.appstorefit_grupo1.data.remote.users.UsersApi
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
class UserRepositoryTest {

    @RelaxedMockK lateinit var api: UsersApi
    private lateinit var repo: UserRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        SessionManager.clear()
        repo = UserRepository(api)
    }

    @After
    fun tearDown() {
        SessionManager.clear()
    }

    @Test
    fun login_falla_si_campos_vacios() = runBlocking {
        val res = repo.login("", "")

        assertTrue(res.isFailure)
    }

    @Test
    fun login_ok_guarda_session() = runBlocking {
        coEvery { api.login(any()) } returns com.example.appstorefit_grupo1.data.remote.dto.users.LoginResponse(
            success = true,
            message = null,
            rut = "11.111.111-1",
            nombre = "User",
            correo = "a@a.cl",
            rolId = 1L,
            rolNombre = "CLIENTE"
        )
        coEvery { api.getUsuarioPorRut(any(), any(), any()) } returns usuarioDto()

        val res = repo.login("user@test.cl", "pass")

        assertTrue(res.isSuccess)
        assertEquals("11.111.111-1", SessionManager.user?.rut)
    }

    @Test
    fun register_falla_por_campos_obligatorios() = runBlocking {
        val res = repo.register("", "", "", "", "", "", "", "")

        assertTrue(res.isFailure)
    }

    @Test
    fun changePassword_validaciones_basicas() = runBlocking {
        val res = repo.changePassword("", "", "", "")

        assertTrue(res.isFailure)
    }

    @Test
    fun updateAddressByEmail_ok_actualiza() = runBlocking {
        SessionManager.user = UserEntity("11.111.111-1", "Name", "a@a.cl", "", "", "")
        SessionManager.roleId = 1L
        SessionManager.roleName = "CLIENTE"
        val dto = usuarioDto()
        coEvery { api.getUsuarioPorCorreo(any(), any(), any()) } returns dto
        coEvery { api.actualizarPerfil(any(), any<UpdatePerfilRequest>(), any(), any()) } returns dto

        val res = repo.updateAddressByEmail("a@a.cl", "nueva")

        assertTrue(res.isSuccess)
        coVerify { api.actualizarPerfil("11.111.111-1", any(), any(), any()) }
    }

    private fun usuarioDto() = UsuarioDto(
        rut = "11.111.111-1",
        nombre = "User",
        apellidos = "Test",
        correo = "a@a.cl",
        telefono = "9",
        direccion = "",
        fechaNacimiento = "",
        fotoUri = ""
    )
}
