package com.example.appstorefit_grupo1.ui.screen

import android.Manifest
import android.content.ContentValues
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import java.text.SimpleDateFormat

@Composable
fun CameraScreen(
    onPhotoTaken: (String) -> Unit,
    onCancel: () -> Unit
) {
    val ctx = LocalContext.current

    var hasPermission by remember { mutableStateOf(
        PermissionChecker.checkSelfPermission(ctx, Manifest.permission.CAMERA)
                == PermissionChecker.PERMISSION_GRANTED
    ) }

    val requestPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasPermission = granted }
    )

    // UI cuando falta permiso
    if (!hasPermission) {
        SideEffect { requestPermission.launch(Manifest.permission.CAMERA) }
        PermissionRationale(
            onRequest = { requestPermission.launch(Manifest.permission.CAMERA) },
            onCancel = onCancel
        )
        return
    }

    // ------ Cámara -------
    val lifecycleOwner = LocalLifecycleOwner.current
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }

    Column(Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            factory = { context ->
                val previewView = PreviewView(context)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val selector = CameraSelector.DEFAULT_FRONT_CAMERA
                    val capture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build()

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            selector,
                            preview,
                            capture
                        )
                        imageCapture = capture
                    } catch (e: Exception) {
                        Log.e("CameraScreen", "Error al inicializar cámara", e)
                    }
                }, ContextCompat.getMainExecutor(context))

                previewView
            }
        )

        // Barra inferior con botón
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onCancel) { Text("Cancelar") }
            FilledTonalButton(
                onClick = {
                    val ic = imageCapture ?: return@FilledTonalButton

                    val name = SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US)
                        .format(System.currentTimeMillis())
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, "StoreFit_$name")
                        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/StoreFit")
                        }
                    }
                    val outputOptions = ImageCapture.OutputFileOptions.Builder(
                        ctx.contentResolver,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        contentValues
                    ).build()

                    ic.takePicture(
                        outputOptions,
                        ContextCompat.getMainExecutor(ctx),
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onError(exc: ImageCaptureException) {
                                Log.e("CameraScreen", "Error al tomar la foto", exc)
                            }
                            override fun onImageSaved(result: ImageCapture.OutputFileResults) {
                                val uri = result.savedUri ?: return
                                onPhotoTaken(uri.toString())
                            }
                        }
                    )
                }
            ) { Text("Tomar foto") }
        }
    }
}

@Composable
private fun PermissionRationale(
    onRequest: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Necesitamos la cámara para tomar tu foto de perfil.",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onCancel) { Text("No ahora") }
            Button(onClick = onRequest) { Text("Permitir cámara") }
        }
    }
}