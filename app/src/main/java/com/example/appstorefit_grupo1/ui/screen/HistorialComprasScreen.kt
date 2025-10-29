package com.example.appstorefit_grupo1.ui.screen

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.appstorefit_grupo1.session.SessionManager
import com.example.appstorefit_grupo1.ViewModel.ComprasViewModel
import com.example.appstorefit_grupo1.ViewModel.ComprasViewModelFactory
import com.example.appstorefit_grupo1.ui.theme.SF_Blue
import com.example.appstorefit_grupo1.ui.theme.SF_Purple
import com.example.appstorefit_grupo1.ui.theme.SF_Teal
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialComprasScreen(navController: NavHostController) {
    val context = LocalContext.current
    val comprasVm: ComprasViewModel = viewModel(factory = ComprasViewModelFactory(context))

    // RUT de la sesión
    val rut = remember { SessionManager.user?.rut.orEmpty() }
    LaunchedEffect(rut) { if (rut.isNotBlank()) comprasVm.cargar(rut) }

    // Estados del VM
    val historial by comprasVm.historial.collectAsStateWithLifecycle()
    val totalGastado by comprasVm.totalGastado.collectAsStateWithLifecycle()

    // Formateadores
    val clp = remember { NumberFormat.getCurrencyInstance(Locale("es", "CL")) }
    val fmt = remember { SimpleDateFormat("dd-MM-yyyy HH:mm", Locale("es", "CL")) }

    // Mismos degradados que usas en Perfil
    val grad1 = remember { Brush.horizontalGradient(listOf(SF_Teal, SF_Blue)) }
    val grad2 = remember { Brush.horizontalGradient(listOf(SF_Blue, SF_Purple)) }

    val cardShape = RoundedCornerShape(16.dp)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de compras") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Total gastado: ${clp.format(totalGastado)}",
                style = MaterialTheme.typography.titleMedium
            )

            if (historial.isEmpty()) {
                Text("Aún no tienes compras registradas.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    itemsIndexed(historial) { index, compraCon ->
                        val compra = compraCon.compra
                        val detalles = compraCon.detalles
                        val totalCompra = detalles.sumOf { it.cantidad * it.precioUnitario }

                        val brush = if (index % 2 == 0) grad1 else grad2

                        // Contenedor con borde degradado
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(width = 2.dp, brush = brush, shape = cardShape)
                                .padding(1.dp) // deja ver el borde
                        ) {
                            ElevatedCard(
                                shape = cardShape,
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("Fecha: ${fmt.format(Date(compra.fechaMillis))}")
                                    Divider()
                                    detalles.forEach { d ->
                                        Text("- ${d.nombreProducto} x${d.cantidad} • ${clp.format(d.precioUnitario)} c/u")
                                    }
                                    Divider(Modifier.padding(top = 6.dp, bottom = 4.dp))
                                    Text("Total compra: ${clp.format(totalCompra)}")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}