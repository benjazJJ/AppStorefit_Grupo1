package com.example.appstorefit_grupo1.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = SF_Blue,
    onPrimary = Color.White,
    secondary = SF_Purple,
    onSecondary = Color.White,
    tertiary = SF_Teal,
    onTertiary = Color.White,

    background = SF_Light,
    onBackground = SF_Text,
    surface = Color.White,
    onSurface = SF_Text,
    surfaceVariant = Color(0xFFF0F2F6),
    onSurfaceVariant = Color(0xFF50535A)
)

private val DarkColorScheme = darkColorScheme(
    primary = SF_Blue,
    onPrimary = Color.White,
    secondary = SF_Purple,
    onSecondary = Color.White,
    tertiary = SF_Teal,
    onTertiary = Color.Black,

    background = SF_Dark,
    onBackground = Color(0xFFEDEDED),
    surface = Color(0xFF15161A),
    onSurface = Color(0xFFEDEDED),
    surfaceVariant = Color(0xFF1E2026),
    onSurfaceVariant = Color(0xFFB8BBC4)
)

@Composable
fun AppStoreFit_Grupo1Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme =
        if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val ctx = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        } else {
            if (darkTheme) DarkColorScheme else LightColorScheme
        }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
