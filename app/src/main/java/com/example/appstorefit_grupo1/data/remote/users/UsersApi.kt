package com.example.appstorefit_grupo1.data.remote.users

import com.example.appstorefit_grupo1.data.remote.dto.users.LoginRequest
import com.example.appstorefit_grupo1.data.remote.dto.users.LoginResponse
import com.example.appstorefit_grupo1.data.remote.dto.users.RegistroCompletoRequest
import com.example.appstorefit_grupo1.data.remote.dto.users.RegistroCompletoResponse
import com.example.appstorefit_grupo1.data.remote.dto.users.UpdateRolRequest
import com.example.appstorefit_grupo1.data.remote.dto.users.UsuarioDto
import com.example.appstorefit_grupo1.data.remote.dto.users.RolDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

// Interfaz Retrofit para comunicarse con users-service
interface UsersApi {

    // Login: valida correo + contraseña y devuelve datos básicos de usuario y rol
    @POST("api/v1/registros/login")
    suspend fun login(
        @Body body: LoginRequest
    ): LoginResponse

    // Registro completo: crea Usuario + Registro de credenciales
    @POST("api/v1/registros/registro-completo")
    suspend fun registroCompleto(
        @Body body: RegistroCompletoRequest
    ): RegistroCompletoResponse

    // Obtener usuario por RUT
    @GET("api/v1/usuarios/{rut}")
    suspend fun getUsuarioPorRut(
        @Path("rut") rut: String
    ): UsuarioDto

    // Obtener usuario por correo
    @GET("api/v1/usuarios/correo/{correo}")
    suspend fun getUsuarioPorCorreo(
        @Path("correo") correo: String
    ): UsuarioDto

    // Listar todos los roles
    @GET("api/v1/roles")
    suspend fun getRoles(): List<RolDto>

    // Obtener un rol por id
    @GET("api/v1/roles/{id}")
    suspend fun getRolPorId(
        @Path("id") id: Long
    ): RolDto

    // Actualizar el rol de un usuario y devolver el usuario actualizado
    @PUT("api/v1/usuarios/{rut}")
    suspend fun actualizarRolUsuario(
        @Path("rut") rut: String,
        @Body body: UpdateRolRequest
    ): UsuarioDto
}
