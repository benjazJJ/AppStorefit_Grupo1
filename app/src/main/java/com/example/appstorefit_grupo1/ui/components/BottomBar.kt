package com.example.appstorefit_grupo1.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.appstorefit_grupo1.navigation.Route
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appstorefit_grupo1.ViewModel.CarritoViewModel
import com.example.appstorefit_grupo1.ViewModel.CarritoViewModelFactory
import com.example.appstorefit_grupo1.data.local.database.AppDatabase

@Composable
fun BottomBar(navController: NavController) {
    val items = listOf(
        Route.Productos,
        Route.Carrito,
        Route.Perfil
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route


    val ctx = LocalContext.current
    val carritoDao = remember { AppDatabase.getInstance(ctx).carritoDao() }
    val carritoVm: CarritoViewModel = viewModel(factory = CarritoViewModelFactory(carritoDao))
    val ui by carritoVm.uiState.collectAsStateWithLifecycle()

    NavigationBar {
        items.forEach { route ->
            val selected = currentRoute?.startsWith(route.path) == true

            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        navController.navigate(route.path) {
                            launchSingleTop = true
                            restoreState = true
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                        }
                    }
                },
                icon = {
                    when (route) {
                        Route.Productos -> Icon(Icons.Filled.Home, contentDescription = "Productos")
                        Route.Carrito -> {
                            BadgedBox(
                                badge = {
                                    if (ui.cantidadTotal > 0) Badge { Text(ui.cantidadTotal.toString()) }
                                }
                            ) {
                                Icon(Icons.Filled.ShoppingCart, contentDescription = "Carrito")
                            }
                        }
                        Route.Perfil -> Icon(Icons.Filled.Person, contentDescription = "Perfil")
                        else -> Icon(Icons.Filled.List, contentDescription = null)
                    }
                },
                label = {
                    Text(
                        when (route) {
                            Route.Productos -> "Productos"
                            Route.Carrito   -> "Carrito"
                            Route.Perfil    -> "Perfil"
                            else            -> ""
                        }
                    )
                }
            )
        }
    }
}