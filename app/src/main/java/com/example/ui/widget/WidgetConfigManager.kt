package com.example.ui.widget

import android.content.Context
import android.graphics.Color

data class WidgetDraftConfig(
    val titleSectionBg: Int = Color.parseColor("#D9FFFFFF"),
    val listSectionBg: Int = Color.parseColor("#99FFFFFF"),
    val listItemBg: Int = Color.parseColor("#CCFFFFFF"),
    val titleTextColor: Int = Color.parseColor("#333333"),
    val subtitleTextColor: Int = Color.parseColor("#666666"),
    val listItemTextColor: Int = Color.parseColor("#4D4D4D"),
    val buttonTintColor: Int = Color.parseColor("#38BDF8"),
    val buttonBgColor: Int = Color.parseColor("#E0F2FE"),
    val infoIconColor: Int = Color.parseColor("#38BDF8"), // Default Light Blue
    val isTitleVisible: Boolean = true,
    val isSubtitleVisible: Boolean = true,
    val isListVisible: Boolean = true
)

object WidgetConfigManager {
    private const val PREFS_NAME = "draft_widget_prefs"
    
    fun saveConfig(context: Context, config: WidgetDraftConfig) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putInt("titleSectionBg", config.titleSectionBg)
            putInt("listSectionBg", config.listSectionBg)
            putInt("listItemBg", config.listItemBg)
            putInt("titleTextColor", config.titleTextColor)
            putInt("subtitleTextColor", config.subtitleTextColor)
            putInt("listItemTextColor", config.listItemTextColor)
            putInt("buttonTintColor", config.buttonTintColor)
            putInt("buttonBgColor", config.buttonBgColor)
            putInt("infoIconColor", config.infoIconColor)
            putBoolean("isTitleVisible", config.isTitleVisible)
            putBoolean("isSubtitleVisible", config.isSubtitleVisible)
            putBoolean("isListVisible", config.isListVisible)
            apply()
        }
    }
    
    fun loadConfig(context: Context): WidgetDraftConfig {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return WidgetDraftConfig(
            titleSectionBg = prefs.getInt("titleSectionBg", Color.parseColor("#D9FFFFFF")),
            listSectionBg = prefs.getInt("listSectionBg", Color.parseColor("#99FFFFFF")),
            listItemBg = prefs.getInt("listItemBg", Color.parseColor("#CCFFFFFF")),
            titleTextColor = prefs.getInt("titleTextColor", Color.parseColor("#333333")),
            subtitleTextColor = prefs.getInt("subtitleTextColor", Color.parseColor("#666666")),
            listItemTextColor = prefs.getInt("listItemTextColor", Color.parseColor("#4D4D4D")),
            buttonTintColor = prefs.getInt("buttonTintColor", Color.parseColor("#38BDF8")),
            buttonBgColor = prefs.getInt("buttonBgColor", Color.parseColor("#E0F2FE")),
            infoIconColor = prefs.getInt("infoIconColor", Color.parseColor("#38BDF8")),
            isTitleVisible = prefs.getBoolean("isTitleVisible", true),
            isSubtitleVisible = prefs.getBoolean("isSubtitleVisible", true),
            isListVisible = prefs.getBoolean("isListVisible", true)
        )
    }
}
