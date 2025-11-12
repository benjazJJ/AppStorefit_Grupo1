package com.example.appstorefit_grupo1.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.appstorefit_grupo1.R
import com.example.appstorefit_grupo1.ui.ViewModel.CarritoViewModel
import com.example.appstorefit_grupo1.ui.ViewModel.CarritoViewModelFactory
import com.example.appstorefit_grupo1.data.local.Productos.ProductosEntity
import com.example.appstorefit_grupo1.data.local.database.AppDatabase
import com.example.appstorefit_grupo1.data.repository.ProductosRepository
import com.example.appstorefit_grupo1.ui.components.AddToCartDialog
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleProductoScreen(
    navController: NavHostController,
    idCategoria: Long,
    modelo: String,
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val repo = remember { ProductosRepository(db.productosDao()) }

    // ViewModel del carrito para agregar directamente
    val carritoVm: CarritoViewModel =
        viewModel(factory = CarritoViewModelFactory(context, db.carritoDao()))

    // Snackbar para mostrar mensajes (p. ej., “Sin stock suficiente”)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Flag + trigger para la animación
    var mostrarAnimAdd by remember { mutableStateOf(false) }
    var animTrigger by remember { mutableIntStateOf(0) }

    //PARA QUE SE MUESTRE SIEMPRE LA ANIMACIÓN
    LaunchedEffect(Unit) {
        carritoVm.eventos.collect { msg ->
            if (msg == "Agregado al carrito") {
                animTrigger++          // nuevo disparo
                mostrarAnimAdd = true  // asegurar que se muestre
            } else {
                snackbarHostState.showSnackbar(message = msg)
            }
        }
    }

    // Escucha de eventos del VM (éxiton y error al agregar)
    val evento = carritoVm.eventos.collectAsState(initial = null).value
    LaunchedEffect(evento) {
        evento?.let { msg ->
            if (msg == "Agregado al carrito") {
                // Éxito: animación
                mostrarAnimAdd = true
            } else {
                // Otros mensajes: Snackbar (errores de stock, etc.)
                snackbarHostState.showSnackbar(message = msg)
            }
        }
    }

    var variantes by remember { mutableStateOf<List<ProductosEntity>>(emptyList()) }
    var selectedTalla by remember { mutableStateOf<String?>(null) }
    var selectedColor by remember { mutableStateOf<String?>(null) }
    var varianteSelect by remember { mutableStateOf<ProductosEntity?>(null) }

    // Orden correcto de tallas para la UI
    val ordenTallas = listOf("XS","S","M","L","XL")
    fun idxTalla(t: String) = ordenTallas.indexOf(t).let { if (it == -1) Int.MAX_VALUE else it }

    val clp = remember {
        NumberFormat.getCurrencyInstance(Locale("es", "CL")).apply {
            currency = Currency.getInstance("CLP")
            maximumFractionDigits = 0
        }
    }

    LaunchedEffect(idCategoria, modelo) {
        val all = repo.getByCategoria(idCategoria).getOrDefault(emptyList())
        variantes = all
            .filter { it.modelo == modelo || it.modelo == "B$modelo" }
            .sortedWith(compareBy<ProductosEntity>({ idxTalla(it.talla) }, { it.color }))
    }

    LaunchedEffect(selectedColor, selectedTalla, idCategoria, modelo) {
        val color = selectedColor ?: return@LaunchedEffect
        val talla = selectedTalla ?: return@LaunchedEffect
        val modeloEfectivo = if (color == "Blanco con detalles negros") "B$modelo" else modelo

        varianteSelect = db.productosDao().getByCatModeloColorTalla(
            idCategoria = idCategoria,
            modelo = modeloEfectivo,
            color = color,
            talla = talla
        )
    }

    val NEGRO = "Negro con detalles blancos"
    val BLANCO = "Blanco con detalles negros"

// Tallas existentes (en cualquier color)
    val tallas = remember(variantes) {
        variantes.map { it.talla }.distinct().sortedBy { idxTalla(it) }
    }

// Disponibilidad por color para la talla actual
    val negroDisponible = remember(variantes, selectedTalla) {
        variantes.any { it.talla == selectedTalla && it.color == NEGRO }
    }
    val blancoDisponible = remember(variantes, selectedTalla) {
        variantes.any { it.talla == selectedTalla && it.color == BLANCO }
    }

    val imagenRes = remember(idCategoria, selectedColor) {
        val blanco = selectedColor == "Blanco con detalles negros"
        when (idCategoria) {
            1L -> if (blanco) R.drawable.polerablancastorefit else R.drawable.polerastorefit
            2L -> if (blanco) R.drawable.poleronblancostorefit else R.drawable.poleronstorefit
            3L -> if (blanco) R.drawable.buzoblancostorefit else R.drawable.buzostorefit
            4L -> if (blanco) R.drawable.topmujerblancostorefit else R.drawable.topmujerstorefit
            else -> if (blanco) R.drawable.polerablancastorefit else R.drawable.polerastorefit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle producto") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) } // mensajes del VM
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Imagen del producto
            Image(
                painter = painterResource(imagenRes),
                contentDescription = modelo,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )

            // Selector de color
            Column {
                Text("Color", fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // BLANCO
                    ColorSwatch(
                        color = Color.White,
                        borderColor = if (blancoDisponible) Color.LightGray else Color.Gray,
                        selected = selectedColor == BLANCO,
                        onClick = {
                            // Si la talla actual no existe en blanco, mover a la primera talla disponible en blanco
                            val tallaOk = variantes.firstOrNull { it.color == BLANCO && it.talla == selectedTalla }?.talla
                                ?: variantes.firstOrNull { it.color == BLANCO }?.talla
                            if (tallaOk != null) {
                                selectedTalla = tallaOk
                                selectedColor = BLANCO
                            } else {
                                // No hay blanco en ninguna talla
                                scope.launch { snackbarHostState.showSnackbar("No disponible en blanco") }
                            }
                        }
                    )

                    // NEGRO
                    ColorSwatch(
                        color = Color.Black,
                        borderColor = if (negroDisponible) Color.LightGray else Color.Gray,
                        selected = selectedColor == NEGRO,
                        onClick = {
                            val tallaOk = variantes.firstOrNull { it.color == NEGRO && it.talla == selectedTalla }?.talla
                                ?: variantes.firstOrNull { it.color == NEGRO }?.talla
                            if (tallaOk != null) {
                                selectedTalla = tallaOk
                                selectedColor = NEGRO
                            } else {
                                scope.launch { snackbarHostState.showSnackbar("No disponible en negro") }
                            }
                        }
                    )
                }
            }

            // Info + tallas
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "${tituloPorCategoria(idCategoria)}  –  $modelo",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("• Tejido transpirable y de secado rápido")
                    Text("• Costuras reforzadas para uso deportivo")
                    Text("• Marca: StoreFit")
                }

                Text("Talla", fontWeight = FontWeight.SemiBold)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tallas.forEach { t ->
                        val selected = selectedTalla == t
                        val disponibleEnColor = variantes.any { it.talla == t && it.color == selectedColor }
                        OutlinedButton(
                            onClick = { if (disponibleEnColor) selectedTalla = t },
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                disabledContentColor = MaterialTheme.colorScheme.outline,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            enabled = disponibleEnColor
                        ) { Text(t) }
                    }
                }

                val tieneSeleccion = (selectedColor != null && selectedTalla != null && varianteSelect != null)
                val precio = varianteSelect?.precio ?: 0
                val stock  = varianteSelect?.stock  ?: 0

                // Precio y stock (solo si hay variante seleccionada)
                if (tieneSeleccion) {
                    Text("Precio: ${clp.format(precio)}", fontWeight = FontWeight.Bold)
                    Text(
                        text = if (stock > 0) "Stock disponible: $stock" else "Sin stock",
                        color = if (stock > 0) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error
                    )
                }

                // Botón: agrega al carrito con el VM
                Button(
                    onClick = {
                        val p = varianteSelect ?: return@Button
                        carritoVm.agregar(
                            idCat = p.idCategoria,
                            idProd = p.idProducto,
                            modelo = p.modelo,
                            color = p.color,
                            talla = p.talla,
                            precioUnit = p.precio
                        )
                    },
                    enabled = (varianteSelect != null && stock > 0),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Añadir al carrito") }

                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("← Volver a productos") }
            }
        }
    }

    // Diálogo con animación Lottie de “Agregado al carrito”
    if (mostrarAnimAdd) {
        AddToCartDialog(
            message = "Producto añadido al carrito",
            trigger = animTrigger,
            onDismiss = { mostrarAnimAdd = false }
        )
    }
}

// Cuadrito de color seleccionable
@Composable
private fun ColorSwatch(
    color: Color,
    borderColor: Color,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val stroke = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, borderColor)
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(color)
            .border(stroke, RoundedCornerShape(6.dp))
            .clickable { onClick() }
    )
}

@Composable
private fun tituloPorCategoria(idCategoria: Long): String =
    when (idCategoria) {
        1L -> "POLERA DEPORTIVA"
        2L -> "POLERÓN DEPORTIVO"
        3L -> "BUZO DEPORTIVO"
        4L -> "CONJUNTO FEMENINO"
        else -> "PRODUCTO"
    }
