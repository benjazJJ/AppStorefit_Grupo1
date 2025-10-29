package com.example.appstorefit_grupo1.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.appstorefit_grupo1.ViewModel.AuthViewModel
import com.example.appstorefit_grupo1.ViewModel.AuthViewModelFactory
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState

// VM Wrapper
@Composable
fun RegisterScreenVm(
    widthClass: WindowWidthSizeClass,
    onRegisteredNavigateLogin: () -> Unit,
    onGoLogin: () -> Unit
) {
    val context = LocalContext.current
    val vm: AuthViewModel = viewModel(factory = AuthViewModelFactory(context))
    val state by vm.register.collectAsStateWithLifecycle()

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
        birthDate = state.birthDate,
        pass = state.pass,
        confirm = state.confirm,
        rutError = state.rutError,
        nameError = state.nameError,
        emailError = state.emailError,
        phoneError = state.phoneError,
        addressError = state.addressError,
        birthDateError = state.birthDateError,
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
        onBirthDateChange = vm::onBirthDateChange,
        onPassChange = vm::onRegisterPassChange,
        onConfirmChange = vm::onConfirmChange,
        onSubmit = vm::submitRegister,
        onGoLogin = onGoLogin
    )
}

// Mismo look & feel que Login
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegisterScreen(
    widthClass: WindowWidthSizeClass,
    rut: String,
    name: String,
    email: String,
    phone: String,
    address: String,
    birthDate: String,
    pass: String,
    confirm: String,
    rutError: String?,
    nameError: String?,
    emailError: String?,
    phoneError: String?,
    addressError: String?,
    birthDateError: String?,
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
    onBirthDateChange: (String) -> Unit,
    onPassChange: (String) -> Unit,
    onConfirmChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onGoLogin: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val maxW = when (widthClass) {
        WindowWidthSizeClass.Expanded -> 600.dp
        WindowWidthSizeClass.Medium   -> 520.dp
        else                          -> 360.dp
    }

    var showPass by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }

    // estado de DatePicker
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

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
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Registro",
                    style = MaterialTheme.typography.headlineSmall,
                    color = cs.onSurface
                )

                // RUT
                val isRutTaken = errorMsg == "RUT ya registrado"
                OutlinedTextField(
                    value = rut,
                    onValueChange = onRutChange,
                    label = { Text("RUT") },
                    singleLine = true,
                    isError = (rutError != null) || isRutTaken,
                    modifier = Modifier.fillMaxWidth()
                )
                AnimatedError(rutError ?: (if (isRutTaken) "RUT ya registrado" else null))

                // Nombre
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("Nombre") },
                    singleLine = true,
                    isError = nameError != null,
                    modifier = Modifier.fillMaxWidth()
                )
                AnimatedError(nameError)

                // Email
                val isEmailTaken = errorMsg == "Correo ya registrado"
                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    label = { Text("Email") },
                    singleLine = true,
                    isError = (emailError != null) || isEmailTaken,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )
                AnimatedError(emailError ?: (if (isEmailTaken) "Correo ya registrado" else null))

                // Teléfono
                val isPhoneTaken = errorMsg == "Este teléfono ya pertenece a otro usuario."
                OutlinedTextField(
                    value = phone,
                    onValueChange = onPhoneChange,
                    label = { Text("Teléfono") },
                    singleLine = true,
                    isError = (phoneError != null) || isPhoneTaken,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                AnimatedError(phoneError ?: (if (isPhoneTaken) "Este teléfono ya pertenece a otro usuario." else null))

                // Dirección
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

                // Fecha de nacimiento
                Box(modifier = Modifier.fillMaxWidth()) {

                    OutlinedTextField(
                        value = birthDate,
                        onValueChange = { /* se setea sólo con el DatePicker */ },
                        label = { Text("Fecha de nacimiento (yyyy-MM-dd)") },
                        singleLine = true,
                        isError = birthDateError != null,
                        readOnly = false,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showDatePicker = true }
                    )
                }

                AnimatedError(birthDateError)

                if (showDatePicker) {
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                val millis = datePickerState.selectedDateMillis
                                if (millis != null) {
                                    val formatted = java.text.SimpleDateFormat(
                                        "yyyy-MM-dd",
                                        java.util.Locale.US
                                    ).format(java.util.Date(millis))
                                    onBirthDateChange(formatted)
                                }
                                showDatePicker = false
                            }) { Text("Aceptar") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
                        }
                    ) {
                        DatePicker(state = datePickerState)
                    }
                }


                // Contraseña
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

                // Confirmación
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
