package com.example.appstorefit_grupo1.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RemoteModule {

    // Interceptor para ver las peticiones/respuestas en Logcat (útil mientras desarrollamos)
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Cliente HTTP compartido
    private val okHttp = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    // Crea un Retrofit con la base URL que le pases
    private fun buildRetrofit(baseUrl: String): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    // Métod0 genérico para crear cualquier API
    fun <T> create(baseUrl: String, service: Class<T>): T =
        buildRetrofit(baseUrl).create(service)
}
