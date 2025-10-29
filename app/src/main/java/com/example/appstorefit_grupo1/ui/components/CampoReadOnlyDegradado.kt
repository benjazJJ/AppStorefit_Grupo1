package com.example.appstorefit_grupo1.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun CampoReadOnlyDegradado(
    etiqueta: String,
    valor: String,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    borderBrush: Brush,
    innerPadding: PaddingValues = PaddingValues(8.dp)
) {
    val cs = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(12.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, borderBrush), shape = shape)
            .padding(innerPadding)
    ) {
        OutlinedTextField(
            value = valor,
            onValueChange = { /* read-only */ },
            readOnly = true,
            enabled = false,
            singleLine = true,
            label = { Text(text = etiqueta) },
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            modifier = Modifier.fillMaxWidth(),
            shape = shape,
            colors = OutlinedTextFieldDefaults.colors(
                // Contenedor
                disabledContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,

                // Texto y label
                disabledTextColor = cs.onSurface,
                disabledLabelColor = cs.onSurfaceVariant,
                focusedLabelColor = cs.onSurfaceVariant,
                unfocusedLabelColor = cs.onSurfaceVariant,
                disabledPlaceholderColor = cs.onSurfaceVariant,

                // Íconos
                disabledLeadingIconColor = cs.onSurfaceVariant,
                disabledTrailingIconColor = cs.onSurfaceVariant,

                // Bordes del OutlinedTextField
                disabledBorderColor = Color.Transparent,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
            )
        )
    }
}