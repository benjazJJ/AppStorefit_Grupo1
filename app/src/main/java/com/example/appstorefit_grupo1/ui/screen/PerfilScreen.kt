package com.example.appstorefit_grupo1.ui.screen

import android.content.Context
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.appstorefit_grupo1.data.remote.RemoteModule
import com.example.appstorefit_grupo1.data.remote.ServiceUrls
import com.example.appstorefit_grupo1.data.remote.users.UsersApi
import com.example.appstorefit_grupo1.data.repository.UserRepository
import com.example.appstorefit_grupo1.navigation.Route
import com.example.appstorefit_grupo1.session.SessionManager
import com.example.appstorefit_grupo1.ui.ViewModel.AuthViewModel
import com.example.appstorefit_grupo1.ui.ViewModel.AuthViewModelFactory
import com.example.appstorefit_grupo1.ui.ViewModel.MensajesViewModel
import com.example.appstorefit_grupo1.ui.ViewModel.MensajesViewModelFactory
import com.example.appstorefit_grupo1.ui.components.CampoReadOnlyDegradado
import com.example.appstorefit_grupo1.ui.theme.SF_Blue
import com.example.appstorefit_grupo1.ui.theme.SF_Purple
import com.example.appstorefit_grupo1.ui.theme.SF_Teal
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun createTempImageFile(context: Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir = File(context.cacheDir, "images").apply { if (!exists()) mkdirs() }
    return File(storageDir, "IMG_.jpg")
}

private fun getImageUriForFile(context: Context, file: File): Uri {
    val authority = "${context.packageName}.fileprovider"
    return FileProvider.getUriForFile(context, authority, file)
}

private fun rutToStableLong(rut: String): Long {
    val onlyDigits = rut.filter { it.isDigit() }
    return onlyDigits.toLongOrNull() ?: rut.hashCode().toLong()
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(navController: NavController) {
    val cs = MaterialTheme.colorScheme
    val grad1 = Brush.horizontalGradient(listOf(SF_Teal, SF_Blue))
    val grad2 = Brush.horizontalGradient(listOf(SF_Blue, SF_Purple))

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val vm: AuthViewModel = viewModel(factory = AuthViewModelFactory(context))
    val perfil by vm.perfil.collectAsStateWithLifecycle()

    val mensajesVm: MensajesViewModel = viewModel(factory = MensajesViewModelFactory(context))
    val envio by mensajesVm.envio.collectAsStateWithLifecycle()
    var mensajeTexto by rememberSaveable { mutableStateOf("") }

    var user by remember { mutableStateOf(SessionManager.user) }
    val roleId = SessionManager.roleId

    val usersApi = remember {
        RemoteModule.create(
            baseUrl = ServiceUrls.USERS_BASE_URL,
            service = UsersApi::class.java
        )
    }
    val repo = remember { UserRepository(api = usersApi) }
    var photoUriString by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingCaptureUri by remember { mutableStateOf<Uri?>(null) }

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
                    user = SessionManager.user
                }
            }
        } else {
            pendingCaptureUri = null
            Toast.makeText(context, "Error al tomar la foto", Toast.LENGTH_SHORT).show()
        }
    }

    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val finalUri = uri.toString()
            photoUriString = finalUri
            Toast.makeText(context, "Foto seleccionada desde galeria", Toast.LENGTH_SHORT).show()
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

    LaunchedEffect(Unit) {
        vm.cargarPerfil()
        val email = user?.email
        if (!email.isNullOrBlank()) {
            val fresh = repo.refreshSessionUserByEmail(email)
            if (fresh != null) {
                user = fresh
                if (photoUriString.isNullOrBlank()) photoUriString = fresh.photoUri
            }
        }
    }

    var editNombre by rememberSaveable { mutableStateOf(false) }
    var editFecha by rememberSaveable { mutableStateOf(false) }
    var editTelefono by rememberSaveable { mutableStateOf(false) }
    var editDireccion by rememberSaveable { mutableStateOf(false) }
    var editCorreo by rememberSaveable { mutableStateOf(false) }
    val anyEditing = editNombre || editFecha || editTelefono || editDireccion || editCorreo

    fun enterEdit() { if (!perfil.modoEdicion) vm.alternarModoEdicionPerfil() }
    fun exitEdit()  { if (perfil.modoEdicion) vm.alternarModoEdicionPerfil() }
    fun clearEdits() {
        editNombre = false
        editFecha = false
        editTelefono = false
        editDireccion = false
        editCorreo = false
    }

    LaunchedEffect(perfil.mensaje, perfil.modoEdicion) {
        if (!perfil.modoEdicion && perfil.mensaje == "Perfil actualizado correctamente.") {
            user = SessionManager.user
            clearEdits()
        }
    }

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

            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(containerColor = cs.surface),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val displayNombre = buildString {
                    val nombreActual = perfil.nombre.ifBlank { u.name }
                    val apellidoActual = perfil.apellido.ifBlank { SessionManager.lastName.orEmpty() }
                    if (nombreActual.isNotBlank()) append(nombreActual)
                    if (apellidoActual.isNotBlank()) {
                        if (isNotEmpty()) append(' ')
                        append(apellidoActual)
                    }
                }.ifBlank { u.name }

                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Person, contentDescription = null, tint = cs.primary)
                    Spacer(Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = displayNombre,
                            style = MaterialTheme.typography.titleMedium,
                            color = cs.onSurface
                        )
                        Spacer(Modifier.height(6.dp))
                        RoleBox(
                            roleName = when (roleId) { 2L -> "ADMINISTRADOR"; 3L -> "SOPORTE"; else -> "CLIENTE" }
                        )
                    }
                }
            }

            // Foto de perfil
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
                    Text("Foto Perfil", style = MaterialTheme.typography.titleMedium, color = cs.onSurface)
                    Spacer(Modifier.height(14.dp))

                    var showDialog by remember { mutableStateOf(false) }

                    if (photoUriString.isNullOrEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(cs.surfaceVariant.copy(alpha = 0.45f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Person, contentDescription = null, tint = cs.onSurfaceVariant)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("Sin foto de perfil", style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
                        Spacer(Modifier.height(14.dp))
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
                            ) { Icon(Icons.Filled.CameraAlt, null); Spacer(Modifier.width(8.dp)); Text("Camara") }
                            OutlinedButton(
                                onClick = {
                                    pickMediaLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            ) { Icon(Icons.Filled.Image, null); Spacer(Modifier.width(8.dp)); Text("Galeria") }
                        }
                    } else {
                        AsyncImage(
                            model = ImageRequest.Builder(context).data(Uri.parse(photoUriString)).crossfade(true).build(),
                            contentDescription = "Foto de perfil",
                            modifier = Modifier
                                .size(140.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.height(14.dp))
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
                            ) { Icon(Icons.Filled.CameraAlt, null); Spacer(Modifier.width(8.dp)); Text("Camara") }
                            OutlinedButton(
                                onClick = {
                                    pickMediaLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            ) { Icon(Icons.Filled.Image, null); Spacer(Modifier.width(8.dp)); Text("Galeria") }
                        }
                        Spacer(Modifier.height(12.dp))
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            OutlinedButton(onClick = { showDialog = true }, modifier = Modifier.widthIn(min = 180.dp)) {
                                Icon(Icons.Filled.Delete, null); Spacer(Modifier.width(8.dp)); Text("Eliminar")
                            }
                        }
                        if (showDialog) {
                            AlertDialog(
                                onDismissRequest = { showDialog = false },
                                title = { Text("Eliminar foto") },
                                text = { Text("Deseas eliminar la foto de perfil?") },
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
                                dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancelar") } }
                            )
                        }
                    }
                }
            }

            // ---------- FORMULARIO ----------

            if (!editCorreo) {
                CampoReadOnlyDegradado(
                    etiqueta = "Correo electronico",
                    valor = perfil.correo.ifBlank { u.email },
                    leadingIcon = { Icon(Icons.Filled.AlternateEmail, null) },
                    trailingIcon = {
                        IconButton(onClick = { editCorreo = true; enterEdit() }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Editar correo")
                        }
                    },
                    borderBrush = grad1
                )
            } else {
                OutlinedTextField(
                    value = perfil.correo,
                    onValueChange = vm::onPerfilEmailChange,
                    label = { Text("Correo electronico") },
                    isError = perfil.errorCorreo != null,
                    supportingText = { perfil.errorCorreo?.let { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Contraseña (solo muestra oculto y abre la pantalla de cambio)
            CampoReadOnlyDegradado(
                etiqueta = "Contraseña",
                valor = "********",
                leadingIcon = { Icon(Icons.Filled.Lock, null) },
                trailingIcon = {
                    IconButton(onClick = { navController.navigate(Route.EditarContrasena.path) }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Editar contraseña")
                    }
                },
                borderBrush = grad2
            )

            CampoReadOnlyDegradado(
                etiqueta = "RUT",
                valor = u.rut,
                leadingIcon = { Icon(Icons.Filled.Badge, null) },
                borderBrush = grad2
            )

            if (!editNombre) {
                CampoReadOnlyDegradado(
                    etiqueta = "Nombre",
                    valor = perfil.nombre.ifBlank { u.name },
                    leadingIcon = { Icon(Icons.Filled.Person, null) },
                    trailingIcon = {
                        IconButton(onClick = { editNombre = true; enterEdit() }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Editar nombre y apellidos")
                        }
                    },
                    borderBrush = grad1
                )
                CampoReadOnlyDegradado(
                    etiqueta = "Apellidos",
                    valor = perfil.apellido.ifBlank { SessionManager.lastName.orEmpty() }.ifBlank { "Sin apellidos" },
                    leadingIcon = { Icon(Icons.Filled.Person, null) },
                    trailingIcon = {
                        IconButton(onClick = { editNombre = true; enterEdit() }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Editar nombre y apellidos")
                        }
                    },
                    borderBrush = grad2
                )
            } else {
                OutlinedTextField(
                    value = perfil.nombre,
                    onValueChange = vm::onPerfilNombreChange,
                    label = { Text("Nombre") },
                    isError = perfil.errorNombre != null,
                    supportingText = { perfil.errorNombre?.let { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = perfil.apellido,
                    onValueChange = vm::onPerfilApellidoChange,
                    label = { Text("Apellidos (opcional)") },
                    isError = perfil.errorApellido != null,
                    supportingText = { perfil.errorApellido?.let { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (!editFecha) {
                CampoReadOnlyDegradado(
                    etiqueta = "Fecha de nacimiento",
                    valor = perfil.fechaNacimiento.ifBlank { u.birthDate },
                    leadingIcon = { Icon(Icons.Filled.Cake, null) },
                    trailingIcon = {
                        IconButton(onClick = { editFecha = true; enterEdit() }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Editar fecha de nacimiento")
                        }
                    },
                    borderBrush = grad1
                )
            } else {
                OutlinedTextField(
                    value = perfil.fechaNacimiento,
                    onValueChange = vm::onPerfilFechaChange,
                    label = { Text("Fecha de nacimiento (yyyy-MM-dd)") },
                    leadingIcon = { Icon(Icons.Filled.Cake, null) },
                    isError = perfil.errorFechaNacimiento != null,
                    supportingText = { perfil.errorFechaNacimiento?.let { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (!editTelefono) {
                CampoReadOnlyDegradado(
                    etiqueta = "Telefono",
                    valor = perfil.telefono.ifBlank { u.phone ?: "No registrado" },
                    leadingIcon = { Icon(Icons.Filled.Phone, null) },
                    trailingIcon = {
                        IconButton(onClick = { editTelefono = true; enterEdit() }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Editar telefono")
                        }
                    },
                    borderBrush = grad2
                )
            } else {
                OutlinedTextField(
                    value = perfil.telefono,
                    onValueChange = vm::onPerfilTelefonoChange,
                    label = { Text("Telefono") },
                    leadingIcon = { Icon(Icons.Filled.Phone, null) },
                    isError = perfil.errorTelefono != null,
                    supportingText = { perfil.errorTelefono?.let { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (!editDireccion) {
                CampoReadOnlyDegradado(
                    etiqueta = "Direccion",
                    valor = perfil.direccion.ifBlank { u.address },
                    leadingIcon = { Icon(Icons.Filled.Home, null) },
                    trailingIcon = {
                        IconButton(onClick = { editDireccion = true; enterEdit() }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Editar direccion")
                        }
                    },
                    borderBrush = grad1
                )
            } else {
                OutlinedTextField(
                    value = perfil.direccion,
                    onValueChange = vm::onPerfilDireccionChange,
                    label = { Text("Direccion") },
                    leadingIcon = { Icon(Icons.Filled.Home, null) },
                    isError = perfil.errorDireccion != null,
                    supportingText = { perfil.errorDireccion?.let { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (anyEditing) {
                if (perfil.cargando) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { vm.submitPerfilGuardar() },
                        enabled = perfil.puedeGuardar && !perfil.cargando,
                        modifier = Modifier.weight(1f)
                    ) { Text("Guardar cambios") }
                    OutlinedButton(
                        onClick = {
                            vm.cargarPerfil(); clearEdits(); exitEdit()
                        },
                        enabled = !perfil.cargando,
                        modifier = Modifier.weight(1f)
                    ) { Text("Cancelar") }
                }
                perfil.mensaje?.let { Text(it, color = cs.primary) }
            }

            Spacer(Modifier.height(16.dp))

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
            ) { Text("Historial de compras", style = MaterialTheme.typography.labelLarge) }

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
            ) { Text("Cerrar sesion", style = MaterialTheme.typography.labelLarge) }

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
                                    idRolDestinoSoporte = 3,
                                    contenido = mensajeTexto
                                )
                            },
                            enabled = mensajeTexto.isNotBlank() && !envio.enviando,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) { Text(if (envio.enviando) "Enviando..." else "Enviar a Soporte") }
                    }
                }

                Spacer(Modifier.height(16.dp))

                var asc by rememberSaveable { mutableStateOf(false) }
                fun fmt(ts: Long): String = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(ts))
                val userIdStable = remember(u.rut) { rutToStableLong(u.rut) }
                val outbox by mensajesVm.observarOutboxClienteConRespuesta(userIdStable, asc)
                    .collectAsStateWithLifecycle(initialValue = emptyList())

                ElevatedCard(
                    colors = CardDefaults.elevatedCardColors(containerColor = cs.surface),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Mensajes de soporte", style = MaterialTheme.typography.titleMedium, color = cs.onSurface)
                            TextButton(onClick = { asc = !asc }) {
                                Text(if (asc) "Orden: antiguo -> reciente" else "Orden: reciente -> antiguo")
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        if (outbox.isEmpty()) {
                            Text("Aun no has enviado mensajes.", style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                outbox.forEach { item ->
                                    val m = item.clienteMensaje
                                    val r = item.respuesta
                                    ElevatedCard(
                                        colors = CardDefaults.elevatedCardColors(containerColor = cs.surface),
                                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(fmt(m.createdAt), style = MaterialTheme.typography.labelLarge)
                                                val estado = if (m.read) "Leido" else "No leido"
                                                AssistChip(onClick = {}, label = { Text(estado) })
                                            }
                                            Spacer(Modifier.height(6.dp))
                                            Text(m.content, style = MaterialTheme.typography.bodyLarge)
                                            Spacer(Modifier.height(10.dp))
                                            Divider()
                                            Spacer(Modifier.height(10.dp))
                                            Text("Respuesta del soporte", style = MaterialTheme.typography.labelLarge)
                                            if (r != null) {
                                                Spacer(Modifier.height(4.dp))
                                                Text(r.content, style = MaterialTheme.typography.bodyMedium)
                                            } else {
                                                Spacer(Modifier.height(4.dp))
                                                Text("No se ha respondido el mensaje", style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
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
                modifier = Modifier.size(10.dp)
            ) {}
            Spacer(Modifier.width(8.dp))
            Text(roleName.uppercase(), style = MaterialTheme.typography.labelLarge, color = cs.onSurface, fontWeight = FontWeight.SemiBold)
        }
    }
}
