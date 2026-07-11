package com.example.ui.theme

import android.content.res.AssetManager
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.AppLanguage

val InterFontFamily = FontFamily(
    Font(resId = R.font.inter_regular, weight = FontWeight.Normal),
    Font(resId = R.font.inter_medium, weight = FontWeight.Medium),
    Font(resId = R.font.inter_bold, weight = FontWeight.Bold)
)

val HindSiliguriFontFamily = FontFamily(
    Font(resId = R.font.hind_siliguri_regular, weight = FontWeight.Normal),
    Font(resId = R.font.hind_siliguri_medium, weight = FontWeight.Medium),
    Font(resId = R.font.hind_siliguri_bold, weight = FontWeight.Bold)
)

fun createTypography(fontFamily: FontFamily): Typography {
    return Typography(
        displayLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.W400,
            fontSize = 57.sp,
            lineHeight = 64.sp,
            letterSpacing = (-0.25).sp,
        ),
        displayMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.W400,
            fontSize = 45.sp,
            lineHeight = 52.sp,
            letterSpacing = 0.sp,
        ),
        displaySmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.W400,
            fontSize = 36.sp,
            lineHeight = 44.sp,
            letterSpacing = 0.sp,
        ),
        headlineLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.W400,
            fontSize = 32.sp,
            lineHeight = 40.sp,
            letterSpacing = 0.sp,
        ),
        headlineMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.W400,
            fontSize = 28.sp,
            lineHeight = 36.sp,
            letterSpacing = 0.sp,
        ),
        headlineSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.W400,
            fontSize = 24.sp,
            lineHeight = 32.sp,
            letterSpacing = 0.sp,
        ),
        titleLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.W400,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            letterSpacing = 0.sp,
        ),
        titleMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.15.sp,
        ),
        titleSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp,
        ),
        bodyLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.W400,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp,
        ),
        bodyMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.W400,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.25.sp,
        ),
        bodySmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.W400,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.4.sp,
        ),
        labelLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp,
        ),
        labelMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp,
        ),
        labelSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp,
        )
    )
}

val InterTypography = createTypography(InterFontFamily)
val HindSiliguriTypography = createTypography(HindSiliguriFontFamily)

private var cachedInterTypography: Typography? = null
private var cachedHindSiliguriTypography: Typography? = null

fun getTypographyForLanguage(language: AppLanguage, assets: AssetManager): Typography {
    return try {
        if (language == AppLanguage.BN) {
            if (cachedHindSiliguriTypography == null) {
                val fontFamily = FontFamily(
                    Font(path = "font/hind_siliguri_regular.ttf", assetManager = assets, weight = FontWeight.Normal),
                    Font(path = "font/hind_siliguri_medium.ttf", assetManager = assets, weight = FontWeight.Medium),
                    Font(path = "font/hind_siliguri_bold.ttf", assetManager = assets, weight = FontWeight.Bold)
                )
                cachedHindSiliguriTypography = createTypography(fontFamily)
            }
            cachedHindSiliguriTypography!!
        } else {
            if (cachedInterTypography == null) {
                val fontFamily = FontFamily(
                    Font(path = "font/inter_regular.ttf", assetManager = assets, weight = FontWeight.Normal),
                    Font(path = "font/inter_medium.ttf", assetManager = assets, weight = FontWeight.Medium),
                    Font(path = "font/inter_bold.ttf", assetManager = assets, weight = FontWeight.Bold)
                )
                cachedInterTypography = createTypography(fontFamily)
            }
            cachedInterTypography!!
        }
    } catch (e: Exception) {
        if (language == AppLanguage.BN) HindSiliguriTypography else InterTypography
    }
}

val Typography = HindSiliguriTypography
