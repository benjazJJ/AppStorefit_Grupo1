package com.example.appstorefit_grupo1.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.example.appstorefit_grupo1.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    widthClass: WindowWidthSizeClass,
    onGoLogin: () -> Unit = {},
    onGoRegister: () -> Unit = {},
    onGoProductos: () -> Unit = {}
) {
    when (widthClass) {
        WindowWidthSizeClass.Compact -> HomeCompact(onGoLogin, onGoRegister, onGoProductos)
        else -> HomeExpanded(onGoLogin, onGoRegister, onGoProductos)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeCompact(
    onGoLogin: () -> Unit,
    onGoRegister: () -> Unit,
    onGoProductos: () -> Unit
) {
    Scaffold(topBar = { TopAppBar(title = { Text("StoreFit") }) }) { inner ->
        val bg = MaterialTheme.colorScheme.surfaceVariant
        Box(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .background(bg)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Home",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(Modifier.height(16.dp))
                Image(
                    painter = painterResource(id = R.drawable.storefitlogo),
                    contentDescription = "Logo App",
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = onGoProductos) { Text("Ver productos") }
                    OutlinedButton(onClick = onGoLogin) { Text("Login") }
                    OutlinedButton(onClick = onGoRegister) { Text("Registro") }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeExpanded(
    onGoLogin: () -> Unit,
    onGoRegister: () -> Unit,
    onGoProductos: () -> Unit
) {
    Scaffold(topBar = { TopAppBar(title = { Text("StoreFit") }) }) { inner ->
        val bg = MaterialTheme.colorScheme.surfaceVariant
        Row(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .background(bg)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    "Home",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = onGoProductos) { Text("Ver productos") }
                    OutlinedButton(onClick = onGoLogin) { Text("Login") }
                    OutlinedButton(onClick = onGoRegister) { Text("Registro") }
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.storefitlogo),
                    contentDescription = "Logo App",
                    modifier = Modifier.fillMaxWidth().height(220.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}


