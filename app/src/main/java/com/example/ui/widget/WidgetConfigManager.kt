package com.example.ui.widget

import android.content.Context
import android.graphics.Color

data class WidgetDraftConfig(
    val titleSectionBg: Int = Color.parseColor("#CCFFFFFF"),
    val listSectionBg: Int = Color.parseColor("#99FFFFFF"),
    val listItemBg: Int = Color.parseColor("#CCFFFFFF"),
    val titleTextColor: Int = Color.parseColor("#333333"),
    val subtitleTextColor: Int = Color.parseColor("#666666"),
    val listItemTextColor: Int = Color.parseColor("#4D4D4D"),
    val buttonTintColor: Int = Color.parseColor("#333333"),
    val buttonBgColor: Int = Color.WHITE,
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
            putBoolean("isTitleVisible", config.isTitleVisible)
            putBoolean("isSubtitleVisible", config.isSubtitleVisible)
            putBoolean("isListVisible", config.isListVisible)
            apply()
        }
    }
    
    fun loadConfig(context: Context): WidgetDraftConfig {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return WidgetDraftConfig(
            titleSectionBg = prefs.getInt("titleSectionBg", Color.TRANSPARENT),
            listSectionBg = prefs.getInt("listSectionBg", Color.TRANSPARENT),
            listItemBg = prefs.getInt("listItemBg", Color.parseColor("#E2E8F0")),
            titleTextColor = prefs.getInt("titleTextColor", Color.parseColor("#1A1A1A")),
            subtitleTextColor = prefs.getInt("subtitleTextColor", Color.parseColor("#595959")),
            listItemTextColor = prefs.getInt("listItemTextColor", Color.parseColor("#333333")),
            buttonTintColor = prefs.getInt("buttonTintColor", Color.parseColor("#1A1A1A")),
            buttonBgColor = prefs.getInt("buttonBgColor", Color.WHITE),
            isTitleVisible = prefs.getBoolean("isTitleVisible", true),
            isSubtitleVisible = prefs.getBoolean("isSubtitleVisible", true),
            isListVisible = prefs.getBoolean("isListVisible", true)
        )
    }
}
