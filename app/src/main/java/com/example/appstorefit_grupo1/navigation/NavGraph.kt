package com.example.appstorefit_grupo1.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable // Pantalla Home (sin formularios, solo navegación/diseño)
fun HomeScreen(
    onGoLogin: () -> Unit,   // Acción a Login
    onGoRegister: () -> Unit // Acción a Registro
) {
    val bg = MaterialTheme.colorScheme.surfaceVariant // Fondo agradable para Home

    Box( // Contenedor a pantalla completa
        modifier = Modifier
            .fillMaxSize() // Ocupa todo
            .background(bg) // Aplica fondo
            .padding(16.dp), // Margen interior
        contentAlignment = Alignment.Center // Centra contenido
    ) {
        Column( // Estructura vertical
            horizontalAlignment = Alignment.CenterHorizontally // Centra hijos
        ) {
            // Cabecera como Row (ejemplo de estructura)
            Row(
                verticalAlignment = Alignment.CenterVertically // Centra vertical
            ) {
                Text( // Título Home
                    text = "Home",
                    style = MaterialTheme.typography.headlineSmall, // Estilo título
                    fontWeight = FontWeight.SemiBold // Seminegrita
                )
                Spacer(Modifier.width(8.dp)) // Separación horizontal
                AssistChip( // Chip decorativo (Material 3)
                    onClick = {}, // Sin acción (demo)
                    label = { Text("Navega desde arriba o aquí") } // Texto chip
                )
            }

            Spacer(Modifier.height(20.dp)) // Separación

            // Tarjeta con un mini “hero”
            ElevatedCard( // Card elevada para remarcar contenido
                modifier = Modifier.fillMaxWidth() // Ancho completo
            ) {
                Column(
                    modifier = Modifier.padding(16.dp), // Margen interno de la card
                    horizontalAlignment = Alignment.CenterHorizontally // Centrado
                ) {
                    Text(
                        "Demostración de navegación con TopBar + Drawer + Botones",
                        style = MaterialTheme.typography.titleMedium, // Estilo medio
                        textAlign = TextAlign.Center // Alineación centrada
                    )
                    Spacer(Modifier.height(12.dp)) // Separación
                    Text(
                        "Usa la barra superior (íconos y menú), el menú lateral o estos botones.",
                        style = MaterialTheme.typography.bodyMedium // Texto base
                    )
                }
            }

            Spacer(Modifier.height(24.dp)) // Separación

            // Botones de navegación principales
            Row( // Dos botones en fila
                horizontalArrangement = Arrangement.spacedBy(12.dp) // Espacio entre botones
            ) {
                Button(onClick = onGoLogin) { Text("Ir a Login") } // Navega a Login
                OutlinedButton(onClick = onGoRegister) { Text("Ir a Registro") } // A Registro
            }
        }
    }
}