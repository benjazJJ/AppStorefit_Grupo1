package com.example.appstorefit_grupo1.data.remote.catalog

import com.example.appstorefit_grupo1.data.remote.dto.catalog.ProductoDto
import com.example.appstorefit_grupo1.data.remote.dto.catalog.ProductoResponseDto
import com.example.appstorefit_grupo1.data.remote.dto.catalog.MessageResponseDto
import com.example.appstorefit_grupo1.data.remote.dto.catalog.StockReservaItemDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface CatalogApi {

    // ======= PRODUCTOS =======

    @GET("api/v1/productos")
    suspend fun getProductos(
        @Header("X-User-Rut") headerRut: String,
        @Header("X-User-Rol") headerRol: String
    ): List<ProductoDto>

    @GET("api/v1/productos/{categoriaId}/{productoId}")
    suspend fun getProductoPorIds(
        @Path("categoriaId") categoriaId: Long,
        @Path("productoId")  productoId: Long,
        @Header("X-User-Rut") headerRut: String,
        @Header("X-User-Rol") headerRol: String
    ): ProductoDto

    @GET("api/v1/productos/categoria/{categoriaId}")
    suspend fun getProductosPorCategoria(
        @Path("categoriaId") categoriaId: Long,
        @Header("X-User-Rut") headerRut: String,
        @Header("X-User-Rol") headerRol: String
    ): List<ProductoDto>

    @POST("api/v1/productos")
    suspend fun crearProducto(
        @Body body: ProductoDto,
        @Header("X-User-Rut") headerRut: String,
        @Header("X-User-Rol") headerRol: String
    ): ProductoResponseDto

    @PUT("api/v1/productos/{categoriaId}/{productoId}")
    suspend fun actualizarProducto(
        @Path("categoriaId") categoriaId: Long,
        @Path("productoId")  productoId: Long,
        @Body body: ProductoDto,
        @Header("X-User-Rut") headerRut: String,
        @Header("X-User-Rol") headerRol: String
    ): ProductoResponseDto

    @DELETE("api/v1/productos/{categoriaId}/{productoId}")
    suspend fun eliminarProducto(
        @Path("categoriaId") categoriaId: Long,
        @Path("productoId")  productoId: Long,
        @Header("X-User-Rut") headerRut: String,
        @Header("X-User-Rol") headerRol: String
    ): MessageResponseDto

    // ======= STOCK / RESERVA =======

    // Este endpoint en el micro no exige headers
    @POST("api/v1/productos/stock/reservar")
    suspend fun reservarStock(
        @Body items: List<StockReservaItemDto>
    ): MessageResponseDto
}