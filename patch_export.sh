# We will just write a new compact export menu card above the budget section
# and delete the old one.

# Delete the old export menu.
# The old export menu is from line 7726 to 7819. We can just delete it.
sed -i '7726,7819d' app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt

# Insert the new export menu above the budget section (line 7545)
sed -i '7545i\
            item {\
                Card(\
                    shape = RoundedCornerShape(16.dp),\
                    colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E293B) else Color.White),\
                    modifier = Modifier\
                        .fillMaxWidth()\
                        .clickable { onExportRequest?.invoke("ALL_DATA") }\
                        .testTag("dashboard_export_menu_button")\
                ) {\
                    Row(\
                        modifier = Modifier\
                            .fillMaxWidth()\
                            .padding(horizontal = 16.dp, vertical = 12.dp),\
                        verticalAlignment = Alignment.CenterVertically,\
                        horizontalArrangement = Arrangement.SpaceBetween\
                    ) {\
                        Row(\
                            verticalAlignment = Alignment.CenterVertically,\
                            horizontalArrangement = Arrangement.spacedBy(12.dp)\
                        ) {\
                            Icon(\
                                imageVector = androidx.compose.material.icons.Icons.Rounded.Download,\
                                contentDescription = null,\
                                tint = FintechBlue,\
                                modifier = Modifier.size(24.dp)\
                            )\
                            Text(\
                                text = if (language == AppLanguage.BN) "রিপোর্ট এক্সপোর্ট করুন" else "Export Report",\
                                fontSize = 14.sp,\
                                fontWeight = FontWeight.Bold,\
                                color = if (isDark) Color.White else Color(0xFF1E293B)\
                            )\
                        }\
                        Icon(\
                            imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Rounded.ArrowForward,\
                            contentDescription = null,\
                            tint = FintechBlue,\
                            modifier = Modifier.size(20.dp)\
                        )\
                    }\
                }\
                Spacer(modifier = Modifier.height(12.dp))\
            }\
' app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt
