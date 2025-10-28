package com.example.appstorefit_grupo1.ui.screen


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
import com.example.appstorefit_grupo1.ViewModel.MensajesViewModel
import com.example.appstorefit_grupo1.ViewModel.MensajesViewModelFactory
import com.example.appstorefit_grupo1.session.SessionManager
import java.util.Date

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

    val inbox by mensajesVm
        .observarBandejaSoporte(3)
        .collectAsStateWithLifecycle(initialValue = emptyList())

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
                                    Date(msg.createdAt).toString(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = cs.onSurfaceVariant
                                )
                                if (!msg.read) {
                                    TextButton(onClick = { mensajesVm.marcarComoLeido(msg.id) }) {
                                        Text("Marcar leído")
                                    }
                                } else {
                                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = cs.primary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
