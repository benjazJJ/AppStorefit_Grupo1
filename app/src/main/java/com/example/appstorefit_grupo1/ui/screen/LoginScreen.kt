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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appstorefit_grupo1.ViewModel.AuthViewModel
import com.example.appstorefit_grupo1.domain.validation.validateEmail
import com.example.appstorefit_grupo1.ui.theme.AppStoreFit_Grupo1Theme

/* =========================
 * 1) Pantalla Login (VM)
 * ========================= */
@Composable
fun LoginScreenVm(
    vm: AuthViewModel,
    widthClass: WindowWidthSizeClass,
    onLoginOkNavigateHome: () -> Unit,
    onGoRegister: () -> Unit
) {
    //val vm: AuthViewModel = viewModel()
    val state by vm.login.collectAsStateWithLifecycle()

    if (state.success) {
        vm.clearLoginResult()
        onLoginOkNavigateHome()
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

/* =======================================================
 * 2) Botón con degradado (igual a tu diseño)
 * ======================================================= */
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

/* ============================================
 * 3) Diseño Login responsive con widthClass
 * ============================================ */
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
    onGoRegister: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val maxW = when (widthClass) {
        WindowWidthSizeClass.Expanded -> 600.dp
        WindowWidthSizeClass.Medium   -> 520.dp
        else                          -> 360.dp
    }

    var showPass by rememberSaveable { mutableStateOf(false) }
    var passTouched by rememberSaveable { mutableStateOf(false) }

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
                .widthIn(max = maxW) // 👈 responsive por size class
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Iniciar sesión",
                    style = MaterialTheme.typography.headlineSmall,
                    color = cs.onSurface,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Bienvenid@ a StoreFit",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = cs.onSurfaceVariant
                )
                Spacer(Modifier.height(20.dp))

                // -------- EMAIL --------
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
                                Text(emailError, color = cs.error, style = MaterialTheme.typography.labelSmall)
                            }
                        }

                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = cs.primary,
                        unfocusedBorderColor = cs.outline,
                        focusedLabelColor = cs.primary,
                        cursorColor = cs.primary,
                        focusedContainerColor = cs.surface,
                        unfocusedContainerColor = cs.surface
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                // -------- PASSWORD --------
                val showPassError = passTouched && (passError != null || pass.isBlank())
                val passErrorMsg = passError ?: "Campo obligatorio"

                OutlinedTextField(
                    value = pass,
                    onValueChange = {
                        if (!passTouched) passTouched = true
                        onPassChange(it)
                    },
                    label = { Text("Contraseña") },
                    singleLine = true,
                    visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showPass = !showPass }) {
                            Icon(
                                imageVector = if (showPass) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (showPass) "Ocultar contraseña" else "Mostrar contraseña",
                                tint = cs.onSurfaceVariant
                            )
                        }
                    },
                    isError = showPassError,
                    supportingText = {
                        if (showPassError) {
                            Text(passErrorMsg, color = cs.error, style = MaterialTheme.typography.labelSmall)
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { if (canSubmit && !isSubmitting) onSubmit() }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = cs.primary,
                        unfocusedBorderColor = cs.outline,
                        focusedLabelColor = cs.primary,
                        cursorColor = cs.primary,
                        focusedContainerColor = cs.surface,
                        unfocusedContainerColor = cs.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { if (!it.isFocused) passTouched = true }
                )

                Spacer(Modifier.height(20.dp))

                // -------- BOTÓN ENTRAR --------
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
                        color = cs.error,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(Modifier.height(12.dp))

                // -------- BOTÓN IR A REGISTRO --------
                OutlinedButton(
                    onClick = onGoRegister,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = cs.primary),
                    border = ButtonDefaults.outlinedButtonBorder
                ) {
                    Text("Crear cuenta")
                }
            }
        }
    }
}

/* ================
 * 4) Previews
 * ================ */
@Preview(showBackground = true, name = "Login – Compacta", widthDp = 360, heightDp = 800, showSystemUi = true)
@Composable
fun PreviewLogin_Compact() {
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    val emailErr: String? = validateEmail(email)
    val passErr: String? = null
    val can = emailErr == null && email.isNotBlank() && pass.isNotBlank()

    AppStoreFit_Grupo1Theme {
        LoginScreen(
            widthClass = WindowWidthSizeClass.Compact,
            email = email,
            pass = pass,
            emailError = emailErr,
            passError = passErr,
            canSubmit = can,
            isSubmitting = false,
            errorMsg = null,
            onEmailChange = { email = it },
            onPassChange = { pass = it },
            onSubmit = { },
            onGoRegister = { }
        )
    }
}

@Preview(showBackground = true, name = "Login – Expandida", widthDp = 1000, heightDp = 800, showSystemUi = true)
@Composable
fun PreviewLogin_Expanded() {
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    val emailErr: String? = validateEmail(email)
    val passErr: String? = null
    val can = emailErr == null && email.isNotBlank() && pass.isNotBlank()

    AppStoreFit_Grupo1Theme {
        LoginScreen(
            widthClass = WindowWidthSizeClass.Expanded,
            email = email,
            pass = pass,
            emailError = emailErr,
            passError = passErr,
            canSubmit = can,
            isSubmitting = false,
            errorMsg = null,
            onEmailChange = { email = it },
            onPassChange = { pass = it },
            onSubmit = { },
            onGoRegister = { }
        )
    }
}
