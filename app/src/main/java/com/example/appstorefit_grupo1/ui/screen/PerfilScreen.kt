package com.example.appstorefit_grupo1.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.appstorefit_grupo1.components.CampoReadOnlyDegradado
import com.example.appstorefit_grupo1.data.local.database.AppDatabase
import com.example.appstorefit_grupo1.data.repository.UserRepository
import com.example.appstorefit_grupo1.navigation.Route
import com.example.appstorefit_grupo1.session.SessionManager
import com.example.appstorefit_grupo1.ui.theme.SF_Blue
import com.example.appstorefit_grupo1.ui.theme.SF_Purple
import com.example.appstorefit_grupo1.ui.theme.SF_Teal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(navController: NavController) {
    val cs = MaterialTheme.colorScheme
    val grad1 = Brush.horizontalGradient(listOf(SF_Teal, SF_Blue))
    val grad2 = Brush.horizontalGradient(listOf(SF_Blue, SF_Purple))

    // instancias para refrescar desde Room
    val ctx = LocalContext.current
    val db  = remember { AppDatabase.getInstance(ctx) }
    val repo = remember {
        UserRepository(
            db = db,                      // <- pasa la DB completa
            userDao = db.userDao(),
            registroDao = db.registroDao()
        )
    }

    // user en estado, partiendo por lo que tenga la sesión
    var user by remember { mutableStateOf(SessionManager.user) }
    val roleId = SessionManager.roleId

    // Al entrar a Perfil, refresca datos desde Room
    LaunchedEffect(Unit) {
        val email = user?.email ?: return@LaunchedEffect
        val fresh = repo.refreshSessionUserByEmail(email)
        if (fresh != null) user = fresh
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("MI PERFIL", fontWeight = FontWeight.Bold) })
        }
    ) { inner ->
        val u = user ?: return@Scaffold

        Column(
            modifier = Modifier
                .padding(inner)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {


            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(containerColor = cs.surface),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Person, contentDescription = null, tint = cs.primary)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        u.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = cs.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.weight(1f))
                    RoleBox(
                        roleName = when (roleId) {
                            2L -> "ADMINISTRADOR"
                            3L -> "SOPORTE"
                            else -> "CLIENTE"
                        }
                    )
                }
            }

            // Campos con el mismo estilo degradado que EditarContraseña
            CampoReadOnlyDegradado(
                etiqueta = "Correo electrónico",
                valor = u.email,
                leadingIcon = { Icon(Icons.Filled.AlternateEmail, contentDescription = null) },
                borderBrush = grad1
            )

            CampoReadOnlyDegradado(
                etiqueta = "RUT",
                valor = u.rut,
                leadingIcon = { Icon(Icons.Filled.Badge, contentDescription = null) },
                borderBrush = grad2
            )

            CampoReadOnlyDegradado(
                etiqueta = "Contraseña",
                valor = "********",
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { navController.navigate(Route.EditarContrasena.path) }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Editar contraseña")
                    }
                },
                borderBrush = grad1
            )

            CampoReadOnlyDegradado(
                etiqueta = "Teléfono",
                valor = u.phone,
                leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null) },
                borderBrush = grad2
            )

            CampoReadOnlyDegradado(
                etiqueta = "Dirección",
                valor = u.address,
                leadingIcon = { Icon(Icons.Filled.Home, contentDescription = null) },
                borderBrush = grad1
            )
        }
    }
}

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
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
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
