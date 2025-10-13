package com.example.appstorefit_grupo1.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.appstorefit_grupo1.ui.theme.SF_Blue
import com.example.appstorefit_grupo1.ui.theme.SF_Purple
import com.example.appstorefit_grupo1.ui.theme.SF_Teal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarContrasenaScreen(
    navController: NavHostController,
    onConfirm: (() -> Unit)? = null
) {
    var oldPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var showOld by remember { mutableStateOf(false) }
    var showNew by remember { mutableStateOf(false) }

    val border1 = Brush.horizontalGradient(listOf(SF_Teal, SF_Blue))
    val border2 = Brush.horizontalGradient(listOf(SF_Blue, SF_Purple))
    val buttonBrush = Brush.horizontalGradient(listOf(SF_Teal, SF_Purple))

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
        containerColor = Color.White
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {

            // ---- Campo: Contraseña antigua ----
            CampoPasswordDegradado(
                etiqueta = "Contraseña antigua",
                value = oldPass,
                onValueChange = { oldPass = it },
                visible = showOld,
                onToggleVisible = { showOld = !showOld },
                borderBrush = border1
            )

            // ---- Campo: Contraseña nueva ----
            CampoPasswordDegradado(
                etiqueta = "Contraseña nueva",
                value = newPass,
                onValueChange = { newPass = it },
                visible = showNew,
                onToggleVisible = { showNew = !showNew },
                borderBrush = border2
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ---- Botón Confirmar (relleno degradado, borde cuadrado) ----
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(brush = buttonBrush, shape = RectangleShape)
            ) {
                Button(
                    onClick = {
                        // aquí luego validas y llamas API.
                        onConfirm?.invoke()
                        navController.popBackStack()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    shape = RectangleShape,
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Text("Confirmar", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

/** Campo de password con borde degradado y relleno blanco (borde cuadrado) */
@Composable
private fun CampoPasswordDegradado(
    etiqueta: String,
    value: String,
    onValueChange: (String) -> Unit,
    visible: Boolean,
    onToggleVisible: () -> Unit,
    borderBrush: Brush
) {
    Column(Modifier.fillMaxWidth()) {
        Text(text = etiqueta, color = Color.Black, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
        // Contenedor con borde degradado + TextField sin indicadores
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp)
                .border(width = 2.dp, brush = borderBrush, shape = RectangleShape)
                .background(Color.White, RectangleShape)
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
                        Icon(
                            imageVector = if (visible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = "Ver/Ocultar",
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
                    focusedIndicatorColor = Color.Transparent,   // sin subrayado
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                )
            )
        }
    }
}