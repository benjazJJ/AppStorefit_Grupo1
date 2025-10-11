package com.example.appstorefit_grupo1.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview


@OptIn(ExperimentalMaterial3Api::class)
@Composable

fun AppTopBar(
    onHome: () -> Unit,
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    onOpenDrawer: () -> Unit,
){
    //Variable para manipular el estado del menu desplegable
    var showMenu by remember { mutableStateOf(false) }

    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        title = {
            Text(
                "Demo Top Center APP BAR",
                style = MaterialTheme.typography.headlineMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(onClick = onOpenDrawer) {
                Icon(imageVector = Icons.Filled.Menu, contentDescription = "Menú")
            }
        },
        actions = {
            IconButton(onClick = onHome) {
                Icon(imageVector = Icons.Filled.Home, contentDescription = "Home")
            }
            IconButton(onClick = onLogin) {
                Icon(imageVector = Icons.Filled.Home, contentDescription = "Login")
            }
            IconButton(onClick = onRegister) {
                Icon(imageVector = Icons.Filled.Home, contentDescription = "Register")
            }
            DropdownMenu(
                expanded = showMenu ,
                onDismissRequest = { showMenu = false}
            ) {
                DropdownMenuItem(
                    text = {Text("Inicio")},
                    onClick = { showMenu = false; onHome}
                )
                DropdownMenuItem(
                    text = {Text("Login")},
                    onClick = { showMenu = false; onLogin}
                )
                DropdownMenuItem(
                    text = {Text("Register")},
                    onClick = { showMenu = false; onRegister}
                )
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun AppTopBarPreview(){
    AppTopBar(
        onHome = {},
        onLogin = {},
        onRegister = {},
        onOpenDrawer = {})
}
