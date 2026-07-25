package com.example.ui.viewmodel

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UpdateInfo(
    val isUpdateAvailable: Boolean = false,
    val isForceUpdate: Boolean = false,
    val updateUrl: String = "",
    val latestVersion: String = "",
    val updateDetails: String = ""
)

class UpdateManager {
    private val _updateInfo = MutableStateFlow(UpdateInfo())
    val updateInfo: StateFlow<UpdateInfo> = _updateInfo.asStateFlow()
    
    private val _isChecking = MutableStateFlow(false)
    val isChecking: StateFlow<Boolean> = _isChecking.asStateFlow()

    fun checkForUpdates(context: Context, onComplete: ((Boolean) -> Unit)? = null) {
        _isChecking.value = true
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(0)
            .build()
        remoteConfig.setConfigSettingsAsync(configSettings)
        
        remoteConfig.setDefaultsAsync(mapOf(
            "is_force_update" to false,
            "latest_version_code" to 1.0,
            "update_url" to ""
        ))
        
        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            _isChecking.value = false
            if (task.isSuccessful) {
                val isForceUpdate = remoteConfig.getBoolean("is_force_update")
                val latestVersionCode = remoteConfig.getDouble("latest_version_code")
                val updateUrl = remoteConfig.getString("update_url")
                val updateDetails = remoteConfig.getString("Update_Details")
                
                val currentVersionCode = getAppVersionCode(context)
                val currentVersionName = getAppVersionName(context)
                
                val isAvailable = latestVersionCode > currentVersionCode
                
                _updateInfo.value = UpdateInfo(
                    isUpdateAvailable = isAvailable,
                    isForceUpdate = isForceUpdate,
                    updateUrl = updateUrl,
                    updateDetails = updateDetails,
                    latestVersion = latestVersionCode.toString()
                )
                onComplete?.invoke(isAvailable)
            } else {
                onComplete?.invoke(false)
            }
        }
    }
    
    fun getAppVersionName(context: Context): String {
        return try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName ?: "1.0.0"
        } catch (e: PackageManager.NameNotFoundException) {
            "1.0.0"
        }
    }
    
    fun getAppVersionCode(context: Context): Double {
        return try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            @Suppress("DEPRECATION")
            pInfo.versionCode.toDouble()
        } catch (e: PackageManager.NameNotFoundException) {
            1.0
        }
    }
}
