package com.example.appstorefit_grupo1.ui.screen

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.appstorefit_grupo1.components.CampoReadOnlyDegradado
import com.example.appstorefit_grupo1.data.local.database.AppDatabase
import com.example.appstorefit_grupo1.data.repository.UserRepository
import com.example.appstorefit_grupo1.navigation.Route
import com.example.appstorefit_grupo1.session.SessionManager
import com.example.appstorefit_grupo1.ui.theme.SF_Blue
import com.example.appstorefit_grupo1.ui.theme.SF_Purple
import com.example.appstorefit_grupo1.ui.theme.SF_Teal
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ===== Helpers de cámara =====
private fun createTempImageFile(context: Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir = File(context.cacheDir, "images").apply { if (!exists()) mkdirs() }
    return File(storageDir, "IMG_${timeStamp}.jpg")
}

private fun getImageUriForFile(context: Context, file: File): Uri {
    val authority = "${context.packageName}.fileprovider"
    return FileProvider.getUriForFile(context, authority, file)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(navController: NavController) {
    val cs = MaterialTheme.colorScheme
    val grad1 = Brush.horizontalGradient(listOf(SF_Teal, SF_Blue))
    val grad2 = Brush.horizontalGradient(listOf(SF_Blue, SF_Purple))

    val context = LocalContext.current
    val scope = rememberCoroutineScope()


    val db = remember { AppDatabase.getInstance(context) }
    val repo = remember {
        UserRepository(
            db = db,
            userDao = db.userDao(),
            registroDao = db.registroDao()
        )
    }

    // 1) Estados de usuario y foto
    var user by remember { mutableStateOf(SessionManager.user) }
    val roleId = SessionManager.roleId
    var photoUriString by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingCaptureUri by remember { mutableStateOf<Uri?>(null) }

    // 2) Launcher de cámara
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            val finalUri = pendingCaptureUri?.toString()
            photoUriString = finalUri
            Toast.makeText(context, "Foto tomada correctamente", Toast.LENGTH_SHORT).show()

            val emailActual = SessionManager.user?.email
            if (!finalUri.isNullOrBlank() && !emailActual.isNullOrBlank()) {
                scope.launch {
                    repo.saveUserPhoto(emailActual!!, finalUri!!)
                    repo.refreshSessionUserByEmail(emailActual)
                    user = SessionManager.user // refresca en memoria
                }
            }
        } else {
            pendingCaptureUri = null
            Toast.makeText(context, "Error al tomar la foto", Toast.LENGTH_SHORT).show()
        }
    }

    // 3) Refresca datos y precarga la foto desde DB al entrar
    LaunchedEffect(Unit) {
        val email = user?.email ?: return@LaunchedEffect
        val fresh = repo.refreshSessionUserByEmail(email)
        if (fresh != null) {
            user = fresh
            if (photoUriString.isNullOrBlank()) photoUriString = fresh.photoUri
        }
    }

    // Badget de rol
    @Composable
    fun RoleBox(roleName: String) {
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

    Scaffold(
        topBar = { TopAppBar(title = { Text("MI PERFIL", fontWeight = FontWeight.Bold) }) }
    ) { inner ->
        val u = user ?: return@Scaffold
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .padding(inner)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ── Tarjeta 1: Encabezado (nombre + rol)
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

            // ── Tarjeta 2: Cámara
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(containerColor = cs.surface),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Foto Perfil",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(12.dp))

                    if (photoUriString.isNullOrEmpty()) {
                        Text(
                            text = "Sin Foto de Perfil",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(12.dp))
                    } else {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(Uri.parse(photoUriString)).crossfade(true)
                                .build(),
                            contentDescription = "foto tomada",
                            modifier = Modifier
                                .size(140.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.height(12.dp))
                    }

                    var showDialog by remember { mutableStateOf(false) }

                    if (photoUriString.isNullOrEmpty()) {
                        Button(
                            onClick = {
                                val file = createTempImageFile(context)
                                val uri = getImageUriForFile(context, file)
                                pendingCaptureUri = uri
                                takePictureLauncher.launch(uri)
                            }
                        ) { Text("Tomar foto") }
                    } else {
                        // Editar + Eliminar en la MISMA fila
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    val file = createTempImageFile(context)
                                    val uri = getImageUriForFile(context, file)
                                    pendingCaptureUri = uri
                                    takePictureLauncher.launch(uri)
                                },
                                modifier = Modifier.weight(1f)
                            ) { Text("Editar foto") }

                            OutlinedButton(
                                onClick = { showDialog = true },
                                modifier = Modifier.weight(1f)
                            ) { Text("Eliminar foto") }
                        }

                        if (showDialog) {
                            AlertDialog(
                                onDismissRequest = { showDialog = false },
                                title = { Text("Confirmación") },
                                text = { Text("¿Desea eliminar la foto?") },
                                confirmButton = {
                                    TextButton(onClick = {
                                        val email = u.email
                                        photoUriString = null
                                        showDialog = false
                                        Toast.makeText(context, "Foto eliminada", Toast.LENGTH_SHORT).show()
                                        // Persiste borrado en DB
                                        scope.launch {
                                            repo.clearUserPhoto(email)
                                            repo.refreshSessionUserByEmail(email)
                                            user = SessionManager.user
                                        }
                                    }) { Text("Aceptar") }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showDialog = false }) { Text("Cancelar") }
                                }
                            )
                        }
                    }
                }
            }

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
                valor = run {
                    val telefono = u.phone
                    if (telefono.isNullOrBlank()) "No registrado" else telefono
                },
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
