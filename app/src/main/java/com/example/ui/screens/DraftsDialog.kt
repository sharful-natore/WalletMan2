package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.DraftTransaction
import com.example.data.DraftParseResult
import com.example.data.Person
import com.example.ui.AppLanguage
import com.example.ui.theme.FintechBlue
import com.example.ui.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DraftsScratchpadDialog(
    viewModel: FinanceViewModel,
    language: AppLanguage,
    isDark: Boolean,
    onDismiss: () -> Unit,
    onPostDraft: (DraftTransaction) -> Unit
) {
    val context = LocalContext.current
    val drafts by viewModel.draftTransactions.collectAsStateWithLifecycle()
    var noteText by remember { mutableStateOf("") }
    var editDraftMode by remember { mutableStateOf<DraftTransaction?>(null) }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val results = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spoken = results?.firstOrNull() ?: ""
            if (spoken.isNotBlank()) {
                val trimmed = spoken.trim()
                val lower = trimmed.lowercase()
                val saveKeywords = listOf("সেভ করো", "সেভ করুন", "সেভ কর", "সেভ", "সংরক্ষণ", "save it", "save")
                var autoSave = false
                var cleanNote = trimmed
                for (kw in saveKeywords) {
                    if (lower.endsWith(kw)) {
                        cleanNote = trimmed.substring(0, trimmed.length - kw.length).trim()
                        autoSave = true
                        break
                    }
                }

                noteText = cleanNote
                if (autoSave && cleanNote.isNotBlank()) {
                    viewModel.addDraftTransaction(cleanNote, null)
                    noteText = ""
                    Toast.makeText(context, if (language == AppLanguage.BN) "ভয়েস সেভ সম্পন্ন হয়েছে!" else "Voice draft auto-saved!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val triggerVoiceInput = {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (language == AppLanguage.BN) "bn-BD" else "en-US")
            putExtra(RecognizerIntent.EXTRA_PROMPT, if (language == AppLanguage.BN) "লেনদেন খসড়া বলুন..." else "Speak transaction draft...")
        }
        try {
            speechLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(context, if (language == AppLanguage.BN) "ভয়েস ইনপুট সমর্থিত নয়" else "Speech recognition not supported", Toast.LENGTH_SHORT).show()
        }
    }

    // Dynamic NLP Preview & Type Selection
    val parsedPreview = remember(noteText) {
        if (noteText.isBlank()) null else viewModel.parseDraftDetails(noteText)
    }
    var selectedTypeOverride by remember(noteText) { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isDark) Color(0xFF121212) else Color(0xFFF1F5F9)),
            color = if (isDark) Color(0xFF121212) else Color(0xFFF1F5F9)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                val headerGradient = if (isDark) listOf(Color(0xFF1C1C1E), Color(0xFF1C1C1E)) else com.example.ui.screens.activeThemeGradient
                
                com.example.ui.components.FintechGradientCard(
                    gradientColors = headerGradient,
                    cornerRadius = 24.dp,
                    padding = PaddingValues(16.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (language == AppLanguage.BN) "লেনদেন খসড়া খাতা" else "Transaction Drafts",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (language == AppLanguage.BN) "দ্রুত লেনদেন ড্রাফট করে রাখুন" else "Quickly save transaction drafts",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                // Add Draft Input Card (Yellow Notepad Styling)
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) Color(0xFF2E271E) else Color(0xFFFEF3C7) // Amber Notebook Theme
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.EditNote,
                                contentDescription = null,
                                tint = if (isDark) Color(0xFFF59E0B) else Color(0xFFD97706)
                            )
                            Text(
                                text = if (language == AppLanguage.BN) "নতুন খসড়া লিখুন বা বলুন" else "Write or speak new draft",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDark) Color(0xFFF59E0B) else Color(0xFFB45309)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextField(
                                value = noteText,
                                onValueChange = { noteText = it },
                                placeholder = {
                                    Text(
                                        text = if (language == AppLanguage.BN) "যেমন: চা ২০ টাকা বা বেতন ৫০০০০..." else "e.g., Tea 20 tk or Salary 50000...",
                                        fontSize = 13.sp,
                                        color = if (isDark) Color.LightGray.copy(alpha = 0.5f) else Color.Gray
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("draft_input_field"),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    focusedIndicatorColor = if (isDark) Color(0xFFF59E0B) else Color(0xFFD97706),
                                    unfocusedIndicatorColor = if (isDark) Color(0xFFF59E0B).copy(alpha = 0.4f) else Color(0xFFD97706).copy(alpha = 0.4f),
                                    focusedTextColor = if (isDark) Color.White else Color(0xFF78350F),
                                    unfocusedTextColor = if (isDark) Color.White else Color(0xFF78350F)
                                ),
                                maxLines = 3
                            )

                            // Voice microphone button
                            IconButton(
                                onClick = triggerVoiceInput,
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(if (isDark) Color(0xFFD97706).copy(alpha = 0.2f) else Color(0xFFFDE68A))
                                    .testTag("draft_voice_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Mic,
                                    contentDescription = "Voice Input",
                                    tint = if (isDark) Color(0xFFF59E0B) else Color(0xFFB45309),
                                    modifier = Modifier.size(26.dp)
                                )
                            }

                            // Add Checkmark button
                            IconButton(
                                onClick = {
                                    if (noteText.isNotBlank()) {
                                        viewModel.addDraftTransaction(noteText, selectedTypeOverride)
                                        noteText = ""
                                    }
                                },
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(if (isDark) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFFA7F3D0))
                                    .testTag("draft_save_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = "Save Draft",
                                    tint = if (isDark) Color(0xFF34D399) else Color(0xFF047857),
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        // Real-time NLP parsing badge
                        AnimatedVisibility(
                            visible = parsedPreview != null,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                        parsedPreview?.let { preview ->
                            val currentSelectedType = selectedTypeOverride ?: preview.type ?: "EXPENSE"
                            Column(
                                modifier = Modifier
                                    .padding(top = 12.dp)
                                    .fillMaxWidth()
                                    .background(
                                        if (isDark) Color.White.copy(alpha = 0.05f) else Color.White.copy(
                                            alpha = 0.6f
                                        ), RoundedCornerShape(12.dp)
                                    )
                                    .padding(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.AutoAwesome,
                                        contentDescription = null,
                                        tint = if (isDark) Color(0xFFF59E0B) else Color(0xFFD97706),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = buildString {
                                            append(if (language == AppLanguage.BN) "অনুমান: " else "Inferred: ")
                                            if (preview.amount != null) append("${if (language == AppLanguage.BN) "পরিমাণ" else "Amt"}: ${preview.amount.toInt()}৳ ")
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) Color.White else Color(0xFF78350F)
                                    )
                                    Text(
                                        text = if (language == AppLanguage.BN) "(ট্যাপ করে সিলেক্ট করুন)" else "(Tap to select)",
                                        fontSize = 10.sp,
                                        color = if (isDark) Color.LightGray else Color(0xFF92400E)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val types = listOf(
                                        Triple("EXPENSE", if (language == AppLanguage.BN) "ব্যয়" else "Expense", Color(0xFFEF4444)),
                                        Triple("INCOME", if (language == AppLanguage.BN) "আয়" else "Income", Color(0xFF10B981)),
                                        Triple("LEND", if (language == AppLanguage.BN) "দেনা" else "Lend", Color(0xFF8B5CF6)),
                                        Triple("BORROW", if (language == AppLanguage.BN) "পাওনা" else "Borrow", Color(0xFFF59E0B)),
                                        Triple("SAVINGS", if (language == AppLanguage.BN) "সঞ্চয়" else "Savings", Color(0xFF2563EB)),
                                        Triple("WITHDRAWAL", if (language == AppLanguage.BN) "উত্তোলন" else "Withdrawal", Color(0xFF0D9488))
                                    )

                                    for (item in types) {
                                        val typeKey = item.first
                                        val label = item.second
                                        val brandColor = item.third
                                        val isSelected = currentSelectedType == typeKey
                                        Surface(
                                            onClick = { selectedTypeOverride = typeKey },
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isSelected) brandColor else brandColor.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = label,
                                                color = if (isSelected) Color.White else brandColor,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        }
                    }
                }

                // Recent drafts section title
                Text(
                    text = if (language == AppLanguage.BN) "বর্তমান খসড়া তালিকা" else "Draft Entries Queue",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color.DarkGray,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                // List of active draft items
                if (drafts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(120.dp),
                                shape = CircleShape,
                                color = if (isDark) Color.White.copy(alpha = 0.05f) else Color(0xFFF1F5F9)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.EditNote,
                                        contentDescription = null,
                                        tint = if (isDark) Color.Gray.copy(alpha = 0.5f) else Color.Gray.copy(alpha = 0.3f),
                                        modifier = Modifier.size(64.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = if (language == AppLanguage.BN) "কোনো ড্রাফট নেই" else "No Drafts Found",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isDark) Color.White else Color(0xFF1E293B)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (language == AppLanguage.BN) 
                                    "আপনার দৈনন্দিন ছোটখাটো খরচগুলো দ্রুত এখানে লিখে রাখুন। দিনশেষে সেগুলো মূল ড্যাশবোর্ডে যোগ করতে পারবেন।" 
                                    else "Quickly jot down your minor daily expenses here. You can post them to the main dashboard at the end of the day.",
                                fontSize = 14.sp,
                                color = if (isDark) Color.Gray else Color(0xFF64748B),
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(drafts, key = { it.id }) { draft ->
                            DraftItemCard(
                                draft = draft,
                                language = language,
                                isDark = isDark,
                                onPost = { onPostDraft(draft) },
                                onDelete = { viewModel.deleteDraftTransaction(draft.id) },
                                onEdit = { editDraftMode = draft }
                            )
                        }

                        item {
                            DraftsSummaryCard(
                                drafts = drafts,
                                viewModel = viewModel,
                                language = language,
                                isDark = isDark
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Edit draft
    if (editDraftMode != null) {
        val editingDraft = editDraftMode!!
        var tempText by remember { mutableStateOf(editingDraft.note) }
        AlertDialog(
            onDismissRequest = { editDraftMode = null },
            title = {
                Text(if (language == AppLanguage.BN) "খসড়া সংশোধন" else "Edit Draft Note", fontWeight = FontWeight.Bold)
            },
            text = {
                TextField(
                    value = tempText,
                    onValueChange = { tempText = it },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = viewModel.parseDraftDetails(tempText)
                        viewModel.updateDraftTransaction(
                            editingDraft.copy(
                                note = parsed.cleanedNote,
                                amount = parsed.amount,
                                type = parsed.type,
                                category = parsed.category
                            )
                        )
                        editDraftMode = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FintechBlue)
                ) {
                    Text(if (language == AppLanguage.BN) "হালনাগাদ" else "Update", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { editDraftMode = null }) {
                    Text(if (language == AppLanguage.BN) "বাতিল" else "Cancel")
                }
            }
        )
    }
}

@Composable
fun DraftItemCard(
    draft: DraftTransaction,
    language: AppLanguage,
    isDark: Boolean,
    onPost: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(if (language == AppLanguage.BN) "ড্রাফট মুছুন" else "Delete Draft") },
            text = { Text(if (language == AppLanguage.BN) "আপনি কি নিশ্চিত যে এই ড্রাফটটি মুছে ফেলতে চান?" else "Are you sure you want to delete this draft?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text(if (language == AppLanguage.BN) "মুছে ফেলুন" else "Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(if (language == AppLanguage.BN) "বাতিল" else "Cancel")
                }
            }
        )
    }

    val itemGradient = com.example.ui.screens.activeThemeGradient

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPost() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(androidx.compose.ui.graphics.Brush.linearGradient(itemGradient))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Rounded.StickyNote2,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = SimpleDateFormat("hh:mm a, dd MMM", Locale.getDefault()).format(Date(draft.timestamp)),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = draft.note,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color.White,
                            lineHeight = 22.sp
                        )
                    }

                    // Action buttons
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                        ) {
                            Icon(Icons.Rounded.Edit, contentDescription = "Edit", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        IconButton(
                            onClick = { showDeleteConfirm = true },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                        ) {
                            Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        draft.type?.let { t ->
                            val label = when (t) {
                                "INCOME" -> if (language == AppLanguage.BN) "আয়" else "Income"
                                "EXPENSE" -> if (language == AppLanguage.BN) "ব্যয়" else "Expense"
                                "LEND" -> if (language == AppLanguage.BN) "দেনা" else "Lend"
                                "BORROW" -> if (language == AppLanguage.BN) "পাওনা" else "Borrow"
                                "SAVINGS" -> if (language == AppLanguage.BN) "সঞ্চয়" else "Savings"
                                "WITHDRAWAL" -> if (language == AppLanguage.BN) "উত্তোলন" else "Withdrawal"
                                else -> t
                            }
                            Surface(
                                color = Color.White.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = label,
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        draft.amount?.let { amt ->
                            Surface(
                                color = Color.White.copy(alpha = 0.9f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "${amt.toInt()}৳",
                                    color = itemGradient.first(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Post button
                    Surface(
                        onClick = onPost,
                        color = Color.White,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Rounded.Send, contentDescription = null, tint = itemGradient.first(), modifier = Modifier.size(14.dp))
                            Text(
                                text = if (language == AppLanguage.BN) "পোস্ট করুন" else "Post",
                                color = itemGradient.first(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DraftsSummaryCard(
    drafts: List<DraftTransaction>,
    viewModel: FinanceViewModel,
    language: AppLanguage,
    isDark: Boolean
) {
    var totalIncome = 0.0
    var totalExpense = 0.0
    var totalLend = 0.0
    var totalBorrow = 0.0
    var totalSavings = 0.0
    var totalWithdrawal = 0.0

    for (d in drafts) {
        val parsed = viewModel.parseDraftDetails(d.note)
        val amt = d.amount ?: parsed.amount ?: 0.0
        val t = d.type ?: parsed.type ?: "EXPENSE"
        when (t) {
            "INCOME" -> totalIncome += amt
            "EXPENSE" -> totalExpense += amt
            "LEND" -> totalLend += amt
            "BORROW" -> totalBorrow += amt
            "SAVINGS" -> totalSavings += amt
            "WITHDRAWAL" -> totalWithdrawal += amt
        }
    }

    val netIncExp = totalIncome - totalExpense
    val incExpStr = if (netIncExp > 0) {
        (if (language == AppLanguage.BN) "আয় " else "Inc ") + "${netIncExp.toInt()}৳"
    } else if (netIncExp < 0) {
        (if (language == AppLanguage.BN) "ব্যয় " else "Exp ") + "${(-netIncExp).toInt()}৳"
    } else if (totalIncome > 0 || totalExpense > 0) {
        if (language == AppLanguage.BN) "আয়/ব্যয় ০৳" else "Inc/Exp 0৳"
    } else null

    val netLendBorrow = totalLend - totalBorrow
    val lendBorrowStr = if (netLendBorrow > 0) {
        (if (language == AppLanguage.BN) "দেনা " else "Lend ") + "${netLendBorrow.toInt()}৳"
    } else if (netLendBorrow < 0) {
        (if (language == AppLanguage.BN) "পাওনা " else "Borrow ") + "${(-netLendBorrow).toInt()}৳"
    } else if (totalLend > 0 || totalBorrow > 0) {
        if (language == AppLanguage.BN) "দেনা/পাওনা ০৳" else "Lend/Borrow 0৳"
    } else null

    val netSavWith = totalSavings - totalWithdrawal
    val savWithStr = if (netSavWith > 0) {
        (if (language == AppLanguage.BN) "সঞ্চয় " else "Savings ") + "${netSavWith.toInt()}৳"
    } else if (netSavWith < 0) {
        (if (language == AppLanguage.BN) "উত্তোলন " else "Withdrawal ") + "${(-netSavWith).toInt()}৳"
    } else if (totalSavings > 0 || totalWithdrawal > 0) {
        if (language == AppLanguage.BN) "সঞ্চয়/উত্তোলন ০৳" else "Savings/Withdrawal 0৳"
    } else null

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.BarChart,
                    contentDescription = null,
                    tint = FintechBlue,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = if (language == AppLanguage.BN) "📊 ড্রাফট নিট হিসাব সামারি:" else "📊 Draft Net Summary:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (isDark) Color.White else Color(0xFF0F172A)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (incExpStr != null) {
                    SummaryChip(text = incExpStr, color = if (netIncExp >= 0) Color(0xFF10B981) else Color(0xFFEF4444))
                }
                if (lendBorrowStr != null) {
                    SummaryChip(text = lendBorrowStr, color = if (netLendBorrow >= 0) Color(0xFF8B5CF6) else Color(0xFFF59E0B))
                }
                if (savWithStr != null) {
                    SummaryChip(text = savWithStr, color = if (netSavWith >= 0) Color(0xFF2563EB) else Color(0xFF0D9488))
                }
                if (incExpStr == null && lendBorrowStr == null && savWithStr == null) {
                    Text(
                        text = if (language == AppLanguage.BN) "মোট ${drafts.size} টি ড্রাফট" else "Total ${drafts.size} drafts",
                        fontSize = 12.sp,
                        color = if (isDark) Color.LightGray else Color(0xFF475569)
                    )
                }
            }
        }
    }
}

@Composable
fun SummaryChip(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}
