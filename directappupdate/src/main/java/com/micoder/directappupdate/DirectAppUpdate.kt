package com.micoder.directappupdate

import android.app.Activity
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import com.micoder.directappupdate.dialogs.UpdateDialog
import com.micoder.directappupdate.listeners.DirectUpdateListener
import com.micoder.directappupdate.model.UpdateDialogState
import com.micoder.directappupdate.model.UpdateType
import com.micoder.directappupdate.viewmodel.NotificationViewModel

@Composable
fun DirectAppUpdate(activity: Activity, configUrl: String, notificationViewModel: NotificationViewModel = hiltViewModel(), appIcon: Int) {

    val updateDialogState = remember { mutableStateOf(UpdateDialogState()) }

    val directAppUpdateManager = remember { DirectAppUpdateManager.Builder(activity) }

    LaunchedEffect(key1 = true) {
        directAppUpdateManager.fetchUpdateConfig(
            configUrl = configUrl,
            onSuccess = { builder ->
                builder.setDirectUpdateListener(object : DirectUpdateListener {
                    override fun onImmediateUpdateAvailable() {
                        updateDialogState.value = updateDialogState.value.copy(
                            visible = true,
                            updateType = UpdateType.Immediate,
                            status = "Immediate Update Available",
                            showUpdateButton = true
                        )
                    }

                    override fun onFlexibleUpdateAvailable() {
                        updateDialogState.value = updateDialogState.value.copy(
                            visible = true,
                            updateType = UpdateType.Flexible,
                            status = "Flexible Update Available",
                            showUpdateButton = true
                        )
                    }

                    override fun onAlreadyUpToDate() {
                        updateDialogState.value = updateDialogState.value.copy(
                            status = "Already up to date",
                            showUpdateButton = false
                        )
                    }

                    override fun onDownloadStart() {
                        updateDialogState.value = updateDialogState.value.copy(
                            status = "Download started",
                            showUpdateButton = false
                        )
                    }

                    override fun onProgress(progress: Float) {
                        notificationViewModel.showProgress(progress = progress.toInt(), icon = appIcon)
                        updateDialogState.value = updateDialogState.value.copy(
                            status = "Downloading: $progress%",
                            progress = progress
                        )
                    }

                    override fun onDownloadComplete() {
                        updateDialogState.value = updateDialogState.value.copy(
                            status = "Download complete",
                            showUpdateButton = false
                        )
                    }

                    override fun onDownloadFailed(error: String) {
                        updateDialogState.value = updateDialogState.value.copy(
                            status = "Download failed: $error",
                            showUpdateButton = false
                        )
                    }
                }).build().checkForUpdate()
            },
            onError = { error ->
                Toast.makeText(activity, error, Toast.LENGTH_SHORT).show()
            }
        )
    }

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