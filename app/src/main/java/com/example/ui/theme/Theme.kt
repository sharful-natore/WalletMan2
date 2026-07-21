package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.ui.AppLanguage

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.toArgb
import android.content.Context
import androidx.compose.runtime.remember


private val DarkColorScheme = darkColorScheme(
    primary = FintechBlue,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = DeepObsidian,
    surface = SurfaceDark,
    onBackground = TextDark,
    onSurface = TextDark,
    primaryContainer = Color(0xFF1C1C1E),
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
fun FinanceNoteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    language: AppLanguage = AppLanguage.BN,
    themePrimaryColor: Color = FintechBlue,
    content: @Composable () -> Unit
) {
    val baseColorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val colorScheme = baseColorScheme.copy(primary = themePrimaryColor)
    val typography = getTypographyForLanguage(language)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}
