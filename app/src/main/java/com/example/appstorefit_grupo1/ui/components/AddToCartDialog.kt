// AddToCartDialog.kt
package com.example.appstorefit_grupo1.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.airbnb.lottie.compose.*
import com.example.appstorefit_grupo1.R
import kotlinx.coroutines.delay

@Composable
fun AddToCartDialog(
    message: String = "Producto añadido al carrito",
    trigger: Int,              //Cada vez que cambia reinicia para ver la animación
    speed: Float = 2.8f,
    minVisibleMillis: Long = 1200L,
    extraHoldMillis: Long = 200L,
    onDismiss: () -> Unit
) {
    val startTime = remember { System.currentTimeMillis() }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.medium, tonalElevation = 6.dp) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Reinicia composición/animación cuando cambia "trigger"
                key(trigger) {
                    val composition by rememberLottieComposition(
                        LottieCompositionSpec.RawRes(R.raw.anadircarrito)
                    )
                    val animState = animateLottieCompositionAsState(
                        composition = composition,
                        iterations = 1,
                        speed = speed
                    )

                    LottieAnimation(
                        composition = composition,
                        progress = { animState.progress },
                        modifier = Modifier.size(120.dp)
                    )

                    // Cerrar cuando termine, respetando un mínimo visible
                    LaunchedEffect(animState.progress) {
                        if (animState.progress >= 1f) {
                            val elapsed = System.currentTimeMillis() - startTime
                            val waitMin = (minVisibleMillis - elapsed).coerceAtLeast(0)
                            delay(waitMin + extraHoldMillis)
                            onDismiss()
                        }
                    }
                }

                Text(text = message, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
