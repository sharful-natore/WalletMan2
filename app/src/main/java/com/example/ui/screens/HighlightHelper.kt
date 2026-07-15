package com.example.ui.screens

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight

fun highlightMatch(text: String, query: String, highlightColor: Color): AnnotatedString {
    if (query.isBlank()) return AnnotatedString(text)
    
    val matches = Regex(query, RegexOption.IGNORE_CASE).findAll(text)
    return buildAnnotatedString {
        var currentIndex = 0
        val highlightBg = highlightColor.copy(alpha = 0.2f)
        for (match in matches) {
            append(text.substring(currentIndex, match.range.first))
            pushStyle(SpanStyle(background = highlightBg, color = highlightColor, fontWeight = FontWeight.ExtraBold))
            append(text.substring(match.range))
            pop()
            currentIndex = match.range.last + 1
        }
        append(text.substring(currentIndex))
    }
}
