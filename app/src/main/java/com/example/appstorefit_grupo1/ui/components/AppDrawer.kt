/**package com.example.appstorefit_grupo1.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import kotlin.collections.forEach


//Estructura para los items del menú
data class drawerItem(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)


@Composable // Componente Drawer para usar en ModalNavigationDrawer
fun AppDrawer(
    currentRoute: String?, // Ruta actual (para marcar seleccionado si quieres)
    items: List<drawerItem>, // Lista de ítems a mostrar
    modifier: Modifier = Modifier // Modificador opcional
) {
    ModalDrawerSheet( // Hoja que contiene el contenido del drawer
        modifier = modifier // Modificador encadenable
    ) {
        // Recorremos las opciones y pintamos ítems
        items.forEach { item -> // Por cada ítem
            NavigationDrawerItem( // Ítem con estados Material
                label = { Text(item.label) }, // Texto visible
                selected = false, // Puedes usar currentRoute == ... si quieres marcar
                onClick = item.onClick, // Acción al pulsar
                icon = { Icon(item.icon, contentDescription = item.label) }, // Ícono
                modifier = Modifier, // Sin mods extra
                colors = NavigationDrawerItemDefaults.colors() // Estilo por defecto
            )
        }
    }
}

@Composable
fun datosDrawerItem(
    onHome: () -> Unit,
    onLogin: () -> Unit,
    onRegister: () -> Unit,
): List<drawerItem> = listOf(
    drawerItem("Ir al Inicio", Icons.Filled.Home, onClick = onHome),
    drawerItem("Ir al Login", Icons.Filled.AccountCircle, onClick = onLogin),
    drawerItem("Ir al Registro", Icons.Filled.Person, onClick = onRegister),
)**/