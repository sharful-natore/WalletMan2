package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import com.example.data.AppDatabase
import com.example.data.DraftTransaction
import com.example.data.DraftParseResult
import kotlinx.coroutines.launch

class DraftInputActivity : ComponentActivity() {
    
    private fun parseDraftDetails(note: String): DraftParseResult {
        var amount: Double? = null
        var cleanedNote = note
        val digitRegex = Regex("[0-9০-৯]+")
        val matches = digitRegex.findAll(note)
        for (match in matches) {
            val numStr = match.value
            val engNumStr = numStr.map { c ->
                if (c in '০'..'৯') (c - '০' + 48).toChar().toString() else c.toString()
            }.joinToString("")
            val parsed = engNumStr.toDoubleOrNull()
            if (parsed != null && parsed > 0) {
                amount = parsed
                cleanedNote = note.replaceFirst(numStr, "").trim().replace(Regex("\\s+"), " ")
                break
            }
        }

        var type: String? = null
        var category: String? = null
        val noteLower = note.lowercase()
        
        if (noteLower.contains("আয়") || noteLower.contains("income") || noteLower.contains("বেতন") || noteLower.contains("salary") || noteLower.contains("পেলাম")) {
            type = "INCOME"
            category = if (noteLower.contains("বেতন") || noteLower.contains("salary")) "Salary" else "Other"
        } else if (noteLower.contains("দেনা") || noteLower.contains("ধারে") || noteLower.contains("পাওনা") || noteLower.contains("lend") || noteLower.contains("borrow") || noteLower.contains("কর্য") || noteLower.contains("কর্জ") || noteLower.contains("ধার")) {
            if (noteLower.contains("দিলাম") || noteLower.contains("gave") || noteLower.contains("পাওনা")) {
                type = "LEND"
                category = "Lending"
            } else {
                type = "BORROW"
                category = "Borrowing"
            }
        } else {
            type = "EXPENSE"
            category = when {
                noteLower.contains("খাবার") || noteLower.contains("চা") || noteLower.contains("ভাত") || noteLower.contains("breakfast") || noteLower.contains("lunch") || noteLower.contains("dinner") || noteLower.contains("food") -> "Food"
                noteLower.contains("বাজার") || noteLower.contains("grocery") -> "Grocery"
                noteLower.contains("গাড়ি") || noteLower.contains("রিকশা") || noteLower.contains("বাস") || noteLower.contains("ভাড়া") || noteLower.contains("rent") || noteLower.contains("travel") || noteLower.contains("fare") -> "Transportation"
                noteLower.contains("জামা") || noteLower.contains("কাপড়") || noteLower.contains("shopping") || noteLower.contains("কেনাকাটা") -> "Shopping"
                else -> "Other"
            }
        }
        
        return DraftParseResult(amount, type, category, cleanedNote)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val isVoice = intent.getBooleanExtra("isVoice", false)
        val isEdit = intent.getBooleanExtra("isEdit", false)
        val draftId = intent.getIntExtra("draft_id", -1)

        setContent {
            var draftText by remember { mutableStateOf("") }
            var isLoading by remember { mutableStateOf(isEdit) }
            val coroutineScope = rememberCoroutineScope()
            val context = androidx.compose.ui.platform.LocalContext.current
            val db = remember { AppDatabase.getDatabase(context) }
            val draftsDao = remember { db.financeDao() }

            val speechLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
                    if (!spokenText.isNullOrBlank()) {
                        draftText = spokenText
                    }
                }
            }

            LaunchedEffect(Unit) {
                if (isEdit && draftId != -1) {
                    val drafts = draftsDao.getAllDraftTransactionsList()
                    val target = drafts.find { it.id == draftId }
                    if (target != null) {
                        draftText = target.note
                    }
                    isLoading = false
                }
                
                if (isVoice && !isEdit) {
                    val voiceIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-BD")
                        putExtra(RecognizerIntent.EXTRA_PROMPT, "লেনদেন বলুন...")
                    }
                    try {
                        speechLauncher.launch(voiceIntent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Voice input not supported on this device", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            var showDeleteDialog by remember { mutableStateOf(false) }

            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Delete Draft?") },
                    text = { Text("Are you sure you want to delete this draft note?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                coroutineScope.launch {
                                    val drafts = draftsDao.getAllDraftTransactionsList()
                                    val target = drafts.find { it.id == draftId }
                                    if (target != null) {
                                        draftsDao.deleteDraftTransactionById(target.id)
                                        val updateIntent = android.content.Intent("com.example.UPDATE_DRAFT_WIDGET")
                                        updateIntent.setPackage(packageName)
                                        sendBroadcast(updateIntent)
                                        Toast.makeText(context, "Draft Deleted", Toast.LENGTH_SHORT).show()
                                        finish()
                                    }
                                }
                                showDeleteDialog = false
                            }
                        ) {
                            Text("Delete", color = Color.Red)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White)
                } else {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text = if (isEdit) "Edit Draft Note" else "Add Draft Transaction",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = draftText,
                                onValueChange = { draftText = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Jot down today's expense...") },
                                maxLines = 3,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedIndicatorColor = Color(0xFFD97706),
                                    unfocusedIndicatorColor = Color.Gray.copy(alpha = 0.5f)
                                )
                            )

                            // Prediction Feature UI
                            val parsedPreview = remember(draftText) {
                                if (draftText.isBlank()) null else parseDraftDetails(draftText)
                            }
                            
                            AnimatedVisibility(
                                visible = parsedPreview != null,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                            parsedPreview?.let { preview ->
                                Row(
                                    modifier = Modifier
                                        .padding(top = 12.dp)
                                        .fillMaxWidth()
                                        .background(Color(0xFFFEF3C7).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color(0xFFD97706),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Column {
                                        Text(
                                            text = buildString {
                                                append("Inferred: ")
                                                if (preview.amount != null) append("Amt: ${preview.amount.toInt()}৳ ")
                                                if (preview.type != null) {
                                                    val typeText = when (preview.type) {
                                                        "INCOME" -> "Income"
                                                        "EXPENSE" -> "Expense"
                                                        "LEND" -> "Lend"
                                                        "BORROW" -> "Borrow"
                                                        else -> preview.type
                                                    }
                                                    append("($typeText)")
                                                }
                                            },
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF78350F)
                                        )
                                        if (preview.cleanedNote != draftText) {
                                            Text(
                                                text = "Note: ${preview.cleanedNote}",
                                                fontSize = 10.sp,
                                                color = Color(0xFF92400E)
                                            )
                                        }
                                    }
                                }
                            }
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isEdit) {
                                    TextButton(onClick = { showDeleteDialog = true }) {
                                        Text("Delete", color = Color.Red)
                                    }
                                }
                                Spacer(modifier = Modifier.weight(1f))
                                TextButton(onClick = { finish() }) {
                                    Text("Cancel", color = Color.Gray)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        if (draftText.isNotBlank()) {
                                            coroutineScope.launch {
                                                if (isEdit && draftId != -1) {
                                                    val drafts = draftsDao.getAllDraftTransactionsList()
                                                    val target = drafts.find { it.id == draftId }
                                                    if (target != null) {
                                                        val parseResult = parseDraftDetails(draftText)
                                                        draftsDao.updateDraftTransaction(target.copy(
                                                            note = parseResult.cleanedNote,
                                                            amount = parseResult.amount,
                                                            type = parseResult.type,
                                                            category = parseResult.category
                                                        ))
                                                    }
                                                } else {
                                                    val parseResult = parseDraftDetails(draftText)
                                                    draftsDao.insertDraftTransaction(DraftTransaction(
                                                        note = parseResult.cleanedNote,
                                                        amount = parseResult.amount,
                                                        type = parseResult.type,
                                                        category = parseResult.category
                                                    ))
                                                }
                                                val updateIntent = android.content.Intent("com.example.UPDATE_DRAFT_WIDGET")
                                                updateIntent.setPackage(packageName)
                                                sendBroadcast(updateIntent)
                                                Toast.makeText(this@DraftInputActivity, if (isEdit) "Draft Updated" else "Draft Saved", Toast.LENGTH_SHORT).show()
                                                finish()
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
                                ) {
                                    Text(if (isEdit) "Update" else "Save Draft", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
