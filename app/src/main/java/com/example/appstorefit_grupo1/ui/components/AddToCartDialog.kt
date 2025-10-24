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
    autoDismissMillis: Long = 900L,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.medium, tonalElevation = 6.dp) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val composition by rememberLottieComposition(
                    LottieCompositionSpec.RawRes(R.raw.anadircarrito) // tu json
                )
                val progress by animateLottieCompositionAsState(
                    composition = composition,
                    iterations = 1
                )
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier.size(120.dp)
                )
                Text(text = message, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
    LaunchedEffect(Unit) {
        delay(autoDismissMillis)
        onDismiss()
    }
}