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
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.appstorefit_grupo1.R
import com.example.appstorefit_grupo1.data.local.Productos.ProductosEntity
import com.example.appstorefit_grupo1.data.local.database.AppDatabase
import com.example.appstorefit_grupo1.data.repository.ProductosRepository
import com.example.appstorefit_grupo1.navigation.Route
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

data class ProductoCardData(
    val idCategoria: Long,
    val modeloBase: String,  // sin prefijo B
    val nombre: String,
    val precio: Int,
    val imagenRes: Int,
    val hayStock: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductosScreen(
    widthClass: WindowWidthSizeClass,
    nav: NavController,
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

    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val repo = remember { ProductosRepository(db.productosDao()) }

    var items by remember { mutableStateOf<List<ProductoCardData>>(emptyList()) }

    LaunchedEffect(Unit) {
        // Cargar todos los productos y agrupar por modelo base (quitando 'B')
        val all: List<ProductosEntity> = withContext(Dispatchers.IO) {
            repo.getAll().getOrDefault(emptyList())
        }

        // Tomamos solo los modelos que quieres mostrar en catálogo (tus 4)
        // Si mañana agregas más, puedes construir esta lista dinámicamente por categoría.
        val modelosCatalogo = listOf(
            1L to "XFITRX",
            3L to "FLEXRUN",
            2L to "WARMGLIDE",
            4L to "FITQUEEN"
        )

        val mapped = modelosCatalogo.map { (idCat, modeloBase) ->
            val variantes = all.filter {
                it.idCategoria == idCat && (it.modelo == modeloBase || it.modelo == "B$modeloBase")
            }

            val hayStock = variantes.any { it.stock > 0 }

            // Precio: usa cualquiera de las variantes del modelo base si existe, si no cualquiera
            val precio = variantes.firstOrNull { it.modelo == modeloBase }?.precio
                ?: variantes.firstOrNull()?.precio
                ?: 0

            val imagenRes = when (idCat) {
                1L -> R.drawable.polerastorefit
                2L -> R.drawable.poleronstorefit
                3L -> R.drawable.buzostorefit
                4L -> R.drawable.topmujerstorefit
                else -> R.drawable.polerastorefit
            }

            val nombre = when (idCat) {
                1L -> "Polera StoreFit ($modeloBase)"
                2L -> "Polerón StoreFit ($modeloBase)"
                3L -> "Buzo StoreFit ($modeloBase)"
                4L -> "Conjunto Femenino ($modeloBase)"
                else -> "Producto ($modeloBase)"
            }

            ProductoCardData(
                idCategoria = idCat,
                modeloBase = modeloBase,
                nombre = nombre,
                precio = precio,
                imagenRes = imagenRes,
                hayStock = hayStock
            )
        }

        items = mapped
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
            items(items, key = { "${it.idCategoria}-${it.modeloBase}" }) { p ->
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

                        // Estado de stock (cualquier variante del modelo)
                        val stockText = if (p.hayStock) "Stock disponible" else "Sin stock"
                        val stockColor = if (p.hayStock) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                        Text(stockText, color = stockColor)

                        Spacer(Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = { onOpenDetalle(p.idCategoria, p.modeloBase) },
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
