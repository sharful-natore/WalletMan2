package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppLanguage

@Composable
fun CalculatorDialog(
    language: AppLanguage,
    isDark: Boolean,
    onDismiss: () -> Unit,
    onInsert: (String) -> Unit
) {
    var display by remember { mutableStateOf("") }
    var operator by remember { mutableStateOf<String?>(null) }
    var operand1 by remember { mutableStateOf<Double?>(null) }
    var isNewOperand by remember { mutableStateOf(false) }

    val buttons = listOf(
        listOf("7", "8", "9", "÷"),
        listOf("4", "5", "6", "×"),
        listOf("1", "2", "3", "-"),
        listOf("C", "0", ".", "+")
    )

    fun calculate() {
        if (operand1 != null && operator != null && display.isNotEmpty()) {
            val op2 = display.toDoubleOrNull() ?: 0.0
            val result = when (operator) {
                "+" -> operand1!! + op2
                "-" -> operand1!! - op2
                "×" -> operand1!! * op2
                "÷" -> if (op2 != 0.0) operand1!! / op2 else 0.0
                else -> op2
            }
            display = if (result % 1.0 == 0.0) result.toLong().toString() else result.toString()
            operand1 = null
            operator = null
            isNewOperand = true
        }
    }

    fun onButtonClick(label: String) {
        when (label) {
            "C" -> {
                display = ""
                operand1 = null
                operator = null
                isNewOperand = false
            }
            "+", "-", "×", "÷" -> {
                if (operand1 == null) {
                    operand1 = display.toDoubleOrNull() ?: 0.0
                } else if (!isNewOperand) {
                    calculate()
                    operand1 = display.toDoubleOrNull() ?: 0.0
                }
                operator = label
                isNewOperand = true
            }
            else -> { // Numbers and dot
                if (isNewOperand) {
                    display = label
                    isNewOperand = false
                } else {
                    display += label
                }
            }
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
                        .height(80.dp)
                        .background(if (isDark) Color(0xFF2D3249) else Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = display.ifEmpty { "0" },
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color.Black
                    )
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
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when {
                                            isOp -> Color(0xFF3B82F6).copy(alpha = 0.2f)
                                            isClear -> Color(0xFFEF4444).copy(alpha = 0.2f)
                                            else -> if (isDark) Color(0xFF2D3249) else Color(0xFFF8FAFC)
                                        }
                                    )
                                    .clickable { onButtonClick(btn) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = btn,
                                    fontSize = 24.sp,
                                    fontWeight = if (isOp || isClear) FontWeight.Bold else FontWeight.Medium,
                                    color = when {
                                        isOp -> Color(0xFF3B82F6)
                                        isClear -> Color(0xFFEF4444)
                                        else -> if (isDark) Color.White else Color.Black
                                    }
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF3B82F6))
                            .clickable {
                                calculate()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "=",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF10B981))
                            .clickable {
                                calculate()
                                onInsert(display)
                                onDismiss()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (language == AppLanguage.BN) "ইনসার্ট" else "Insert",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
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
