with open('app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt', 'r') as f:
    lines = f.readlines()

# Find the end of FinanceNoteApp function
# It ends at line 2340 according to previous view_file (but line numbers might have shifted)
# Let's find it by looking for the last lines of the function.

end_idx = -1
for i, line in enumerate(lines):
    if i > 2300 and line.strip() == "}":
        # Check if it's the right one (followed by @Composable or end of file or something)
        if i + 1 < len(lines) and "@Composable" in lines[i+1]:
            end_idx = i
            break
        elif i + 1 == len(lines):
            end_idx = i
            break

if end_idx != -1:
    # Insert notification overlay call before the end of FinanceNoteApp
    notif_call = """
    // Custom Notification Overlay
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        androidx.compose.animation.AnimatedVisibility(
            visible = customNotification != null,
            enter = androidx.compose.animation.slideInVertically(initialOffsetY = { -it }) + androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.slideOutVertically(targetOffsetY = { -it }) + androidx.compose.animation.fadeOut()
        ) {
            customNotification?.let { notif ->
                CustomNotificationOverlay(
                    notification = notif,
                    language = language,
                    isDark = isDarkTheme
                )
            }
        }
    }
"""
    lines.insert(end_idx, notif_call)

# Add CustomNotificationOverlay definition at the end of file
overlay_def = """
@Composable
fun CustomNotificationOverlay(
    notification: com.example.ui.viewmodel.CustomNotification,
    language: AppLanguage,
    isDark: Boolean
) {
    val bgColor = if (notification.isSuccess) {
        if (isDark) Color(0xFF065F46) else Color(0xFFD1FAE5)
    } else {
        if (isDark) Color(0xFF991B1B) else Color(0xFFFEE2E2)
    }
    
    val contentColor = if (notification.isSuccess) {
        if (isDark) Color(0xFF34D399) else Color(0xFF065F46)
    } else {
        if (isDark) Color(0xFFF87171) else Color(0xFF991B1B)
    }

    val icon = when (notification.type) {
        "SUCCESS" -> Icons.Rounded.CheckCircle
        "ERROR" -> Icons.Rounded.Error
        "SIGN_IN" -> Icons.AutoMirrored.Rounded.Login
        "SIGN_OUT" -> Icons.AutoMirrored.Rounded.Logout
        "SYNC" -> Icons.Rounded.Sync
        "BACKUP" -> Icons.Rounded.CloudUpload
        "RESTORE" -> Icons.Rounded.CloudDownload
        else -> Icons.Rounded.Info
    }

    Card(
        modifier = Modifier
            .padding(top = 40.dp)
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .shadow(elevation = 12.dp, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = notification.message,
                color = contentColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
"""
lines.append(overlay_def)

with open('app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt', 'w') as f:
    f.writelines(lines)
