package com.example.appstorefit_grupo1.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.appstorefit_grupo1.navigation.Route

@Composable
fun BottomBar(navController: NavController) {
    // Tabs visibles en la barra inferior
    val items = listOf(
        Route.Home,
        Route.Productos,
        Route.Carrito,
        Route.Perfil
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

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
                        Route.Home -> Icon(Icons.Filled.Home, contentDescription = "Inicio")
                        Route.Productos -> Icon(Icons.Filled.List, contentDescription = "Productos")
                        Route.Carrito -> Icon(Icons.Filled.ShoppingCart, contentDescription = "Carrito")
                        Route.Perfil -> Icon(Icons.Filled.Person, contentDescription = "Perfil")
                        else -> Icon(Icons.Filled.Home, contentDescription = null)
                    }
                },
                label = {
                    Text(
                        when (route) {
                            Route.Home -> "Inicio"
                            Route.Productos -> "Productos"
                            Route.Carrito -> "Carrito"
                            Route.Perfil -> "Perfil"
                            else -> ""
                        }
                    )
                }
            )
        }
    }
}