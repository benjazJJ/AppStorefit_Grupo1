package com.example.appstorefit_grupo1.navigation

import android.net.Uri

sealed class Route(val path: String){

    // Auth
    data object Splash           : Route("splash")
    data object Login            : Route("login")            // Ruta para login screen
    data object Register         : Route("registro")         // Ruta para register screen

    // App
    data object Productos        : Route("productos")        // Ruta para productos screen
    data object Carrito          : Route("carrito")          // Ruta para carrito screen
    data object Perfil           : Route("perfil")           // Ruta para perfil screen
    data object EditarContrasena : Route("editarContrasena") // Ruta para editar contraseña screen

    data object Panel            : Route("panel")

    data object DetalleProducto  : Route("detalleProducto") {
        fun create(idCategoria: Long, modelo: String): String =
            "detalleProducto?idCategoria=$idCategoria&modelo=${Uri.encode(modelo)}"
    }
}
