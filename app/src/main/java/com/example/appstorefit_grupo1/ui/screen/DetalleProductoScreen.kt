package com.example.appstorefit_grupo1.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.appstorefit_grupo1.R
import com.example.appstorefit_grupo1.data.local.Productos.ProductosEntity
import com.example.appstorefit_grupo1.data.local.database.AppDatabase
import com.example.appstorefit_grupo1.data.repository.ProductosRepository
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
    onAddToCart: (ProductosEntity) -> Unit = {},  // te entrega la variante exacta
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val repo = remember { ProductosRepository(db.productosDao()) }
    val scope = rememberCoroutineScope()

    var variantes by remember { mutableStateOf<List<ProductosEntity>>(emptyList()) }
    var selectedTalla by remember { mutableStateOf<String?>(null) }
    var selectedColor by remember { mutableStateOf<String?>(null) }

    // CLP sin decimales
    val clp = remember {
        NumberFormat.getCurrencyInstance(Locale("es", "CL")).apply {
            currency = Currency.getInstance("CLP")
            maximumFractionDigits = 0
        }
    }

    // Carga de variantes (idCategoria + modelo)
    LaunchedEffect(idCategoria, modelo) {
        variantes = repo.getByCategoria(idCategoria)
            .getOrDefault(emptyList())
            .filter { it.modelo == modelo }
            .sortedWith(compareBy<ProductosEntity> { it.talla }.thenBy { it.color })
        // autoselección inicial si hay data
        if (variantes.isNotEmpty()) {
            selectedTalla = variantes.first().talla
            selectedColor = variantes.first().color
        }
    }

    val tallas = remember(variantes) { variantes.map { it.talla }.distinct() }
    val colores = listOf("Blanco con detalles negros", "Negro con detalles blancos")
    val imagenRes = when (idCategoria) {
        1L -> R.drawable.polerastorefit
        2L -> R.drawable.poleronstorefit
        3L -> R.drawable.buzostorefit
        4L -> R.drawable.topmujerstorefit
        else -> R.drawable.polerastorefit
    }

    val varianteSeleccionada = variantes.firstOrNull { it.talla == selectedTalla && it.color == selectedColor }
    val precio = varianteSeleccionada?.precio ?: variantes.firstOrNull()?.precio ?: 0
    val stock = varianteSeleccionada?.stock ?: 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = modelo) },
                navigationIcon = {
                    TextButton(onClick = { navController.popBackStack() }) { Text("Volver") }
                }
            )
        }
    ) { inner ->
        // Layout responsive simple: imagen a la izquierda, info a la derecha (en pantallas estrechas se apilan)
        Column(
            modifier = Modifier
                .padding(inner)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Imagen
            Image(
                painter = painterResource(imagenRes),
                contentDescription = modelo,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )

            // Info
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = tituloPorCategoria(idCategoria),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                // Bullets (dummy; puedes personalizar por categoría)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("• Tejido transpirable y de secado rápido")
                    Text("• Costuras reforzadas para uso deportivo")
                    Text("• Marca: StoreFit")
                }

                // Tallas
                Text("Talla", fontWeight = FontWeight.SemiBold)
                FlowRowWrap {
                    tallas.forEach { t ->
                        val selected = selectedTalla == t
                        OutlinedButton(
                            onClick = { selectedTalla = t },
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                            )
                        ) { Text(t) }
                    }
                }

                // Colores
                Text("Color", fontWeight = FontWeight.SemiBold)
                FlowRowWrap {
                    colores.forEach { c ->
                        val selected = selectedColor == c
                        OutlinedButton(
                            onClick = { selectedColor = c },
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                            )
                        ) { Text(c) }
                    }
                }

                // Precio y stock
                Text("Precio: ${clp.format(precio)}", fontWeight = FontWeight.Bold)
                Text(
                    text = if (stock > 0) "Stock disponible: $stock" else "Sin stock",
                    color = if (stock > 0) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error
                )

                // Botones principales
                Button(
                    onClick = {
                        varianteSeleccionada?.let { onAddToCart(it) }
                    },
                    enabled = (selectedTalla != null && selectedColor != null && varianteSeleccionada != null && stock > 0),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Añadir al carrito") }

                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("← Volver a productos") }
            }
        }
    }
}

/**
 * Chips en varias líneas sin depender de versiones específicas de FlowRow.
 * (Si prefieres FlowRow oficial, puedes usar foundation: FlowRow)
 */
@Composable
private fun FlowRowWrap(
    horizontalGap: Dp = 8.dp,
    verticalGap: Dp = 8.dp,
    content: @Composable RowScope.() -> Unit
) {
    // Implementación simple: en la práctica, si ya usas foundation FlowRow, reemplaza por:
    // FlowRow(horizontalArrangement = Arrangement.spacedBy(horizontalGap), verticalArrangement = Arrangement.spacedBy(verticalGap)) { ... }
    Row(
        horizontalArrangement = Arrangement.spacedBy(horizontalGap),
        verticalAlignment = Alignment.CenterVertically
    ) { content() }
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