package com.example.appstorefit_grupo1.data.repository
import com.example.appstorefit_grupo1.data.local.Compras.CompraConDetalles
import com.example.appstorefit_grupo1.data.local.Compras.CompraDetalleEntity
import com.example.appstorefit_grupo1.data.local.Compras.CompraEntity
import com.example.appstorefit_grupo1.data.remote.dto.orders.CompraDetalleDto
import com.example.appstorefit_grupo1.data.remote.dto.orders.CompraDto
import com.example.appstorefit_grupo1.data.remote.orders.OrdersApi
import com.example.appstorefit_grupo1.session.SessionManager
import retrofit2.HttpException
import kotlin.math.abs


 // Toma el usuario y rol actual desde SessionManager
 // Arma los headers X-User-Rut y X-User-Rol (CLIENTE / ADMIN / SOPORTE)
 // Llama a OrdersApi
 // Mapea CompraDto → CompraConDetalles para reutilizar la UI que ya tienes.

class OrdersRepository(
    private val api: OrdersApi
) {


    // Helpers de sesión / headers
     //* Obtiene rut + rol desde SessionManager y los prepara para enviar como headers.
     //* Lanza IllegalStateException si no hay sesión.
    private fun headersOrThrow(): Pair<String, String> {
        val user = SessionManager.user
            ?: throw IllegalStateException("Inicia sesión para operar con compras.")
        val rut = user.rut.trim()
        require(rut.isNotBlank()) { "RUT no disponible en la sesión." }

        val roleId = SessionManager.roleId ?: 0L
        // Mapeo 100% alineado con lo que se sembró en AppDatabase y con los microservicios
        val rolHeader = when (roleId) {
            1L -> "CLIENTE"
            2L -> "ADMIN"
            3L -> "SOPORTE"
            else -> roleId.toString()
        }

        return rut to rolHeader
    }






     // Convierte una CompraDto (del microservicio) a tu modelo local CompraConDetalles,
     // usando CompraEntity + CompraDetalleEntity.

     // Si por algún motivo el backend no manda idCompra o idDetalle, generamos IDs
     // deterministas para evitar nulls en la app.

    private fun CompraDto.toCompraConDetalles(fallbackIndex: Int = 0): CompraConDetalles {
        val safeId = idCompra
            ?: abs((rutUsuario + (fechaMillis ?: 0L) + fallbackIndex).hashCode().toLong()) + 1L
        val fecha = fechaMillis ?: System.currentTimeMillis()

        val compra = CompraEntity(
            idCompra = safeId,
            rutUsuario = rutUsuario,
            fechaMillis = fecha
        )

        val detalles = detalles.mapIndexed { idx, d ->
            val detId = d.idDetalle ?: safeId * 10_000 + idx + 1
            CompraDetalleEntity(
                idDetalle = detId,
                idCompra = safeId,
                idProducto = d.idProducto,
                nombreProducto = d.nombreProducto,
                cantidad = d.cantidad,
                precioUnitario = d.precioUnitario
            )
        }

        return CompraConDetalles(
            compra = compra,
            detalles = detalles
        )
    }


     //* Convierte tu snapshot de carrito (modelo ya usado en CompraRepository)
     //* a CompraDetalleDto para enviar al microservicio.

    private fun ItemCarritoSnapshot.toDetalleDto(): CompraDetalleDto =
        CompraDetalleDto(
            idDetalle = null,              // lo genera el backend
            idProducto = idProducto,
            nombreProducto = nombreProducto,
            cantidad = cantidad,
            precioUnitario = precioUnitario
        )

    // =========================================================
    // OPERACIONES PÚBLICAS
    // =========================================================


     // CLIENTE: crea una compra en orders-service a partir de los ítems del carrito.
     //Valida que haya sesión.
     //Valida que el carrito no esté vacío.
     //Envía X-User-Rut y X-User-Rol como headers.
     //El backend valida que el rut del body coincida con el del header.

     // @return Result con CompraConDetalles para que la UI pueda reutilizar
     //         lo mismo que usa el historial local.

    suspend fun crearCompra(
        items: List<ItemCarritoSnapshot>
    ): Result<CompraConDetalles> {
        if (items.isEmpty()) {
            return Result.failure(IllegalArgumentException("El carrito está vacío."))
        }

        val (rut, rol) = headersOrThrow()

        val body = CompraDto(
            idCompra = null,          // lo genera el backend
            rutUsuario = rut,
            fechaMillis = null,       // el backend pone System.currentTimeMillis()
            detalles = items.map { it.toDetalleDto() }
        )

        return try {
            val dto = api.crearCompra(
                headerRut = rut,
                headerRol = rol,
                compra = body
            )
            Result.success(dto.toCompraConDetalles())
        } catch (e: HttpException) {
            val msg = when (e.code()) {
                400 -> "Datos de compra inválidos."
                401 -> "No estás autenticado."
                403 -> "No tienes permisos para realizar esta compra."
                404 -> "Recurso de pedidos no encontrado."
                409 -> "Conflicto al crear la compra."
                503 -> "Servicios externos no disponibles, intenta más tarde."
                else -> "Error al comunicarse con pedidos (${e.code()})."
            }
            Result.failure(IllegalStateException(msg, e))
        } catch (e: Exception) {
            Result.failure(IllegalStateException("No se pudo crear la compra: ${e.message}", e))
        }
    }

    // * CLIENTE / ADMIN: obtiene una compra por su ID.
    //* El microservicio valida si el usuario actual es dueño de la compra o ADMIN.

    suspend fun obtenerCompra(idCompra: Long): Result<CompraConDetalles> = runCatching {
        val (rut, rol) = headersOrThrow()
        api.getCompraById(
            id = idCompra,
            headerRut = rut,
            headerRol = rol
        ).toCompraConDetalles()
    }


     // CLIENTE: historial del usuario actual.
     // ADMIN: puede pedir historial de cualquier rut (backend valida).
     // Si rut es null o vacío → usa el rut de la sesión.

    suspend fun historialCliente(rut: String? = null): Result<List<CompraConDetalles>> = runCatching {
        val (headerRut, headerRol) = headersOrThrow()
        val targetRut = rut?.takeIf { it.isNotBlank() } ?: headerRut

        api.getCompraByRut(
            rut = targetRut,
            headerRut = headerRut,
            headerRol = headerRol
        ).mapIndexed { idx, dto -> dto.toCompraConDetalles(idx) }
    }


     // CLIENTE: total gastado por el usuario actual.
     // ADMIN: puede consultar el total de cualquier rut.

    suspend fun totalGastado(rut: String? = null): Result<Int> = runCatching {
        val (headerRut, headerRol) = headersOrThrow()
        val targetRut = rut?.takeIf { it.isNotBlank() } ?: headerRut

        api.getTotalGastado(
            rut = targetRut,
            headerRut = headerRut,
            headerRol = headerRol
        )
    }


    // ADMIN: lista todas las compras existentes.

    suspend fun adminListarCompras(): Result<List<CompraConDetalles>> = runCatching {
        val (rut, rol) = headersOrThrow()

        // Validación rápida en el cliente (el backend igual vuelve a validar)
        if (SessionManager.roleId != 2L) {
            error("Solo un ADMIN puede listar todas las compras.")
        }

        api.getAllCompras(
            headerRut = rut,
            headerRol = rol
        ).mapIndexed { idx, dto -> dto.toCompraConDetalles(idx) }
    }
}
