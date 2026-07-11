package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.ui.AppLanguage

private val DarkColorScheme = darkColorScheme(
    primary = FintechBlue,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = DeepObsidian,
    surface = SurfaceDark,
    onBackground = TextDark,
    onSurface = TextDark,
    primaryContainer = Color(0xFF1F2336),
    onPrimaryContainer = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = FintechBlue,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = Color(0xFFF3F4F6),
    surface = SurfaceLight,
    onBackground = TextLight,
    onSurface = TextLight,
    primaryContainer = Color(0xFFE0E7FF),
    onPrimaryContainer = Color(0xFF312E81)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    language: AppLanguage = AppLanguage.BN,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val typography = getTypographyForLanguage(language, LocalContext.current.assets)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}
