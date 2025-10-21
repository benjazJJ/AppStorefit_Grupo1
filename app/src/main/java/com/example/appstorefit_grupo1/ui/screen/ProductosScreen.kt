package com.example.appstorefit_grupo1.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.appstorefit_grupo1.R
import com.example.appstorefit_grupo1.navigation.Route
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

// ➜ Incluye idCategoria y modelo para poder ir al detalle
data class Producto(
    val id: String,
    val idCategoria: Long,
    val modelo: String,
    val nombre: String,
    val precio: Int,
    val imagenRes: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductosScreen(
    widthClass: WindowWidthSizeClass,
    nav: NavController,
    // Se mantiene por compatibilidad si lo usabas en previews o llamados anteriores (no se usa ya)
    onAddToCart: (Producto) -> Unit = { },
    onOpenDetalle: (Long, String) -> Unit = { idCategoria, modelo ->
        nav.navigate(Route.DetalleProducto.create(idCategoria, modelo))
    }
) {
    val columns = when (widthClass) {
        WindowWidthSizeClass.Expanded -> 4
        WindowWidthSizeClass.Medium   -> 3
        else                          -> 2
    }

    val clp = remember {
        NumberFormat.getCurrencyInstance(Locale("es", "CL")).apply {
            currency = Currency.getInstance("CLP")
            maximumFractionDigits = 0
        }
    }

    // ➜ Solo tus 4 categorías, con modelo por categoría (coincide con el seed)
    val productos = remember {
        listOf(
            Producto("1", idCategoria = 1L, modelo = "XFITRX",    nombre = "Polera StoreFit (XFITRX)",     precio = 9_990,  imagenRes = R.drawable.polerastorefit),
            Producto("2", idCategoria = 3L, modelo = "FLEXRUN",   nombre = "Buzo StoreFit (FLEXRUN)",      precio = 14_990, imagenRes = R.drawable.buzostorefit),
            Producto("3", idCategoria = 2L, modelo = "WARMGLIDE", nombre = "Polerón StoreFit (WARMGLIDE)", precio = 17_990, imagenRes = R.drawable.poleronstorefit),
            Producto("4", idCategoria = 4L, modelo = "FITQUEEN",  nombre = "Conjunto Femenino (FITQUEEN)", precio = 19_990, imagenRes = R.drawable.topmujerstorefit),
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Productos") }) }
    ) { inner ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
        ) {
            items(productos, key = { it.id }) { p ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Image(
                            painter = painterResource(p.imagenRes),
                            contentDescription = p.nombre,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(Modifier.height(10.dp))

                        Text(
                            p.nombre,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2
                        )
                        Text(
                            clp.format(p.precio),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(Modifier.height(10.dp))

                        // 👉 Solo queda el botón Detalle
                        OutlinedButton(
                            onClick = { onOpenDetalle(p.idCategoria, p.modelo) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                        ) { Text("Detalle") }
                    }
                }
            }
        }
    }
}

/* ===== Previews (solo IDE) ===== */
@Preview(name = "Productos – Compacta", widthDp = 360, heightDp = 800, showSystemUi = true)
@Composable
private fun PreviewProductosCompact() {
    ProductosScreen(
        widthClass = WindowWidthSizeClass.Compact,
        nav = rememberNavController()
    )
}

@Preview(name = "Productos – Expandida", widthDp = 1000, heightDp = 800, showSystemUi = true)
@Composable
private fun PreviewProductosExpanded() {
    ProductosScreen(
        widthClass = WindowWidthSizeClass.Expanded,
        nav = rememberNavController()
    )
}