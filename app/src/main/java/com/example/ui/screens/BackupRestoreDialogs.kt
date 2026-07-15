package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.clickable
import com.example.ui.AppLanguage
import com.example.ui.viewmodel.BackupStats

@Composable
fun BackupStatsDialog(
    title: String,
    stats: BackupStats,
    language: AppLanguage,
    isDark: Boolean,
    isRestoreMode: Boolean = false,
    initialFileName: String = "",
    workspaces: List<com.example.data.WorkspaceStats> = emptyList(),
    onBackupRequested: (() -> Unit)? = null,
    onConfirm: (fileName: String, comment: String, selectedWorkspaceIds: List<String>?) -> Unit,
    onDismiss: () -> Unit
) {
    var fileName by remember { mutableStateOf(initialFileName) }
    var comment by remember { mutableStateOf(stats.comment) }
    
    val availableWorkspaces = remember(isRestoreMode, workspaces, stats.workspaces) {
        if (isRestoreMode) {
            stats.workspaces
        } else {
            workspaces.map { it.workspace }
        }
    }
    
    val allWorkspaceIds = remember(availableWorkspaces) { availableWorkspaces.map { it.id }.toSet() }
    var selectedWorkspaces by remember { mutableStateOf(allWorkspaceIds) }
    
    LaunchedEffect(allWorkspaceIds) {
        selectedWorkspaces = allWorkspaceIds
    }
    
    val timestampStr = remember { java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date()) }
    
    LaunchedEffect(selectedWorkspaces) {
        if (!isRestoreMode && allWorkspaceIds.isNotEmpty()) {
            val prefix = if (selectedWorkspaces.size == allWorkspaceIds.size) {
                "All"
            } else {
                availableWorkspaces.filter { selectedWorkspaces.contains(it.id) }
                          .joinToString("_") { it.name.replace(" ", "") }
            }
            val sanitizedPrefix = if (prefix.isEmpty()) "Empty" else prefix.take(20)
            fileName = "financenote_${sanitizedPrefix}_$timestampStr.json"
        }
    }

    // Captcha variables (only used if isRestoreMode is true)
    val captchaCode = remember {
        val chars = "23456789ABCDEFGHJKMNPQRSTUVWXYZ"
        (1..4).map { chars.random() }.joinToString("")
    }
    var userInput by remember { mutableStateOf("") }
    val isCorrect = !isRestoreMode || userInput.trim().equals(captchaCode, ignoreCase = true)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) Color(0xFF1E2235) else Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (isRestoreMode) Icons.Rounded.Restore else Icons.Rounded.Backup,
                        contentDescription = null,
                        tint = Color(0xFF3B82F6),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color(0xFF1E293B)
                    )
                }

                HorizontalDivider(color = if (isDark) Color(0xFF2E334D) else Color(0xFFE2E8F0))

                // Warning section for Restore Mode
                if (isRestoreMode) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDark) Color(0xFF381E21) else Color(0xFFFFECEF)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = if (language == AppLanguage.BN) "গুরুত্বপূর্ণ সতর্কতা!" else "Important Warning!",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFEF4444)
                                )
                            }
                            Text(
                                text = if (language == AppLanguage.BN)
                                    "ব্যাকআপ রিস্টোর করলে বর্তমান স্থানীয় সকল ডেটা সম্পূর্ণভাবে মুছে যাবে এবং ব্যাকআপ ফাইলের ডেটা দিয়ে প্রতিস্থাপিত হবে।"
                                    else "Restoring a backup will completely overwrite and delete all your current local data.",
                                fontSize = 12.sp,
                                color = if (isDark) Color(0xFFFCA5A5) else Color(0xFF991B1B)
                            )
                            
                            if (onBackupRequested != null) {
                                Button(
                                    onClick = onBackupRequested,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF3B82F6)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Icon(Icons.Rounded.Backup, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (language == AppLanguage.BN) "বর্তমান ডেটার ব্যাকআপ রাখুন" else "Backup Current Data First",
                                        fontSize = 11.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                // Stats Section Title
                Text(
                    text = if (language == AppLanguage.BN) "ডেটার বিবরণ / সামারি" else "Data Summary Details",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (isDark) Color.LightGray else Color(0xFF475569)
                )

                // Grid of Stats
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatItemCard(
                            modifier = Modifier.weight(1f),
                            title = if (language == AppLanguage.BN) "মোট আয়" else "Total Income",
                            value = "৳${stats.totalIncome}",
                            icon = Icons.AutoMirrored.Rounded.TrendingUp,
                            iconColor = Color(0xFF10B981),
                            isDark = isDark
                        )
                        StatItemCard(
                            modifier = Modifier.weight(1f),
                            title = if (language == AppLanguage.BN) "মোট ব্যয়" else "Total Expense",
                            value = "৳${stats.totalExpense}",
                            icon = Icons.AutoMirrored.Rounded.TrendingDown,
                            iconColor = Color(0xFFEF4444),
                            isDark = isDark
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatItemCard(
                            modifier = Modifier.weight(1f),
                            title = if (language == AppLanguage.BN) "মোট পাওনা" else "Total Credit",
                            value = "৳${stats.totalOwedToMe}",
                            icon = Icons.Rounded.ArrowUpward,
                            iconColor = Color(0xFF3B82F6),
                            isDark = isDark
                        )
                        StatItemCard(
                            modifier = Modifier.weight(1f),
                            title = if (language == AppLanguage.BN) "মোট দেনা" else "Total Debt",
                            value = "৳${stats.totalIOwe}",
                            icon = Icons.Rounded.ArrowDownward,
                            iconColor = Color(0xFFF59E0B),
                            isDark = isDark
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatItemCard(
                            modifier = Modifier.weight(1f),
                            title = if (language == AppLanguage.BN) "মোট ব্যক্তি" else "Total Persons",
                            value = "${stats.totalPersons}",
                            icon = Icons.Rounded.People,
                            iconColor = Color(0xFF64748B),
                            isDark = isDark
                        )
                        StatItemCard(
                            modifier = Modifier.weight(1f),
                            title = if (language == AppLanguage.BN) "মোট সঞ্চয় কার্ড" else "Total Savings Cards",
                            value = "${stats.totalCards}",
                            icon = Icons.Rounded.CreditCard,
                            iconColor = Color(0xFF8B5CF6),
                            isDark = isDark
                        )
                    }
                }

                // File Name Field
                if (availableWorkspaces.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = if (isRestoreMode) {
                                if (language == AppLanguage.BN) "কোন ওয়ার্কস্পেস রিস্টোর করবেন?" else "Which Workspaces to Restore?"
                            } else {
                                if (language == AppLanguage.BN) "কোন ওয়ার্কস্পেস ব্যাকআপ করবেন?" else "Which Workspaces to Backup?"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) Color.Gray else Color(0xFF64748B)
                        )
                        
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 160.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            item {
                                val allSelected = selectedWorkspaces.size == allWorkspaceIds.size
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (allSelected) Color(0xFF3B82F6).copy(alpha = 0.1f) else Color.Transparent)
                                        .clickable {
                                            selectedWorkspaces = if (allSelected) emptySet() else allWorkspaceIds
                                        }
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = allSelected,
                                        onCheckedChange = null,
                                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF3B82F6))
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (language == AppLanguage.BN) "সবগুলো নির্বাচন করুন" else "Select All",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) Color.White else Color.Black
                                    )
                                }
                            }
                            
                            items(availableWorkspaces) { ws ->
                                val isSelected = selectedWorkspaces.contains(ws.id)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) Color(0xFF3B82F6).copy(alpha = 0.1f) else Color.Transparent)
                                        .clickable {
                                            selectedWorkspaces = if (isSelected) {
                                                selectedWorkspaces - ws.id
                                            } else {
                                                selectedWorkspaces + ws.id
                                            }
                                        }
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = null,
                                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF3B82F6))
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    val isNewWorkspace = isRestoreMode && workspaces.none { it.workspace.id == ws.id }
                                    Text(
                                        text = if (isNewWorkspace) {
                                            "${ws.name} ${if (language == AppLanguage.BN) "(নতুন)" else "(New)"}"
                                        } else {
                                            ws.name
                                        },
                                        fontSize = 13.sp,
                                        color = if (isDark) Color.White else Color.Black
                                    )
                                }
                            }
                        }
                    }
                }

                if (isRestoreMode) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = if (language == AppLanguage.BN) "ফাইলের নাম" else "File Name",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) Color.Gray else Color(0xFF64748B)
                        )
                        Text(
                            text = fileName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isDark) Color.White else Color(0xFF1E293B),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isDark) Color(0xFF283046) else Color(0xFFF8FAFC),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(12.dp)
                        )
                    }
                } else {
                    OutlinedTextField(
                        value = fileName,
                        onValueChange = { fileName = it },
                        label = { Text(if (language == AppLanguage.BN) "ফাইলের নাম" else "File Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedBorderColor = if (isDark) Color(0xFF2E334D) else Color(0xFFCBD5E1),
                            focusedTextColor = if (isDark) Color.White else Color(0xFF1E293B),
                            unfocusedTextColor = if (isDark) Color.White else Color(0xFF1E293B)
                        )
                    )
                }

                // Comment / Remark Field
                if (isRestoreMode) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = if (language == AppLanguage.BN) "মন্তব্য / নোট" else "Comment / Remarks",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) Color.Gray else Color(0xFF64748B)
                        )
                        Text(
                            text = if (comment.isBlank()) (if (language == AppLanguage.BN) "কোনো মন্তব্য নেই" else "No comments found") else comment,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = if (isDark) Color.White else Color(0xFF1E293B),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isDark) Color(0xFF283046) else Color(0xFFF8FAFC),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(12.dp)
                        )
                    }
                } else {
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        label = { Text(if (language == AppLanguage.BN) "মন্তব্য যোগ করুন (ঐচ্ছিক)" else "Add Comment (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedBorderColor = if (isDark) Color(0xFF2E334D) else Color(0xFFCBD5E1),
                            focusedTextColor = if (isDark) Color.White else Color(0xFF1E293B),
                            unfocusedTextColor = if (isDark) Color.White else Color(0xFF1E293B)
                        )
                    )
                }

                // Captcha Input Field for Restore Mode
                if (isRestoreMode) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isDark) Color(0xFF282E47) else Color(0xFFF1F5F9),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (language == AppLanguage.BN) "রিস্টোর নিশ্চিত করতে ক্যাপচা কোডটি টাইপ করুন" else "To confirm restore, type the captcha code",
                            fontSize = 12.sp,
                            color = if (isDark) Color.Gray else Color(0xFF64748B),
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = captchaCode,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 6.sp,
                            color = Color(0xFF3B82F6),
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                        OutlinedTextField(
                            value = userInput,
                            onValueChange = { userInput = it },
                            placeholder = { Text(if (language == AppLanguage.BN) "ক্যাপচা কোড" else "Captcha Code") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (isCorrect) Color(0xFF10B981) else Color(0xFF3B82F6),
                                unfocusedBorderColor = if (isDark) Color(0xFF2E334D) else Color(0xFFCBD5E1),
                                focusedTextColor = if (isDark) Color.White else Color(0xFF1E293B),
                                unfocusedTextColor = if (isDark) Color.White else Color(0xFF1E293B)
                            )
                        )
                    }
                }

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (language == AppLanguage.BN) "বাতিল" else "Cancel",
                            color = if (isDark) Color.LightGray else Color(0xFF475569)
                        )
                    }

                    Button(
                        onClick = { onConfirm(fileName, comment, selectedWorkspaces.toList()) },
                        enabled = isCorrect && selectedWorkspaces.isNotEmpty(),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRestoreMode) Color(0xFF10B981) else Color(0xFF3B82F6),
                            disabledContainerColor = if (isDark) Color(0xFF2E334D) else Color(0xFFE2E8F0)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isRestoreMode) {
                                if (language == AppLanguage.BN) "রিস্টোর করুন" else "Restore"
                            } else {
                                if (language == AppLanguage.BN) "ব্যাকআপ নিন" else "Backup"
                            },
                            color = if (isCorrect && selectedWorkspaces.isNotEmpty()) Color.White else (if (isDark) Color.Gray else Color(0xFF94A3B8)),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatItemCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    isDark: Boolean
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF282E47) else Color(0xFFF1F5F9)
        ),
        border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isDark) Color.Gray else Color(0xFF475569)
                )
            }
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else Color(0xFF1E293B)
            )
        }
    }
}

@Composable
fun CaptchaDeleteDialog(
    fileName: String,
    language: AppLanguage,
    isDark: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    // Generate random 4 character alphanumeric captcha
    val captchaCode = remember {
        val chars = "23456789ABCDEFGHJKMNPQRSTUVWXYZ" // exclude confusing characters like 1, 0, I, O
        (1..4).map { chars.random() }.joinToString("")
    }
    var userInput by remember { mutableStateOf("") }
    val isCorrect = userInput.trim().equals(captchaCode, ignoreCase = true)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) Color(0xFF1E2235) else Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = if (language == AppLanguage.BN) "ডিলিট নিশ্চিত করুন" else "Confirm Delete",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color(0xFF1E293B)
                    )
                }

                HorizontalDivider(color = if (isDark) Color(0xFF2E334D) else Color(0xFFE2E8F0))

                Text(
                    text = if (language == AppLanguage.BN)
                        "আপনি কি নিশ্চিত যে '$fileName' ব্যাকআপটি ক্লাউড থেকে ডিলিট করতে চান?"
                        else "Are you sure you want to delete '$fileName' from Google Drive?",
                    fontSize = 14.sp,
                    color = if (isDark) Color.LightGray else Color(0xFF475569)
                )

                // Captcha box
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isDark) Color(0xFF282E47) else Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (language == AppLanguage.BN) "নিরাপত্তা ক্যাপচা কোড" else "Security Captcha Code",
                        fontSize = 12.sp,
                        color = if (isDark) Color.Gray else Color(0xFF64748B),
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = captchaCode,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 6.sp,
                        color = Color(0xFF3B82F6),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                OutlinedTextField(
                    value = userInput,
                    onValueChange = { userInput = it },
                    label = { Text(if (language == AppLanguage.BN) "ক্যাপচা কোডটি টাইপ করুন" else "Type the Captcha Code") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isCorrect) Color(0xFF10B981) else Color(0xFF3B82F6),
                        unfocusedBorderColor = if (isDark) Color(0xFF2E334D) else Color(0xFFCBD5E1),
                        focusedTextColor = if (isDark) Color.White else Color(0xFF1E293B),
                        unfocusedTextColor = if (isDark) Color.White else Color(0xFF1E293B)
                    )
                )

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (language == AppLanguage.BN) "বাতিল" else "Cancel",
                            color = if (isDark) Color.LightGray else Color(0xFF475569)
                        )
                    }

                    Button(
                        onClick = onConfirm,
                        enabled = isCorrect,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEF4444),
                            disabledContainerColor = if (isDark) Color(0xFF2E334D) else Color(0xFFE2E8F0)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (language == AppLanguage.BN) "ডিলিট করুন" else "Delete",
                            color = if (isCorrect) Color.White else (if (isDark) Color.Gray else Color(0xFF94A3B8)),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun TrashDialog(
    viewModel: com.example.ui.viewmodel.FinanceViewModel,
    language: AppLanguage,
    isDarkTheme: Boolean,
    onDismiss: () -> Unit
) {
    val trashItems by viewModel.allTrashItems.collectAsState()
    var captchaRequiredId by remember { mutableStateOf<Int?>(null) }
    var restoreConfirmId by remember { mutableStateOf<Int?>(null) }
    var generatedCaptcha by remember { mutableStateOf("") }
    var captchaInput by remember { mutableStateOf("") }
    var captchaError by remember { mutableStateOf(false) }

    fun generateNewCaptcha() {
        val chars = "1234567890" // Simplified to numbers for easier typing on mobile
        generatedCaptcha = (1..4).map { chars.random() }.joinToString("")
    }

    Dialog(onDismissRequest = onDismiss, properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)) {
        Card(
            modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.9f).padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = if (isDarkTheme) Color(0xFF1E2235) else Color.White)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth().background(if (isDarkTheme) Color(0xFF2A2F45) else Color(0xFFF1F5F9)).padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.size(40.dp).background(com.example.ui.theme.FintechRed.copy(alpha=0.15f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.DeleteOutline, contentDescription = null, tint = com.example.ui.theme.FintechRed)
                        }
                        Text(
                            text = if (language == AppLanguage.BN) "ট্র্যাশ (রিসাইকেল বিন)" else "Trash (Recycle Bin)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = if (isDarkTheme) Color.White else Color(0xFF1E293B)
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Rounded.Close, contentDescription = null, modifier = Modifier.size(20.dp), tint = if (isDarkTheme) Color.White else Color.Black)
                    }
                }
                
                if (trashItems.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (language == AppLanguage.BN) "ট্র্যাশ ফাঁকা" else "Trash is empty",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }
                } else {
                    androidx.compose.foundation.lazy.LazyColumn(
                        modifier = Modifier.weight(1f).padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(trashItems.size) { i ->
                            val item = trashItems[i]
                            val daysLeft = maxOf(0L, 30L - ((System.currentTimeMillis() - item.deletedAt) / (24L*60L*60L*1000L)))
                            
                            val itemTitle = when(item.itemType) {
                                "PERSON_WITH_TXS" -> if (language == AppLanguage.BN) "ব্যক্তি ও লেনদেন" else "Person & Transactions"
                                "PERSON" -> if (language == AppLanguage.BN) "ব্যক্তি" else "Person"
                                "TRANSACTION" -> if (language == AppLanguage.BN) "লেনদেন" else "Transaction"
                                "SAVINGS_GOAL" -> if (language == AppLanguage.BN) "কার্ড" else "Card"
                                "SAVINGS_GOAL_WITH_TXS" -> if (language == AppLanguage.BN) "কার্ড ও লেনদেন" else "Card & Transactions"
                                "SAVINGS_TRANSACTION" -> if (language == AppLanguage.BN) "কার্ড লেনদেন" else "Card Transaction"
                                "GDRIVE_BACKUP" -> if (language == AppLanguage.BN) "গুগল ড্রাইভ ব্যাকআপ" else "Google Drive Backup"
                                else -> item.itemType
                            }
                            
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = if (isDarkTheme) Color(0xFF252A3F) else Color(0xFFF8FAFC)),
                                border = BorderStroke(1.dp, if (isDarkTheme) Color(0xFF2E334D) else Color(0xFFE2E8F0))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(
                                            text = itemTitle,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDarkTheme) Color.White else Color.Black,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = if (language == AppLanguage.BN) "$daysLeft দিন বাকি" else "$daysLeft days left",
                                            color = if (daysLeft <= 3) com.example.ui.theme.FintechRed else com.example.ui.theme.FintechGreen,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Surface(
                                        color = if (isDarkTheme) Color(0xFF1E2433) else Color(0xFFEFF6FF),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = viewModel.getTrashItemSummary(item, language),
                                            fontSize = 13.sp,
                                            color = if (isDarkTheme) Color.LightGray else Color.DarkGray,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    if (captchaRequiredId == item.id) {
                                        // Captcha mode for delete
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Text(
                                                    text = generatedCaptcha,
                                                    color = com.example.ui.theme.FintechRed,
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 18.sp,
                                                    letterSpacing = 4.sp,
                                                    modifier = Modifier.background(com.example.ui.theme.FintechRed.copy(alpha = 0.1f), RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                                OutlinedTextField(
                                                    value = captchaInput,
                                                    onValueChange = { captchaInput = it; captchaError = false },
                                                    modifier = Modifier.weight(1f),
                                                    placeholder = { Text(if (language == AppLanguage.BN) "সংখ্যাটি লিখুন" else "Type code") },
                                                    isError = captchaError,
                                                    singleLine = true,
                                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                                                )
                                                IconButton(onClick = { captchaRequiredId = null; captchaInput = "" }) {
                                                    Icon(Icons.Rounded.Close, contentDescription = null)
                                                }
                                            }
                                            Button(
                                                onClick = {
                                                    if (captchaInput == generatedCaptcha) {
                                                        viewModel.permanentDeleteTrashItem(item.id)
                                                        captchaRequiredId = null
                                                        captchaInput = ""
                                                    } else {
                                                        captchaError = true
                                                    }
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.FintechRed)
                                            ) {
                                                Text(if (language == AppLanguage.BN) "নিশ্চিত ডিলেট" else "Confirm Delete")
                                            }
                                        }
                                    } else if (restoreConfirmId == item.id) {
                                        // Restore confirmation mode
                                        Row(
                                            modifier = Modifier.fillMaxWidth().background(if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFF1F5F9), RoundedCornerShape(8.dp)).padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = if (language == AppLanguage.BN) "রিস্টোর করবেন?" else "Restore this?",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isDarkTheme) Color.White else Color.Black
                                            )
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                TextButton(onClick = { restoreConfirmId = null }) {
                                                    Text(if (language == AppLanguage.BN) "না" else "No")
                                                }
                                                Button(
                                                    onClick = { 
                                                        viewModel.restoreTrashItem(item)
                                                        restoreConfirmId = null
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.FintechGreen)
                                                ) {
                                                    Text(if (language == AppLanguage.BN) "হ্যাঁ" else "Yes")
                                                }
                                            }
                                        }
                                    } else {
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Button(
                                                onClick = { restoreConfirmId = item.id },
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.FintechGreen)
                                            ) {
                                                Text(if (language == AppLanguage.BN) "রিস্টোর" else "Restore")
                                            }
                                            OutlinedButton(
                                                onClick = { 
                                                    captchaRequiredId = item.id
                                                    captchaInput = ""
                                                    generateNewCaptcha()
                                                },
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = com.example.ui.theme.FintechRed),
                                                border = BorderStroke(1.dp, com.example.ui.theme.FintechRed)
                                            ) {
                                                Text(if (language == AppLanguage.BN) "ডিলেট" else "Delete")
                                            }
                                        }
                                    }
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
fun MoveToWorkspaceDialog(
    itemName: String,
    language: AppLanguage,
    isDark: Boolean,
    workspaces: List<com.example.data.WorkspaceStats>,
    currentWorkspaceId: String,
    onConfirm: (targetWorkspaceId: String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedWorkspaceId by remember { mutableStateOf<String?>(null) }
    val availableWorkspaces = remember(workspaces, currentWorkspaceId) {
        workspaces.filter { it.workspace.id != currentWorkspaceId }
    }

    // Captcha variables
    val captchaCode = remember {
        val chars = "23456789ABCDEFGHJKMNPQRSTUVWXYZ"
        (1..4).map { chars.random() }.joinToString("")
    }
    var userInput by remember { mutableStateOf("") }
    val isCorrect = userInput.trim().equals(captchaCode, ignoreCase = true)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) Color(0xFF1E2235) else Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SwapHoriz,
                        contentDescription = null,
                        tint = Color(0xFF3B82F6),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = if (language == AppLanguage.BN) "ওয়ার্কস্পেস পরিবর্তন করুন" else "Move to Workspace",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color(0xFF1E293B)
                    )
                }

                HorizontalDivider(color = if (isDark) Color(0xFF2E334D) else Color(0xFFE2E8F0))

                Text(
                    text = if (language == AppLanguage.BN) 
                        "'$itemName' এবং এর সকল লেনদেন ডেটা অন্য ওয়ার্কস্পেসে মুভ করতে চান? নিচের ওয়ার্কস্পেসগুলো থেকে একটি নির্বাচন করুন।" 
                        else "Do you want to move '$itemName' and all its transaction data to another workspace? Select one from below.",
                    fontSize = 14.sp,
                    color = if (isDark) Color.LightGray else Color(0xFF475569)
                )

                // Workspace list
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    availableWorkspaces.forEach { ws ->
                        val isSelected = selectedWorkspaceId == ws.workspace.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) Color(0xFF3B82F6).copy(alpha = 0.1f) else Color.Transparent)
                                .border(
                                    1.dp,
                                    if (isSelected) Color(0xFF3B82F6) else (if (isDark) Color(0xFF2E334D) else Color(0xFFE2E8F0)),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedWorkspaceId = ws.workspace.id }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedWorkspaceId = ws.workspace.id },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF3B82F6))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = ws.workspace.name,
                                color = if (isDark) Color.White else Color.Black,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                // Captcha Input Field
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isDark) Color(0xFF282E47) else Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (language == AppLanguage.BN) "নিশ্চিত করতে ক্যাপচা কোডটি টাইপ করুন" else "To confirm, type the captcha code",
                        fontSize = 12.sp,
                        color = if (isDark) Color.Gray else Color(0xFF64748B),
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = captchaCode,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 6.sp,
                        color = Color(0xFF3B82F6),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                    OutlinedTextField(
                        value = userInput,
                        onValueChange = { userInput = it },
                        placeholder = { Text(if (language == AppLanguage.BN) "ক্যাপচা কোড" else "Captcha Code") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isCorrect) Color(0xFF10B981) else Color(0xFF3B82F6),
                            unfocusedBorderColor = if (isDark) Color(0xFF2E334D) else Color(0xFFCBD5E1),
                            focusedTextColor = if (isDark) Color.White else Color(0xFF1E293B),
                            unfocusedTextColor = if (isDark) Color.White else Color(0xFF1E293B)
                        )
                    )
                }

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (language == AppLanguage.BN) "বাতিল" else "Cancel",
                            color = if (isDark) Color.LightGray else Color(0xFF475569)
                        )
                    }

                    Button(
                        onClick = { selectedWorkspaceId?.let { onConfirm(it) } },
                        enabled = isCorrect && selectedWorkspaceId != null,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3B82F6),
                            disabledContainerColor = if (isDark) Color(0xFF2E334D) else Color(0xFFE2E8F0)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (language == AppLanguage.BN) "মুভ করুন" else "Move Now",
                            color = if (isCorrect && selectedWorkspaceId != null) Color.White else (if (isDark) Color.Gray else Color(0xFF94A3B8)),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
