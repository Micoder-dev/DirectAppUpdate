package com.micoder.directappupdate.model

/**
 * [AppUpdateConfig] data class to hold the app update configuration
 */
data class AppUpdateConfig(
    val appName: String,
    val currentVersionCode: Int,
    val versionCode: Int,
    val versionName: String,
    val downloadUrl: String,
    val apkFileName: String,
    val releaseNotes: String,
    val immediateUpdate: Boolean
)