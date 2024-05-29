package com.micoder.directappupdate

import android.app.Activity
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import com.micoder.directappupdate.components.UpdateDialog
import com.micoder.directappupdate.listeners.DirectUpdateListener
import com.micoder.directappupdate.model.UpdateDialogState
import com.micoder.directappupdate.model.UpdateType
import com.micoder.directappupdate.viewmodel.NotificationViewModel

/**
 * [DirectAppUpdate] composable function that handles the direct app update process.
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

    /**
     * [LaunchedEffect] that fetches the update config and listens for the update process and also to avoid recomposition.
     */
    LaunchedEffect(key1 = true) {
        directAppUpdateManager.fetchUpdateConfig(
            configUrl = configUrl,
            onSuccess = { builder ->
                val appUpdateConfig = builder.appUpdateConfig
                builder.setDirectUpdateListener(object : DirectUpdateListener {
                    override fun onImmediateUpdateAvailable() {
                        updateDialogState.value = updateDialogState.value.copy(
                            visible = true,
                            updateType = UpdateType.Immediate,
                            status = "Update Available",
                            showUpdateButton = true,
                            config = appUpdateConfig
                        )
                    }

                    override fun onFlexibleUpdateAvailable() {
                        updateDialogState.value = updateDialogState.value.copy(
                            visible = true,
                            updateType = UpdateType.Flexible,
                            status = "Update Available",
                            showUpdateButton = true,
                            config = appUpdateConfig
                        )
                    }

                    override fun onAlreadyUpToDate() {
                        updateDialogState.value = updateDialogState.value.copy(
                            status = "Already up to date",
                            showUpdateButton = false,
                            config = appUpdateConfig
                        )
                    }

                    override fun onDownloadStart() {
                        updateDialogState.value = updateDialogState.value.copy(
                            status = "Download started",
                            showUpdateButton = false,
                            config = appUpdateConfig
                        )
                    }

                    override fun onProgress(progress: Float) {
                        notificationViewModel.showProgress(progress = progress.toInt(), icon = appIcon)
                        updateDialogState.value = updateDialogState.value.copy(
                            status = "Downloading: $progress%",
                            progress = progress,
                            config = appUpdateConfig
                        )
                    }

                    override fun onDownloadComplete() {
                        updateDialogState.value = updateDialogState.value.copy(
                            status = "Download complete",
                            showUpdateButton = false,
                            config = appUpdateConfig
                        )
                    }

                    override fun onDownloadFailed(error: String) {
                        updateDialogState.value = updateDialogState.value.copy(
                            status = "Download failed: $error",
                            showUpdateButton = false,
                            config = appUpdateConfig
                        )
                    }
                }).build().checkForUpdate()
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
        onUpdateClick = { directAppUpdateManager.build().startUpdate() },
        onCancelClick = {
            if (updateDialogState.value.updateType == UpdateType.Flexible) {
                updateDialogState.value = updateDialogState.value.copy(visible = false)
            }
        }
    )

}