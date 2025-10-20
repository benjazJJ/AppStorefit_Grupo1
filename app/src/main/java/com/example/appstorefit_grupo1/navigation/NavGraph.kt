// navigation/NavGraph.kt
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
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.composable
import com.example.appstorefit_grupo1.ui.screen.*

private data class TopDest(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)


private val TOP_DESTINATIONS = listOf(
    TopDest(Route.Home.path, "Inicio", Icons.Filled.Home),
    TopDest(Route.Productos.path, "Productos", Icons.Filled.ShoppingCart),
    TopDest(Route.Perfil.path, "Perfil", Icons.Filled.Person)
)

@Composable
fun AppNavGraph(
    navController: NavHostController,
    widthClass: WindowWidthSizeClass
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    // Helper de navegación que preserva estado y evita duplicados
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

    // ---------- Layout adaptativo de navegación ----------
    when (widthClass) {
        WindowWidthSizeClass.Expanded -> {
            // Tablets grandes / desktop: NavigationRail + contenido
            Row(Modifier.fillMaxSize()) {
                NavigationRail {
                    TOP_DESTINATIONS.forEach { dest ->
                        val selected = currentRoute?.startsWith(dest.route) == true
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
            // Teléfonos / medium: NavigationBar inferior + contenido
            androidx.compose.material3.Scaffold(
                bottomBar = {
                    // Oculta la barra en pantallas que no sean top-level (por ejemplo Login/Register)
                    if (TOP_DESTINATIONS.any { currentRoute?.startsWith(it.route) == true }) {
                        NavigationBar {
                            TOP_DESTINATIONS.forEach { dest ->
                                val selected = currentRoute?.startsWith(dest.route) == true
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

/**
 * NavHost real con todas las rutas. Separado para poder reutilizarlo
 * en ambos layouts (Rail o BottomBar).
 */
@Composable
private fun GraphHost(
    navController: NavHostController,
    widthClass: WindowWidthSizeClass
) {
    NavHost(
        navController = navController,
        startDestination = Route.Login.path
    ) {
        composable(Route.Home.path) {
            HomeScreen(
                widthClass = widthClass,
                onGoLogin = { navController.navigate(Route.Login.path) },
                onGoRegister = { navController.navigate(Route.Register.path) },
                onGoProductos = { navController.navigate(Route.Productos.path) }
            )
        }

        // Login: al terminar, reemplaza el stack para no volver con "atrás"
        composable(Route.Login.path) {
            LoginScreenVm(
                widthClass = widthClass,
                onLoginOkNavigateHome = {
                    navController.navigate(Route.Home.path) {
                        popUpTo(Route.Login.path) { inclusive = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onGoRegister = { navController.navigate(Route.Register.path) }
            )
        }

        // Register: usa el wrapper con VM (NO la UI directa)
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
                onGoLogin = { navController.navigate(Route.Login.path) },
                onGoToCamera = { navController.navigate(Route.Camera.path) }
            )
        }

        composable(Route.Productos.path) {
            ProductosScreen(widthClass = widthClass, nav = navController)
        }

        // ---- PERFIL (ver perfil) ----
        composable(Route.Perfil.path) {
            PerfilScreen(navController = navController)
        }

        composable(Route.EditarContrasena.path) {
            EditarContrasenaScreen(navController = navController)
        }

        composable(Route.Camera.path) {
            CameraScreen(
                onPhotoTaken = {
                    navController.previousBackStackEntry?.savedStateHandle?.set("photo_uri", it)
                    navController.popBackStack()
                },
                onCancel = { navController.popBackStack() }
            )
        }


        // Si quieres habilitar Carrito YA, agrega su pantalla y (opcional) súmalo a TOP_DESTINATIONS:
        // composable(Route.Carrito.path)       { CarritoScreen(widthClass = widthClass, navController) }
        // composable(Route.EditarPerfil.path)  { EditarPerfilScreen(widthClass = widthClass, navController) }
    }
}