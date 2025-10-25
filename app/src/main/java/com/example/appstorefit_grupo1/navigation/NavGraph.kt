package com.example.appstorefit_grupo1.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.composable
import com.example.appstorefit_grupo1.ui.screen.*
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.appstorefit_grupo1.session.SessionManager

private data class TopDest(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

//construimos el menú según el rol
private fun topDestinationsFor(roleId: Long?): List<TopDest> {
    val common = listOf(
        TopDest(Route.Productos.path, "Productos", Icons.Filled.Home),
        TopDest(Route.Carrito.path,   "Carrito",   Icons.Filled.ShoppingCart),
        TopDest(Route.Perfil.path,    "Perfil",    Icons.Filled.Person)
    )
    return if (roleId == 2L) { // 2L = ADMIN (ajusta si tu BD usa otro id)
        listOf(
            TopDest(Route.Panel.path, "Panel", Icons.Filled.Person) // puedes cambiar el ícono si prefieres
        ) + common
    } else {
        common
    }
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    widthClass: WindowWidthSizeClass
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    //obtenemos rol y generamos el menú
    val roleId = SessionManager.roleId
    val TOP_DESTINATIONS = remember(roleId) { topDestinationsFor(roleId) }

    fun navigateTopLevel(toRoute: String) {
        if (toRoute == currentRoute) return
        navController.navigate(toRoute) {
            launchSingleTop = true
            restoreState = true
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
        }
    }

    when (widthClass) {
        WindowWidthSizeClass.Expanded -> {
            Row(Modifier.fillMaxSize()) {
                NavigationRail {
                    TOP_DESTINATIONS.forEach { dest ->
                        val selected =
                            if (dest.route == Route.Productos.path)
                                (currentRoute?.startsWith(Route.Productos.path) == true) ||
                                        (currentRoute?.startsWith(Route.DetalleProducto.path) == true)
                            else
                                currentRoute?.startsWith(dest.route) == true

                        NavigationRailItem(
                            selected = selected,
                            onClick = { navigateTopLevel(dest.route) },
                            icon = { Icon(dest.icon, contentDescription = dest.label) },
                            label = { Text(dest.label) }
                        )
                    }
                }
                Box(Modifier.fillMaxSize()) {
                    GraphHost(navController = navController, widthClass = widthClass)
                }
            }
        }
        else -> {
            Scaffold(
                bottomBar = {
                    // mostramos la barra si estamos en alguna top-dest (dinámica) o en detalle de producto
                    if (TOP_DESTINATIONS.any { currentRoute?.startsWith(it.route) == true } ||
                        currentRoute?.startsWith(Route.DetalleProducto.path) == true) {
                        NavigationBar {
                            TOP_DESTINATIONS.forEach { dest ->
                                val selected =
                                    if (dest.route == Route.Productos.path)
                                        (currentRoute?.startsWith(Route.Productos.path) == true) ||
                                                (currentRoute?.startsWith(Route.DetalleProducto.path) == true)
                                    else
                                        currentRoute?.startsWith(dest.route) == true

                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { navigateTopLevel(dest.route) },
                                    icon = { Icon(dest.icon, contentDescription = dest.label) },
                                    label = { Text(dest.label) }
                                )
                            }
                        }
                    }
                }
            ) { inner ->
                Box(Modifier.padding(inner).fillMaxSize()) {
                    GraphHost(navController = navController, widthClass = widthClass)
                }
            }
        }
    }
}

@Composable
private fun GraphHost(
    navController: NavHostController,
    widthClass: WindowWidthSizeClass
) {
    NavHost(
        navController = navController,
        startDestination = Route.Splash.path
    ) {
        composable(Route.Splash.path) {
            SplashScreen(navController)
        }

        // Login -> Productos / Panel si es admin)
        composable(Route.Login.path) {
            LoginScreenVm(
                widthClass = widthClass,
                onLoginOkNavigateHome = {
                    navController.navigate(Route.Productos.path) {
                        popUpTo(Route.Login.path) { inclusive = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onLoginOkNavigateAdmin = {  // NUEVO
                    navController.navigate(Route.Panel.path) {
                        popUpTo(Route.Login.path) { inclusive = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onGoRegister = { navController.navigate(Route.Register.path) }
            )
        }


        // Register
        composable(Route.Register.path) {
            RegisterScreenVm(
                widthClass = widthClass,
                onRegisteredNavigateLogin = {
                    navController.navigate(Route.Login.path) {
                        popUpTo(Route.Register.path) { inclusive = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onGoLogin = { navController.navigate(Route.Login.path) }
            )
        }

        // Productos (inicio real tras login normal)
        composable(Route.Productos.path) {
            ProductosScreen(widthClass = widthClass, nav = navController)
        }

        // Carrito
        composable(Route.Carrito.path) {
            CarritoScreen(navController)
        }

        // Perfil
        composable(Route.Perfil.path) {
            PerfilScreen(navController = navController)
        }

        // Editar contraseña
        composable(Route.EditarContrasena.path) {
            EditarContrasenaScreen(navController = navController)
        }

        //Panel (solo admin)
        composable(Route.Panel.path) {
            val roleId = SessionManager.roleId
            if (roleId == 2L) {
                AdminScreen(navController = navController)
            } else {
                //si no es admin, volver a productos
                navController.popBackStack()
                navController.navigate(Route.Productos.path) {
                    launchSingleTop = true
                }
            }
        }

        // Detalle de producto
        composable(
            route = "${Route.DetalleProducto.path}?idCategoria={idCategoria}&modelo={modelo}",
            arguments = listOf(
                navArgument("idCategoria") { type = NavType.LongType },
                navArgument("modelo")      { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val idCategoria = backStackEntry.arguments!!.getLong("idCategoria")
            val modelo      = backStackEntry.arguments!!.getString("modelo")!!
            DetalleProductoScreen(
                navController = navController,
                idCategoria   = idCategoria,
                modelo        = modelo
            )
        }
    }
}
