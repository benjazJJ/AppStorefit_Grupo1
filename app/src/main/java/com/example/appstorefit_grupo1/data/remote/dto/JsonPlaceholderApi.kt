package com.example.appstorefit_grupo1.data.remote.dto

import retrofit2.http.GET

interface JsonPlaceholderApi{
    //endpoint de comunicacion con la api rest
    @GET("/posts")
    suspend fun obtenerPublicaciones(): List<PostDto>


}