sed -i '15095i\
                // Action Confirmation with Fingerprint Toggle\
                androidx.compose.animation.AnimatedVisibility(visible = isBiometricEnabled) {\
                    Row(\
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),\
                        verticalAlignment = Alignment.CenterVertically,\
                        horizontalArrangement = Arrangement.spacedBy(16.dp)\
                    ) {\
                        Box(\
                            modifier = Modifier\
                                .size(40.dp)\
                                .background(Color(0xFF3B82F6).copy(alpha = 0.12f), CircleShape),\
                            contentAlignment = Alignment.Center\
                        ) {\
                            Icon(\
                                imageVector = Icons.Rounded.VerifiedUser,\
                                contentDescription = null,\
                                tint = Color(0xFF3B82F6),\
                                modifier = Modifier.size(22.dp)\
                            )\
                        }\
                        Column(modifier = Modifier.weight(1f)) {\
                            Text(\
                                text = if (language == AppLanguage.BN) "একশন কনফার্মেশন" else "Action Confirmation",\
                                fontSize = 16.sp,\
                                fontWeight = FontWeight.SemiBold,\
                                color = if (isDark) Color.White else Color(0xFF1E293B)\
                            )\
                            Text(\
                                text = if (language == AppLanguage.BN) "ডিলিট এর মতো স্পর্শকাতর কাজে ফিঙ্গারপ্রিন্ট ব্যবহার করুন" else "Use fingerprint for sensitive actions like deleting",\
                                fontSize = 12.sp,\
                                color = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Gray\
                            )\
                        }\
                        Switch(\
                            checked = isBiometricActionEnabled,\
                            onCheckedChange = { enable ->\
                                viewModel.setBiometricActionEnabled(context, enable)\
                            },\
                            colors = SwitchDefaults.colors(\
                                checkedThumbColor = Color.White,\
                                checkedTrackColor = Color(0xFF10B981),\
                                uncheckedThumbColor = if (isDark) Color.Gray else Color.White,\
                                uncheckedTrackColor = if (isDark) Color(0xFF2A2E42) else Color(0xFFE2E8F0)\
                            )\
                        )\
                    }\
                }\
                androidx.compose.animation.AnimatedVisibility(visible = isBiometricEnabled) {\
                    HorizontalDivider(\
                        modifier = Modifier.padding(vertical = 4.dp),\
                        color = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)\
                    )\
                }' app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt
