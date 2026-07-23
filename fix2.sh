sed -i '4998,5032c\
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
                            ) {' app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt
