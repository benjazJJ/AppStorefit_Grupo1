package com.example.appstorefit_grupo1.ui.screen

import androidx.compose.foundation.background                 // Fondo
import androidx.compose.foundation.layout.*                   // Box/Column/Row/Spacer
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons                  // Íconos Material
import androidx.compose.material.icons.filled.Visibility      // Ícono mostrar contraseña
import androidx.compose.material.icons.filled.VisibilityOff   // Ícono ocultar contraseña
import androidx.compose.material3.*                           // Material 3
import androidx.compose.runtime.*                             // remember y Composable
import androidx.compose.ui.Alignment                          // Alineaciones
import androidx.compose.ui.Modifier                           // Modificador
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*                       // KeyboardOptions/Types/Transformations
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp                            // DPs
import androidx.lifecycle.compose.collectAsStateWithLifecycle // Observa StateFlow con lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel         // Obtiene ViewModel // Nuestro ViewModel
import com.example.appstorefit_grupo1.ViewModel.AuthViewModel
import com.example.appstorefit_grupo1.domain.validation.validateEmail
import com.example.appstorefit_grupo1.ui.theme.AppStoreFit_Grupo1Theme


//1 Lo primero que creamos en el archivo
@Composable                                                  // Pantalla Login conectada al VM
fun LoginScreenVm(
    onLoginOkNavigateHome: () -> Unit,                       // Navega a Home cuando el login es exitoso
    onGoRegister: () -> Unit                                 // Navega a Registro
) {
    val vm: AuthViewModel = viewModel()                      // Crea/obtiene VM
    val state by vm.login.collectAsStateWithLifecycle()      // Observa el StateFlow en tiempo real

    if (state.success) {                                     // Si login fue exitoso…
        vm.clearLoginResult()                                // Limpia banderas
        onLoginOkNavigateHome()                              // Navega a Home
    }

    LoginScreen(                                             // Delegamos a UI presentacional
        email = state.email,                                 // Valor de email
        pass = state.pass,                                   // Valor de password
        emailError = state.emailError,                       // Error de email
        passError = state.passError,                         // (Opcional) error de pass en login
        canSubmit = state.canSubmit,                         // Habilitar botón
        isSubmitting = state.isSubmitting,                   // Loading
        errorMsg = state.errorMsg,                           // Error global
        onEmailChange = vm::onLoginEmailChange,              // Handler email
        onPassChange = vm::onLoginPassChange,                // Handler pass
        onSubmit = vm::submitLogin,                          // Acción enviar
        onGoRegister = onGoRegister                          // Ir a Registro
    )
}


//2 modificamos la funcion principal haciendo private y agregando variable y elementos dle fiormulario
@Composable // Pantalla Login (solo navegación, sin formularios)
private fun LoginScreen(
    //3 Modificamos estos parametros
    email: String,                                           // Campo email
    pass: String,                                            // Campo contraseña
    emailError: String?,                                     // Error de email
    passError: String?,                                      // Error de password (opcional)
    canSubmit: Boolean,                                      // Habilitar botón
    isSubmitting: Boolean,                                   // Flag loading
    errorMsg: String?,                                       // Error global (credenciales)
    onEmailChange: (String) -> Unit,                         // Handler cambio email
    onPassChange: (String) -> Unit,                          // Handler cambio password
    onSubmit: () -> Unit,                                    // Acción enviar
    onGoRegister: () -> Unit                                 // Acción ir a registro
) {
    val bg = MaterialTheme.colorScheme.background // Fondo distinto para contraste
    //4 Agregamos la siguiente linea
    var showPass by remember { mutableStateOf(false) }        // Estado local para mostrar/ocultar contraseña


    Box(
        modifier = Modifier
            .fillMaxSize() // Ocupa todo
            .background(bg) // Fondo
            .padding(16.dp), // Margen
        contentAlignment = Alignment.Center // Centro
    ) {

        Column(
            //5 Anexamos el modificador
            modifier = Modifier.fillMaxWidth(),              // Ancho completo
            horizontalAlignment = Alignment.CenterHorizontally // Centrado horizontal
        ) {
            Text(
                text = "Iniciar Sesión",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold// Título
            )
            Spacer(Modifier.height(12.dp)) // Separación

            Text(
                text = "Bienvendi@ a StoreFit",
                textAlign = TextAlign.Center // Alineación centrada
            )
            Spacer(Modifier.height(20.dp)) // Separación

            //5 Borramos los elementos anteriores y comenzamos a agregar los elementos dle formulario
// ---------- EMAIL ----------
            OutlinedTextField(
                value = email,                               // Valor actual
                onValueChange = onEmailChange,               // Notifica VM (valida email)
                label = { Text("Email") },                   // Etiqueta
                singleLine = true,                           // Una línea
                isError = emailError != null,                // Marca error si corresponde
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email        // Teclado de email
                ),
                modifier = Modifier.fillMaxWidth()           // Ancho completo
            )
            if (emailError != null) {                        // Muestra mensaje si hay error
                Text(emailError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
            }

            Spacer(Modifier.height(8.dp))                    // Espacio

            // ---------- PASSWORD (oculta por defecto) ----------
            var passTouched by remember { mutableStateOf(false) }

            val showPassError = passTouched && (passError != null || pass.isBlank())
            val passErrorMsg = passError ?: "Campo obligatorio"

            OutlinedTextField(
                value = pass,
                onValueChange = {
                    passTouched = true          // ya interactuó
                    onPassChange(it)
                },
                label = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showPass = !showPass }) {
                        Icon(
                            imageVector = if (showPass) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (showPass) "Ocultar contraseña" else "Mostrar contraseña"
                        )
                    }
                },
                isError = showPassError,
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged() { if (!it.isFocused) passTouched = true } //Cuando pierde foco
            )

            if (showPassError) {
                Text(
                    passErrorMsg,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Spacer(Modifier.height(16.dp))                   // Espacio

            // ---------- BOTÓN ENTRAR ----------
            Button(
                onClick = onSubmit,                          // Envía login
                enabled = canSubmit && !isSubmitting,        // Solo si válido y no cargando
                modifier = Modifier.fillMaxWidth()           // Ancho completo
            ) {
                if (isSubmitting) {                          // UI de carga
                    CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Validando...")
                } else {
                    Text("Entrar")
                }
            }

            if (errorMsg != null) {                          // Error global
                Spacer(Modifier.height(8.dp))
                Text(errorMsg, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(12.dp))

            // ---------- BOTÓN IR A REGISTRO ----------
            OutlinedButton(onClick = onGoRegister, modifier = Modifier.fillMaxWidth()) {
                Text("Crear cuenta")
            }
            //fin modificacion de formulario
        }
    }
}

//ESTO ES PARA VISTA PREVIA DE LA PANTALLA DE LOGIN

@Preview(showBackground = true, name = "Login")
@Composable
fun PreviewLogin_InteractiveValidated() {
    var email by remember { mutableStateOf("") }
    var pass  by remember { mutableStateOf("") }

    //Validamos para que el botón funcione o no
    val emailErr: String? = validateEmail(email)
    val passErr: String? = null //solo que no esté vacío
    val can = emailErr == null && email.isNotBlank() && pass.isNotBlank()

    AppStoreFit_Grupo1Theme {
        LoginScreen(
            email = email,
            pass = pass,
            emailError = emailErr,
            passError = passErr,
            canSubmit = can,
            isSubmitting = false,
            errorMsg = null,
            onEmailChange = { email = it },
            onPassChange = { pass = it },
            onSubmit = { /* no-op en preview */ },
            onGoRegister = { /* no-op en preview */ }
        )
    }
}

