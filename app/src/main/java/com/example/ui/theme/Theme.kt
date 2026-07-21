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

private fun loadCustomGradients(context: Context): List<List<Color>> {
    val serialized = context.getSharedPreferences("financenote_prefs", Context.MODE_PRIVATE)
        .getString("custom_gradients_v3", "") ?: ""
    if (serialized.isBlank()) {
        val oldSerialized = context.getSharedPreferences("financenote_prefs", Context.MODE_PRIVATE)
            .getString("custom_gradients_v2", "") ?: ""
        if (oldSerialized.isBlank()) return emptyList()
        return try {
            oldSerialized.split(";").map { gradStr ->
                gradStr.split(",").map { colorStr ->
                    Color(colorStr.toInt())
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    return try {
        serialized.split(";").mapNotNull { gradStr ->
            if (gradStr.isBlank()) return@mapNotNull null
            val parts = gradStr.split(":")
            val colorsStr = if (parts.isNotEmpty()) parts[0] else gradStr
            colorsStr.split(",").map { Color(it.toInt()) }
        }
    } catch (e: Exception) {
        emptyList()
    }
}

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
    themeGradientIndex: Int = 0,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val customGradients = remember(context) { loadCustomGradients(context) }
    val fullGradientsList = GradientsList + customGradients

    val themePrimaryColor = if (themeGradientIndex in fullGradientsList.indices) {
        fullGradientsList[themeGradientIndex][0]
    } else {
        FintechBlue
    }
    val baseColorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val colorScheme = baseColorScheme.copy(primary = themePrimaryColor)
    val typography = getTypographyForLanguage(language)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}
