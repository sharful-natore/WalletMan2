sed -i '4935,5144c\
                        // Unsaved Changes Row\
                        if (hasUnsavedChanges) {\
                            val unsyncedItems = viewModel.getUnsyncedItems()\
                            Column(\
                                modifier = Modifier.fillMaxWidth(),\
                                verticalArrangement = Arrangement.spacedBy(4.dp)\
                            ) {\
                                Text(\
                                    text = if (language == AppLanguage.BN) "অসংরক্ষিত ডাটা:" else "Unsaved Data:",\
                                    fontWeight = FontWeight.SemiBold,\
                                    fontSize = 14.sp,\
                                    color = if (isDarkTheme) Color.LightGray else Color(0xFF475569)\
                                )\
                                Text(\
                                    text = unsyncedItems.joinToString(", "),\
                                    fontSize = 13.sp,\
                                    color = Color(0xFFFFB300),\
                                    fontWeight = FontWeight.Bold\
                                )\
                            }\
                        } else {\
                            Row(\
                                modifier = Modifier.fillMaxWidth(),\
                                verticalAlignment = Alignment.CenterVertically,\
                                horizontalArrangement = Arrangement.SpaceBetween\
                            ) {\
                                Text(\
                                    text = if (language == AppLanguage.BN) "সব সংরক্ষিত:" else "All Saved:",\
                                    fontWeight = FontWeight.SemiBold,\
                                    fontSize = 14.sp,\
                                    color = if (isDarkTheme) Color.LightGray else Color(0xFF475569)\
                                )\
                                Text(\
                                    text = if (language == AppLanguage.BN) "হ্যাঁ (সব সংরক্ষিত)" else "Yes (Fully saved)",\
                                    fontSize = 14.sp,\
                                    fontWeight = FontWeight.Bold,\
                                    color = Color(0xFF4CAF50)\
                                )\
                            }\
                        }\
\
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(if (isDarkTheme) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)))\
\
                        // Last Sync Time Row\
                        Row(\
                            modifier = Modifier.fillMaxWidth(),\
                            verticalAlignment = Alignment.CenterVertically,\
                            horizontalArrangement = Arrangement.SpaceBetween\
                        ) {\
                            Text(\
                                text = if (language == AppLanguage.BN) "সর্বশেষ সিঙ্ক:" else "Last Synced:",\
                                fontWeight = FontWeight.SemiBold,\
                                fontSize = 14.sp,\
                                color = if (isDarkTheme) Color.LightGray else Color(0xFF475569)\
                            )\
                            Text(\
                                text = formatSyncTime(lastSyncTime, language),\
                                fontSize = 14.sp,\
                                fontWeight = FontWeight.Bold,\
                                color = if (isDarkTheme) Color.White else Color(0xFF1E293B)\
                            )\
                        }\
\
                        if (lastMutationAction != null) {\
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(if (isDarkTheme) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)))\
\
                            Column(\
                                modifier = Modifier\
                                    .fillMaxWidth()\
                                    .clip(RoundedCornerShape(8.dp))\
                                    .background(if (isDarkTheme) Color.White.copy(alpha = 0.03f) else Color.Black.copy(alpha = 0.02f))\
                                    .padding(10.dp),\
                                verticalArrangement = Arrangement.spacedBy(4.dp)\
                            ) {\
                                Text(\
                                    text = if (hasUnsavedChanges) {\
                                        if (language == AppLanguage.BN) "অসংরক্ষিত ডাটা (সিঙ্ক প্রয়োজন):" else "Unsaved Data (Sync Needed):"\
                                    } else {\
                                        if (language == AppLanguage.BN) "সর্বশেষ সিঙ্কড ডাটা:" else "Last Synced Data:"\
                                    },\
                                    fontSize = 13.sp,\
                                    fontWeight = FontWeight.Bold,\
                                    color = if (hasUnsavedChanges) Color(0xFFFFB300) else Color(0xFF4CAF50)\
                                )\
                                Spacer(modifier = Modifier.height(2.dp))\
                                val actionText = when (lastMutationAction) {\
                                    "ADD" -> if (language == AppLanguage.BN) "যোগ করা হয়েছে" else "Added"\
                                    "EDIT" -> if (language == AppLanguage.BN) "আপডেট করা হয়েছে" else "Updated"\
                                    "DELETE" -> if (language == AppLanguage.BN) "মুছে ফেলা হয়েছে" else "Deleted"\
                                    else -> lastMutationAction ?: ""\
                                }\
                                Row(\
                                    modifier = Modifier.fillMaxWidth(),\
                                    horizontalArrangement = Arrangement.SpaceBetween\
                                ) {\
                                    Text(\
                                        text = if (language == AppLanguage.BN) "কাজ:" else "Action:",\
                                        fontSize = 12.sp,\
                                        color = if (isDarkTheme) Color.LightGray else Color(0xFF475569)\
                                    )\
                                    Text(\
                                        text = actionText,\
                                        fontSize = 12.sp,\
                                        fontWeight = FontWeight.SemiBold,\
                                        color = if (isDarkTheme) Color.White else Color(0xFF1E293B)\
                                    )\
                                }\
                                Row(\
                                    modifier = Modifier.fillMaxWidth(),\
                                    horizontalArrangement = Arrangement.SpaceBetween\
                                ) {\
                                    Text(\
                                        text = if (language == AppLanguage.BN) "ব্যক্তির নাম:" else "Name:",\
                                        fontSize = 12.sp,\
                                        color = if (isDarkTheme) Color.LightGray else Color(0xFF475569)\
                                    )\
                                    Text(\
                                        text = lastMutationName ?: "-",\
                                        fontSize = 12.sp,\
                                        fontWeight = FontWeight.SemiBold,\
                                        color = if (isDarkTheme) Color.White else Color(0xFF1E293B)\
                                    )\
                                }\
                                Row(\
                                    modifier = Modifier.fillMaxWidth(),\
                                    horizontalArrangement = Arrangement.SpaceBetween\
                                ) {\
                                    Text(\
                                        text = if (language == AppLanguage.BN) "ধরণ:" else "Category:",\
                                        fontSize = 12.sp,\
                                        color = if (isDarkTheme) Color.LightGray else Color(0xFF475569)\
                                    )\
                                    val categoryDisplay = when (lastMutationCategory) {\
                                        "PERSON" -> if (language == AppLanguage.BN) "ব্যক্তি/লেনদেন" else "Person/Debt"\
                                        "SAVINGS_GOAL" -> if (language == AppLanguage.BN) "সঞ্চয় লক্ষ্য" else "Savings Goal"\
                                        "SAVINGS_CONTRIBUTION" -> if (language == AppLanguage.BN) "সঞ্চয় লেনদেন" else "Savings Transaction"\
                                        else -> {\
                                            val cat = lastMutationCategory ?: ""\
                                            if (language == AppLanguage.BN) {\
                                                cat.replace("EXPENSE", "ব্যয়").replace("INCOME", "আয়")\
                                            } else {\
                                                cat\
                                            }\
                                        }\
                                    }\
                                    Text(\
                                        text = categoryDisplay,\
                                        fontSize = 12.sp,\
                                        fontWeight = FontWeight.SemiBold,\
                                        color = if (isDarkTheme) Color.White else Color(0xFF1E293B)\
                                    )\
                                }\
                                if (lastMutationAmount != null && lastMutationAmount!! > 0.0) {\
                                    Row(\
                                        modifier = Modifier.fillMaxWidth(),\
                                        horizontalArrangement = Arrangement.SpaceBetween\
                                    ) {\
                                        Text(\
                                            text = if (language == AppLanguage.BN) "পরিমান:" else "Amount:",\
                                            fontSize = 12.sp,\
                                            color = if (isDarkTheme) Color.LightGray else Color(0xFF475569)\
                                        )\
                                        Text(\
                                            text = if (language == AppLanguage.BN) "৳${String.format("%,.2f", lastMutationAmount)}" else "$${String.format("%,.2f", lastMutationAmount)}",\
                                            fontSize = 12.sp,\
                                            fontWeight = FontWeight.SemiBold,\
                                            color = if (isDarkTheme) Color.White else Color(0xFF1E293B)\
                                        )\
                                    }\
                                }\
                            }\
                        } else {\
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(if (isDarkTheme) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)))\
                            Text(\
                                text = if (hasUnsavedChanges) {\
                                    if (language == AppLanguage.BN) "অসংরক্ষিত ডাটা আছে" else "Unsaved data present"\
                                } else {\
                                    if (language == AppLanguage.BN) "সব ডেটা সিঙ্ক হয়েছে" else "All data synced"\
                                },\
                                fontSize = 12.sp,\
                                fontStyle = FontStyle.Italic,\
                                color = if (isDarkTheme) Color.Gray else Color.DarkGray,\
                                modifier = Modifier.align(Alignment.CenterHorizontally)\
                            )\
                        }' app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt
