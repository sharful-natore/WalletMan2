package com.example.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.zip.ZipInputStream
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

// Model URL on alphacephei (approx. 33MB)
private const val VOSK_BN_MODEL_URL = "https://alphacephei.com/vosk/models/vosk-model-small-bn-0.3.zip"

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
        } catch (e: Throwable) {
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

fun isInternetConnected(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    } else {
        @Suppress("DEPRECATION")
        val activeNetworkInfo = cm.activeNetworkInfo
        @Suppress("DEPRECATION")
        activeNetworkInfo != null && activeNetworkInfo.isConnected
    }
}

fun isVoskDownloaded(context: Context): Boolean {
    val modelDir = File(context.filesDir, "sherpa-model-bn")
    return SherpaModelFinder.findModelRoot(modelDir) != null
}

fun shouldShowOfflineVoicePrompt(context: Context): Boolean {
    val prefs = context.getSharedPreferences("financenote_prefs", Context.MODE_PRIVATE)
    val alreadyConfigured = prefs.getBoolean("offline_voice_pack_configured", false)
    if (alreadyConfigured) return false
    if (isVoskDownloaded(context)) return false
    
    val ignoreUntil = prefs.getLong("offline_voice_ignore_until", 0L)
    if (System.currentTimeMillis() < ignoreUntil) return false
    
    return isInternetConnected(context)
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

suspend fun fetchVoskLink(context: Context): String = suspendCancellableCoroutine { continuation ->
    try {
        com.example.FinanceApplication.ensureFirebaseInitialized(context)
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(0)
            .build()
        remoteConfig.setConfigSettingsAsync(configSettings)
        
        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val link = remoteConfig.getString("Vosk_link")
                continuation.resume(link)
            } else {
                continuation.resume("")
            }
        }.addOnFailureListener { exception ->
            continuation.resume("")
        }
    } catch (e: Throwable) {
        continuation.resume("")
    }
}

sealed class DownloadState {
    object Idle : DownloadState()
    data class Downloading(val progress: Int) : DownloadState()
    object Extracting : DownloadState()
    object Success : DownloadState()
    data class Error(val message: String) : DownloadState()
}

object VoskDownloader {
    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState: StateFlow<DownloadState> = _downloadState

    private var downloadJob: Job? = null

    fun startDownload(context: Context, coroutineScope: CoroutineScope) {
        if (_downloadState.value is DownloadState.Downloading || _downloadState.value is DownloadState.Extracting) return

        downloadJob = coroutineScope.launch(Dispatchers.IO) {
            var lastException: Throwable? = null
            var success = false

            try {
                _downloadState.value = DownloadState.Downloading(0)
                
                // Fetch download link from Remote Config
                var downloadUrl = fetchVoskLink(context).trim()
                if (downloadUrl.isEmpty()) {
                    throw Exception("রিমোট কনফিগ থেকে Vosk_link পাওয়া যায়নি!")
                }

                // Handle Dropbox shared links conversion to direct download links
                if (downloadUrl.contains("dropbox.com")) {
                    if (downloadUrl.contains("dl=0")) {
                        downloadUrl = downloadUrl.replace("dl=0", "dl=1")
                    } else if (!downloadUrl.contains("dl=1")) {
                        downloadUrl = if (downloadUrl.contains("?")) {
                            "$downloadUrl&dl=1"
                        } else {
                            "$downloadUrl?dl=1"
                        }
                    }
                }

                var url = URL(downloadUrl)
                var connection = url.openConnection() as java.net.HttpURLConnection
                connection.connectTimeout = 30000
                connection.readTimeout = 30000
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Safari/537.36")
                connection.instanceFollowRedirects = true
                
                var status = connection.responseCode
                
                // Handle redirects (critical for Dropbox links)
                var redirectCount = 0
                while ((status == java.net.HttpURLConnection.HTTP_MOVED_TEMP || 
                        status == java.net.HttpURLConnection.HTTP_MOVED_PERM || 
                        status == 307 || status == 308) && redirectCount < 5) {
                    val newUrl = connection.getHeaderField("Location") ?: break
                    connection.disconnect()
                    url = URL(url, newUrl)
                    connection = url.openConnection() as java.net.HttpURLConnection
                    connection.connectTimeout = 30000
                    connection.readTimeout = 30000
                    connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Safari/537.36")
                    connection.instanceFollowRedirects = true
                    status = connection.responseCode
                    redirectCount++
                }

                if (status != java.net.HttpURLConnection.HTTP_OK) {
                    throw java.io.IOException("Server returned HTTP response code: $status")
                }

                val fileLength = connection.contentLength
                val input = BufferedInputStream(connection.getInputStream())
                val zipFile = File(context.cacheDir, "sherpa_model_bn.zip")
                val output = FileOutputStream(zipFile)

                val data = ByteArray(16384)
                var total: Long = 0
                var count: Int
                while (input.read(data).also { count = it } != -1) {
                    total += count
                    if (fileLength > 0) {
                        _downloadState.value = DownloadState.Downloading(((total * 100) / fileLength).toInt())
                    } else {
                        // Estimate progress if fileLength is not provided (assume ~83MB size)
                        val mb = (total / (1024 * 1024)).toInt()
                        val progress = if (mb < 83) (mb * 100 / 83) else 99
                        _downloadState.value = DownloadState.Downloading(progress)
                    }
                    output.write(data, 0, count)
                }
                output.flush()
                output.close()
                input.close()
                connection.disconnect()

                _downloadState.value = DownloadState.Extracting
                unzipModel(zipFile, context)
                
                if (zipFile.exists()) {
                    zipFile.delete()
                }

                _downloadState.value = DownloadState.Success
                context.getSharedPreferences("financenote_prefs", Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean("offline_voice_pack_configured", true)
                    .apply()
                
                success = true

            } catch (e: Throwable) {
                lastException = e
            }

            if (!success) {
                val errorMsg = lastException?.localizedMessage ?: "ডাউনলোড করতে সমস্যা হয়েছে। দয়া করে ইন্টারনেট চেক করুন।"
                _downloadState.value = DownloadState.Error(errorMsg)
            }
        }
    }

    private fun unzipModel(zipFile: File, context: Context) {
        val targetDir = File(context.filesDir, "sherpa-model-bn")
        if (targetDir.exists()) {
            targetDir.deleteRecursively()
        }
        targetDir.mkdirs()

        ZipInputStream(BufferedInputStream(zipFile.inputStream())).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                val file = File(targetDir, entry.name)
                val canonicalPath = file.canonicalPath
                if (!canonicalPath.startsWith(targetDir.canonicalPath)) {
                    throw SecurityException("Malicious zip entry: ${entry.name}")
                }

                if (entry.isDirectory) {
                    file.mkdirs()
                } else {
                    file.parentFile?.mkdirs()
                    FileOutputStream(file).use { fos ->
                        val buffer = ByteArray(16384)
                        var len: Int
                        while (zis.read(buffer).also { len = it } != -1) {
                            fos.write(buffer, 0, len)
                        }
                    }
                }
                entry = zis.nextEntry
            }
        }

        // Search recursively for model folders and flatten them into the targetDir root
        findAndFlattenModel(targetDir)
    }

    private fun findAndFlattenModel(targetDir: File) {
        val modelRoot = findModelRoot(targetDir) ?: return
        
        if (modelRoot.absolutePath != targetDir.absolutePath) {
            val contents = modelRoot.listFiles()
            if (contents != null) {
                for (file in contents) {
                    val dest = File(targetDir, file.name)
                    if (dest.exists()) {
                        dest.deleteRecursively()
                    }
                    file.renameTo(dest)
                }
            }
            
            // Clean up empty folders recursively
            deleteEmptyDirectories(targetDir)
        }
    }

    private fun findModelRoot(dir: File): File? {
        if (!dir.exists() || !dir.isDirectory) return null
        
        val files = dir.listFiles() ?: return null
        
        val hasAm = files.any { it.name == "tokens.txt" }
        val hasLang = files.any { it.name.endsWith(".onnx") }
        
        if (hasAm || hasLang) {
            return dir
        }
        
        for (file in files) {
            if (file.isDirectory) {
                val found = findModelRoot(file)
                if (found != null) {
                    return found
                }
            }
        }
        
        return null
    }

    private fun deleteEmptyDirectories(dir: File) {
        val files = dir.listFiles() ?: return
        for (file in files) {
            if (file.isDirectory) {
                if (file.name == "am-onnx" || file.name == "am" || file.name == "lang") {
                    continue
                }
                deleteEmptyDirectories(file)
                if (file.listFiles()?.isEmpty() == true) {
                    file.delete()
                }
            }
        }
    }
    
    fun reset() {
        downloadJob?.cancel()
        _downloadState.value = DownloadState.Idle
    }
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
                    text = if (isBn) "অফলাইন ভয়েস টাইপিং!" else "Offline Voice Typing!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF92400E)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isBn) 
                        "সম্পূর্ণ অফলাইনে ইন্টারনেট ছাড়া ভয়েস ইনপুট পেতে বাংলা অফলাইন মডেলটি এখনই ডাউনলোড করুন (৮৩ এমবি)।" 
                    else 
                        "Download Bengali offline speech recognition model (83 MB) to type by voice fully offline.",
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
                            text = if (isBn) "ডাউনলোড ও সেটআপ" else "Download & Setup",
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
    val coroutineScope = rememberCoroutineScope()
    val downloadState by VoskDownloader.downloadState.collectAsState()

    DisposableEffect(Unit) {
        onDispose {
            if (downloadState is DownloadState.Success || downloadState is DownloadState.Error) {
                VoskDownloader.reset()
            }
        }
    }

    AlertDialog(
        onDismissRequest = {
            if (downloadState !is DownloadState.Downloading && downloadState !is DownloadState.Extracting) {
                onDismiss()
            }
        },
        icon = {
            Icon(
                imageVector = if (downloadState is DownloadState.Success) Icons.Rounded.CheckCircle else Icons.Rounded.Warning,
                contentDescription = null,
                tint = if (downloadState is DownloadState.Success) Color(0xFF10B981) else Color(0xFFD97706),
                modifier = Modifier.size(40.dp)
            )
        },
        title = {
            Text(
                text = if (isBn) "বাংলা অফলাইন ভয়েস টাইপিং" else "Bangla Offline Speech Model",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF1E293B)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (val state = downloadState) {
                    is DownloadState.Idle -> {
                        Text(
                            text = if (isBn) 
                                "ইন্টারনেট বা ওয়াইফাই সংযোগ ছাড়া অফলাইনে মুখে বলে লেনদেন খসড়া করার জন্য 'Vosk Small Bangla Model' নামিয়ে নিন।" 
                            else 
                                "To use voice-to-text fully offline without internet connection, download the 'Vosk Small Bangla Model'.",
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
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = if (isBn) "ফিচারের সুবিধা সমূহ:" else "Feature Benefits:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF92400E)
                               )
                                Text(
                                    text = if (isBn)
                                        "• সম্পূর্ণ অফলাইন - কোন ইন্টারনেট লাগবেনা।\n" +
                                        "• ৮৩ এমবি সাইজ - ওয়াইফাই বা সেলুলার ডাটা দিয়ে সহজে ডাউনলোড।\n" +
                                        "• সরাসরি অ্যাপের ভেতর সুরক্ষিত ও দ্রুত ডাউনলোড সম্পন্ন হবে।\n" +
                                        "• ডাটা সাশ্রয়ী - আপনার কোন ভয়েস ডেটা ইন্টারনেটে যাবে না।"
                                    else
                                        "• 100% Offline - No internet connection required.\n" +
                                        "• ~83 MB Size - Easy download via Wi-Fi or Mobile Data.\n" +
                                        "• Directly downloaded and securely stored inside the app.\n" +
                                        "• Complete Privacy - Your voice data stays on-device.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF78350F),
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                    is DownloadState.Downloading -> {
                        Text(
                            text = if (isBn) "ভয়েস মডেল ডাউনলোড হচ্ছে..." else "Downloading Voice Model...",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Color(0xFF1E293B)
                        )
                        LinearProgressIndicator(
                            progress = { state.progress / 100f },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = Color(0xFFD97706),
                            trackColor = Color(0xFFFEF3C7)
                        )
                        Text(
                            text = if (isBn) "অগ্রগতি: ${state.progress}% (৮৩ এমবি)" else "Progress: ${state.progress}% of ~83MB",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    is DownloadState.Extracting -> {
                        Text(
                            text = if (isBn) "ফাইল আনজিপ ও সেটআপ করা হচ্ছে..." else "Extracting and configuring files...",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Color(0xFF1E293B)
                        )
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = Color(0xFFD97706),
                            trackColor = Color(0xFFFEF3C7)
                        )
                        Text(
                            text = if (isBn) "অনুগ্রহ করে ১ মিনিট অপেক্ষা করুন..." else "Please wait a moment...",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                    is DownloadState.Success -> {
                        Text(
                            text = if (isBn) 
                                "অভিনন্দন! বাংলা অফলাইন ভয়েস টাইপিং সফলভাবে ইনস্টল হয়েছে। এখন অফলাইনে থাকলেও দুর্দান্ত স্পিডে মুখে বলে খসড়া করতে পারবেন!" 
                            else 
                                "Success! Bangla offline speech-to-text is fully configured. Voice inputs will now work seamlessly offline!",
                            fontSize = 13.sp,
                            color = Color(0xFF0F766E),
                            lineHeight = 18.sp
                        )
                    }
                    is DownloadState.Error -> {
                        Text(
                            text = if (isBn) "ডাউনলোড ব্যর্থ হয়েছে!" else "Download Failed!",
                            fontWeight = FontWeight.Bold,
                            color = Color.Red,
                            fontSize = 14.sp
                        )
                        Text(
                            text = state.message,
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }
        },
        confirmButton = {
            when (downloadState) {
                is DownloadState.Idle -> {
                    Button(
                        onClick = {
                            VoskDownloader.startDownload(context, coroutineScope)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
                    ) {
                        Icon(imageVector = Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isBn) "ডাউনলোড শুরু করুন" else "Start Download",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                is DownloadState.Success -> {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        Text(text = if (isBn) "শুরু করুন" else "Get Started", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
                is DownloadState.Error -> {
                    Button(
                        onClick = {
                            VoskDownloader.reset()
                            VoskDownloader.startDownload(context, coroutineScope)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
                    ) {
                        Text(text = if (isBn) "আবার চেষ্টা করুন" else "Retry", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
                else -> { /* In-progress: Disable button actions */ }
            }
        },
        dismissButton = {
            if (downloadState !is DownloadState.Downloading && downloadState !is DownloadState.Extracting) {
                TextButton(onClick = onDismiss) {
                    Text(if (isBn) "বন্ধ করুন" else "Close", color = Color.Gray)
                }
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp)
    )
}

/**
 * Highly interactive, fully offline speech input overlay using Vosk
 */
@Composable
fun VoskSpeechInputDialog(
    isBn: Boolean,
    onResult: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    var partialText by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }
    var isInitializing by remember { mutableStateOf(true) }
    var statusMessage by remember { mutableStateOf(if (isBn) "অফলাইন মডেল লোড হচ্ছে..." else "Loading offline model...") }
    
    var recognizer by remember { mutableStateOf<com.k2fsa.sherpa.onnx.OnlineRecognizer?>(null) }
    var audioRecord by remember { mutableStateOf<android.media.AudioRecord?>(null) }

    val infiniteTransition = rememberInfiniteTransition()
    val micScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.25f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasAudioPermission = granted
        if (!granted) {
            Toast.makeText(context, if (isBn) "ভয়েস রেকর্ডের পারমিশন প্রয়োজন।" else "Microphone permission is required.", Toast.LENGTH_SHORT).show()
            onDismiss()
        }
    }

    LaunchedEffect(hasAudioPermission) {
        if (!hasAudioPermission) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    LaunchedEffect(hasAudioPermission) {
        if (hasAudioPermission) {
            withContext(Dispatchers.IO) {
                try {
                    val rootDir = File(context.filesDir, "sherpa-model-bn")
                    val modelDir = SherpaModelFinder.findModelRoot(rootDir)
                    if (modelDir != null) {
                        val config = SherpaModelFinder.buildConfig(modelDir)
                        recognizer = com.k2fsa.sherpa.onnx.OnlineRecognizer(config = config)
                        isInitializing = false
                        statusMessage = if (isBn) "বলুন, আমি শুনছি..." else "Listening..."
                        isListening = true
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, if (isBn) "ভয়েস মডেল খুঁজে পাওয়া যায়নি!" else "Model files not found!", Toast.LENGTH_LONG).show()
                            onDismiss()
                        }
                    }
                } catch (e: Throwable) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Sherpa Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                        onDismiss()
                    }
                }
            }
        }
    }

    LaunchedEffect(recognizer, isListening) {
        val rec = recognizer
        if (rec != null && isListening) {
            withContext(Dispatchers.IO) {
                try {
                    val sampleRateInHz = 16000
                    val channelConfig = android.media.AudioFormat.CHANNEL_IN_MONO
                    val audioFormat = android.media.AudioFormat.ENCODING_PCM_16BIT
                                        val numBytes = android.media.AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat)
                    if (numBytes <= 0) throw Exception("AudioRecord unsupported")
                    
                    val record = android.media.AudioRecord(
                        android.media.MediaRecorder.AudioSource.MIC,
                        sampleRateInHz,
                        channelConfig,
                        audioFormat,
                        numBytes * 2
                    )
                    if (record.state != android.media.AudioRecord.STATE_INITIALIZED) {
                        throw Exception("AudioRecord failed to initialize")
                    }
                    audioRecord = record
                    
                    record.startRecording()
                    
                    val stream = rec.createStream()
                    val interval = 0.1
                    val bufferSize = (interval * sampleRateInHz).toInt()
                    val buffer = ShortArray(bufferSize)
                    
                    while (isListening) {
                        val ret = record.read(buffer, 0, buffer.size)
                        if (ret > 0) {
                            val samples = FloatArray(ret) { buffer[it] / 32768.0f }
                            stream.acceptWaveform(samples, sampleRate = sampleRateInHz)
                            
                            while (rec.isReady(stream)) {
                                rec.decode(stream)
                            }
                            
                            val isEndpoint = rec.isEndpoint(stream)
                            var text = rec.getResult(stream).text
                            
                            if (isEndpoint && rec.config.modelConfig.paraformer.encoder.isNotBlank()) {
                                val tailPaddings = FloatArray((0.8 * sampleRateInHz).toInt())
                                stream.acceptWaveform(tailPaddings, sampleRate = sampleRateInHz)
                                while (rec.isReady(stream)) {
                                    rec.decode(stream)
                                }
                                text = rec.getResult(stream).text
                            }
                            
                            withContext(Dispatchers.Main) {
                                if (text.isNotBlank()) {
                                    if (isEndpoint) {
                                        resultText = if (resultText.isBlank()) text else "$resultText $text"
                                        partialText = ""
                                        rec.reset(stream)
                                    } else {
                                        partialText = text
                                    }
                                }
                            }
                        }
                    }
                    
                    record.stop()
                    record.release()
                    stream.release()
                    
                } catch (e: Throwable) {
                    isListening = false
                }
            }
        } else {
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            isListening = false
            audioRecord?.stop()
            audioRecord?.release()
        }
    }

    LaunchedEffect(resultText, partialText) {
        val currentText = if (partialText.isNotBlank()) "$resultText $partialText" else resultText
        if (currentText.isNotBlank()) {
            val lower = currentText.trim().lowercase()
            val saveKeywords = listOf("ওকে", "ok", "okay", "সেভ করো", "সেভ করুন", "সেভ কর", "সেভ", "সংরক্ষণ", "save it", "save")
            for (kw in saveKeywords) {
                if (lower.endsWith(kw)) {
                    val cleanText = currentText.trim().substring(0, currentText.trim().length - kw.length).trim()
                    onResult(cleanText)
                    Toast.makeText(context, if (isBn) "ভয়েস সেভ সম্পন্ন হয়েছে!" else "Voice draft saved!", Toast.LENGTH_SHORT).show()
                    onDismiss()
                    break
                }
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isBn) "অফলাইন বাংলা ভয়েস টাইপিং" else "Offline Voice Typing",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD97706)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color.Gray.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(imageVector = Icons.Rounded.Close, contentDescription = "Close", modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Pulsing Mic Icon / Loader
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(micScale)
                        .background(
                            if (isListening) Color(0xFFD97706).copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.1f),
                            CircleShape
                        )
                        .clickable(enabled = !isInitializing) {
                            isListening = !isListening
                            statusMessage = if (isListening) {
                                if (isBn) "বলুন, আমি শুনছি..." else "Listening..."
                            } else {
                                if (isBn) "রেকর্ডিং বন্ধ আছে" else "Recording paused"
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                if (isListening) Color(0xFFD97706) else Color.Gray,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isInitializing) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(36.dp), strokeWidth = 3.dp)
                        } else {
                            Icon(
                                imageVector = if (isListening) Icons.Rounded.Mic else Icons.Rounded.PlayArrow,
                                contentDescription = "Mic",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }

                Text(
                    text = statusMessage,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isListening) Color(0xFFD97706) else Color.Gray
                )

                // Live Text Preview Area
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp, max = 200.dp)
                        .clickable { /* prevent bubble clicks */ },
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        val currentText = if (partialText.isNotBlank()) "$resultText $partialText" else resultText
                        
                        if (currentText.isBlank()) {
                            Text(
                                text = if (isBn) 
                                    "যেমন বলুন: '৫০০ টাকা মোবাইল রিচার্জ কর ওক'\n\n(লেখার শেষে 'ওকে' বা 'সেভ করুন' বললে স্বয়ংক্রিয়ভাবে সেভ হবে)" 
                                else 
                                    "Say something like: 'Spent 500 taka on fuel save'\n\n(Say 'save' or 'okay' at the end to auto-save)",
                                fontSize = 13.sp,
                                color = Color.LightGray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        } else {
                            Text(
                                text = currentText,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1E293B),
                                textAlign = TextAlign.Left,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Bottom Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            resultText = ""
                            partialText = ""
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.5f))
                    ) {
                        Text(text = if (isBn) "মুছে ফেলুন" else "Clear", color = Color.Gray)
                    }

                    Button(
                        onClick = {
                            val finalSpeech = if (partialText.isNotBlank()) "$resultText $partialText" else resultText
                            if (finalSpeech.isNotBlank()) {
                                onResult(finalSpeech.trim())
                                onDismiss()
                            } else {
                                Toast.makeText(context, if (isBn) "কোন শব্দ রেকর্ড করা হয়নি!" else "No word recorded!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
                    ) {
                        Text(text = if (isBn) "সম্পন্ন করুন" else "Confirm", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
