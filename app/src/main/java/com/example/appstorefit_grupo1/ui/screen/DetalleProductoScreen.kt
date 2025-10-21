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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.appstorefit_grupo1.R
import com.example.appstorefit_grupo1.data.local.Productos.ProductosEntity
import com.example.appstorefit_grupo1.data.local.database.AppDatabase
import com.example.appstorefit_grupo1.data.repository.ProductosRepository
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleProductoScreen(
    navController: NavHostController,
    idCategoria: Long,
    modelo: String,
    onAddToCart: (ProductosEntity) -> Unit = {},
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val repo = remember { ProductosRepository(db.productosDao()) }

    var variantes by remember { mutableStateOf<List<ProductosEntity>>(emptyList()) }
    var selectedTalla by remember { mutableStateOf<String?>(null) }
    var selectedColor by remember { mutableStateOf<String?>(null) }

    val clp = remember {
        NumberFormat.getCurrencyInstance(Locale("es", "CL")).apply {
            currency = Currency.getInstance("CLP")
            maximumFractionDigits = 0
        }
    }

    // Cargar variantes (idCategoria + modelo)
    LaunchedEffect(idCategoria, modelo) {
        variantes = repo.getByCategoria(idCategoria)
            .getOrDefault(emptyList())
            .filter { it.modelo == modelo }
            .sortedWith(compareBy<ProductosEntity> { it.talla }.thenBy { it.color })

        if (variantes.isNotEmpty()) {
            selectedTalla = variantes.first().talla
            // Si aún no hay color seleccionado, parte con el negro (si existe) o el primero
            selectedColor = variantes.firstOrNull { it.color == "Negro con detalles blancos" }?.color
                ?: variantes.first().color
        } else {
            selectedColor = "Negro con detalles blancos"
        }
    }

    val tallas = remember(variantes) { variantes.map { it.talla }.distinct() }

    // Imagen según categoría + color seleccionado
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

            // Selector de color (BAJO LA FOTO)
            Column {
                Text("Color", fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ColorSwatch(
                        color = Color.White,
                        borderColor = Color.LightGray,
                        selected = selectedColor == "Blanco con detalles negros",
                        onClick = { selectedColor = "Blanco con detalles negros" }
                    )
                    ColorSwatch(
                        color = Color.Black,
                        borderColor = Color.LightGray,
                        selected = selectedColor == "Negro con detalles blancos",
                        onClick = { selectedColor = "Negro con detalles blancos" }
                    )
                }
            }

            // Información y selección de talla
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = tituloPorCategoria(idCategoria),
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

                // Precio y stock
                Text("Precio: ${clp.format(precio)}", fontWeight = FontWeight.Bold)
                Text(
                    text = if (stock > 0) "Stock disponible: $stock" else "Sin stock",
                    color = if (stock > 0) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error
                )

                // Botones
                Button(
                    onClick = { varianteSeleccionada?.let(onAddToCart) },
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

/** Cuadrito de color seleccionable */
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