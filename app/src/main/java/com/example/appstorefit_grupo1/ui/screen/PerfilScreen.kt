package com.example.appstorefit_grupo1.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.appstorefit_grupo1.navigation.Route
import com.example.appstorefit_grupo1.ui.theme.SF_Blue
import com.example.appstorefit_grupo1.ui.theme.SF_Purple
import com.example.appstorefit_grupo1.ui.theme.SF_Teal

// ---- Modelo simple (mock por ahora) ----
data class ProfileData(
    val nombreCompleto: String = "Benjamín Palma",
    val correo: String = "benja@storefit.cl",
    val rut: String = "12.345.678-9",
    val telefono: String = "+56 9 1234 5678",
    val direccion: String = "Av. Siempre Viva 123, Santiago",
    val fechaRegistro: String = "01/10/2025"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    navController: NavHostController,
    datos: ProfileData = ProfileData()
) {
    val items = listOf(
        PerfilItemUi("Nombre completo", datos.nombreCompleto, Icons.Filled.Person),
        PerfilItemUi("Correo", datos.correo, Icons.Filled.Email),
        PerfilItemUi("RUT", datos.rut, Icons.Filled.AssignmentInd),
        PerfilItemUi("Teléfono", datos.telefono, Icons.Filled.Phone),
        PerfilItemUi("Dirección", datos.direccion, Icons.Filled.LocationOn),
        PerfilItemUi("Fecha de registro", datos.fechaRegistro, Icons.Filled.CalendarMonth)
    )

    Scaffold(
        containerColor = Color.White,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("MI PERFIL", fontWeight = FontWeight.SemiBold, color = Color.Black) },

                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black,
                    actionIconContentColor = Color.Black
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Bloques estándar
            itemsIndexed(items) { index, item ->
                PerfilDatoBox(
                    etiquetaArriba = item.label,
                    valor = item.value,
                    leadingIcon = item.icon,
                    trailing = null,
                    borderBrush = gradientForIndex(index)
                )
            }

            // ---- Bloque de CONTRASEÑA (cuadro blanco, letras negras, borde degradado + botón lápiz) ----
            item {
                PerfilDatoBox(
                    etiquetaArriba = "Contraseña",
                    valor = "********",
                    leadingIcon = Icons.Filled.Lock,
                    trailing = {
                        IconButton(onClick = { navController.navigate(Route.EditarContrasena.path) }) {
                            Icon(imageVector = Icons.Filled.Edit, contentDescription = "Editar contraseña", tint = Color.Black)
                        }
                    },
                    borderBrush = gradientForIndex( items.size ) // siguiente degradado de la secuencia
                )
            }
        }
    }
}

/** Cuadro blanco con borde degradado y borde CUADRADO. Etiqueta ARRIBA. Texto/íconos en NEGRO. */
@Composable
private fun PerfilDatoBox(
    etiquetaArriba: String,
    valor: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    trailing: (@Composable () -> Unit)?,
    borderBrush: Brush
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Etiqueta arriba
        Text(
            text = etiquetaArriba,
            color = Color.Black,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )
        // Contenedor con borde degradado (cuadrado)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 2.dp, brush = borderBrush, shape = RectangleShape)
                .background(color = Color.White, shape = RectangleShape)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row {
                Icon(imageVector = leadingIcon, contentDescription = etiquetaArriba, tint = Color.Black)
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(
                        text = valor,
                        color = Color.Black,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
            if (trailing != null) trailing()
        }
    }
}

private data class PerfilItemUi(
    val label: String,
    val value: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

/** Degradado que cicla la paleta */
private fun gradientForIndex(index: Int): Brush =
    when (index % 3) {
        0 -> Brush.horizontalGradient(listOf(SF_Teal, SF_Blue))
        1 -> Brush.horizontalGradient(listOf(SF_Blue, SF_Purple))
        else -> Brush.horizontalGradient(listOf(SF_Purple, SF_Teal))
    }

@Preview(showBackground = true)
@Composable
private fun PreviewPerfilScreen() {
    PerfilScreen(rememberNavController(), ProfileData())
}