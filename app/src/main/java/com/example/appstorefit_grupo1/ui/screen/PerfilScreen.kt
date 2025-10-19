// ui/screen/PerfilScreen.kt
package com.example.appstorefit_grupo1.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.appstorefit_grupo1.session.SessionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(navController: NavController) {
    val cs = MaterialTheme.colorScheme

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("MI PERFIL", fontWeight = FontWeight.Bold) })
        }
    ) { inner ->
        // Si por alguna razón no hay sesión, simplemente no dibujamos nada (sin mensajes).
        val user = SessionManager.user ?: return@Scaffold
        val roleId = SessionManager.roleId

        Column(
            modifier = Modifier
                .padding(inner)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Cabecera minimal (nombre + rol)
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(containerColor = cs.surface),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row {
                        Icon(Icons.Filled.Person, contentDescription = null, tint = cs.primary)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            user.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = cs.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    RoleBox(
                        roleName = when (roleId) {
                            2L -> "ADMINISTRADOR"
                            3L -> "SOPORTE"
                            1L, null -> "CLIENTE"
                            else -> "ESTUDIANTE"
                        }
                    )
                }
            }

            OutlinedTextField(
                value = user.email,
                onValueChange = {},
                leadingIcon = { Icon(Icons.Filled.AlternateEmail, null) },
                label = { Text("Correo") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = user.rut,
                onValueChange = {},
                leadingIcon = { Icon(Icons.Filled.Badge, null) },
                label = { Text("RUT") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = user.phone,
                onValueChange = {},
                leadingIcon = { Icon(Icons.Filled.Phone, null) },
                label = { Text("Teléfono") },
                readOnly = true,
                visualTransformation = VisualTransformation.None,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = user.address,
                onValueChange = {},
                leadingIcon = { Icon(Icons.Filled.Home, null) },
                label = { Text("Dirección") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/** Caja minimalista para el rol (borde sutil, tipografía clara, respeta tu paleta). */
@Composable
private fun RoleBox(roleName: String) {
    val cs = MaterialTheme.colorScheme
    OutlinedCard(
        colors = CardDefaults.outlinedCardColors(containerColor = cs.surface),
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .wrapContentWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Surface(
                color = cs.primary,
                contentColor = cs.onPrimary,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .size(10.dp)
                    .clip(MaterialTheme.shapes.small)
            ) {}
            Spacer(Modifier.width(8.dp))
            Text(
                roleName.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = cs.onSurface,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
