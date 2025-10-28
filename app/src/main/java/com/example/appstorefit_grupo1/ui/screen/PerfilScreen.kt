package com.example.appstorefit_grupo1.ui.screen

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.appstorefit_grupo1.ViewModel.MensajesViewModel
import com.example.appstorefit_grupo1.ViewModel.MensajesViewModelFactory
import com.example.appstorefit_grupo1.ui.components.CampoReadOnlyDegradado
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



//Helpers de cámara
private fun createTempImageFile(context: Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir = File(context.cacheDir, "images").apply { if (!exists()) mkdirs() }
    return File(storageDir, "IMG_${timeStamp}.jpg")
}

private fun getImageUriForFile(context: Context, file: File): Uri {
    val authority = "${context.packageName}.fileprovider"
    return FileProvider.getUriForFile(context, authority, file)
}

// Convierte el RUT a un Long estable para usar como senderUserId
private fun rutToStableLong(rut: String): Long {
    val onlyDigits = rut.filter { it.isDigit() }
    return onlyDigits.toLongOrNull() ?: rut.hashCode().toLong()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(navController: NavController) {
    val cs = MaterialTheme.colorScheme
    val grad1 = Brush.horizontalGradient(listOf(SF_Teal, SF_Blue))
    val grad2 = Brush.horizontalGradient(listOf(SF_Blue, SF_Purple))

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val mensajesVm: MensajesViewModel = viewModel (factory = MensajesViewModelFactory(context))
    val envio by mensajesVm.envio.collectAsStateWithLifecycle()
    var mensajeTexto by rememberSaveable { mutableStateOf("") }


    val db = remember { AppDatabase.getInstance(context) }
    val repo = remember {
        UserRepository(
            db = db,
            userDao = db.userDao(),
            registroDao = db.registroDao(),
            rolDao = db.rolDao()
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
                    repo.saveUserPhoto(emailActual, finalUri)
                    repo.refreshSessionUserByEmail(emailActual)
                    user = SessionManager.user // refresca en memoria
                }
            }
        } else {
            pendingCaptureUri = null
            Toast.makeText(context, "Error al tomar la foto", Toast.LENGTH_SHORT).show()
        }
    }

    // Launcher de Photo Picker (solo IMÁGENES)
    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val finalUri = uri.toString()
            photoUriString = finalUri
            Toast.makeText(context, "Foto seleccionada desde galería", Toast.LENGTH_SHORT).show()

            val emailActual = SessionManager.user?.email
            if (!emailActual.isNullOrBlank()) {
                scope.launch {
                    repo.saveUserPhoto(emailActual, finalUri)
                    repo.refreshSessionUserByEmail(emailActual)
                    user = SessionManager.user
                }
            }
        }
    }

    // 3) Refresca datos al entrar
    LaunchedEffect(Unit) {
        val email = user?.email ?: return@LaunchedEffect
        val fresh = repo.refreshSessionUserByEmail(email)
        if (fresh != null) {
            user = fresh
            if (photoUriString.isNullOrBlank()) photoUriString = fresh.photoUri
        }
    }

    // Feedback de envío de mensaje
    LaunchedEffect(envio.ok, envio.error) {
        when {
            envio.ok -> {
                Toast.makeText(context, "Mensaje enviado a Soporte", Toast.LENGTH_SHORT).show()
                mensajesVm.limpiarEstadoEnvio()
                mensajeTexto = ""
            }
            envio.error != null -> {
                Toast.makeText(context, envio.error ?: "Error al enviar", Toast.LENGTH_SHORT).show()
                mensajesVm.limpiarEstadoEnvio()
            }
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
                    modifier = Modifier.size(10.dp)
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
        topBar = {
            TopAppBar(
                title = { Text("MI PERFIL", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = cs.surface,
                    titleContentColor = cs.onSurface,
                    navigationIconContentColor = cs.onSurface,
                    actionIconContentColor = cs.onSurface
                )
            )
        }
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

            // ── Tarjeta 2: Foto perfil (con cambios de UI)
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(containerColor = cs.surface),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Foto Perfil",
                        style = MaterialTheme.typography.titleMedium,
                        color = cs.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(14.dp))

                    var showDialog by remember { mutableStateOf(false) }

                    if (photoUriString.isNullOrEmpty()) {
                        // Placeholder redondo
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(cs.surfaceVariant.copy(alpha = 0.45f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = null,
                                tint = cs.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Sin foto de perfil",
                            style = MaterialTheme.typography.bodyMedium,
                            color = cs.onSurfaceVariant
                        )
                        Spacer(Modifier.height(14.dp))

                        //Fila con Cámara + Galería
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    val file = createTempImageFile(context)
                                    val uri = getImageUriForFile(context, file)
                                    pendingCaptureUri = uri
                                    takePictureLauncher.launch(uri)
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Filled.CameraAlt, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Cámara")
                            }
                            OutlinedButton(
                                onClick = {
                                    pickMediaLauncher.launch(
                                        PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly // Solo imágenes
                                        )
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Filled.Image, contentDescription = null) // ✅ CAMBIO
                                Spacer(Modifier.width(8.dp))
                                Text("Galería")
                            }
                        }
                        // (No mostramos “Eliminar” porque no hay foto aún)
                    } else {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(Uri.parse(photoUriString)).crossfade(true)
                                .build(),
                            contentDescription = "Foto de perfil",
                            modifier = Modifier
                                .size(140.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.height(14.dp))

                        //fila solo con Cámara + Galería
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    val file = createTempImageFile(context)
                                    val uri = getImageUriForFile(context, file)
                                    pendingCaptureUri = uri
                                    takePictureLauncher.launch(uri)
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Filled.CameraAlt, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Cámara")
                            }
                            OutlinedButton(
                                onClick = {
                                    pickMediaLauncher.launch(
                                        PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly // Solo imágenes
                                        )
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Filled.Image, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Galería")
                            }
                        }

                        // Botón “Eliminar” debajo y centrado
                        Spacer(Modifier.height(12.dp))
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            OutlinedButton(
                                onClick = { showDialog = true },
                                modifier = Modifier.widthIn(min = 180.dp) // para que se vea centrado y consistente
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Eliminar")
                            }
                        }

                        if (showDialog) {
                            AlertDialog(
                                onDismissRequest = { showDialog = false },
                                title = { Text("Eliminar foto") },
                                text = { Text("¿Deseas eliminar la foto de perfil?") },
                                confirmButton = {
                                    TextButton(onClick = {
                                        val email = u.email
                                        photoUriString = null
                                        showDialog = false
                                        Toast.makeText(context, "Foto eliminada", Toast.LENGTH_SHORT).show()
                                        scope.launch {
                                            repo.clearUserPhoto(email)
                                            repo.refreshSessionUserByEmail(email)
                                            user = SessionManager.user
                                        }
                                    }) { Text("Eliminar") }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showDialog = false }) { Text("Cancelar") }
                                }
                            )
                        }
                    }
                }
            }

            // Campos read-only con borde degradado
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
                etiqueta = "Fecha de Nacimiento",
                valor = u.birthDate,
                leadingIcon = { Icon(Icons.Filled.Cake, contentDescription = null) },
                borderBrush = grad1
            )
            CampoReadOnlyDegradado(
                etiqueta = "Teléfono",
                valor = u.phone?.takeIf { it.isNotBlank() } ?: "No registrado",
                leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null) },
                borderBrush = grad2
            )
            CampoReadOnlyDegradado(
                etiqueta = "Dirección",
                valor = u.address,
                leadingIcon = { Icon(Icons.Filled.Home, contentDescription = null) },
                borderBrush = grad1
            )

            Spacer(Modifier.height(16.dp))

            // ---- Botón para ver historial de compras ----
            Button(
                onClick = { navController.navigate(Route.HistorialCompras.path) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Historial de compras", style = MaterialTheme.typography.labelLarge)
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    SessionManager.user = null
                    SessionManager.roleId = null
                    navController.navigate(Route.Login.path) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = cs.primary,
                    contentColor = cs.onPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Cerrar sesión", style = MaterialTheme.typography.labelLarge)
            }

            // Contactar Soporte (solo si el rol es CLIENTE = 1L)
            if (roleId == 1L) {
                ElevatedCard(
                    colors = CardDefaults.elevatedCardColors(containerColor = cs.surface),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Contactar Soporte", style = MaterialTheme.typography.titleMedium, color = cs.onSurface)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = mensajeTexto,
                            onValueChange = { mensajeTexto = it },
                            label = { Text("Escribe tu mensaje") },
                            minLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = {
                                val uSafe = user ?: return@Button
                                val senderId = rutToStableLong(uSafe.rut)
                                mensajesVm.enviarMensaje(
                                    idUsuarioRemitente = senderId,
                                    idRolDestinoSoporte = 3, // SOPORTE
                                    contenido = mensajeTexto
                                )
                            },
                            enabled = mensajeTexto.isNotBlank() && !envio.enviando,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text(if (envio.enviando) "Enviando..." else "Enviar a Soporte")
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

        }
    }
}
