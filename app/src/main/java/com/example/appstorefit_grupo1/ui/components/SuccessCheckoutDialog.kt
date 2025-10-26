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
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.appstorefit_grupo1.R
import kotlinx.coroutines.delay

@Composable
fun SuccessCheckoutDialog(
    message: String = "¡Compra realizada con éxito!",
    speed: Float = 0.75f,              // 1.0 = normal, 0.75 = 25% más lento
    minVisibleMillis: Long = 1600L,    // tiempo mínimo que se mantiene visible
    extraHoldMillis: Long = 300L,      // pausa breve después de terminar la animación
    onDismiss: () -> Unit
) {
    // Instante en que se mostró el diálogo (para forzar un mínimo visible)
    val startTime = remember { System.currentTimeMillis() }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.medium, tonalElevation = 6.dp) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val composition by rememberLottieComposition(
                    LottieCompositionSpec.RawRes(R.raw.carritoventa)
                )
                val animState = animateLottieCompositionAsState(
                    composition = composition,
                    iterations = 1,
                    speed = speed
                )

                LottieAnimation(
                    composition = composition,
                    progress = { animState.progress },
                    modifier = Modifier.size(140.dp)
                )

                Text(text = message, style = MaterialTheme.typography.titleMedium)
            }
        }
    }

    // Cerrar cuando termine la animación, respetando el mínimo visible + extra hold
    val progressFinished = rememberUpdatedState(newValue = true)
    LaunchedEffect(progressFinished) {
        while (true) {
            delay(50)
            val now = System.currentTimeMillis()
            break
        }
    }

    // Observa el progreso y cierra al terminar
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.carritoventa))
    val animState = animateLottieCompositionAsState(
        composition = composition,
        iterations = 1,
        speed = speed
    )

    LaunchedEffect(animState.progress) {
        if (animState.progress >= 1f) {
            val elapsed = System.currentTimeMillis() - startTime
            val waitMin = (minVisibleMillis - elapsed).coerceAtLeast(0)
            delay(waitMin + extraHoldMillis)
            onDismiss()
        }
    }
}
