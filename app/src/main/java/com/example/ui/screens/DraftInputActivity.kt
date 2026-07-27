package com.example.ui.screens

import android.app.Activity
import android.content.Context
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
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.foundation.verticalScroll
import com.example.data.AppDatabase
import com.example.data.DraftTransaction
import com.example.data.DraftParseResult
import com.example.ui.widget.DraftWidgetProvider
import android.appwidget.AppWidgetManager
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import kotlinx.coroutines.launch

class DraftInputActivity : ComponentActivity() {
    
    private fun parseDraftDetails(note: String): DraftParseResult {
        return com.example.data.DraftParser.parse(note)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val isVoice = intent.getBooleanExtra("isVoice", false)
        val isEdit = intent.getBooleanExtra("isEdit", false)
        val showInfoDialogExtra = intent.getBooleanExtra("showInfoDialog", false)
        val draftId = intent.getIntExtra("draft_id", -1)

        setContent {
            var draftText by remember { mutableStateOf("") }
            var isLoading by remember { mutableStateOf(isEdit) }
            var showInfoDialogState by remember { mutableStateOf(showInfoDialogExtra) }
            var showOfflineGuideDialog by remember { mutableStateOf(false) }
            val coroutineScope = rememberCoroutineScope()
            val context = androidx.compose.ui.platform.LocalContext.current
            val db = remember { AppDatabase.getDatabase(context) }
            val draftsDao = remember { db.financeDao() }

            val prefs = remember { context.getSharedPreferences("financenote_prefs", Context.MODE_PRIVATE) }
            val langStr = remember { prefs.getString("app_language", "BN") ?: "BN" }
            val isBn = langStr == "BN"

            val speechLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
                    if (!spokenText.isNullOrBlank()) {
                        val trimmed = spokenText.trim()
                        val lower = trimmed.lowercase()
                        val saveKeywords = listOf("ওকে", "ok", "okay", "সেভ করো", "সেভ করুন", "সেভ কর", "সেভ", "সংরক্ষণ", "save it", "save")
                        var autoSave = false
                        var cleanNote = trimmed
                        for (kw in saveKeywords) {
                            if (lower.endsWith(kw)) {
                                cleanNote = trimmed.substring(0, trimmed.length - kw.length).trim()
                                autoSave = true
                                break
                            }
                        }

                        draftText = cleanNote
                        if (autoSave && cleanNote.isNotBlank()) {
                            coroutineScope.launch {
                                val parseResult = parseDraftDetails(cleanNote)
                                draftsDao.insertDraftTransaction(DraftTransaction(
                                    note = parseResult.cleanedNote,
                                    amount = parseResult.amount,
                                    type = parseResult.type,
                                    category = parseResult.category
                                ))
                                com.example.widget.updateAllWidgets(context)
                                val updateIntent = Intent("com.example.UPDATE_DRAFT_WIDGET").apply {
                                    setPackage(context.packageName)
                                }
                                context.sendBroadcast(updateIntent)
                                Toast.makeText(context, if (isBn) "ভয়েস সেভ সম্পন্ন হয়েছে!" else "Voice draft saved!", Toast.LENGTH_SHORT).show()
                                (context as? Activity)?.finish()
                            }
                        }
                    }
                } else {
                    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
                    val isConnected = cm?.activeNetworkInfo?.isConnected == true
                    if (!isConnected) {
                        showOfflineGuideDialog = true
                    }
                }
            }

            val triggerVoice = {
                val voiceIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (isBn) "bn-BD" else "en-US")
                    putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
                    putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf(if (isBn) "bn-BD" else "en-US"))
                    putExtra(RecognizerIntent.EXTRA_PROMPT, if (isBn) "লেনদেন ড্রাফট বলুন (অফলাইন সমর্থিত)..." else "Speak transaction draft...")
                }
                try {
                    speechLauncher.launch(voiceIntent)
                } catch (e: Exception) {
                    showOfflineGuideDialog = true
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
                    triggerVoice()
                }
            }

            var showDeleteDialog by remember { mutableStateOf(false) }

            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text(if (isBn) "ড্রাফট মুছে ফেলবেন?" else "Delete Draft?") },
                    text = { Text(if (isBn) "আপনি কি নিশ্চিতভাবে এই ড্রাফট নোটটি মুছে ফেলতে চান?" else "Are you sure you want to delete this draft note?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                coroutineScope.launch {
                                    val drafts = draftsDao.getAllDraftTransactionsList()
                                    val target = drafts.find { it.id == draftId }
                                    if (target != null) {
                                        val moshi = com.squareup.moshi.Moshi.Builder().addLast(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory()).build()
                                        val adapter = moshi.adapter(DraftTransaction::class.java)
                                        val trashItem = com.example.data.TrashItem(
                                            originalId = target.id,
                                            itemType = "DRAFT_TRANSACTION",
                                            itemJson = adapter.toJson(target)
                                        )
                                        db.financeDao().insertTrashItem(trashItem)
                                        draftsDao.deleteDraftTransactionById(target.id)
                                        val updateIntent = android.content.Intent("com.example.UPDATE_DRAFT_WIDGET")
                                        updateIntent.setPackage(packageName)
                                        sendBroadcast(updateIntent)
                                        com.example.widget.updateAllWidgets(context)
                                        Toast.makeText(context, if (isBn) "ড্রাফট মুছে ফেলা হয়েছে" else "Draft Deleted", Toast.LENGTH_SHORT).show()
                                        finish()
                                    }
                                }
                                showDeleteDialog = false
                            }
                        ) {
                            Text(if (isBn) "মুছুন" else "Delete", color = Color.Red)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text(if (isBn) "বাতিল" else "Cancel")
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
                            if (showInfoDialogState) {
                                DraftInfoDialog(
                                    onDismiss = {
                                        showInfoDialogState = false
                                        if (showInfoDialogExtra && !isEdit && draftText.isBlank()) {
                                            finish()
                                        }
                                    }
                                )
                            }

                            if (showOfflineGuideDialog) {
                                OfflineVoiceGuideDialog(
                                    isBn = isBn,
                                    onDismiss = { showOfflineGuideDialog = false }
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isEdit) {
                                        if (isBn) "ড্রাফট এডিট করুন" else "Edit Draft Note"
                                    } else {
                                        if (isBn) "নতুন ড্রাফট যোগ করুন" else "Add Draft Transaction"
                                    },
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { showOfflineGuideDialog = true }) {
                                        Icon(
                                            imageVector = Icons.Rounded.Warning,
                                            contentDescription = "Offline Voice Guide",
                                            tint = Color(0xFFD97706)
                                        )
                                    }
                                    IconButton(onClick = { showInfoDialogState = true }) {
                                        Icon(
                                            imageVector = Icons.Rounded.Info,
                                            contentDescription = "Draft Info",
                                            tint = Color(0xFF0284C7)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = draftText,
                                onValueChange = { draftText = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text(if (isBn) "আজকের খরচ বা লেনদেন ড্রাফট করুন..." else "Jot down today's expense...") },
                                trailingIcon = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(onClick = { showOfflineGuideDialog = true }) {
                                            Icon(
                                                imageVector = Icons.Rounded.Info,
                                                contentDescription = "Offline Voice Settings",
                                                tint = Color(0xFFD97706).copy(alpha = 0.7f),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        IconButton(onClick = triggerVoice) {
                                            Icon(
                                                imageVector = Icons.Rounded.Mic,
                                                contentDescription = "Voice Input",
                                                tint = Color(0xFFD97706)
                                            )
                                        }
                                    }
                                },
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
                            var selectedTypeOverride by remember(draftText) { mutableStateOf<String?>(null) }
                            
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
                                            .background(Color(0xFFFEF3C7).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                            .padding(10.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.AutoAwesome,
                                                contentDescription = null,
                                                tint = Color(0xFFD97706),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = buildString {
                                                    append("অনুমান: ")
                                                    if (preview.amount != null) append("${preview.amount.toInt()}৳ ")
                                                },
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF78350F)
                                            )
                                            Text(
                                                text = "(ধরণ সিলেক্ট করতে ট্যাপ করুন)",
                                                fontSize = 10.sp,
                                                color = Color(0xFF92400E)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Type selection chips (Horizontal scrollable)
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            val types = listOf(
                                                Triple("EXPENSE", "ব্যয়", Color(0xFFEF4444)),
                                                Triple("INCOME", "আয়", Color(0xFF10B981)),
                                                Triple("LEND", "পাওনা", Color(0xFF8B5CF6)),
                                                Triple("BORROW", "দেনা", Color(0xFFF59E0B)),
                                                Triple("SAVINGS", "সঞ্চয়", Color(0xFF2563EB)),
                                                Triple("WITHDRAWAL", "উত্তোলন", Color(0xFF0D9488))
                                            )

                                            for (item in types) {
                                                val typeKey = item.first
                                                val label = item.second
                                                val brandColor = item.third
                                                val isSelected = currentSelectedType == typeKey
                                                Surface(
                                                    onClick = { selectedTypeOverride = typeKey },
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = if (isSelected) brandColor else brandColor.copy(alpha = 0.12f)
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

                            Spacer(modifier = Modifier.height(24.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isEdit) {
                                    TextButton(onClick = { showDeleteDialog = true }) {
                                        Text(if (isBn) "মুছুন" else "Delete", color = Color.Red)
                                    }
                                }
                                Spacer(modifier = Modifier.weight(1f))
                                TextButton(onClick = { finish() }) {
                                    Text(if (isBn) "বাতিল" else "Cancel", color = Color.Gray)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        if (draftText.isNotBlank()) {
                                            coroutineScope.launch {
                                                val parseResult = parseDraftDetails(draftText)
                                                val finalType = selectedTypeOverride ?: parseResult.type ?: "EXPENSE"
                                                if (isEdit && draftId != -1) {
                                                    val drafts = draftsDao.getAllDraftTransactionsList()
                                                    val target = drafts.find { it.id == draftId }
                                                    if (target != null) {
                                                        draftsDao.updateDraftTransaction(target.copy(
                                                            note = parseResult.cleanedNote,
                                                            amount = parseResult.amount,
                                                            type = finalType,
                                                            category = parseResult.category
                                                        ))
                                                    }
                                                } else {
                                                    draftsDao.insertDraftTransaction(DraftTransaction(
                                                        note = parseResult.cleanedNote,
                                                        amount = parseResult.amount,
                                                        type = finalType,
                                                        category = parseResult.category
                                                    ))
                                                }
                                                val updateIntent = android.content.Intent("com.example.UPDATE_DRAFT_WIDGET")
                                                updateIntent.setPackage(packageName)
                                                sendBroadcast(updateIntent)
                                                com.example.widget.updateAllWidgets(this@DraftInputActivity)
                                                Toast.makeText(
                                                    this@DraftInputActivity,
                                                    if (isEdit) {
                                                        if (isBn) "ড্রাফট আপডেট করা হয়েছে" else "Draft Updated"
                                                    } else {
                                                        if (isBn) "ড্রাফট সেভ করা হয়েছে" else "Draft Saved"
                                                    },
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                finish()
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
                                ) {
                                    Text(
                                        text = if (isEdit) {
                                            if (isBn) "আপডেট করুন" else "Update"
                                        } else {
                                            if (isBn) "সেভ করুন" else "Save Draft"
                                        },
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DraftInfoDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("বুঝতে পেরেছি", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Info,
                    contentDescription = null,
                    tint = Color(0xFF0284C7),
                    modifier = Modifier.size(26.dp)
                )
                Text(
                    text = "লেনদেনের ড্রাফটসমূহ নির্দেশিকা",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Section 1: Why
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "১. কেন ব্যবহার করবেন? (Why)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF0284C7)
                    )
                    Text(
                        text = "তাৎক্ষণিকভাবে কোনো লেনদেন ঘটে গেলে দ্রুত টুকে রাখার জন্য এটি তৈরি। পরবর্তীতে সময় সুযোগ নিয়ে সঠিক খাত, ক্যাটাগরি ও হিসাব মিলিয়ে মূল হিসাব বা ওয়ার্কস্পেস এ যুক্ত (Posting) করে নেওয়ার সুবিধা পাবেন।",
                        fontSize = 13.sp,
                        color = Color(0xFF334155),
                        lineHeight = 18.sp
                    )
                }

                HorizontalDivider(color = Color(0xFFE2E8F0))

                // Section 2: How to use
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "২. কীভাবে ড্রাফট করবেন? (How to use)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF0284C7)
                    )
                    Text(
                        text = "• টাইপ করে বা উইজেটের মাইক্রোফোনে ভয়েস দিয়ে লিখুন (যেমন: \"বাজার ৫০০\" বা \"রহিম দিল ২০০০\")।\n• সিস্টেম নিজ থেকেই অ্যামাউন্ট ও টাইপ (আয়/ব্যয়/দেনা-পাওনা) চিনে নিবে।\n• ভয়েস ইনপুটের সময় শেষে \"ওকে\" বা \"সেভ\" বললে স্বয়ংক্রিয়ভাবে ড্রাফট হয়ে যাবে।",
                        fontSize = 13.sp,
                        color = Color(0xFF334155),
                        lineHeight = 18.sp
                    )
                }

                HorizontalDivider(color = Color(0xFFE2E8F0))

                // Section 3: How to post
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "৩. কীভাবে পোস্টিং করবেন? (How to post)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF0284C7)
                    )
                    Text(
                        text = "• ড্রাফট লিস্টের যেকোনো আইটেমের পাশে থাকা 'পোস্ট' বাটনে ক্লিক করুন।\n• আপনার সুবিধাজনক সক্রিয় 'ওয়ার্কস্পেস' নির্বাচন করে নিশ্চিত করলেই ড্রাফটটি মূল লেনদেনে যুক্ত হবে এবং ড্রাফট খাতা থেকে সরে যাবে।",
                        fontSize = 13.sp,
                        color = Color(0xFF334155),
                        lineHeight = 18.sp
                    )
                }
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White
    )
}
