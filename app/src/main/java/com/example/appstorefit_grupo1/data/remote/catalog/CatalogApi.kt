package com.example.appstorefit_grupo1.data.remote.catalog

import com.example.appstorefit_grupo1.data.remote.dto.catalog.CategoriaDto
import com.example.appstorefit_grupo1.data.remote.dto.catalog.ProductoDto
import com.example.appstorefit_grupo1.data.remote.dto.catalog.StockReservaItemDto
import com.example.appstorefit_grupo1.data.remote.dto.catalog.CategoriaResponseDto
import com.example.appstorefit_grupo1.data.remote.dto.catalog.ProductoResponseDto
import com.example.appstorefit_grupo1.data.remote.dto.catalog.MessageResponseDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/* Interfaz Retrofit para comunicarse con catalog-service */
interface CatalogApi {

    // CATEGORIAS
    // GET
    @GET("api/v1/categorias")
    suspend fun getCategorias(): List<CategoriaDto>

    // GET
    @GET("api/v1/categorias/{id}")
    suspend fun getCategoriaPorId(
        @Path("id") id: Long
    ): CategoriaDto

    // POST
    @POST("api/v1/categorias")
    suspend fun crearCategoria(
        @Body body: CategoriaDto
    ): CategoriaResponseDto

    // PUT
    @PUT("api/v1/categorias/{id}")
    suspend fun actualizarCategoria(
        @Path("id") id: Long,
        @Body body: CategoriaDto
    ): CategoriaResponseDto

    // DELETE
    @DELETE("api/v1/categorias/{id}")
    suspend fun eliminarCategoria(
        @Path("id") id: Long
    ): MessageResponseDto

    // PRODUCTOS

    // GET
    @GET("api/v1/productos")
    suspend fun getProductos(): List<ProductoDto>

    // GET
    @GET("api/v1/productos/{categoriaId}/{productoId}")
    suspend fun getProductoPorIds(
        @Path("categoriaId") categoriaId: Long,
        @Path("productoId") productoId: Long
    ): ProductoDto

    // GET
    @GET("api/v1/productos/categoria/{categoriaId}")
    suspend fun getProductosPorCategoria(
        @Path("categoriaId") categoriaId: Long
    ): List<ProductoDto>

    // POST
    @POST("api/v1/productos")
    suspend fun crearProducto(
        @Body body: ProductoDto
    ): ProductoResponseDto

    // PUT
    @PUT("api/v1/productos/{categoriaId}/{productoId}")
    suspend fun actualizarProducto(
        @Path("categoriaId") categoriaId: Long,
        @Path("productoId") productoId: Long,
        @Body body: ProductoDto
    ): ProductoResponseDto

    // DELETE
    @DELETE("api/v1/productos/{categoriaId}/{productoId}")
    suspend fun eliminarProducto(
        @Path("categoriaId") categoriaId: Long,
        @Path("productoId") productoId: Long
    ): MessageResponseDto

    // STOCK
    @POST("api/v1/productos/stock/reservar")
    suspend fun reservarStock(
        @Body items: List<StockReservaItemDto>
    ): MessageResponseDto
}