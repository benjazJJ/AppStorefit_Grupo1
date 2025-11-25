package com.example.appstorefit_grupo1.data.repository

import com.example.appstorefit_grupo1.data.local.Categoria.CategoriaDao
import com.example.appstorefit_grupo1.data.local.Categoria.CategoriaEntity
import com.example.appstorefit_grupo1.data.local.Categoria.CategoriaResumen
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
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
class CategoriaRepositoryTest {

    @RelaxedMockK lateinit var dao: CategoriaDao
    private lateinit var repo: CategoriaRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        repo = CategoriaRepository(dao)
    }

    @Test
    fun create_inserta_cuando_nombre_es_nuevo() = runBlocking {
        coEvery { dao.getByNombre("Fitness") } returns null
        coEvery { dao.insert(any()) } returns 1L

        val result = repo.create("  Fitness  ")

        assertTrue(result.isSuccess)
        coVerify { dao.insert(match { it.nombre == "Fitness" }) }
    }

    @Test
    fun create_falla_por_nombre_vacio() = runBlocking {
        val result = repo.create("   ")

        assertTrue(result.isFailure)
        val ex = result.exceptionOrNull()
        assertTrue(ex is IllegalArgumentException)
        assertTrue(ex?.message?.contains("no puede estar vac", ignoreCase = true) == true)
        coVerify(exactly = 0) { dao.insert(any()) }
    }

    @Test
    fun create_falla_por_nombre_duplicado() = runBlocking {
        coEvery { dao.getByNombre("Fitness") } returns CategoriaEntity(1, "Fitness")

        val result = repo.create("Fitness")

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { dao.insert(any()) }
    }

    @Test
    fun getAll_devuelve_lista() = runBlocking {
        coEvery { dao.getAll() } returns listOf(CategoriaEntity(1, "A"))

        val result = repo.getAll()

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
    }

    @Test
    fun getById_ok() = runBlocking {
        coEvery { dao.getById(5) } returns CategoriaEntity(5, "Z")

        val result = repo.getById(5)

        assertTrue(result.isSuccess)
        assertEquals(5L, result.getOrNull()?.id)
    }

    @Test
    fun getById_falla_si_no_existe() = runBlocking {
        coEvery { dao.getById(5) } returns null

        val result = repo.getById(5)

        assertTrue(result.isFailure)
    }

    @Test
    fun update_ok() = runBlocking {
        val cat = CategoriaEntity(1, "Original")
        coEvery { dao.getByNombre("Nuevo") } returns null
        coEvery { dao.update(any()) } returns 1

        val result = repo.update(cat.copy(nombre = "Nuevo"))

        assertTrue(result.isSuccess)
        coVerify { dao.update(cat.copy(nombre = "Nuevo")) }
    }

    @Test
    fun update_falla_por_nombre_vacio() = runBlocking {
        val cat = CategoriaEntity(1, "Original")

        val result = repo.update(cat.copy(nombre = ""))

        assertTrue(result.isFailure)
        val ex = result.exceptionOrNull()
        assertTrue(ex is IllegalArgumentException)
        assertTrue(ex?.message?.contains("Nombre", ignoreCase = true) == true)
        coVerify(exactly = 0) { dao.update(any()) }
    }


    @Test
    fun update_falla_por_nombre_ocupado_por_otro() = runBlocking {
        val cat = CategoriaEntity(1, "Original")
        coEvery { dao.getByNombre("Nuevo") } returns CategoriaEntity(2, "Nuevo")

        val result = repo.update(cat.copy(nombre = "Nuevo"))

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { dao.update(any()) }
    }

    @Test
    fun update_falla_si_no_actualiza_filas() = runBlocking {
        val cat = CategoriaEntity(1, "Original")
        coEvery { dao.getByNombre("Nuevo") } returns null
        coEvery { dao.update(any()) } returns 0

        val result = repo.update(cat.copy(nombre = "Nuevo"))

        assertTrue(result.isFailure)
    }

    @Test
    fun renombrar_ok() = runBlocking {
        coEvery { dao.getByNombre("Nuevo") } returns null
        coEvery { dao.renombrar(1, "Nuevo") } returns 1

        val result = repo.renombrar(1, "Nuevo")

        assertTrue(result.isSuccess)
        coVerify { dao.renombrar(1, "Nuevo") }
    }

    @Test
    fun renombrar_falla_por_nombre_vacio() = runBlocking {
        val result = repo.renombrar(1, "   ")

        assertTrue(result.isFailure)
        assertEquals("Nombre requerido", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { dao.renombrar(any(), any()) }
    }

    @Test
    fun renombrar_falla_por_nombre_duplicado() = runBlocking {
        coEvery { dao.getByNombre("Nuevo") } returns CategoriaEntity(2, "Nuevo")

        val result = repo.renombrar(1, "Nuevo")

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { dao.renombrar(any(), any()) }
    }

    @Test
    fun renombrar_falla_si_no_actualiza_filas() = runBlocking {
        coEvery { dao.getByNombre("Nuevo") } returns null
        coEvery { dao.renombrar(1, "Nuevo") } returns 0

        val result = repo.renombrar(1, "Nuevo")

        assertTrue(result.isFailure)
    }

    @Test
    fun observeResumen_emite_lo_del_dao() = runBlocking {
        val esperado = listOf(CategoriaResumen(1, "A", productos = 2, modelos = 1))
        coEvery { dao.observeResumen() } returns flowOf(esperado)

        val result = repo.observeResumen().first()

        assertEquals(esperado, result)
        verify { dao.observeResumen() }
    }
}