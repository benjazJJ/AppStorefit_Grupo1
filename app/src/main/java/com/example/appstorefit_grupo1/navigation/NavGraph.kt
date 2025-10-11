// navigation/NavGraph.kt
package com.example.appstorefit_grupo1.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.appstorefit_grupo1.ui.screen.HomeScreen
import com.example.appstorefit_grupo1.ui.screen.LoginScreenVm
import com.example.appstorefit_grupo1.ui.screen.ProductosScreenPreview
import com.example.appstorefit_grupo1.ui.screen.RegisterScreenVm


@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController, startDestination = Route.Home.path) {

        composable(Route.Home.path) {
            HomeScreen(
                onGoLogin = { navController.navigate(Route.Login.path) },
                onGoRegister = { navController.navigate(Route.Register.path) }
            )
        }
        composable(Route.Productos.path)    { ProductosScreenPreview() }
    }
}
