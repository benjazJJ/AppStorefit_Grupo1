package com.example.appstorefit_grupo1.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.appstorefit_grupo1.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onGoLogin: () -> Unit = {},
    onGoRegister: () -> Unit = {},
    onGoProductos: () -> Unit = {}
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
                    Text("Home", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.width(8.dp))
                    AssistChip(onClick = {}, label = { Text("Navega desde aquí") })
                }
                Spacer(Modifier.height(16.dp))
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Demostración de navegación", style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(8.dp))
                        Text("Usa TopBar/BottomBar o estos botones.", style = MaterialTheme.typography.bodyMedium)
                    }
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

@Preview(showBackground = true)
@Composable fun HomeScreenPreview() { HomeScreen() }
