        if (isSyncing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .background(if (isDarkTheme) Color(0xFF1E2235) else Color.White, RoundedCornerShape(16.dp))
                        .padding(32.dp)
                ) {
                    CircularProgressIndicator(color = FintechBlue)
                    Text(
                        text = if (language == AppLanguage.BN) "দয়া করে অপেক্ষা করুন..." else "Please wait...",
                        color = if (isDarkTheme) Color.White else Color(0xFF1E293B),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
