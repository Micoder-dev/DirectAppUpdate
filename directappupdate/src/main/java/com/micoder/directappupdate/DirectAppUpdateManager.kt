package com.micoder.directappupdate

import android.app.Activity
import android.content.Intent
import android.net.Uri
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

    /**
     * Check for the update.
     */
    fun checkForUpdate() {
        if (appUpdateConfig.versionCode > appUpdateConfig.currentVersionCode) {
            if (appUpdateConfig.immediateUpdate) {
                directUpdateListener.onImmediateUpdateAvailable()
            } else {
                directUpdateListener.onFlexibleUpdateAvailable()
            }
        } else {
            directUpdateListener.onAlreadyUpToDate()
        }
    }

    /**
     * Start the update process.
     */
    fun startUpdate() {
        directUpdateListener.onDownloadStart()
        downloadApk()
    }

    /**
     * Download the APK file.
     */
    private fun downloadApk() {
        val progressListener = object : ProgressListener {
            override fun update(bytesRead: Long, contentLength: Long, done: Boolean) {
                val progress = ((bytesRead.toFloat() / contentLength) * 100).roundToInt().toFloat()
                activity.runOnUiThread {
                    directUpdateListener.onProgress(progress)
                }

                if (done) {
                    activity.runOnUiThread {
                        directUpdateListener.onDownloadComplete()
                    }
                    installApk()
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

                response.body?.source()?.use { bufferedSource ->
                    val bufferedSink = activity.filesDir.resolve(appUpdateConfig.apkFileName).sink().buffer()
                    bufferedSink.writeAll(bufferedSource)
                    bufferedSink.close()
                }

                activity.runOnUiThread {
                    directUpdateListener.onDownloadComplete()
                }
            } catch (e: Exception) {
                activity.runOnUiThread {
                    directUpdateListener.onDownloadFailed(e.message ?: "Unknown error")
                }
            }
        }
    }

    /**
     * Install the APK file.
     */
    private fun installApk() {
        val apkFile = activity.filesDir.resolve(appUpdateConfig.apkFileName)
        val uri: Uri = FileProvider.getUriForFile(activity, "${activity.packageName}.provider", apkFile)
        val intent = Intent(Intent.ACTION_INSTALL_PACKAGE).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        activity.startActivity(intent)
    }

}