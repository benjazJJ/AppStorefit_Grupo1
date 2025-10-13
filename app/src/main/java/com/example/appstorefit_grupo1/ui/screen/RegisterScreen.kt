package com.example.appstorefit_grupo1.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appstorefit_grupo1.ViewModel.AuthViewModel
import com.example.appstorefit_grupo1.domain.validation.*
import com.example.appstorefit_grupo1.ui.theme.AppStoreFit_Grupo1Theme

/* =========================
 * 1) Wrapper con ViewModel
 * ========================= */
@Composable
fun RegisterScreenVm(
    widthClass: WindowWidthSizeClass,
    onRegisteredNavigateLogin: () -> Unit,
    onGoLogin: () -> Unit
) {
    val vm: AuthViewModel = viewModel()
    val state by vm.register.collectAsStateWithLifecycle()

    if (state.success) {
        vm.clearRegisterResult()
        onRegisteredNavigateLogin()
    }

    RegisterForm(
        widthClass = widthClass,
        name = state.name,
        email = state.email,
        phone = state.phone,
        pass = state.pass,
        confirm = state.confirm,
        nameError = state.nameError,
        emailError = state.emailError,
        phoneError = state.phoneError,
        passError = state.passError,
        confirmError = state.confirmError,
        canSubmit = state.canSubmit,
        isSubmitting = state.isSubmitting,
        errorMsg = state.errorMsg,
        onNameChange = vm::onNameChange,
        onEmailChange = vm::onRegisterEmailChange,
        onPhoneChange = vm::onPhoneChange,
        onPassChange = vm::onRegisterPassChange,
        onConfirmChange = vm::onConfirmChange,
        onSubmit = vm::submitRegister,
        onGoLogin = onGoLogin
    )
}

/* =========================
 * 2) UI responsive (sin VM)
 * ========================= */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegisterForm(
    widthClass: WindowWidthSizeClass,
    name: String,
    email: String,
    phone: String,
    pass: String,
    confirm: String,
    nameError: String?,
    emailError: String?,
    phoneError: String?,
    passError: String?,
    confirmError: String?,
    canSubmit: Boolean,
    isSubmitting: Boolean,
    errorMsg: String?,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
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

    Scaffold(topBar = { TopAppBar(title = { Text("Registro") }) }) { inner ->
        Box(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .background(cs.surfaceVariant)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = maxW)
            ) {
                // Nombre
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("Nombre") },
                    singleLine = true,
                    isError = nameError != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    modifier = Modifier.fillMaxWidth()
                )
                if (nameError != null) {
                    Text(nameError, color = cs.error, style = MaterialTheme.typography.labelSmall)
                }

                Spacer(Modifier.height(8.dp))

                // Email
                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    label = { Text("Email") },
                    singleLine = true,
                    isError = emailError != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )
                if (emailError != null) {
                    Text(emailError, color = cs.error, style = MaterialTheme.typography.labelSmall)
                }

                Spacer(Modifier.height(8.dp))

                // Teléfono
                OutlinedTextField(
                    value = phone,
                    onValueChange = onPhoneChange,
                    label = { Text("Teléfono") },
                    singleLine = true,
                    isError = phoneError != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                if (phoneError != null) {
                    Text(phoneError, color = cs.error, style = MaterialTheme.typography.labelSmall)
                }

                Spacer(Modifier.height(8.dp))

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
                                contentDescription = if (showPass) "Ocultar contraseña" else "Mostrar contraseña"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                if (passError != null) {
                    Text(passError, color = cs.error, style = MaterialTheme.typography.labelSmall)
                }

                Spacer(Modifier.height(8.dp))

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
                                contentDescription = if (showConfirm) "Ocultar confirmación" else "Mostrar confirmación"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                if (confirmError != null) {
                    Text(confirmError, color = cs.error, style = MaterialTheme.typography.labelSmall)
                }

                Spacer(Modifier.height(16.dp))

                // Botón Registrar
                Button(
                    onClick = onSubmit,
                    enabled = canSubmit && !isSubmitting,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Creando cuenta...")
                    } else {
                        Text("Registrar")
                    }
                }

                if (errorMsg != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(errorMsg, color = cs.error, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(Modifier.height(12.dp))

                // Ir a Login
                OutlinedButton(onClick = onGoLogin, modifier = Modifier.fillMaxWidth()) {
                    Text("Ir a Login")
                }
            }
        }
    }
}

/* ================
 * 3) Previews
 * ================ */
@Preview(showBackground = true, name = "Registro – Compacta", widthDp = 360, heightDp = 800, showSystemUi = true)
@Composable
private fun PreviewRegister_Compact() {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }

    val nameErr    = validateNombre(name)
    val emailErr   = validateEmail(email)
    val phoneErr   = validateTelefono(phone)
    val passErr    = validateContraseña(pass)
    val confirmErr = validateConfir(pass, confirm)

    val filled   = name.isNotBlank() && email.isNotBlank() && phone.isNotBlank() && pass.isNotBlank() && confirm.isNotBlank()
    val noErrors = listOf(nameErr, emailErr, phoneErr, passErr, confirmErr).all { it == null }
    val can      = filled && noErrors

    AppStoreFit_Grupo1Theme {
        RegisterForm(
            widthClass = WindowWidthSizeClass.Compact,
            name = name, email = email, phone = phone, pass = pass, confirm = confirm,
            nameError = nameErr, emailError = emailErr, phoneError = phoneErr, passError = passErr, confirmError = confirmErr,
            canSubmit = can, isSubmitting = false, errorMsg = null,
            onNameChange = { name = it.filter { ch -> ch.isLetter() || ch.isWhitespace() } },
            onEmailChange = { email = it },
            onPhoneChange = { phone = it.filter { ch -> ch.isDigit() } },
            onPassChange = { pass = it },
            onConfirmChange = { confirm = it },
            onSubmit = { }, onGoLogin = { }
        )
    }
}

@Preview(showBackground = true, name = "Registro – Expandida", widthDp = 1000, heightDp = 800, showSystemUi = true)
@Composable
private fun PreviewRegister_Expanded() {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }

    val nameErr    = validateNombre(name)
    val emailErr   = validateEmail(email)
    val phoneErr   = validateTelefono(phone)
    val passErr    = validateContraseña(pass)
    val confirmErr = validateConfir(pass, confirm)

    val filled   = name.isNotBlank() && email.isNotBlank() && phone.isNotBlank() && pass.isNotBlank() && confirm.isNotBlank()
    val noErrors = listOf(nameErr, emailErr, phoneErr, passErr, confirmErr).all { it == null }
    val can      = filled && noErrors

    AppStoreFit_Grupo1Theme {
        RegisterForm(
            widthClass = WindowWidthSizeClass.Expanded,
            name = name, email = email, phone = phone, pass = pass, confirm = confirm,
            nameError = nameErr, emailError = emailErr, phoneError = phoneErr, passError = passErr, confirmError = confirmErr,
            canSubmit = can, isSubmitting = false, errorMsg = null,
            onNameChange = { name = it.filter { ch -> ch.isLetter() || ch.isWhitespace() } },
            onEmailChange = { email = it },
            onPhoneChange = { phone = it.filter { ch -> ch.isDigit() } },
            onPassChange = { pass = it },
            onConfirmChange = { confirm = it },
            onSubmit = { }, onGoLogin = { }
        )
    }
}

