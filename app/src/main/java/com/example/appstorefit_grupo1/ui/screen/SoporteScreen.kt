package com.example.appstorefit_grupo1.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appstorefit_grupo1.ui.ViewModel.MensajesViewModel
import com.example.appstorefit_grupo1.ui.ViewModel.MensajesViewModelFactory
import com.example.appstorefit_grupo1.session.SessionManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoporteScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val cs = MaterialTheme.colorScheme
    val mensajesVm: MensajesViewModel = viewModel(factory = MensajesViewModelFactory(context))
    val roleId = SessionManager.roleId

    // Solo SOPORTE (3)
    if (roleId != 3L) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Acceso solo para Soporte")
        }
        return
    }

    // ===== Helpers y estados =====
    fun rutToStableLong(rut: String?): Long {
        if (rut.isNullOrBlank()) return 0L
        val onlyDigits = rut.filter { it.isDigit() }
        return onlyDigits.toLongOrNull() ?: rut.hashCode().toLong()
    }
    fun fmt(ts: Long): String =
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(ts))

    val soporteId = remember(SessionManager.user?.rut) {
        rutToStableLong(SessionManager.user?.rut)
    }

    val inbox by mensajesVm
        .observarBandejaSoporte(3)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    // Diálogo de respuesta
    var dialogOpenForThread by remember { mutableStateOf<Long?>(null) }
    var dialogClienteId by remember { mutableStateOf<Long?>(null) }
    var respuestaTexto by remember { mutableStateOf("") }

    val respuestaState by mensajesVm.respuesta.collectAsStateWithLifecycle()

    LaunchedEffect(respuestaState.ok, respuestaState.error) {
        when {
            respuestaState.ok -> {
                Toast.makeText(context, "Respuesta enviada", Toast.LENGTH_SHORT).show()
                // Cerrar y limpiar
                dialogOpenForThread = null
                dialogClienteId = null
                respuestaTexto = ""
                mensajesVm.limpiarEstadoRespuesta()
            }
            respuestaState.error != null -> {
                Toast.makeText(
                    context,
                    respuestaState.error ?: "No se pudo responder",
                    Toast.LENGTH_SHORT
                ).show()
                mensajesVm.limpiarEstadoRespuesta()
            }
        }
    }
    Scaffold(
        topBar = { TopAppBar(title = { Text("Bandeja de Soporte") }) }
    ) { inner ->
        if (inbox.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(inner), contentAlignment = Alignment.Center) {
                Text("No hay mensajes aún")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(inner)
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(inbox, key = { it.id }) { msg ->
                    ElevatedCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text("De ID/RUT: ${msg.senderUserId}", style = MaterialTheme.typography.labelLarge)
                            Spacer(Modifier.height(4.dp))
                            Text(msg.content, style = MaterialTheme.typography.bodyLarge)
                            Spacer(Modifier.height(8.dp))
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    fmt(msg.createdAt),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = cs.onSurfaceVariant
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (!msg.read) {
                                        TextButton(onClick = { mensajesVm.marcarComoLeido(msg.id) }) {
                                            Text("Marcar leído")
                                        }
                                    } else {
                                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = cs.primary)
                                    }

                                    Spacer(Modifier.width(8.dp))

                                    // ===== Botón Responder (al lado de Marcar leído) =====
                                    TextButton(onClick = {
                                        val tid = msg.threadId ?: msg.id
                                        dialogOpenForThread = tid
                                        dialogClienteId = msg.senderUserId
                                        respuestaTexto = ""
                                    }) {
                                        Text("Responder")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ===== Diálogo para escribir y enviar respuesta =====
    if (dialogOpenForThread != null && dialogClienteId != null) {
        AlertDialog(
            onDismissRequest = {
                dialogOpenForThread = null
                dialogClienteId = null
                respuestaTexto = ""
            },
            title = { Text("Respuesta al cliente") },
            text = {
                OutlinedTextField(
                    value = respuestaTexto,
                    onValueChange = { respuestaTexto = it },
                    label = { Text("Escribe la respuesta") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    enabled = respuestaTexto.isNotBlank() && !respuestaState.enviando,
                    onClick = {
                        mensajesVm.responderMensaje(
                            threadId = dialogOpenForThread!!,
                            idUsuarioSoporte = soporteId,
                            idUsuarioCliente = dialogClienteId!!,
                            contenido = respuestaTexto
                        )
                    }
                ) { Text(if (respuestaState.enviando) "Enviando..." else "Enviar") }
            },
            dismissButton = {
                TextButton(onClick = {
                    dialogOpenForThread = null
                    dialogClienteId = null
                    respuestaTexto = ""
                }) { Text("Cancelar") }
            }
        )
    }
}
