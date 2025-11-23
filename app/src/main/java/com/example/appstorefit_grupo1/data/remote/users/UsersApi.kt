package com.example.appstorefit_grupo1.data.remote.users

// DTOs de users-service
import com.example.appstorefit_grupo1.data.remote.dto.users.LoginRequest
import com.example.appstorefit_grupo1.data.remote.dto.users.LoginResponse
import com.example.appstorefit_grupo1.data.remote.dto.users.RegistroCompletoRequest
import com.example.appstorefit_grupo1.data.remote.dto.users.RegistroCompletoResponse
import com.example.appstorefit_grupo1.data.remote.dto.users.UsuarioDto
import com.example.appstorefit_grupo1.data.remote.dto.users.RolDto
import com.example.appstorefit_grupo1.data.remote.dto.users.UpdateRolRequest
import com.example.appstorefit_grupo1.data.remote.dto.users.ChangePasswordRequest
import com.example.appstorefit_grupo1.data.remote.dto.users.ChangePasswordResponse
import com.example.appstorefit_grupo1.data.remote.dto.users.UpdatePerfilRequest
import com.example.appstorefit_grupo1.data.remote.dto.users.UpdateFotoRequest
import com.example.appstorefit_grupo1.data.remote.dto.users.CheckResponse
import com.example.appstorefit_grupo1.data.remote.dto.users.AdminCrearUsuarioRequest
import com.example.appstorefit_grupo1.data.remote.dto.users.AdminActualizarUsuarioRequest
import com.example.appstorefit_grupo1.data.remote.dto.users.UsuarioConRolDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

// Interfaz Retrofit para comunicarse con users-service
interface UsersApi {

    // Login: valida correo + contraseña y devuelve info básica
    @POST("api/v1/registros/login")
    suspend fun login(
        @Body body: LoginRequest
    ): LoginResponse

    // Registro completo: crea Usuario + Registro de credenciales
    @POST("api/v1/registros/registro-completo")
    suspend fun registroCompleto(
        @Body body: RegistroCompletoRequest
    ): RegistroCompletoResponse

    // Cambiar contraseña (usuario autenticado)
    @POST("api/v1/registros/cambiar-contrasenia")
    suspend fun cambiarContrasenia(
        @Body body: ChangePasswordRequest
    ): ChangePasswordResponse

    // Obtener usuario por RUT
    @GET("api/v1/usuarios/{rut}")
    suspend fun getUsuarioPorRut(
        @Path("rut") rut: String,
        @Header("X-User-Rut") headerRut: String,
        @Header("X-User-Rol") headerRol: String
    ): UsuarioDto

    // Obtener usuario por correo
    @GET("api/v1/usuarios/correo/{correo}")
    suspend fun getUsuarioPorCorreo(
        @Path("correo") correo: String,
        @Header("X-User-Rut") headerRut: String,
        @Header("X-User-Rol") headerRol: String
    ): UsuarioDto

    // Actualizar perfil completo de un usuario (perfil)
    @PUT("api/v1/usuarios/{rut}/perfil")
    suspend fun actualizarPerfil(
        @Path("rut") rut: String,
        @Body body: UpdatePerfilRequest,
        @Header("X-User-Rut") headerRut: String,
        @Header("X-User-Rol") headerRol: String
    ): UsuarioDto

    // Actualizar solo la foto de perfil
    @PATCH("api/v1/usuarios/{rut}/foto")
    suspend fun actualizarFoto(
        @Path("rut") rut: String,
        @Body body: UpdateFotoRequest,
        @Header("X-User-Rut") headerRut: String,
        @Header("X-User-Rol") headerRol: String
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
        @Body body: UpdateRolRequest,
        @Header("X-User-Rut") headerRut: String,
        @Header("X-User-Rol") headerRol: String
    ): UsuarioDto

    //Checks para REGISTRO

    // ¿RUT disponible para registrar?
    @GET("api/v1/usuarios/check/rut/{rut}")
    suspend fun checkRut(
        @Path("rut") rut: String
    ): CheckResponse

    // ¿Correo disponible para registrar?
    @GET("api/v1/usuarios/check/correo/{correo}")
    suspend fun checkCorreo(
        @Path("correo") correo: String
    ): CheckResponse

    // ¿Teléfono disponible para registrar?
    @GET("api/v1/usuarios/check/telefono/{telefono}")
    suspend fun checkTelefono(
        @Path("telefono") telefono: String
    ): CheckResponse

    // Checks para EDITAR PERFIL

    // ¿Correo disponible para actualizar (considerando el mismo RUT)?
    @GET("api/v1/usuarios/check-actualizar/correo")
    suspend fun checkCorreoActualizar(
        @Query("rut") rut: String,
        @Query("correo") correo: String,
        @Header("X-User-Rut") headerRut: String,
        @Header("X-User-Rol") headerRol: String
    ): CheckResponse

    // ¿Teléfono disponible para actualizar (considerando el mismo RUT)?
    @GET("api/v1/usuarios/check-actualizar/telefono")
    suspend fun checkTelefonoActualizar(
        @Query("rut") rut: String,
        @Query("telefono") telefono: String,
        @Header("X-User-Rut") headerRut: String,
        @Header("X-User-Rol") headerRol: String
    ): CheckResponse

    //Zona ADMIN (requiere rol ADMIN)

    // Listar usuarios con su rol
    @GET("api/v1/admin/usuarios")
    suspend fun adminListUsuarios(
        @Header("X-User-Rut") headerRut: String,
        @Header("X-User-Rol") headerRol: String
    ): List<UsuarioConRolDto>

    // Detalle de un usuario con su rol
    @GET("api/v1/admin/usuarios/{rut}")
    suspend fun adminGetUsuario(
        @Path("rut") rut: String,
        @Header("X-User-Rut") headerRut: String,
        @Header("X-User-Rol") headerRol: String
    ): UsuarioConRolDto

    // Crear usuario desde panel admin (usuario + registro + rol)
    @POST("api/v1/admin/usuarios")
    suspend fun adminCrearUsuario(
        @Body body: AdminCrearUsuarioRequest,
        @Header("X-User-Rut") headerRut: String,
        @Header("X-User-Rol") headerRol: String
    ): UsuarioConRolDto

    // Actualizar datos de usuario desde panel admin
    @PUT("api/v1/admin/usuarios/{rut}")
    suspend fun adminActualizarUsuario(
        @Path("rut") rut: String,
        @Body body: AdminActualizarUsuarioRequest,
        @Header("X-User-Rut") headerRut: String,
        @Header("X-User-Rol") headerRol: String
    ): UsuarioConRolDto
}
