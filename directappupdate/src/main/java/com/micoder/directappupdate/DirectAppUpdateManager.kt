package com.micoder.directappupdate

import android.app.Activity
import android.app.UiModeManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.core.content.FileProvider
import com.micoder.directappupdate.listeners.DirectUpdateListener
import com.micoder.directappupdate.listeners.DownloadProgressBody
import com.micoder.directappupdate.listeners.ProgressListener
import com.micoder.directappupdate.model.AppUpdateConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.buffer
import okio.sink
import org.json.JSONObject
import java.io.File
import kotlin.math.roundToInt

/**
 * [DirectAppUpdateManager] is a class that provides a simple way to check for updates and download the APK file directly from a URL.
 */
class DirectAppUpdateManager private constructor(
    private val activity: Activity,
    private val appUpdateConfig: AppUpdateConfig,
    private val directUpdateListener: DirectUpdateListener
) {

    /**
     * [Builder] is a class that helps to build the [DirectAppUpdateManager] instance.
     */
    class Builder(private val activity: Activity) {
        lateinit var appUpdateConfig: AppUpdateConfig
        private lateinit var directUpdateListener: DirectUpdateListener

        /**
         * Fetch the update configuration from the provided URL.
         */
        fun fetchUpdateConfig(configUrl: String, onSuccess: (Builder) -> Unit, onError: (String) -> Unit) {
            val okHttpClient = OkHttpClient()
            val request = Request.Builder().url(configUrl).build()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response: Response = okHttpClient.newCall(request).execute()
                    if (!response.isSuccessful) throw Exception("Request failed with code: ${response.code}")

                    val jsonObject = JSONObject(response.body?.string() ?: "")
                    val appName = jsonObject.getString("appName")
                    val versionCode = jsonObject.getInt("versionCode")
                    val versionName = jsonObject.getString("versionName")
                    val downloadUrl = jsonObject.getString("downloadUrl")
                    val apkFileName = jsonObject.getString("apkFileName")
                    val releaseNotes = jsonObject.getString("releaseNotes")
                    val immediateUpdate = jsonObject.getBoolean("immediateUpdate")

                    val appUpdateConfig = AppUpdateConfig(
                        appName,
                        activity.packageManager.getPackageInfo(activity.packageName, 0).versionCode,
                        versionCode,
                        versionName,
                        downloadUrl,
                        apkFileName,
                        releaseNotes,
                        immediateUpdate
                    )

                    this@Builder.appUpdateConfig = appUpdateConfig

                    CoroutineScope(Dispatchers.Main).launch {
                        onSuccess(this@Builder)
                    }
                } catch (e: Exception) {
                    CoroutineScope(Dispatchers.Main).launch {
                        onError(e.message ?: "Unknown error")
                    }
                }
            }
        }

        /**
         * Set the [AppUpdateConfig] for the update.
         */
        fun setDirectUpdateListener(listener: DirectUpdateListener) = apply { this.directUpdateListener = listener }

        /**
         * Build the [DirectAppUpdateManager] instance.
         */
        fun build(): DirectAppUpdateManager {
            return DirectAppUpdateManager(
                activity,
                appUpdateConfig,
                directUpdateListener
            )
        }
    }

    private val downloadDir = if (isAndroidTV()) {
        // Use external files directory for TV to persist cache
        File(activity.getExternalFilesDir("app_updates") ?: activity.filesDir, "app_updates")
    } else {
        File(activity.filesDir, "app_updates")
    }

    init {
        // Create download directory if it doesn't exist
        if (!downloadDir.exists()) {
            downloadDir.mkdirs()
        }
    }

    /**
     * Check if running on Android TV
     */
    private fun isAndroidTV(): Boolean {
        val uiModeManager = activity.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
        return uiModeManager.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION
    }

    /**
     * Clean old APK files if current version matches
     */
    private fun cleanOldApkIfVersionMatches() {
        try {
            val currentVersion = activity.packageManager.getPackageInfo(activity.packageName, 0).versionCode
            if (isAndroidTV()) {
                // Conservative for TV - only clean if current version is significantly higher
                if (currentVersion > appUpdateConfig.versionCode) {
                    val apkFile = File(downloadDir, appUpdateConfig.apkFileName)
                    if (apkFile.exists()) {
                        apkFile.delete()
                    }
                }
            } else {
                // Original mobile behavior - clean if current version matches or is higher
                if (currentVersion >= appUpdateConfig.versionCode) {
                    val apkFile = File(downloadDir, appUpdateConfig.apkFileName)
                    if (apkFile.exists()) {
                        apkFile.delete()
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore cleanup errors
        }
    }

    /**
     * Check for the update - fixed logic to prevent unnecessary re-downloads
     */
    fun checkForUpdate() {
        // Clean old APKs only if they're truly outdated
        cleanOldApkIfVersionMatches()

        if (appUpdateConfig.versionCode > appUpdateConfig.currentVersionCode) {
            // Check if APK is already downloaded
            val apkFile = File(downloadDir, appUpdateConfig.apkFileName)

            if (isAndroidTV()) {
                // Stricter validation for TV
                if (apkFile.exists() && apkFile.length() > 0 && isApkValid(apkFile)) {
                    directUpdateListener.onApkAlreadyDownloaded()
                } else {
                    // Only remove if truly invalid (corrupted or empty)
                    if (apkFile.exists() && (apkFile.length() == 0L || !isApkValid(apkFile))) {
                        apkFile.delete()
                    }

                    if (appUpdateConfig.immediateUpdate) {
                        directUpdateListener.onImmediateUpdateAvailable()
                    } else {
                        directUpdateListener.onFlexibleUpdateAvailable()
                    }
                }
            } else {
                // Enhanced mobile behavior - only remove truly invalid files
                if (apkFile.exists() && apkFile.length() > 0) {
                    directUpdateListener.onApkAlreadyDownloaded()
                } else {
                    // Only remove if empty or doesn't exist
                    if (apkFile.exists() && apkFile.length() == 0L) {
                        apkFile.delete()
                    }

                    if (appUpdateConfig.immediateUpdate) {
                        directUpdateListener.onImmediateUpdateAvailable()
                    } else {
                        directUpdateListener.onFlexibleUpdateAvailable()
                    }
                }
            }
        } else {
            directUpdateListener.onAlreadyUpToDate()
        }
    }

    /**
     * Validate APK file integrity
     */
    private fun isApkValid(apkFile: File): Boolean {
        return try {
            // Basic validation - check if file can be read and has reasonable size
            apkFile.exists() && apkFile.length() > 1024 * 1024 && apkFile.canRead()
        } catch (e: Exception) {
            false
        }
    }


    /**
     * Start the update process - more conservative approach to prevent unnecessary re-downloads
     */
    fun startUpdate() {
        val apkFile = File(downloadDir, appUpdateConfig.apkFileName)

        if (isAndroidTV()) {
            // Stricter validation for TV but don't delete valid files
            if (apkFile.exists() && apkFile.length() > 0 && isApkValid(apkFile)) {
                directUpdateListener.onApkAlreadyDownloaded()
                return
            } else {
                // Only remove if truly corrupted (exists but invalid)
                if (apkFile.exists() && (!isApkValid(apkFile) || apkFile.length() == 0L)) {
                    apkFile.delete()
                }
            }
        } else {
            // Mobile behavior - be more conservative about deletion
            if (apkFile.exists() && apkFile.length() > 0) {
                directUpdateListener.onApkAlreadyDownloaded()
                return
            } else {
                // Only remove empty files
                if (apkFile.exists() && apkFile.length() == 0L) {
                    apkFile.delete()
                }
            }
        }

        // Start download only if APK doesn't exist or was truly invalid
        directUpdateListener.onDownloadStart()
        downloadApk()
    }

    /**
     * Updated clean cache method - more conservative for TV
     */
    private fun cleanCache() {
        try {
            if (!isAndroidTV()) {
                // Only clean cache on mobile, not on TV
                if (downloadDir.exists()) {
                    downloadDir.listFiles()?.forEach { it.delete() }
                }
            }
            // On TV, keep the cache for future app sessions
        } catch (e: Exception) {
            // Ignore cleanup errors
        }
    }

    /**
     * Updated install APK method - prevent deletion of valid APK files
     */
    fun installApk() {
        val apkFile = File(downloadDir, appUpdateConfig.apkFileName)

        // More conservative check - only re-download if file truly doesn't exist or is corrupted
        if (!apkFile.exists()) {
            // File doesn't exist, need to download
            startUpdate()
            return
        }

        // Check if file is corrupted (empty or too small)
        if (apkFile.length() < 1024 * 50) { // Less than 50KB is likely corrupted
            try {
                apkFile.delete()
            } catch (e: Exception) {
                // Ignore cleanup errors
            }
            startUpdate()
            return
        }

        // For Android TV, do additional validation but don't delete on failure
        if (isAndroidTV() && !isApkValid(apkFile)) {
            // Don't delete, just try to install anyway - user might have cancelled mid-process
            // but file could still be valid
        }

        try {
            // Use a more compatible approach for FileProvider
            val uri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(
                    activity,
                    "${activity.packageName}.fileprovider",
                    apkFile
                )
            } else {
                Uri.fromFile(apkFile)
            }

            val intent = Intent(Intent.ACTION_INSTALL_PACKAGE).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            // Alternative approach if FileProvider fails
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                try {
                    activity.startActivity(intent)
                } catch (e: Exception) {
                    // Fallback: copy to external storage if possible
                    installApkFallback(apkFile)
                }
            } else {
                activity.startActivity(intent)
            }

            // For TV, don't schedule cache cleanup - keep files persistent
            if (!isAndroidTV()) {
                // Schedule cache cleanup after a longer delay only for mobile
                Handler(Looper.getMainLooper()).postDelayed({
                    cleanCache()
                }, 30000) // Clean after 30 seconds instead of 5
            }

        } catch (e: Exception) {
            // On installation failure, don't immediately delete and re-download
            // Just show an error or try fallback installation
            try {
                installApkFallback(apkFile)
            } catch (fallbackException: Exception) {
                // Only if both methods fail, then consider re-downloading
                // But still be conservative
                if (apkFile.length() < 1024 * 100) { // Only if less than 100KB
                    try {
                        apkFile.delete()
                    } catch (cleanupException: Exception) {
                        // Ignore cleanup errors
                    }
                    startUpdate()
                }
            }
        }
    }

    /**
     * Download the APK file with proper completion handling
     */
    private fun downloadApk() {
        val progressListener = object : ProgressListener {
            override fun update(bytesRead: Long, contentLength: Long, done: Boolean) {
                val progress = if (contentLength > 0) {
                    ((bytesRead.toFloat() / contentLength) * 100).roundToInt().toFloat()
                } else {
                    0f
                }

                activity.runOnUiThread {
                    directUpdateListener.onProgress(progress)
                }

                if (done) {
                    activity.runOnUiThread {
                        // Verify the downloaded file exists and has content
                        val apkFile = File(downloadDir, appUpdateConfig.apkFileName)
                        if (apkFile.exists() && apkFile.length() > 0) {
                            directUpdateListener.onDownloadComplete()
                        } else {
                            directUpdateListener.onDownloadFailed("Download verification failed")
                        }
                    }
                }
            }
        }

        val okHttpClient = OkHttpClient.Builder().addNetworkInterceptor {
            val originalResponse = it.proceed(it.request())
            val responseBody = originalResponse.body ?: return@addNetworkInterceptor originalResponse

            return@addNetworkInterceptor originalResponse.newBuilder()
                .body(DownloadProgressBody(responseBody, progressListener))
                .build()
        }.build()

        val request = Request.Builder().url(appUpdateConfig.downloadUrl).build()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = okHttpClient.newCall(request).execute()

                if (!response.isSuccessful) throw Exception("Request failed with code: ${response.code}")

                val apkFile = File(downloadDir, appUpdateConfig.apkFileName)

                // Ensure parent directory exists
                apkFile.parentFile?.mkdirs()

                response.body?.source()?.use { bufferedSource ->
                    val bufferedSink = apkFile.sink().buffer()
                    bufferedSink.writeAll(bufferedSource)
                    bufferedSink.close()
                }

                // Final verification on IO thread
                if (apkFile.exists() && apkFile.length() > 0) {
                    activity.runOnUiThread {
                        directUpdateListener.onDownloadComplete()
                    }
                } else {
                    activity.runOnUiThread {
                        directUpdateListener.onDownloadFailed("Download file verification failed")
                    }
                }
            } catch (e: Exception) {
                activity.runOnUiThread {
                    directUpdateListener.onDownloadFailed(e.message ?: "Unknown error")
                }
            }
        }
    }

    /**
     * Updated fallback installation method - don't delete on failure
     */
    private fun installApkFallback(sourceFile: File) {
        try {
            // Try copying to external files directory
            val externalDir = activity.getExternalFilesDir(null)
            if (externalDir != null) {
                val externalApk = File(externalDir, appUpdateConfig.apkFileName)
                sourceFile.copyTo(externalApk, overwrite = true)

                val uri = FileProvider.getUriForFile(
                    activity,
                    "${activity.packageName}.fileprovider",
                    externalApk
                )

                val intent = Intent(Intent.ACTION_INSTALL_PACKAGE).apply {
                    setDataAndType(uri, "application/vnd.android.package-archive")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                activity.startActivity(intent)
            } else {
                throw Exception("Cannot access external storage")
            }
        } catch (e: Exception) {
            // On fallback failure, don't delete the original file
            // Let user try again later
            Toast.makeText(activity, "Installation failed. Please try again.", Toast.LENGTH_LONG).show()
        }
    }

}