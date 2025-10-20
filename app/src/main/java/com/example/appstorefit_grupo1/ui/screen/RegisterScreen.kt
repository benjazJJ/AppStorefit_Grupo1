package com.example.appstorefit_grupo1.ui.screen

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.appstorefit_grupo1.ViewModel.AuthViewModel
import com.example.appstorefit_grupo1.ViewModel.AuthViewModelFactory

/* ---------- VM Wrapper ---------- */
@Composable
fun RegisterScreenVm(
    navController: NavController, // Cambiado para recibir NavController
    widthClass: WindowWidthSizeClass,
    onRegisteredNavigateLogin: () -> Unit,
    onGoLogin: () -> Unit
) {
    val context = LocalContext.current
    val vm: AuthViewModel = viewModel(factory = AuthViewModelFactory(context))
    val state by vm.register.collectAsStateWithLifecycle()

    // Listener para el resultado de la cámara
    val photoUriResult = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getLiveData<String>("photo_uri")?.observeAsState()

    LaunchedEffect(photoUriResult) {
        photoUriResult?.value?.let {
            vm.onPhotoUriChange(it)
            // Limpiamos para no volver a recibirlo en una recomposición
            navController.currentBackStackEntry?.savedStateHandle?.remove<String>("photo_uri")
        }
    }

    if (state.success) {
        vm.clearRegisterResult()
        onRegisteredNavigateLogin()
    }

    RegisterScreen(
        widthClass = widthClass,
        rut = state.rut,
        name = state.name,
        email = state.email,
        phone = state.phone,
        address = state.address,
        pass = state.pass,
        confirm = state.confirm,
        photoUri = state.photoUri,
        rutError = state.rutError,
        nameError = state.nameError,
        emailError = state.emailError,
        phoneError = state.phoneError,
        addressError = state.addressError,
        passError = state.passError,
        confirmError = state.confirmError,
        canSubmit = state.canSubmit,
        isSubmitting = state.isSubmitting,
        errorMsg = state.errorMsg,
        onRutChange = vm::onRutChange,
        onNameChange = vm::onNameChange,
        onEmailChange = vm::onRegisterEmailChange,
        onPhoneChange = vm::onPhoneChange,
        onAddressChange = vm::onAddressChange,
        onPassChange = vm::onRegisterPassChange,
        onConfirmChange = vm::onConfirmChange,
        onSubmit = vm::submitRegister,
        onGoLogin = onGoLogin,
        onGoToCamera = { navController.navigate("camera") } // Navegamos a la cámara
    )
}

/* ---------- Mismo look & feel que Login ---------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegisterScreen(
    widthClass: WindowWidthSizeClass,
    rut: String,
    name: String,
    email: String,
    phone: String,
    address: String,
    pass: String,
    confirm: String,
    photoUri: String?,
    rutError: String?,
    nameError: String?,
    emailError: String?,
    phoneError: String?,
    addressError: String?,
    passError: String?,
    confirmError: String?,
    canSubmit: Boolean,
    isSubmitting: Boolean,
    errorMsg: String?,
    onRutChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onPassChange: (String) -> Unit,
    onConfirmChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onGoLogin: () -> Unit,
    onGoToCamera: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val maxW = when (widthClass) {
        WindowWidthSizeClass.Expanded -> 600.dp
        WindowWidthSizeClass.Medium   -> 520.dp
        else                          -> 360.dp
    }

    var showPass by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(cs.surfaceVariant)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        ElevatedCard(
            colors = CardDefaults.elevatedCardColors(containerColor = cs.surface),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = maxW)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Registro",
                    style = MaterialTheme.typography.headlineSmall,
                    color = cs.onSurface
                )

                // --- Vista previa de la foto ---
                if (photoUri != null) {
                    Box(contentAlignment = Alignment.Center) {
                        AsyncImage(
                            model = Uri.parse(photoUri),
                            contentDescription = "Foto de perfil",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .border(2.dp, cs.primary, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                // --- Botón para añadir/editar foto ---
                OutlinedButton(
                    onClick = onGoToCamera,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val icon = if (photoUri == null) Icons.Default.AddAPhoto else Icons.Default.Edit
                    val text = if (photoUri == null) "Añadir Foto (Opcional)" else "Cambiar Foto"
                    Icon(icon, contentDescription = text, modifier = Modifier.padding(end = 8.dp))
                    Text(text)
                }

                /* RUT */
                OutlinedTextField(
                    value = rut,
                    onValueChange = onRutChange,
                    label = { Text("RUT") },
                    singleLine = true,
                    isError = rutError != null,
                    modifier = Modifier.fillMaxWidth()
                )
                AnimatedError(rutError)

                /* Nombre */
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("Nombre") },
                    singleLine = true,
                    isError = nameError != null,
                    modifier = Modifier.fillMaxWidth()
                )
                AnimatedError(nameError)

                /* Email */
                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    label = { Text("Email") },
                    singleLine = true,
                    isError = emailError != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )
                AnimatedError(emailError)

                /* Teléfono */
                OutlinedTextField(
                    value = phone,
                    onValueChange = onPhoneChange,
                    label = { Text("Teléfono") },
                    singleLine = true,
                    isError = phoneError != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                AnimatedError(phoneError)

                OutlinedTextField(
                    value = address,
                    onValueChange = onAddressChange,
                    label = { Text("Dirección") },
                    singleLine = false,
                    minLines = 1,
                    isError = addressError != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    modifier = Modifier.fillMaxWidth()
                )
                AnimatedError(addressError)

                /* Contraseña */
                OutlinedTextField(
                    value = pass,
                    onValueChange = onPassChange,
                    label = { Text("Contraseña") },
                    singleLine = true,
                    isError = passError != null,
                    visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showPass = !showPass }) {
                            Icon(
                                imageVector = if (showPass) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = null
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                AnimatedError(passError)

                /* Confirmación */
                OutlinedTextField(
                    value = confirm,
                    onValueChange = onConfirmChange,
                    label = { Text("Confirmar contraseña") },
                    singleLine = true,
                    isError = confirmError != null,
                    visualTransformation = if (showConfirm) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showConfirm = !showConfirm }) {
                            Icon(
                                imageVector = if (showConfirm) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = null
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                AnimatedError(confirmError)

                Spacer(Modifier.height(6.dp))

                GradientButton(
                    text = if (isSubmitting) "Creando cuenta…" else "Registrar",
                    enabled = canSubmit && !isSubmitting,
                    loading = isSubmitting,
                    onClick = onSubmit,
                    modifier = Modifier.fillMaxWidth()
                )

                AnimatedVisibility(
                    visible = errorMsg != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    if (errorMsg != null) {
                        Text(
                            errorMsg,
                            color = cs.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                OutlinedButton(
                    onClick = onGoLogin,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Ir a Login") }
            }
        }
    }
}

/* --- Helpers visuales para mantener el mismo diseño que Login --- */
@Composable
private fun AnimatedError(msg: String?) {
    val cs = MaterialTheme.colorScheme
    AnimatedVisibility(
        visible = msg != null,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        if (msg != null) {
            Text(msg, color = cs.error, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    val cs = MaterialTheme.colorScheme
    val gradient = Brush.horizontalGradient(
        colors = listOf(cs.tertiary, cs.primary, cs.secondary)
    )
    Surface(
        enabled = enabled,
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = Color.Transparent,
        contentColor = cs.onPrimary,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .background(gradient, RoundedCornerShape(10.dp))
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            if (loading) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier
                            .size(18.dp)
                            .padding(end = 8.dp),
                        color = cs.onPrimary
                    )
                    Text(text)
                }
            } else {
                Text(text)
            }
        }
    }
}
