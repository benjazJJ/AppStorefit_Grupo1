package com.example.appstorefit_grupo1.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.appstorefit_grupo1.R
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
import androidx.compose.foundation.BorderStroke

data class Producto(val id: String, val nombre: String, val precio: Int, val imagenRes: Int)

@Composable
fun ProductoScreen(
    onAddToCart: (Producto) -> Unit = {},
    onOpenDetalle: (String) -> Unit = {}
) {
    // FORMATEADOR PARA LOS PRECIOS A CLP!!
    val clp = remember {
        NumberFormat.getCurrencyInstance(Locale("es", "CL")).apply {
            currency = Currency.getInstance("CLP")
            maximumFractionDigits = 0
        }
    }

    val productos = remember {
        listOf(
            Producto("1", "Polera Deportiva", 12990, R.drawable.polerastorefit),
            Producto("2", "Buzo Deportivo", 12990, R.drawable.buzostorefit),
            Producto("3", "Polerón Deportivo", 12990, R.drawable.poleronstorefit),
            Producto("4", "Top Deportivo", 12990, R.drawable.topmujerstorefit),
            Producto("5", "Zapatillas Deportivas", 12990, R.drawable.zapatillastorefit)
        )
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 180.dp),           // celdas más anchas
        contentPadding = PaddingValues(16.dp),                    // margen del grid
        verticalArrangement = Arrangement.spacedBy(16.dp),        // espacio vertical entre cards
        horizontalArrangement = Arrangement.spacedBy(16.dp),      // espacio horizontal entre cards
        modifier = Modifier.fillMaxSize()
    ) {
        items(productos, key = { it.id }) { p ->
            Card {
                Column(Modifier.padding(12.dp)) {
                    // Imagen cuadrada, recorte suave y tamaño mínimo
                    Image(
                        painter = painterResource(p.imagenRes),
                        contentDescription = p.nombre,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)                      // 1:1 para evitar cortes de las imagenes
                            .heightIn(min = 160.dp)
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

                    Button(
                        onClick = { onAddToCart(p) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Agregar", maxLines = 1)
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { onOpenDetalle(p.id) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Detalle", maxLines = 1)
                    }
                }
            }
        }
    }
}

// PREVIEW
@Preview(showBackground = true)
@Composable
fun ProductosScreenPreview() {
    ProductoScreen()
}
