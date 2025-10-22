package com.example.appstorefit_grupo1.ui.screen

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
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
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


// crear el archivo temporal en cache/images
private fun createTempImageFile(context: Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir = File(context.cacheDir, "images").apply {
        if (!exists()) mkdirs()
    }
    return File(storageDir, "IMG_${timeStamp}.jpg")
}

// obtener la Uri del archivo en el cache con FileProvider
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

    //contexto
    val  context = LocalContext.current
    //guardar la ultima foto tomada
    var photoUriString by rememberSaveable { mutableStateOf<String?>(null) }
    //temporal para guardar la foto
    var pendingCaptureUri by remember { mutableStateOf<Uri?>(null) }
    //launcher para camamara
    val takePicturelauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if(success){
            //si tomo la foto
            photoUriString = pendingCaptureUri?.toString()
            Toast.makeText(context,"Foto tomada correctamente", Toast.LENGTH_SHORT).show()
        }else{
            pendingCaptureUri = null
            Toast.makeText(context,"Error al tomar la foto", Toast.LENGTH_SHORT).show()
        }

    }

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

    @Composable
    fun RoleBox(roleName: String) {
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

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("MI PERFIL", fontWeight = FontWeight.Bold) })
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

                // === CÁMARA (VISIBLE EN PERFIL) ===
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Captura de foto",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(12.dp))

                    if (photoUriString.isNullOrEmpty()) {
                        Text(
                            text = "no se ah tomado fotos",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(12.dp))
                    } else {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(Uri.parse(photoUriString)).crossfade(true)
                                .build(),
                            contentDescription = "foto tomada",
                            modifier = Modifier.fillMaxWidth().height(150.dp),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.height(12.dp))
                    }

                    var showDialog by remember { mutableStateOf(false) }

                    Button(onClick = {
                        val file = createTempImageFile(context)
                        val uri = getImageUriForFile(context, file)
                        pendingCaptureUri = uri
                        takePicturelauncher.launch(uri)
                    }) {
                        Text(if (photoUriString.isNullOrEmpty()) "Abrir Camara" else "Volver a Tomar")
                    }

                    if (!photoUriString.isNullOrEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(onClick = { showDialog = true }) {
                            Text("Eliminar Foto")
                        }
                    }

                    if (showDialog) {
                        AlertDialog(
                            onDismissRequest = { showDialog = false },
                            title = { Text("confirmacion") },
                            text = { Text("¿Desea eliminar la foto") },
                            confirmButton = {
                                TextButton(onClick = {
                                    photoUriString = null
                                    showDialog = false
                                    Toast.makeText(context, "Foto eliminada", Toast.LENGTH_SHORT).show()
                                }) { Text("Aceptar") }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDialog = false }) { Text("Cancelar") }
                            }
                        )
                    }
                }
                // === FIN CÁMARA (VISIBLE EN PERFIL) ===

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
                    valor = run {
                        val telefono = u.phone
                        if (telefono.isNullOrBlank()) "No registrado"
                        else (telefono)
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
}
