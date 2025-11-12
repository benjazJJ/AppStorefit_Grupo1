package com.example.appstorefit_grupo1.data.repository

import com.example.appstorefit_grupo1.data.remote.RemoteModule
import com.example.appstorefit_grupo1.data.remote.dto.JsonPlaceholderApi
import com.example.appstorefit_grupo1.data.remote.dto.PostDto

class PostsRepository(
    private val api: JsonPlaceholderApi = RemoteModule.create(JsonPlaceholderApi::class.java)
){
    //funciones para ejecutar cada endpoint de mi api
    suspend fun fetchPost(): Result<List<PostDto>> =try {
        //llamamos a la api
        val data = api.obtenerPublicaciones()
        Result.success(data)
    }catch (e: Exception){
        Result.failure(e)
    }

}