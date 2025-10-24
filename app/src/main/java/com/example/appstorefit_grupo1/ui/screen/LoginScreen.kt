package com.example.appstorefit_grupo1.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appstorefit_grupo1.ViewModel.AuthViewModel
import com.example.appstorefit_grupo1.ViewModel.AuthViewModelFactory
import com.example.appstorefit_grupo1.session.SessionManager
import com.example.appstorefit_grupo1.ui.components.SuccessLoginDialog

@Composable
fun LoginScreenVm(
    widthClass: WindowWidthSizeClass,
    onLoginOkNavigateHome: () -> Unit,      // Productos
    onLoginOkNavigateAdmin: () -> Unit,     // Panel
    onGoRegister: () -> Unit
) {
    val context = LocalContext.current
    val vm: AuthViewModel = viewModel(factory = AuthViewModelFactory(context))
    val state by vm.login.collectAsStateWithLifecycle()

    // Mostrar diálogo de éxito y navegar al cerrarse
    if (state.success) {
        SuccessLoginDialog(
            message = "¡Sesión iniciada exitosamente!",
            onDismiss = {
                vm.clearLoginResult()
                val isAdmin = (SessionManager.roleId == 2L)
                if (isAdmin) onLoginOkNavigateAdmin() else onLoginOkNavigateHome()
            }
        )
    }

    LoginScreen(
        widthClass = widthClass,
        email = state.email,
        pass = state.pass,
        emailError = state.emailError,
        passError = state.passError,
        canSubmit = state.canSubmit,
        isSubmitting = state.isSubmitting,
        errorMsg = state.errorMsg,
        onEmailChange = vm::onLoginEmailChange,
        onPassChange = vm::onLoginPassChange,
        onSubmit = vm::submitLogin,
        onGoRegister = onGoRegister
    )
}

@Composable
private fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    val colorScheme = MaterialTheme.colorScheme
    val gradient = Brush.horizontalGradient(
        colors = listOf(colorScheme.tertiary, colorScheme.primary, colorScheme.secondary)
    )
    Surface(
        enabled = enabled,
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = Color.Transparent,
        contentColor = colorScheme.onPrimary,
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
                        color = colorScheme.onPrimary
                    )
                    Text(text)
                }
            } else {
                Text(text)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoginScreen(
    widthClass: WindowWidthSizeClass,
    email: String,
    pass: String,
    emailError: String?,
    passError: String?,
    canSubmit: Boolean,
    isSubmitting: Boolean,
    errorMsg: String?,
    onEmailChange: (String) -> Unit,
    onPassChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onGoRegister: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val maxW = when (widthClass) {
        WindowWidthSizeClass.Expanded -> 600.dp
        WindowWidthSizeClass.Medium   -> 520.dp
        else                          -> 360.dp
    }

    var showPass by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.surfaceVariant)
            .padding(16.dp)
            .imePadding(),
        contentAlignment = Alignment.Center
    ) {
        ElevatedCard(
            colors = CardDefaults.elevatedCardColors(containerColor = colorScheme.surface),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = maxW)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Iniciar sesión",
                    style = MaterialTheme.typography.headlineSmall,
                    color = colorScheme.onSurface,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Bienvenid@ a StoreFit",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(20.dp))

                // -------- Email ----------
                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    label = { Text("Email") },
                    singleLine = true,
                    isError = emailError != null,
                    supportingText = {
                        AnimatedVisibility(
                            visible = emailError != null,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            if (emailError != null) {
                                Text(emailError, color = colorScheme.error, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorScheme.primary,
                        unfocusedBorderColor = colorScheme.outline,
                        focusedLabelColor = colorScheme.primary,
                        cursorColor = colorScheme.primary,
                        focusedContainerColor = colorScheme.surface,
                        unfocusedContainerColor = colorScheme.surface
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                // -------- Contraseña ----------
                OutlinedTextField(
                    value = pass,
                    onValueChange = onPassChange,
                    label = { Text("Contraseña") },
                    singleLine = true,
                    visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showPass = !showPass }) {
                            Icon(
                                imageVector = if (showPass) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (showPass) "Ocultar contraseña" else "Mostrar contraseña",
                            )
                        }
                    },
                    isError = passError != null,
                    supportingText = {
                        AnimatedVisibility(
                            visible = passError != null,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            if (passError != null) {
                                Text(passError, color = colorScheme.error, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { if (canSubmit && !isSubmitting) onSubmit() }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(20.dp))

                GradientButton(
                    text = if (isSubmitting) "Validando…" else "Entrar",
                    enabled = canSubmit && !isSubmitting,
                    loading = isSubmitting,
                    onClick = onSubmit,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMsg != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        errorMsg,
                        color = colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onGoRegister,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Crear cuenta") }
            }
        }
    }
}