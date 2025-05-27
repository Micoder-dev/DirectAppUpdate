package com.micoder.directappupdate

import android.app.Activity
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import com.micoder.directappupdate.components.UpdateDialog
import com.micoder.directappupdate.helper.BroadcastRegisterHelper
import com.micoder.directappupdate.listeners.DirectUpdateListener
import com.micoder.directappupdate.listeners.InstallBroadcastReceiver
import com.micoder.directappupdate.model.UpdateDialogState
import com.micoder.directappupdate.model.UpdateType
import com.micoder.directappupdate.viewmodel.NotificationViewModel

/**
 * Final updated DirectAppUpdate composable - using helper for receiver registration
 */
@Composable
fun DirectAppUpdate(activity: Activity, configUrl: String, notificationViewModel: NotificationViewModel = hiltViewModel(), appIcon: Int) {

    /**
     * [UpdateDialogState] instance to hold the update dialog state.
     */
    val updateDialogState = remember { mutableStateOf(UpdateDialogState()) }

    /**
     * [DirectAppUpdateManager] instance to handle the direct app update process.
     */
    val directAppUpdateManager = remember { DirectAppUpdateManager.Builder(activity) }

    // Register broadcast receiver for notification clicks
    LaunchedEffect(key1 = true) {
        BroadcastRegisterHelper.registerInstallReceiver(activity)

        // Store reference for later use
        InstallBroadcastReceiver.directAppUpdateManager = null
    }

    // Cleanup on disposal
    DisposableEffect(key1 = true) {
        onDispose {
            BroadcastRegisterHelper.unregisterInstallReceiver(activity)
        }
    }

    /**
     * [LaunchedEffect] that fetches the update config and listens for the update process and also to avoid recomposition.
     */
    LaunchedEffect(key1 = configUrl) {
        directAppUpdateManager.fetchUpdateConfig(
            configUrl = configUrl,
            onSuccess = { builder ->
                val appUpdateConfig = builder.appUpdateConfig
                val builtManager = builder.setDirectUpdateListener(object : DirectUpdateListener {
                    override fun onImmediateUpdateAvailable() {
                        updateDialogState.value = updateDialogState.value.copy(
                            visible = true,
                            updateType = UpdateType.Immediate,
                            status = "New update available",
                            showUpdateButton = true,
                            config = appUpdateConfig
                        )
                    }

                    override fun onFlexibleUpdateAvailable() {
                        updateDialogState.value = updateDialogState.value.copy(
                            visible = true,
                            updateType = UpdateType.Flexible,
                            status = "New update available",
                            showUpdateButton = true,
                            config = appUpdateConfig
                        )
                    }

                    override fun onAlreadyUpToDate() {
                        updateDialogState.value = updateDialogState.value.copy(
                            status = "App is up to date",
                            showUpdateButton = false,
                            config = appUpdateConfig
                        )
                    }

                    override fun onDownloadStart() {
                        updateDialogState.value = updateDialogState.value.copy(
                            status = "Preparing download...",
                            showUpdateButton = false,
                            config = appUpdateConfig
                        )
                    }

                    override fun onProgress(progress: Float) {
                        notificationViewModel.showProgress(progress = progress.toInt(), icon = appIcon)
                        updateDialogState.value = updateDialogState.value.copy(
                            status = "Downloading update: ${progress.toInt()}%",
                            progress = progress,
                            config = appUpdateConfig
                        )
                    }

                    override fun onDownloadComplete() {
                        updateDialogState.value = updateDialogState.value.copy(
                            status = "Download complete - Ready to install",
                            showUpdateButton = true,
                            isReadyToInstall = true,
                            config = appUpdateConfig
                        )
                    }

                    override fun onDownloadFailed(error: String) {
                        updateDialogState.value = updateDialogState.value.copy(
                            status = "Download failed: $error",
                            showUpdateButton = true,
                            config = appUpdateConfig
                        )
                    }

                    override fun onApkAlreadyDownloaded() {
                        updateDialogState.value = updateDialogState.value.copy(
                            visible = true,
                            updateType = if (appUpdateConfig.immediateUpdate) UpdateType.Immediate else UpdateType.Flexible,
                            status = "Update ready to install",
                            showUpdateButton = true,
                            isReadyToInstall = true,
                            config = appUpdateConfig
                        )
                    }
                }).build()

                // Store the built manager for notification receiver
                InstallBroadcastReceiver.directAppUpdateManager = builtManager

                // Check for update
                builtManager.checkForUpdate()
            },
            onError = { error ->
                Toast.makeText(activity, error, Toast.LENGTH_SHORT).show()
            }
        )
    }

    /**
     * [UpdateDialog] composable function that shows the update status and progress.
     */
    UpdateDialog(
        dialogState = updateDialogState.value,
        onUpdateClick = {
            if (updateDialogState.value.isReadyToInstall) {
                directAppUpdateManager.build().installApk()
            } else {
                directAppUpdateManager.build().startUpdate()
            }
        },
        onCancelClick = {
            if (updateDialogState.value.updateType == UpdateType.Flexible) {
                updateDialogState.value = updateDialogState.value.copy(visible = false)
            }
        }
    )
}