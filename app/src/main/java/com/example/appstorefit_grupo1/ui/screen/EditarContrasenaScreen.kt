package com.example.appstorefit_grupo1.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.appstorefit_grupo1.data.remote.RemoteModule
import com.example.appstorefit_grupo1.data.remote.ServiceUrls
import com.example.appstorefit_grupo1.data.remote.users.UsersApi
import com.example.appstorefit_grupo1.data.repository.UserRepository
import com.example.appstorefit_grupo1.session.SessionManager
import com.example.appstorefit_grupo1.ui.theme.SF_Blue
import com.example.appstorefit_grupo1.ui.theme.SF_Purple
import com.example.appstorefit_grupo1.ui.theme.SF_Teal
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarContrasenaScreen(
    navController: NavHostController,
    onConfirm: (() -> Unit)? = null
) {
    var oldPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }

    var showOld by remember { mutableStateOf(false) }
    var showNew by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }

    val border1 = Brush.horizontalGradient(listOf(SF_Teal, SF_Blue))
    val border2 = Brush.horizontalGradient(listOf(SF_Blue, SF_Purple))
    val buttonBrush = Brush.horizontalGradient(listOf(SF_Teal, SF_Purple))
    val fieldShape = RoundedCornerShape(12.dp)
    val buttonShape = RoundedCornerShape(24.dp)

    val ctx = LocalContext.current
    val usersApi = remember {
        RemoteModule.create(
            baseUrl = ServiceUrls.USERS_BASE_URL,
            service = UsersApi::class.java
        )
    }
    val repo = remember { UserRepository(api = usersApi) }
    val scope = rememberCoroutineScope()
    val snack = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Editar contraseña", color = Color.Black, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        },
        snackbarHost = { SnackbarHost(snack) },   //MOSTRAMOS MENSAJE
        containerColor = Color.White
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {

            //Contraseña antigua
            CampoPasswordDegradado(
                etiqueta = "Contraseña antigua",
                value = oldPass,
                onValueChange = { oldPass = it },
                visible = showOld,
                onToggleVisible = { showOld = !showOld },
                borderBrush = border1,
                shape = fieldShape
            )

            //Contraseña nueva
            CampoPasswordDegradado(
                etiqueta = "Contraseña nueva",
                value = newPass,
                onValueChange = { newPass = it },
                visible = showNew,
                onToggleVisible = { showNew = !showNew },
                borderBrush = border2,
                shape = fieldShape
            )

            //Confirmar contraseña
            CampoPasswordDegradado(
                etiqueta = "Confirmar contraseña",
                value = confirmPass,
                onValueChange = { confirmPass = it },
                visible = showConfirm,
                onToggleVisible = { showConfirm = !showConfirm },
                borderBrush = border2,
                shape = fieldShape
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Botón Confirmar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(brush = buttonBrush, shape = buttonShape)
            ) {
                Button(
                    onClick = {
                        val email = SessionManager.user?.email ?: ""
                        scope.launch {
                            val result = repo.changePassword(
                                email = email,
                                oldPass = oldPass,
                                newPass = newPass,
                                confirmPass = confirmPass
                            )
                            result.onSuccess {
                                onConfirm?.invoke()
                                snack.showSnackbar("Contraseña actualizada")
                                navController.popBackStack()
                            }.onFailure { e ->
                                snack.showSnackbar(e.message ?: "Error al actualizar")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    shape = buttonShape,
                    contentPadding = PaddingValues(vertical = 0.dp) // ya controlamos altura
                ) {
                    Text("Confirmar", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun CampoPasswordDegradado(
    etiqueta: String,
    value: String,
    onValueChange: (String) -> Unit,
    visible: Boolean,
    onToggleVisible: () -> Unit,
    borderBrush: Brush,
    shape: RoundedCornerShape = RoundedCornerShape(12.dp)
) {
    Column(Modifier.fillMaxWidth()) {
        Text(
            text = etiqueta,
            color = Color.Black,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp)
                .border(width = 2.dp, brush = borderBrush, shape = shape)
                .background(Color.White, shape)
                .padding(horizontal = 12.dp, vertical = 2.dp)
        ) {
            TextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = onToggleVisible) {
                        // Si es visible, mostramos el icono de "ocultar"
                        Icon(
                            imageVector = if (visible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (visible) "Ocultar" else "Mostrar",
                            tint = Color.Black
                        )
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White,
                    cursorColor = Color.Black,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                )
            )
        }
    }
}
