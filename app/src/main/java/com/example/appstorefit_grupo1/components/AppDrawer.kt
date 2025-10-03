package com.example.appstorefit_grupo1.components

import android.graphics.drawable.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector


//Estructura para los items del menú
data class drawerItem(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)


//Crear funcion que rellene una lista con los items  del menú

@Composable
fun datosDrawerItem(
    onHome: () -> Unit,
    onLogin: () -> Unit,
    onRegister: () -> Unit,
): List<drawerItem> = listOf(
    drawerItem("Ir al Inicio", Icons.Filled.Home, onClick = onHome),
    drawerItem("Ir al Login", Icons.Filled.AccountCircle, onClick = onLogin),
    drawerItem("Ir al Registro", Icons.Filled.Person, onClick = onRegister),
)