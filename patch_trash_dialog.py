with open('app/src/main/java/com/example/ui/screens/BackupRestoreDialogs.kt', 'r') as f:
    content = f.read()

trash_dialog = """
@Composable
fun TrashDialog(
    viewModel: com.example.ui.viewmodel.FinanceViewModel,
    language: AppLanguage,
    isDarkTheme: Boolean,
    onDismiss: () -> Unit
) {
    val trashItems by viewModel.allTrashItems.collectAsState()
    var captchaRequiredId by remember { mutableStateOf<Int?>(null) }
    var captchaInput by remember { mutableStateOf("") }
    var captchaError by remember { mutableStateOf(false) }

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
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp).background(if (isDarkTheme) Color(0x33FFFFFF) else Color(0x1A000000), RoundedCornerShape(8.dp))) {
                        Icon(Icons.Rounded.Close, contentDescription = null, modifier = Modifier.size(16.dp), tint = if (isDarkTheme) Color.White else Color.Black)
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
                            
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = if (isDarkTheme) Color(0xFF252A3F) else Color(0xFFF8FAFC)),
                                border = BorderStroke(1.dp, if (isDarkTheme) Color(0xFF2E334D) else Color(0xFFE2E8F0))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(
                                            text = "${item.itemType} #${item.originalId}",
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDarkTheme) Color.White else Color.Black
                                        )
                                        Text(
                                            text = if (language == AppLanguage.BN) "$daysLeft দিন বাকি" else "$daysLeft days left",
                                            color = if (daysLeft <= 3) com.example.ui.theme.FintechRed else com.example.ui.theme.FintechGreen,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = item.itemJson.take(100) + "...",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    if (captchaRequiredId == item.id) {
                                        // Captcha mode
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            OutlinedTextField(
                                                value = captchaInput,
                                                onValueChange = { captchaInput = it; captchaError = false },
                                                modifier = Modifier.weight(1f),
                                                placeholder = { Text(if (language == AppLanguage.BN) "CONFIRM লিখুন" else "Type CONFIRM") },
                                                isError = captchaError,
                                                singleLine = true
                                            )
                                            Button(
                                                onClick = {
                                                    if (captchaInput.equals("CONFIRM", ignoreCase = true)) {
                                                        viewModel.permanentDeleteTrashItem(item.id)
                                                        captchaRequiredId = null
                                                        captchaInput = ""
                                                    } else {
                                                        captchaError = true
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.FintechRed)
                                            ) {
                                                Text(if (language == AppLanguage.BN) "ডিলেট" else "Delete")
                                            }
                                        }
                                    } else {
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Button(
                                                onClick = { viewModel.restoreTrashItem(item) },
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.FintechGreen)
                                            ) {
                                                Text(if (language == AppLanguage.BN) "রিস্টোর" else "Restore")
                                            }
                                            OutlinedButton(
                                                onClick = { captchaRequiredId = item.id; captchaInput = "" },
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
"""

content = content + "\n" + trash_dialog

with open('app/src/main/java/com/example/ui/screens/BackupRestoreDialogs.kt', 'w') as f:
    f.write(content)
