
package com.example.appstorefit_grupo1.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.appstorefit_grupo1.ViewModel.AdminUsuariosUiState
import com.example.appstorefit_grupo1.ViewModel.AdminUsuariosViewModel
import com.example.appstorefit_grupo1.ViewModel.AdminsUsersViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(navController: NavController) {
    val cs = MaterialTheme.colorScheme

    val tabs = listOf(
        AdminTab.Usuarios,
        AdminTab.Productos,
        AdminTab.Categorias,
        AdminTab.Reportes
    )
    var selectedTab by remember { mutableStateOf(AdminTab.Usuarios) }

    val items = listOf(
        Triple(AdminTab.Usuarios,   "Usuarios",   Icons.Filled.Group),
        Triple(AdminTab.Productos,  "Productos",  Icons.Filled.Inventory2),
        Triple(AdminTab.Categorias, "Categorías", Icons.Filled.Category),
        Triple(AdminTab.Reportes,   "Reportes",   Icons.Filled.Assessment)
    )

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = { Text("Panel de Administración", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = cs.surface,
                        titleContentColor = cs.onSurface
                    )
                )
                NavigationBar(
                    containerColor = cs.surface,
                    tonalElevation = 0.dp
                ) {
                    items.forEach { (tab, label, icon) ->
                        val selected = (tab == selectedTab)
                        NavigationBarItem(
                            selected = selected,
                            onClick = { selectedTab = tab },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                            )

                        )
                    }
                }
                Divider(color = cs.outline.copy(alpha = 0.2f))
            }
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
        ) {
            when (selectedTab) {
                AdminTab.Usuarios   -> AdminUsuariosTab()
                AdminTab.Productos  -> AdminProductosTab()
                AdminTab.Categorias -> AdminCategoriasTab()
                AdminTab.Reportes   -> AdminReportesTab()
            }
        }
    }
}

private enum class AdminTab { Usuarios, Productos, Categorias, Reportes }

//TAB USUARIOS

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminUsuariosTab() {
    val context = LocalContext.current
    val vm: AdminUsuariosViewModel = viewModel(factory = AdminsUsersViewModelFactory(context))
    val estado by vm.ui.collectAsStateWithLifecycle()
    val cs = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Usuarios registrados", style = MaterialTheme.typography.titleMedium)
            Button(onClick = { vm.abrirCrear() }) { Text("Crear usuario") }
        }

        if (estado.cargando) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

        estado.mensajeError?.let {
            Text(it, color = cs.error)
            Spacer(Modifier.height(8.dp))
        }

        if (estado.usuarios.isEmpty()) {
            EmptyState("Sin usuarios.\nUsa “Crear usuario” para agregar uno nuevo.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(estado.usuarios) { row ->
                    ElevatedCard {
                        ListItem(
                            headlineContent = { Text(row.name) },
                            supportingContent = {
                                val rol = row.roleName ?: "SIN ROL"
                                Text("${row.email} · ${row.phone ?: "sin teléfono"} · $rol")
                            },
                            trailingContent = {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    // EDITAR
                                    IconButton(onClick = { vm.abrirEditar(row.rut) }) {
                                        Icon(Icons.Filled.Edit, contentDescription = "Editar")
                                    }
                                    // ASIGNAR ROL
                                    IconButton(onClick = { vm.abrirAsignarRol(row.rut) }) {
                                        Icon(Icons.Filled.Badge, contentDescription = "Asignar rol")
                                    }
                                    // ELIMINAR
                                    IconButton(onClick = { vm.solicitarEliminar(row.rut) }) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Eliminar")
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Confirmación eliminar
    estado.rutAConfirmarEliminacion?.let { rutToDelete ->
        AlertDialog(
            onDismissRequest = { vm.cancelarEliminar() },
            title = { Text("Eliminar usuario") },
            text = { Text("¿Eliminar definitivamente el usuario con RUT $rutToDelete?") },
            confirmButton = { TextButton(onClick = { vm.confirmarEliminar() }) { Text("Eliminar") } },
            dismissButton = { TextButton(onClick = { vm.cancelarEliminar() }) { Text("Cancelar") } }
        )
    }

    // Editar usuario (fecha de nacimiento y dirección bloqueadas por política)
    if (estado.mostrarEditar) {
        AlertDialog(
            onDismissRequest = { vm.cerrarEditar() },
            confirmButton = {
                TextButton(
                    onClick = { vm.confirmarEditar() },
                    enabled = estado.puedeEditar && !estado.editando
                ) {
                    if (estado.editando) {
                        CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                    }
                    Text("Guardar")
                }
            },
            dismissButton = { TextButton(onClick = { vm.cerrarEditar() }) { Text("Cancelar") } },
            title = { Text("Editar usuario") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                    OutlinedTextField(
                        value = estado.eRutPk,
                        onValueChange = { /* no editable */ },
                        label = { Text("RUT") },
                        singleLine = true,
                        enabled = false,
                        readOnly = true
                    )

                    OutlinedTextField(
                        value = estado.eNombre2,
                        onValueChange = vm::onCambiarNombreEditar,
                        label = { Text("Nombre") },
                        isError = estado.errNombre2 != null,
                        singleLine = true
                    )
                    estado.errNombre2?.let {
                        Text(it, color = cs.error, style = MaterialTheme.typography.labelSmall)
                    }

                    OutlinedTextField(
                        value = estado.eTelefono2,
                        onValueChange = vm::onCambiarTelefonoEditar,
                        label = { Text("Teléfono (opcional)") },
                        isError = estado.errTelefono2 != null,
                        singleLine = true
                    )
                    estado.errTelefono2?.let {
                        Text(it, color = cs.error, style = MaterialTheme.typography.labelSmall)
                    }

                    // BLOQUEADOS POR POLÍTICA
                    OutlinedTextField(
                        value = estado.eDireccion2,
                        onValueChange = { /* nada */ },
                        label = { Text("Dirección (no editable)") },
                        singleLine = true,
                        enabled = false,
                        readOnly = true
                    )

                    OutlinedTextField(
                        value = estado.eNacimiento2,
                        onValueChange = { /* nada */ },
                        label = { Text("Fecha de nacimiento (no editable)") },
                        singleLine = true,
                        enabled = false,
                        readOnly = true
                    )
                }
            }
        )
    }

    // Asignar rol
    if (estado.mostrarAsignarRol) {
        var expanded by remember { mutableStateOf(false) }
        val roles = estado.rolesDisponibles
        val seleccionado = roles.firstOrNull { it.id == estado.rolSeleccionadoId }

        AlertDialog(
            onDismissRequest = { vm.cerrarAsignarRol() },
            confirmButton = {
                TextButton(
                    onClick = { vm.confirmarAsignarRol() },
                    enabled = (estado.rolSeleccionadoId != null) && !estado.asignandoRol
                ) {
                    if (estado.asignandoRol) {
                        CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                    }
                    Text("Asignar")
                }
            },
            dismissButton = {
                TextButton(onClick = { vm.cerrarAsignarRol() }) { Text("Cancelar") }
            },
            title = { Text("Asignar rol") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("RUT: ${estado.rutParaAsignar}")

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            modifier = Modifier.menuAnchor(),
                            value = seleccionado?.name ?: "Selecciona un rol",
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Rol") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            roles.forEach { rol ->
                                DropdownMenuItem(
                                    text = { Text(rol.name) },
                                    onClick = {
                                        vm.onSeleccionarRol(rol.id)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        )
    }

    // Crear usuario (UI)
    if (estado.mostrarCrear) {
        CrearUsuarioDialog(estado = estado, vm = vm)
    }
}

// ========== OTRAS TABS (placeholders) ==========

@Composable
private fun AdminProductosTab() {
    val productos by remember { mutableStateOf(listOf<String>()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Catálogo de productos", style = MaterialTheme.typography.titleMedium)
            Button(onClick = { /* TODO Paso 3 */ }, enabled = false) { Text("Nuevo producto") }
        }

        if (productos.isEmpty()) {
            EmptyState("Sin productos aún.\nEn el Paso 3 traeremos datos y haremos CRUD.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(productos) { p ->
                    ElevatedCard {
                        ListItem(
                            headlineContent = { Text(p) },
                            supportingContent = { Text("Precio: $0 — Stock: 0") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminCategoriasTab() {
    val categorias by remember { mutableStateOf(listOf<String>()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Categorías", style = MaterialTheme.typography.titleMedium)
            Button(onClick = { /* TODO Paso 3 */ }, enabled = false) { Text("Nueva categoría") }
        }

        if (categorias.isEmpty()) {
            EmptyState("Sin categorías.\nPaso 3: conectaremos Room/Repo y CRUD.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categorias) { c ->
                    ElevatedCard {
                        ListItem(
                            headlineContent = { Text(c) },
                            supportingContent = { Text("Productos en la categoría: 0") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminReportesTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Reportes", style = MaterialTheme.typography.titleMedium)
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Próximamente:", fontWeight = FontWeight.SemiBold)
                Text("• Ventas por período")
                Text("• Top productos")
                Text("• Usuarios activos")
            }
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = { /* TODO Paso 4 */ }, enabled = false) { Text("Abrir reportes") }
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = message, style = MaterialTheme.typography.bodyMedium)
    }
}

//CREAR USUARIO (UI)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CrearUsuarioDialog(
    estado: AdminUsuariosUiState,
    vm: AdminUsuariosViewModel
) {
    val cs = MaterialTheme.colorScheme
    var rolesExpanded by remember { mutableStateOf(false) }
    val roles = estado.rolesDisponibles

    AlertDialog(
        onDismissRequest = { vm.cerrarCrear() },
        confirmButton = {
            TextButton(
                onClick = { vm.confirmarCrear() },
                enabled = estado.puedeCrear && !estado.creando
            ) {
                if (estado.creando) {
                    CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                }
                Text("Crear")
            }
        },
        dismissButton = { TextButton(onClick = { vm.cerrarCrear() }) { Text("Cancelar") } },
        title = { Text("Crear usuario") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                OutlinedTextField(
                    value = estado.cRut,
                    onValueChange = vm::onCambiarRutCrear,
                    label = { Text("RUT") },
                    singleLine = true,
                    isError = estado.errRut != null
                )
                estado.errRut?.let { Text(it, color = cs.error, style = MaterialTheme.typography.labelSmall) }

                OutlinedTextField(
                    value = estado.cNombre,
                    onValueChange = vm::onCambiarNombreCrear,
                    label = { Text("Nombre") },
                    singleLine = true,
                    isError = estado.errNombre != null
                )
                estado.errNombre?.let { Text(it, color = cs.error, style = MaterialTheme.typography.labelSmall) }

                OutlinedTextField(
                    value = estado.cEmail,
                    onValueChange = vm::onCambiarEmailCrear,
                    label = { Text("Email") },
                    singleLine = true,
                    isError = estado.errEmail != null
                )
                estado.errEmail?.let { Text(it, color = cs.error, style = MaterialTheme.typography.labelSmall) }

                // Contraseña
                var mostrarPass by remember { mutableStateOf(false) }
                OutlinedTextField(
                    value = estado.cPassword,
                    onValueChange = vm::onCambiarPasswordCrear,
                    label = { Text("Contraseña") },
                    singleLine = true,
                    isError = estado.errPassword != null,
                    visualTransformation = if (mostrarPass) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { mostrarPass = !mostrarPass }) {
                            Icon(
                                imageVector = if (mostrarPass) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = null
                            )
                        }
                    }
                )
                estado.errPassword?.let { Text(it, color = cs.error, style = MaterialTheme.typography.labelSmall) }



                OutlinedTextField(
                    value = estado.cTelefono,
                    onValueChange = vm::onCambiarTelefonoCrear,
                    label = { Text("Teléfono (opcional)") },
                    singleLine = true,
                    isError = estado.errTelefono != null
                )
                estado.errTelefono?.let { Text(it, color = cs.error, style = MaterialTheme.typography.labelSmall) }

                OutlinedTextField(
                    value = estado.cDireccion,
                    onValueChange = vm::onCambiarDireccionCrear,
                    label = { Text("Dirección (opcional)") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = estado.cNacimiento,
                    onValueChange = vm::onCambiarNacimientoCrear,
                    label = { Text("Fecha de nacimiento (yyyy-MM-dd)") },
                    singleLine = true,
                    isError = estado.errNacimiento != null
                )
                estado.errNacimiento?.let { Text(it, color = cs.error, style = MaterialTheme.typography.labelSmall) }

                // Rol
                ExposedDropdownMenuBox(
                    expanded = rolesExpanded,
                    onExpandedChange = { rolesExpanded = !rolesExpanded }
                ) {
                    OutlinedTextField(
                        modifier = Modifier.menuAnchor(),
                        value = estado.cRolNombreSeleccionado ?: "",
                        onValueChange = { /* solo via dropdown */ },
                        readOnly = true,
                        label = { Text("Rol") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = rolesExpanded) },
                        isError = estado.errRol != null
                    )
                    ExposedDropdownMenu(
                        expanded = rolesExpanded,
                        onDismissRequest = { rolesExpanded = false }
                    ) {
                        roles.forEach { rol ->
                            DropdownMenuItem(
                                text = { Text(rol.name) },
                                onClick = {
                                    vm.onSeleccionarRolCrear(rol.id, rol.name)
                                    rolesExpanded = false
                                }
                            )
                        }
                    }
                }
                estado.errRol?.let { Text(it, color = cs.error, style = MaterialTheme.typography.labelSmall) }
            }
        }
    )
}
