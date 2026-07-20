package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppLanguage

fun evaluateExpression(expr: String): Double {
    return object : Any() {
        var pos = -1
        var ch = 0

        fun nextChar() {
            ch = if (++pos < expr.length) expr[pos].code else -1
        }

        fun eat(charToEat: Int): Boolean {
            while (ch == ' '.code) nextChar()
            if (ch == charToEat) {
                nextChar()
                return true
            }
            return false
        }

        fun parse(): Double {
            nextChar()
            val x = parseExpression()
            if (pos < expr.length) throw RuntimeException("Unexpected: " + ch.toChar())
            return x
        }

        fun parseExpression(): Double {
            var x = parseTerm()
            while (true) {
                if (eat('+'.code)) x += parseTerm() // addition
                else if (eat('-'.code)) x -= parseTerm() // subtraction
                else return x
            }
        }

        fun parseTerm(): Double {
            var x = parseFactor()
            while (true) {
                if (eat('*'.code) || eat('×'.code)) x *= parseFactor() // multiplication
                else if (eat('/'.code) || eat('÷'.code)) x /= parseFactor() // division
                else return x
            }
        }

        fun parseFactor(): Double {
            if (eat('+'.code)) return parseFactor() // unary plus
            if (eat('-'.code)) return -parseFactor() // unary minus

            var x: Double
            val startPos = this.pos
            if (eat('('.code)) { // parentheses
                x = parseExpression()
                eat(')'.code)
            } else if (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) { // numbers
                while (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) nextChar()
                x = expr.substring(startPos, this.pos).toDouble()
            } else {
                throw RuntimeException("Unexpected: " + ch.toChar())
            }

            if (eat('%'.code)) x /= 100.0 // percentage

            return x
        }
    }.parse()
}

@Composable
fun CalculatorDialog(
    language: AppLanguage,
    isDark: Boolean,
    onDismiss: () -> Unit,
    onInsert: (String) -> Unit
) {
    var display by remember { mutableStateOf("") }
    var history by remember { mutableStateOf("") }
    var hasResult by remember { mutableStateOf(false) }

    val buttons = listOf(
        listOf("C", "(", ")", "÷"),
        listOf("7", "8", "9", "×"),
        listOf("4", "5", "6", "-"),
        listOf("1", "2", "3", "+"),
        listOf("%", "0", ".", "=")
    )

    fun onButtonClick(label: String) {
        when (label) {
            "C" -> {
                display = ""
                history = ""
                hasResult = false
            }
            "=" -> {
                try {
                    if (display.isNotEmpty()) {
                        val result = evaluateExpression(display.replace("×", "*").replace("÷", "/"))
                        history = display + "="
                        display = if (result % 1.0 == 0.0) result.toLong().toString() else result.toString()
                        hasResult = true
                    }
                } catch (e: Exception) {
                    history = display + "="
                    display = "Error"
                    hasResult = true
                }
            }
            else -> {
                if (hasResult) {
                    if (label in listOf("+", "-", "×", "÷", "%")) {
                        // Continue from result
                        display += label
                        hasResult = false
                    } else {
                        // Start new calculation
                        display = label
                        history = ""
                        hasResult = false
                    }
                } else {
                    display += label
                }
            }
        }
    }

    fun onBackspace() {
        if (hasResult) {
            display = ""
            history = ""
            hasResult = false
        } else if (display.isNotEmpty()) {
            display = display.dropLast(1)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (language == AppLanguage.BN) "ক্যালকুলেটর" else "Calculator",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = if (isDark) Color.White else Color.Black
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(if (isDark) Color(0xFF2D3249) else Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = history,
                            fontSize = 16.sp,
                            color = if (isDark) Color.LightGray else Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val scrollState = rememberScrollState()
                            LaunchedEffect(display) {
                                scrollState.animateScrollTo(scrollState.maxValue)
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .horizontalScroll(scrollState),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Text(
                                    text = display.ifEmpty { "0" },
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (display == "Error") Color.Red else (if (isDark) Color.White else Color.Black),
                                    maxLines = 1,
                                    textAlign = TextAlign.End
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = { onBackspace() }, modifier = Modifier.size(32.dp)) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.Backspace,
                                    contentDescription = "Backspace",
                                    tint = if (isDark) Color.LightGray else Color.Gray
                                )
                            }
                        }
                    }
                }
                
                buttons.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { btn ->
                            val isOp = btn in listOf("+", "-", "×", "÷")
                            val isEq = btn == "="
                            val isClear = btn == "C"
                            val isFunc = btn in listOf("(", ")", "%")
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1.2f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when {
                                            isOp -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                            isEq -> MaterialTheme.colorScheme.primary
                                            isClear -> Color(0xFFEF4444).copy(alpha = 0.2f)
                                            isFunc -> Color(0xFF10B981).copy(alpha = 0.2f)
                                            else -> if (isDark) Color(0xFF2D3249) else Color(0xFFF8FAFC)
                                        }
                                    )
                                    .clickable { onButtonClick(btn) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = btn,
                                    fontSize = 24.sp,
                                    fontWeight = if (isOp || isEq || isClear || isFunc) FontWeight.Bold else FontWeight.Medium,
                                    color = when {
                                        isOp -> MaterialTheme.colorScheme.primary
                                        isEq -> Color.White
                                        isClear -> Color(0xFFEF4444)
                                        isFunc -> Color(0xFF10B981)
                                        else -> if (isDark) Color.White else Color.Black
                                    }
                                )
                            }
                        }
                    }
                }
                
                val canInsert = try {
                    val currentVal = if (hasResult) display else {
                        if (display.isEmpty() || display == "Error") "0"
                        else evaluateExpression(display.replace("×", "*").replace("÷", "/")).toString()
                    }
                    currentVal.toDouble() > 0.0 && display != "Error"
                } catch (e: Exception) {
                    false
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Cross Button (Small Red) - Using Icon instead of Text for a cleaner look
                    Box(
                        modifier = Modifier
                            .weight(0.25f)
                            .height(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // 2. Insert Button (Green)
                    Box(
                        modifier = Modifier
                            .weight(0.75f)
                            .height(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (canInsert) Color(0xFF10B981) else Color.Gray.copy(alpha = 0.2f))
                            .clickable(enabled = canInsert) {
                                if (hasResult && display != "Error") {
                                    onInsert(display)
                                    onDismiss()
                                } else if (display.isNotEmpty() && display != "Error") {
                                    try {
                                        val result = evaluateExpression(display.replace("×", "*").replace("÷", "/"))
                                        val resultStr = if (result % 1.0 == 0.0) result.toLong().toString() else result.toString()
                                        onInsert(resultStr)
                                        onDismiss()
                                    } catch (e: Exception) {
                                        history = display + "="
                                        display = "Error"
                                        hasResult = true
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (language == AppLanguage.BN) "ইনসার্ট করুন" else "Insert Amount",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (canInsert) Color.White else (if (isDark) Color.White.copy(alpha = 0.3f) else Color.Gray)
                        )
                    }
                }
            }
        },
        confirmButton = {},
        shape = RoundedCornerShape(16.dp),
        containerColor = if (isDark) Color(0xFF1E2235) else Color.White
    )
}
