
package com.example.appstorefit_grupo1.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Remove
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
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.appstorefit_grupo1.ViewModel.AdminCategoriasViewModel
import com.example.appstorefit_grupo1.ViewModel.AdminCategoriasViewModelFactory
import com.example.appstorefit_grupo1.ViewModel.AdminProductsViewModel
import com.example.appstorefit_grupo1.ViewModel.AdminProductsViewModelFactory
import com.example.appstorefit_grupo1.ViewModel.AdminReportesViewModel
import com.example.appstorefit_grupo1.ViewModel.AdminReportesViewModelFactory
import com.example.appstorefit_grupo1.ViewModel.AdminUsuariosUiState
import com.example.appstorefit_grupo1.ViewModel.AdminUsuariosViewModel
import com.example.appstorefit_grupo1.ViewModel.AdminsUsersViewModelFactory
import com.example.appstorefit_grupo1.data.local.Productos.ProductosEntity

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
                                }
                            }
                        )
                    }
                }
            }
        }
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
                        onValueChange = { },
                        label = { Text("Dirección (no editable)") },
                        singleLine = true,
                        enabled = false,
                        readOnly = true
                    )

                    OutlinedTextField(
                        value = estado.eNacimiento2,
                        onValueChange = { },
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


@Composable
private fun AdminProductosTab() {
    val context = LocalContext.current
    val vm: AdminProductsViewModel = viewModel(factory = AdminProductsViewModelFactory(context))

    val productos by vm.productos.collectAsStateWithLifecycle()
    val errorMsg by vm.error.collectAsStateWithLifecycle()
    val cs = MaterialTheme.colorScheme

    var editing by remember { mutableStateOf<ProductosEntity?>(null) }

    //Controles de filtro/búsqueda
    var search by remember { mutableStateOf("") }
    // null = Todas las categorías
    var selectedCat: Long? by remember { mutableStateOf(null) }

    // categorías detectadas desde los datos
    val categoriasIds = remember(productos) {
        productos.map { it.idCategoria }.distinct().sorted()
    }

    // Filtrado + búsqueda
    val filtrados = remember(productos, selectedCat, search) {
        val s = search.trim()
        productos
            .asSequence()
            .filter { selectedCat == null || it.idCategoria == selectedCat }
            .filter {
                if (s.isEmpty()) true
                else {
                    it.modelo.contains(s, ignoreCase = true) ||
                            it.color.contains(s, ignoreCase = true)  ||
                            it.talla.contains(s, ignoreCase = true)
                }
            }
            .toList()
    }

    //Agrupación por modelo (por categoría+marca+modelo)
    val grupos = remember(filtrados) {
        filtrados.groupBy { Triple(it.idCategoria, it.marca, it.modelo) }
            .toSortedMap(compareBy<Triple<Long, String, String>>({ it.first }, { it.third }))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Catálogo de productos", style = MaterialTheme.typography.titleMedium)
        }

        // Error (si hay)
        errorMsg?.let {
            Text(it, color = cs.error)
        }

        // Búsqueda
        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            label = { Text("Buscar por modelo, color o talla") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Chips de categoría (solo las existentes)
        CategoryFilterRow(
            categorias = categoriasIds,
            selected = selectedCat,
            onSelect = { selectedCat = it }
        )

        // Resumen conteo
        Text(
            "Mostrando ${filtrados.size} variantes en ${grupos.size} modelos",
            style = MaterialTheme.typography.bodySmall,
            color = cs.onSurfaceVariant
        )

        if (filtrados.isEmpty()) {
            EmptyState("No hay productos que coincidan con los filtros.")
        } else {
            // Lista por grupos (modelo)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                grupos.forEach { (clave, variantes) ->
                    val (catId, marca, modelo) = clave
                    item(key = "hdr-${catId}-${marca}-${modelo}") {
                        GroupedProductCard(
                            catId = catId,
                            marca = marca,
                            modelo = modelo,
                            variantes = variantes,
                            onEdit = { editing = it }
                        )
                    }
                }
            }
        }
    }

    //
    editing?.let { producto ->
        EditVariantStockDialog(
            producto = producto,
            onDismiss = { editing = null },
            onConfirmSet = { nuevo ->
                vm.setStock(producto.idCategoria, producto.idProducto, nuevo)
                editing = null
            }
        )
    }
}


@Composable
private fun CategoryFilterRow(
    categorias: List<Long>,
    selected: Long?,
    onSelect: (Long?) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilterChip(
            selected = (selected == null),
            onClick = { onSelect(null) },
            label = { Text("Todas") }
        )
        categorias.forEach { id ->
            FilterChip(
                selected = (selected == id),
                onClick = { onSelect(id) },
                label = { Text(id.toString()) }
            )
        }
    }
}


@Composable
private fun GroupedProductCard(
    catId: Long,
    marca: String,
    modelo: String,
    variantes: List<ProductosEntity>,
    onEdit: (ProductosEntity) -> Unit
) {
    ElevatedCard {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Encabezado del grupo (modelo)
            Text("$marca • $modelo", fontWeight = FontWeight.SemiBold)
            Text("Categoría: $catId · Variantes: ${variantes.size}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Divider()

            // Variantes (color+talla)
            variantes.sortedWith(
                compareBy<ProductosEntity>({ it.color }, { it.talla })
            ).forEach { v ->
                VariantRow(v, onEdit)
            }
        }
    }
}

@Composable
private fun VariantRow(
    v: ProductosEntity,
    onEdit: (ProductosEntity) -> Unit
) {
    ListItem(
        headlineContent = {
            Text("Color: ${v.color} • Talla: ${v.talla}")
        },
        supportingContent = {
            Text("Precio: ${formatCLP(v.precio)} · Stock: ${v.stock}")
        },
        trailingContent = {
            IconButton(onClick = { onEdit(v) }) {
                Icon(Icons.Filled.Edit, contentDescription = "Editar stock")
            }
        }
    )
}

private fun formatCLP(monto: Int): String {
    // CLP sin decimales y con separador de miles
    return try {
        val nf = java.text.NumberFormat.getInstance(java.util.Locale("es", "CL"))
        nf.maximumFractionDigits = 0
        nf.minimumFractionDigits = 0
        "\$${nf.format(monto)}"
    } catch (_: Exception) {
        "\$$monto"
    }
}




/**@Composable
private fun ProductoAdminItem(
    p: ProductosEntity,
    onEdit: () -> Unit
) {
    ElevatedCard {
        ListItem(
            headlineContent = { Text("${p.marca} • ${p.modelo}", fontWeight = FontWeight.SemiBold) },
            supportingContent = {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Categoría: ${p.idCategoria} · ID: ${p.idProducto}")
                    Text("Color: ${p.color} · Talla: ${p.talla}")
                    Text("Precio: ${p.precio} CLP · Stock: ${p.stock}")
                }
            },
            trailingContent = {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Filled.Edit, contentDescription = "Editar stock")
                }
            }
        )
    }
}
**/

@Composable
private fun EditVariantStockDialog(
    producto: ProductosEntity,
    onDismiss: () -> Unit,
    onConfirmSet: (Int) -> Unit
) {
    val cs = MaterialTheme.colorScheme
    var stock by remember(producto) { mutableStateOf(producto.stock) }
    var texto by remember(producto) { mutableStateOf(producto.stock.toString()) }
    var error by remember { mutableStateOf<String?>(null) }

    fun syncFromText(newText: String) {
        texto = newText
        val n = newText.toIntOrNull()
        if (n == null) {
            error = "Ingresa un número válido"
        } else if (n < 0) {
            error = "El stock no puede ser negativo"
        } else {
            error = null
            stock = n
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar stock") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("${producto.marca} • ${producto.modelo}", fontWeight = FontWeight.SemiBold)
                Text("Color: ${producto.color} • Talla: ${producto.talla}")
                Text("Stock actual: ${producto.stock}", style = MaterialTheme.typography.bodySmall)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = {
                            if (stock > 0) {
                                stock -= 1
                                texto = stock.toString()
                                error = null
                            }
                        }
                    ) { Icon(Icons.Filled.Remove, contentDescription = "Restar 1") }

                    OutlinedTextField(
                        value = texto,
                        onValueChange = { syncFromText(it) },
                        label = { Text("Stock") },
                        singleLine = true,
                        isError = error != null,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = {
                            stock += 1
                            texto = stock.toString()
                            error = null
                        }
                    ) { Icon(Icons.Filled.Add, contentDescription = "Sumar 1") }
                }

                // Saltos rápidos
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = {
                        if (stock >= 5) {
                            stock -= 5; texto = stock.toString(); error = null
                        } else {
                            stock = 0; texto = "0"; error = null
                        }
                    }) { Text("−5") }
                    TextButton(onClick = {
                        stock += 5; texto = stock.toString(); error = null
                    }) { Text("+5") }
                }

                if (error != null) {
                    Text(error!!, color = cs.error, style = MaterialTheme.typography.labelSmall)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (error == null) onConfirmSet(stock) },
                enabled = (error == null)
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}





@Composable
private fun AdminCategoriasTab() {
    val context = LocalContext.current

    val vm: AdminCategoriasViewModel =
        viewModel(factory = AdminCategoriasViewModelFactory(context))

    val categorias by vm.resumen.collectAsStateWithLifecycle(initialValue = emptyList())
    val errorMsg  by vm.error.collectAsStateWithLifecycle(initialValue = null)


    val cs = MaterialTheme.colorScheme
    var renaming by remember { mutableStateOf<Pair<Long, String>?>(null) }

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
        }

        // si el repo devolvió algún error (p.ej. nombre duplicado al renombrar), lo mostramos
        errorMsg?.let { Text(it, color = cs.error) }

        if (categorias.isEmpty()) {
            EmptyState("Sin categorías.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(
                    items = categorias,
                    key = { it.id }
                ) { c: com.example.appstorefit_grupo1.data.local.Categoria.CategoriaResumen ->
                    ElevatedCard {
                        ListItem(
                            headlineContent = { Text(c.nombre) },
                            supportingContent = {
                                Text("Productos en la categoría: ${c.productos} · Modelos: ${c.modelos}")
                            },
                            trailingContent = {
                                IconButton(onClick = { renaming = c.id to c.nombre }) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Renombrar")
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Diálogo para renombrar
    renaming?.let { (id, actual) ->
        var nuevo by remember(id) { mutableStateOf(actual) }
        var errorLocal by remember { mutableStateOf<String?>(null) }

        androidx.compose.material3.AlertDialog(
            onDismissRequest = { renaming = null },
            title = { Text("Renombrar categoría") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = nuevo,
                        onValueChange = { nuevo = it; errorLocal = null },
                        label = { Text("Nuevo nombre") },
                        singleLine = true,
                        isError = errorLocal != null
                    )
                    errorLocal?.let { Text(it, color = cs.error, style = MaterialTheme.typography.labelSmall) }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (nuevo.isBlank()) { errorLocal = "Nombre requerido"; return@TextButton }
                    vm.renombrar(id, nuevo)
                    renaming = null
                }) { Text("Guardar") }
            },
            dismissButton = { TextButton(onClick = { renaming = null }) { Text("Cancelar") } }
        )
    }
}


@Composable
private fun AdminReportesTab() {
    val context = LocalContext.current

    // VM de reportes (total usuarios + últimos registrados)
    val vm: AdminReportesViewModel =
        viewModel(factory = AdminReportesViewModelFactory(context))

    // Estados en vivo (con valor inicial para evitar “cannot infer type”)
    val totalUsuarios by vm.totalUsuarios.collectAsStateWithLifecycle(initialValue = 0)
    val ultimos        by vm.ultimosRegistrados.collectAsStateWithLifecycle(initialValue = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Reportes", style = MaterialTheme.typography.titleMedium)

        // Tarjeta: total de usuarios
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("Usuarios registrados", fontWeight = FontWeight.SemiBold)
                Text("Total: $totalUsuarios")
            }
        }

        // Tarjeta: últimos registrados (nombre + correo)
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Últimos registrados", fontWeight = FontWeight.SemiBold)

                if (ultimos.isEmpty()) {
                    Text("Sin registraciones recientes.")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(ultimos, key = { it.rut }) { row ->
                            ListItem(
                                headlineContent = { Text(row.name) },
                                supportingContent = { Text(row.email) }
                            )
                            Divider()
                        }
                    }
                }
            }
        }
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
                    label = { Text("Teléfono") },
                    singleLine = true,
                    isError = estado.errTelefono != null
                )
                estado.errTelefono?.let { Text(it, color = cs.error, style = MaterialTheme.typography.labelSmall) }

                OutlinedTextField(
                    value = estado.cDireccion,
                    onValueChange = vm::onCambiarDireccionCrear,
                    label = { Text("Dirección") },
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
                        onValueChange = { },
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
