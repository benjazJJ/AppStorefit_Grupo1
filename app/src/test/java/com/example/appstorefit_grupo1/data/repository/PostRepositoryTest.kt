package com.example.appstorefit_grupo1.data.repository

import com.example.appstorefit_grupo1.data.remote.dto.JsonPlaceholderApi
import com.example.appstorefit_grupo1.data.remote.dto.PostDto
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PostRepositoryTest{
    @Test
    fun fetchPost_retorna_lista_valida() = runBlocking {
        val api = mockk<JsonPlaceholderApi>() //mock de la api (copia)
        val repo = PostsRepository(api)
        //json fake de retorno par el mock de la api
        val sample = listOf(PostDto(1,1,"Hola","Mensaje"))

        //Al mock como actuar
        coEvery { api.obtenerPublicaciones() } returns sample
        val result = repo.fetchPost()
        //Criterios de aceptaicón
        assertTrue(result.isSuccess)
        assertEquals(1,result.getOrNull()!!.size)
        assertEquals("Hola", result.getOrNull()!![0].title)
    }
}
