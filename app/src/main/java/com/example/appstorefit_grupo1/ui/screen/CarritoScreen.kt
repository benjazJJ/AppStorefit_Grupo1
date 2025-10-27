package com.example.appstorefit_grupo1.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.appstorefit_grupo1.R
import com.example.appstorefit_grupo1.ViewModel.CarritoViewModel
import com.example.appstorefit_grupo1.ViewModel.CarritoViewModelFactory
import com.example.appstorefit_grupo1.data.local.database.AppDatabase
import com.example.appstorefit_grupo1.navigation.Route
import com.example.appstorefit_grupo1.ui.components.SuccessCheckoutDialog
import java.text.NumberFormat
import java.util.Locale

private const val COLOR_BLANCO = "Blanco con detalles negros"
private const val COLOR_NEGRO  = "Negro con detalles blancos"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarritoScreen(navController: NavHostController) {
    val ctx = LocalContext.current

    val carritoDao = remember { AppDatabase.getInstance(ctx).carritoDao() }
    val vm: CarritoViewModel = viewModel(factory = CarritoViewModelFactory(ctx, carritoDao))
    val state by vm.uiState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var mostrarDialogoExito by remember { mutableStateOf(false) }
    // Abre/cierra el sheet de confirmación de compra
    var mostrarCheckout by remember { mutableStateOf(false) }

    // Sheet state recomendado (evita estado parcialmente expandido)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val evento = vm.eventos.collectAsState(initial = null).value
    LaunchedEffect(evento) {
        evento?.let { msg ->
            if (msg.startsWith("Compra confirmada")) {
                mostrarDialogoExito = true
            }
            snackbarHostState.showSnackbar(message = msg)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TOTAL: ${state.totalCLP.toCLP()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = { mostrarCheckout = true },
                        enabled = state.items.isNotEmpty(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("FINALIZAR COMPRA")
                    }
                }
            }
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "TU CARRITO",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black
            )

            if (state.items.isEmpty()) {
                Text("Aún no tienes productos en el carrito.")
            } else {
                state.items.forEach { item ->
                    CarritoItemCard(
                        modelo = item.modelo,
                        talla = item.talla,
                        cantidad = item.cantidad,
                        precioUnitarioCLP = item.precioUnitario,
                        imageRes = modeloToDrawable(item.modelo, item.color),
                        onSumar = {
                            vm.agregar(
                                idCat = item.idCategoria,
                                idProd = item.idProducto,
                                modelo = item.modelo,
                                color = item.color,
                                talla = item.talla,
                                precioUnit = item.precioUnitario
                            )
                        },
                        onRestar = {
                            vm.disminuir(item.idCategoria, item.idProducto, item.color, item.talla)
                        },
                        onEliminar = {
                            vm.eliminar(item.idCategoria, item.idProducto, item.color, item.talla)
                        }
                    )
                }
            }
        }
    }

    // BottomSheet de confirmación de compra
    if (mostrarCheckout) {
        ModalBottomSheet(
            onDismissRequest = { mostrarCheckout = false },
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Confirmar compra",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                // Resumen simple
                Text("Productos: ${state.items.size}")
                Text("Total a pagar: ${state.totalCLP.toCLP()}", fontWeight = FontWeight.SemiBold)

                // Lista rápida de productos
                Divider()
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    state.items.forEach { it ->
                        Text("• ${it.modelo} — Talla ${it.talla} × ${it.cantidad}")
                    }
                }
                Divider()

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = { mostrarCheckout = false },
                        modifier = Modifier.weight(1f)
                    ) { Text("Cancelar") }

                    Button(
                        onClick = {
                            // Ejecuta la compra real
                            mostrarCheckout = false
                            vm.onComprar()
                            // El LaunchedEffect(evento) mostrará snackbar + SuccessCheckoutDialog
                        },
                        enabled = state.items.isNotEmpty(),
                        modifier = Modifier.weight(1f)
                    ) { Text("Confirmar compra") }
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }

    if (mostrarDialogoExito) {
        SuccessCheckoutDialog(
            message = "¡Compra realizada con éxito!",
            onDismiss = {
                mostrarDialogoExito = false
                navController.navigate(Route.Productos.path) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }
        )
    }
}

@Composable
private fun ProductThumb(
    imageRes: Int,
    contentDescription: String?,
    size: Dp = 64.dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(14.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(imageRes),
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun CarritoItemCard(
    modelo: String,
    talla: String,
    cantidad: Int,
    precioUnitarioCLP: Int,
    imageRes: Int,
    onSumar: () -> Unit,
    onRestar: () -> Unit,
    onEliminar: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProductThumb(imageRes = imageRes, contentDescription = modelo, size = 68.dp)

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text(
                    modelo,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    "Talla: $talla",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalButton(
                        onClick = onRestar,
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.size(28.dp)
                    ) { Text("−") }

                    Text(" $cantidad ", style = MaterialTheme.typography.bodyLarge)

                    FilledTonalButton(
                        onClick = onSumar,
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.size(28.dp)
                    ) { Text("+") }
                }
            }

            Spacer(Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = (precioUnitarioCLP * cantidad).toCLP(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                TextButton(
                    onClick = {
                        if (cantidad > 1) onRestar() else onEliminar()
                    }
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Eliminar")
                }
            }
        }
    }
}

private fun modeloToDrawable(modelo: String, color: String): Int {
    val m = modelo.trim()
    val base = m.removePrefix("B").uppercase()

    // Blanco si: color EXACTO "Blanco con detalles negros" o el modelo viene con prefijo B
    val esBlanco = color.equals(COLOR_BLANCO, ignoreCase = true) ||
            m.startsWith("B", ignoreCase = true)

    return when (base) {
        "XFITRX"    -> if (esBlanco) R.drawable.polerablancastorefit     else R.drawable.polerastorefit
        "WARMGLIDE" -> if (esBlanco) R.drawable.poleronblancostorefit    else R.drawable.poleronstorefit
        "FLEXRUN"   -> if (esBlanco) R.drawable.buzoblancostorefit       else R.drawable.buzostorefit
        "FITQUEEN"  -> if (esBlanco) R.drawable.topmujerblancostorefit   else R.drawable.topmujerstorefit
        else        -> R.drawable.storefitlogo
    }
}

private fun Int.toCLP(): String {
    val f = NumberFormat.getNumberInstance(Locale("es", "CL"))
    return "$" + f.format(this)
}
