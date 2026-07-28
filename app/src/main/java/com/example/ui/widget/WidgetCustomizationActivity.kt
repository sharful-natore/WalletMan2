package com.example.ui.widget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import com.example.ui.theme.FinanceNoteTheme

class WidgetCustomizationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FinanceNoteTheme {
                WidgetCustomizationScreen(onBackPressed = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetCustomizationScreen(onBackPressed: () -> Unit) {
    val context = LocalContext.current
    var config by remember { mutableStateOf(WidgetConfigManager.loadConfig(context)) }
    var activeColorPicker by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Widget Customization") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            WidgetConfigManager.saveConfig(context, config)
                            // Notify widget to update
                            val intent = android.content.Intent(context, DraftWidgetProvider::class.java).apply {
                                action = "com.example.UPDATE_DRAFT_WIDGET"
                            }
                            context.sendBroadcast(intent)
                            onBackPressed()
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Live Preview
            WidgetPreview(config = config)

            Divider()

            // Settings List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Text("Visibility Settings", fontWeight = FontWeight.Bold, fontSize = 18.sp) }
                item {
                    VisibilityToggle("Title Visible", config.isTitleVisible) { 
                        config = config.copy(isTitleVisible = it) 
                    }
                }
                item {
                    VisibilityToggle("Subtitle Visible", config.isSubtitleVisible) { 
                        config = config.copy(isSubtitleVisible = it) 
                    }
                }
                item {
                    VisibilityToggle("List Visible", config.isListVisible) { 
                        config = config.copy(isListVisible = it) 
                    }
                }

                item { Text("Color Settings", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(top = 8.dp)) }
                
                item {
                    ColorSettingItem("Header Background", Color(config.titleSectionBg)) { 
                        activeColorPicker = "titleSectionBg" 
                    }
                }
                item {
                    ColorSettingItem("List Area Background", Color(config.listSectionBg)) { 
                        activeColorPicker = "listSectionBg" 
                    }
                }
                item {
                    ColorSettingItem("List Item Background", Color(config.listItemBg)) { 
                        activeColorPicker = "listItemBg" 
                    }
                }
                item {
                    ColorSettingItem("Title Text Color", Color(config.titleTextColor)) { 
                        activeColorPicker = "titleTextColor" 
                    }
                }
                item {
                    ColorSettingItem("Subtitle Text Color", Color(config.subtitleTextColor)) { 
                        activeColorPicker = "subtitleTextColor" 
                    }
                }
                item {
                    ColorSettingItem("List Item Text Color", Color(config.listItemTextColor)) { 
                        activeColorPicker = "listItemTextColor" 
                    }
                }
                item {
                    ColorSettingItem("Button Icon Tint", Color(config.buttonTintColor)) { 
                        activeColorPicker = "buttonTintColor" 
                    }
                }
                item {
                    ColorSettingItem("Button Background", Color(config.buttonBgColor)) { 
                        activeColorPicker = "buttonBgColor" 
                    }
                }
                item {
                    ColorSettingItem("Info Icon Color", Color(config.infoIconColor)) { 
                        activeColorPicker = "infoIconColor" 
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { config = WidgetDraftConfig() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reset to Default")
                    }
                }
                
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }

        if (activeColorPicker != null) {
            ColorPickerDialog(
                initialColor = when(activeColorPicker) {
                    "titleSectionBg" -> Color(config.titleSectionBg)
                    "listSectionBg" -> Color(config.listSectionBg)
                    "listItemBg" -> Color(config.listItemBg)
                    "titleTextColor" -> Color(config.titleTextColor)
                    "subtitleTextColor" -> Color(config.subtitleTextColor)
                    "listItemTextColor" -> Color(config.listItemTextColor)
                    "buttonTintColor" -> Color(config.buttonTintColor)
                    "buttonBgColor" -> Color(config.buttonBgColor)
                    "infoIconColor" -> Color(config.infoIconColor)
                    else -> Color.White
                },
                onDismiss = { activeColorPicker = null },
                onColorSelected = { selectedColor ->
                    config = when(activeColorPicker) {
                        "titleSectionBg" -> config.copy(titleSectionBg = selectedColor.toArgb())
                        "listSectionBg" -> config.copy(listSectionBg = selectedColor.toArgb())
                        "listItemBg" -> config.copy(listItemBg = selectedColor.toArgb())
                        "titleTextColor" -> config.copy(titleTextColor = selectedColor.toArgb())
                        "subtitleTextColor" -> config.copy(subtitleTextColor = selectedColor.toArgb())
                        "listItemTextColor" -> config.copy(listItemTextColor = selectedColor.toArgb())
                        "buttonTintColor" -> config.copy(buttonTintColor = selectedColor.toArgb())
                        "buttonBgColor" -> config.copy(buttonBgColor = selectedColor.toArgb())
                        "infoIconColor" -> config.copy(infoIconColor = selectedColor.toArgb())
                        else -> config
                    }
                    activeColorPicker = null
                }
            )
        }
    }
}

@Composable
fun VisibilityToggle(label: String, isVisible: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label)
        Switch(checked = isVisible, onCheckedChange = onToggle)
    }
}

@Composable
fun ColorSettingItem(label: String, currentColor: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label)
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(currentColor)
                .border(1.dp, Color.Gray, CircleShape)
        )
    }
}

@Composable
fun WidgetPreview(config: WidgetDraftConfig) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val wallpaperImageBitmap = remember(context) {
        try {
            val wallpaperManager = android.app.WallpaperManager.getInstance(context)
            val drawable = wallpaperManager.drawable
            drawable?.let {
                val bitmap = it.toBitmap()
                bitmap.asImageBitmap()
            }
        } catch (e: Exception) {
            null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(240.dp)
            .clip(RoundedCornerShape(24.dp)),
        contentAlignment = Alignment.Center
    ) {
        // Wallpaper Backdrop
        if (wallpaperImageBitmap != null) {
            androidx.compose.foundation.Image(
                bitmap = wallpaperImageBitmap,
                contentDescription = "Wallpaper Backdrop",
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            listOf(Color(0xFF3B0764), Color(0xFF1E1B4B), Color(0xFF0F172A))
                        )
                    )
            )
        }

        // Outer Container (The actual widget background)
        Box(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxSize()
                .clip(RoundedCornerShape(20.dp))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Container
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                        .background(Color(config.titleSectionBg))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        if (config.isTitleVisible) {
                            Text(
                                "Finance Note",
                                color = Color(config.titleTextColor),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                        if (config.isSubtitleVisible) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Text(
                                    "দ্রুত লেনদেন ড্রাফট করে রাখুন",
                                    color = Color(config.subtitleTextColor),
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Info",
                                    modifier = Modifier.size(13.dp),
                                    tint = Color(config.infoIconColor)
                                )
                            }
                        }
                    }
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PreviewButton(Icons.Default.Palette, config)
                        PreviewButton(Icons.Default.Mic, config)
                        PreviewButton(Icons.Default.Add, config)
                    }
                }

                // List Container
                if (config.isListVisible) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(config.listSectionBg))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        repeat(2) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(config.listItemBg))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val serial = if (it == 0) "১" else "২"
                                Text(
                                    "$serial.", 
                                    color = Color(config.listItemTextColor), 
                                    fontWeight = FontWeight.Bold, 
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                val text = if (it == 0) "বাজার খরচ ৫০০৳" else "অফিস যাতায়াত ১০০৳"
                                Text(
                                    text, 
                                    color = Color(config.listItemTextColor), 
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PreviewButton(icon: androidx.compose.ui.graphics.vector.ImageVector, config: WidgetDraftConfig) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(Color(config.buttonBgColor)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = Color(config.buttonTintColor)
        )
    }
}

@Composable
fun ColorPickerDialog(
    initialColor: Color,
    onDismiss: () -> Unit,
    onColorSelected: (Color) -> Unit
) {
    var red by remember { mutableFloatStateOf(initialColor.red) }
    var green by remember { mutableFloatStateOf(initialColor.green) }
    var blue by remember { mutableFloatStateOf(initialColor.blue) }
    var alpha by remember { mutableFloatStateOf(initialColor.alpha) }

    val presetColors = listOf(
        Color.Black, Color.White, Color.Transparent, 
        Color(0xFF0284C7), // Fintech Blue
        Color(0xFFD97706), // Amber
        Color(0xFF10B981), // Success Green
        Color(0xFFEF4444), // Error Red
        Color(0xFF4F46E5), // Indigo
        Color(0xFF1E1B4B), // Deep Indigo
        Color(0xFF64748B), // Slate
        Color(0xFFF1F5F9), // Slate Light
        Color(0xFF1A1A1A), // 90% Black
        Color(0xFF595959), // 65% Black
        Color(0xFF333333), // 80% Black
        Color(0xFF4D4D4D), // 70% Black
        Color(0xFF666666), // 60% Black
        Color(0xFFE2E8F0),
        Color(0xCCFFFFFF), // 80% White
        Color(0x99FFFFFF)  // 60% White
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pick Color") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(red, green, blue, alpha))
                        .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                )

                ColorSlider("Red", red) { red = it }
                ColorSlider("Green", green) { green = it }
                ColorSlider("Blue", blue) { blue = it }
                ColorSlider("Alpha", alpha) { alpha = it }

                // Preset Colors
                Text("Presets", modifier = Modifier.padding(top = 8.dp), fontWeight = FontWeight.Bold)
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(presetColors) { color ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(1.dp, Color.LightGray, CircleShape)
                                .clickable {
                                    red = color.red
                                    green = color.green
                                    blue = color.blue
                                    alpha = color.alpha
                                }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onColorSelected(Color(red, green, blue, alpha)) }) {
                Text("Select")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ColorSlider(label: String, value: Float, onValueChange: (Float) -> Unit) {
    Column {
        Text("$label: ${(value * 255).toInt()}", fontSize = 12.sp)
        Slider(value = value, onValueChange = onValueChange)
    }
}
