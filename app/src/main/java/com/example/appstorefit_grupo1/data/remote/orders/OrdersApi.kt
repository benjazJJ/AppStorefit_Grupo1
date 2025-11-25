package com.example.appstorefit_grupo1.data.remote.orders

import com.example.appstorefit_grupo1.data.remote.dto.orders.CompraDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface OrdersApi {

    //----Admin Lista todas las compras----
    @GET("api/v1/compras")
    suspend fun getAllCompras(
        @Header("X-User-Rut") headerRut: String,
        @Header("X-User-Rol") headerRol: String
    ): List<CompraDto>

    //----Obtener compra por id (dueño o admin)
    @GET("api/v1/compras/{id}")
    suspend fun getCompraById(
        @Path("id") id: Long,
        @Header("X-User-Rut") headerRut: String,
        @Header("X-User-Rol") headerRol: String
    ): CompraDto

    //----Obtener compra por RUT (cliente)
    @GET("api/v1/compras/usuario/{rut}")
    suspend fun getCompraByRut(
        @Path("rut") rut: String,
        @Header("X-User-Rut") headerRut: String,
        @Header("X-User-Rol") headerRol: String
    ): List<CompraDto>

    //----Total gastado por RUT
    @GET("api/v1/compras/usuario/{rut}/total")
    suspend fun getTotalGastado(
        @Path("rut") rut: String,
        @Header("X-User-Rut") headerRut: String,
        @Header("X-User-Rol") headerRol: String
    ): Int

    //----Crear una nueva compra (solo cliente)
    @POST("api/v1/compras")
    suspend fun crearCompra(
        @Header("X-User-Rut") headerRut: String,
        @Header("X-User-Rol") headerRol: String,
        @Body compra: CompraDto
    ): CompraDto

}
