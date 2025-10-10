package com.example.appstorefit_grupo1.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable // Pantalla Registro (solo navegación)
fun RegisterScreen(
    onRegistered: () -> Unit, // Acción para ir a Login
    onGoLogin: () -> Unit     // Acción alternativa a Login
) {
    val bg = MaterialTheme.colorScheme.tertiaryContainer // Fondo único

    Box(
        modifier = Modifier
            .fillMaxSize() // Ocupa todo
            .background(bg) // Fondo
            .padding(16.dp), // Margen
        contentAlignment = Alignment.Center // Centro
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) { // Estructura vertical
            Text(
                text = "Registro",
                style = MaterialTheme.typography.headlineSmall // Título
            )
            Spacer(Modifier.height(12.dp)) // Separación
            Text(
                text = "Pantalla de Registro (demo). Practica navegación con botones.",
                textAlign = TextAlign.Center // Centra el texto
            )
            Spacer(Modifier.height(20.dp)) // Separación

            // Botones con Row para variar la composición
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { // Espacio entre botones
                Button(onClick = onRegistered) { Text("Ir a Login") } // Navega a Login
                OutlinedButton(onClick = onGoLogin) { Text("Volver a Login") } // También a Login
            }
        }
    }
}