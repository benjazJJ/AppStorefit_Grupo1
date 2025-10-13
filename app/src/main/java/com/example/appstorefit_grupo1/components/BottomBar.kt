package com.example.appstorefit_grupo1.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.appstorefit_grupo1.navigation.Route
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem

@Composable
fun BottomBar(navController: NavController) {
    //ITEMS PARA MOSTRAR EN LA BARRA
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
            val selected = currentRoute == route.path
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (currentRoute != route.path) {
                        navController.navigate(route.path) {
                            // evita acumular destinos repetidos en la pila
                            launchSingleTop = true
                            restoreState = true
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                        }
                    }
                },
                icon = {
                    // Iconos por ruta
                    when (route) {
                        Route.Home -> Icon(Icons.Default.Home, contentDescription = "Home")
                        Route.Productos -> Icon(Icons.Default.List, contentDescription = "Productos")
                        Route.Carrito -> Icon(Icons.Default.ShoppingCart, contentDescription = "Carrito")
                        Route.Perfil -> Icon(Icons.Default.Person, contentDescription = "Perfil")
                        else -> Icon(Icons.Default.Home, contentDescription = null)
                    }
                },
                label = { Text(text = when(route) {
                    Route.Home -> "Inicio"
                    Route.Productos -> "Productos"
                    Route.Carrito -> "Carrito"
                    Route.Perfil -> "Perfil"
                    else -> ""
                }) }
            )
        }
    }
}