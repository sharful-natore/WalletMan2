#!/bin/bash
awk '
/^\/\/ ---------------- DASHBOARD TAB ----------------$/ {
    print "        if (isSyncing) {"
    print "            Box("
    print "                modifier = Modifier"
    print "                    .fillMaxSize()"
    print "                    .background(Color.Black.copy(alpha = 0.5f))"
    print "                    .clickable(enabled = false) {},"
    print "                contentAlignment = Alignment.Center"
    print "            ) {"
    print "                Column("
    print "                    horizontalAlignment = Alignment.CenterHorizontally,"
    print "                    verticalArrangement = Arrangement.spacedBy(16.dp),"
    print "                    modifier = Modifier"
    print "                        .background(if (isDarkTheme) Color(0xFF1E2235) else Color.White, RoundedCornerShape(16.dp))"
    print "                        .padding(32.dp)"
    print "                ) {"
    print "                    CircularProgressIndicator(color = FintechBlue)"
    print "                    Text("
    print "                        text = if (language == AppLanguage.BN) \"দয়া করে অপেক্ষা করুন...\" else \"Please wait...\","
    print "                        color = if (isDarkTheme) Color.White else Color(0xFF1E293B),"
    print "                        fontWeight = FontWeight.Medium"
    print "                    )"
    print "                }"
    print "            }"
    print "        }"
    print "    }"
    print "}"
}
{ print }
' app/src/main/java/com/example/ui/screens/SanchayApp.kt > app/src/main/java/com/example/ui/screens/SanchayApp.kt.tmp
mv app/src/main/java/com/example/ui/screens/SanchayApp.kt.tmp app/src/main/java/com/example/ui/screens/SanchayApp.kt
