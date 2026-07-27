package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

fun openOfflineVoiceSettings(context: Context) {
    val intents = listOf(
        Intent("com.google.android.voicesearch.SETTINGS").apply {
            setPackage("com.google.android.googlequicksearchbox")
        },
        Intent(android.provider.Settings.ACTION_VOICE_INPUT_SETTINGS),
        Intent("android.speech.action.CONFIGURE_SPEECH_RECOGNIZER"),
        Intent(android.provider.Settings.ACTION_INPUT_METHOD_SETTINGS),
        Intent(android.provider.Settings.ACTION_SETTINGS)
    )
    var opened = false
    for (intent in intents) {
        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            opened = true
            break
        } catch (e: Exception) {
            // continue
        }
    }
    if (!opened) {
        Toast.makeText(context, "সেটিংস খোলা যায়নি। ফোনে Language & Input -> Voice Typing থেকে অফলাইন বাংলা ইনস্টল করুন।", Toast.LENGTH_LONG).show()
    }
}

fun isWifiConnected(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    } else {
        @Suppress("DEPRECATION")
        val activeNetworkInfo = cm.activeNetworkInfo
        @Suppress("DEPRECATION")
        activeNetworkInfo != null && activeNetworkInfo.isConnected && activeNetworkInfo.type == ConnectivityManager.TYPE_WIFI
    }
}

fun shouldShowWifiVoicePrompt(context: Context): Boolean {
    val prefs = context.getSharedPreferences("financenote_prefs", Context.MODE_PRIVATE)
    val alreadyConfigured = prefs.getBoolean("offline_voice_pack_configured", false)
    if (alreadyConfigured) return false
    
    val ignoreUntil = prefs.getLong("offline_voice_ignore_until", 0L)
    if (System.currentTimeMillis() < ignoreUntil) return false
    
    return isWifiConnected(context)
}

fun markVoicePackConfigured(context: Context, configured: Boolean) {
    val prefs = context.getSharedPreferences("financenote_prefs", Context.MODE_PRIVATE)
    prefs.edit().putBoolean("offline_voice_pack_configured", configured).apply()
}

fun snoozeVoicePackPrompt(context: Context) {
    val prefs = context.getSharedPreferences("financenote_prefs", Context.MODE_PRIVATE)
    // Snooze for 3 days
    val snoozeTime = System.currentTimeMillis() + (3 * 24 * 60 * 60 * 1000L)
    prefs.edit().putLong("offline_voice_ignore_until", snoozeTime).apply()
}

@Composable
fun OfflineVoiceWifiBanner(
    isBn: Boolean,
    onSetupClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFFEF3C7),
        border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Warning,
                contentDescription = null,
                tint = Color(0xFFD97706),
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isBn) "ওয়াইফাই সংযুক্ত আছে!" else "Wi-Fi Connected!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF92400E)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isBn) 
                        "অফলাইনেও ইন্টারনেট ছাড়া ভয়েস ইনপুট পেতে বাংলা ভয়েস প্যাক ডাউনলোড করুন।" 
                    else 
                        "Download Bengali Voice Pack for offline speech recognition.",
                    fontSize = 12.sp,
                    color = Color(0xFF78350F),
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onSetupClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(
                            text = if (isBn) "এখনই সেটআপ" else "Setup Now",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    TextButton(
                        onClick = onDismiss,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(
                            text = if (isBn) "পরে জানান" else "Later",
                            fontSize = 11.sp,
                            color = Color(0xFF92400E)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OfflineVoiceGuideDialog(
    isBn: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Rounded.Warning,
                contentDescription = null,
                tint = Color(0xFFD97706),
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text(
                text = if (isBn) "অফলাইন ভয়েস ইনপুট গাইড" else "Offline Voice Setup Guide",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF1E293B)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (isBn) 
                        "ইন্টারনেট ছাড়া অফলাইনে মুখে বলে লেনদেন রেকর্ড করতে আপনার ফোনে গুগলের বাংলা (বাংলাদেশ) অফলাইন স্পিচ ভয়েস প্যাক ইনস্টল করা থাকতে হবে।" 
                    else 
                        "To save drafts by voice offline without internet, you need Google's Offline Bangla/English Voice Pack installed on your device.",
                    fontSize = 13.sp,
                    color = Color(0xFF334155),
                    lineHeight = 18.sp
                )
                
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFEF3C7),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (isBn) "সহজ ৪টি স্টেপে সেটআপ করুন:" else "Setup Steps:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF92400E)
                        )
                        Text(
                            text = if (isBn)
                                "১. নিচের 'গুগল স্পিচ সেটিংস খুলুন' বাটনে ক্লিক করুন।\n" +
                                "২. 'Offline speech recognition' অপশনে যান।\n" +
                                "৩. 'ALL' ট্যাব বেছে নিয়ে 'বাংলা (বাংলাদেশ)' বা 'Bengali (Bangladesh)' ডাউনলোড দিন।\n" +
                                "৪. ডাউনলোড হয়ে গেলে ইন্টারнеটের সংযোগ ছাড়াই ভয়েসে সেভ করতে পারবেন!"
                            else
                                "1. Click 'Open Voice Settings' below.\n" +
                                "2. Go to 'Offline speech recognition'.\n" +
                                "3. Go to 'ALL' tab and download 'Bengali (Bangladesh)'.\n" +
                                "4. Once downloaded, voice recognition will work offline seamlessly!",
                            fontSize = 12.sp,
                            color = Color(0xFF78350F),
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    openOfflineVoiceSettings(context)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
            ) {
                Text(
                    text = if (isBn) "গুগল স্পিচ সেটিংস খুলুন" else "Open Voice Settings",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBn) "বন্ধ করুন" else "Close", color = Color.Gray)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp)
    )
}
