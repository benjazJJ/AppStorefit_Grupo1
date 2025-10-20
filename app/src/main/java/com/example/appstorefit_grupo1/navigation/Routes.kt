package com.example.appstorefit_grupo1.navigation

sealed class Route(val path: String){

    data object Login: Route("login") //Ruta para login screen
    data object Register: Route("registro") //Ruta para register screen
    data object Home: Route("home") //Ruta para home screen
    data object Productos: Route("productos") //Ruta para productos screen
    data object Carrito: Route("carrito") //Ruta para carrito screen
    data object Configuracion: Route("configuracion") //Ruta para configuracion screen
    data object Perfil: Route("perfil") //Ruta para perfil screen
    data object EditarContrasena : Route("editarContrasena") //Ruta para editar contraseña en perfil screen

}