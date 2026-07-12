package com.example.ui.theme

import androidx.compose.material3.Typography
import com.example.ui.AppLanguage

val Typography = Typography()

fun getTypographyForLanguage(language: AppLanguage): Typography {
    return Typography
}
