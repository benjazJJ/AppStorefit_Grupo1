package com.example.appstorefit_grupo1.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.appstorefit_grupo1.R
import com.example.appstorefit_grupo1.ViewModel.CarritoViewModel
import com.example.appstorefit_grupo1.ViewModel.CarritoViewModelFactory
import com.example.appstorefit_grupo1.data.local.database.AppDatabase
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CarritoScreen(navController: NavHostController) {
    val ctx = LocalContext.current
    val carritoDao = remember { AppDatabase.getInstance(ctx).carritoDao() }
    val vm: CarritoViewModel = viewModel(factory = CarritoViewModelFactory(carritoDao))
    val state by vm.uiState.collectAsStateWithLifecycle()

    Scaffold(
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
                        onClick = { /* TODO: ir a confirmación/pago */ },
                        enabled = state.totalCLP > 0,
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
                        imageRes = modeloToDrawable(item.modelo), // ajusta mapeo abajo
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
                        onRestar = { vm.disminuir(item.idCategoria, item.idProducto, item.color, item.talla) },
                        onEliminar = { vm.eliminar(item.idCategoria, item.idProducto, item.color, item.talla) }
                    )
                }
            }
        }
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
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Image(
                painter = painterResource(imageRes),
                contentDescription = modelo,
                modifier = Modifier.size(64.dp)
            )

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
                    "TALLA $talla",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilledTonalButton(
                    onClick = onRestar,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(32.dp)
                ) { Text("−") }

                Text("$cantidad", style = MaterialTheme.typography.bodyLarge)

                FilledTonalButton(
                    onClick = onSumar,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(32.dp)
                ) { Text("+") }
            }

            Spacer(Modifier.width(8.dp))

            Text(
                text = (precioUnitarioCLP * cantidad).toCLP(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.width(8.dp))

            OutlinedButton(onClick = onEliminar) {
                Text("Eliminar")
            }
        }
    }
}

/** Mapea el modelo a tu drawable en res/drawable */
private fun modeloToDrawable(modelo: String): Int = when (modelo.uppercase()) {
    // 👇 Ajusta estos nombres al de tus PNG reales (según tus capturas)
    "XFITRX"    -> R.drawable.polerastorefit
    "WARMGLIDE" -> R.drawable.poleronstorefit
    "FLEXRUN"   -> R.drawable.buzostorefit
    "FITQUEEN"  -> R.drawable.topmujerstorefit
    else        -> R.drawable.storefitlogo
}

/** 24990 -> $24.990 (Chile) */
private fun Int.toCLP(): String {
    val f = NumberFormat.getNumberInstance(Locale("es", "CL"))
    return "$" + f.format(this)
}