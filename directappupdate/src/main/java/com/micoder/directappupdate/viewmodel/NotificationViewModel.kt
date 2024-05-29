package com.micoder.directappupdate.viewmodel

import android.annotation.SuppressLint
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.micoder.directappupdate.di.MainNotificationCompatBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * A [ViewModel] that handles the notification progress.
 */
@SuppressLint("MissingPermission")
@HiltViewModel
class NotificationViewModel @Inject constructor(
    @MainNotificationCompatBuilder private val notificationBuilder: NotificationCompat.Builder,
    private val notificationManager: NotificationManagerCompat
) : ViewModel() {

    fun showProgress(progress: Int, icon: Int) {
        val max = 100
        viewModelScope.launch {
            notificationManager.notify(3, notificationBuilder
                .setContentTitle("Downloading $progress%")
                .setContentText("")
                .setSmallIcon(icon)
                .setProgress(max, progress, false)
                .build())
            if (progress == max) {
                notificationManager.cancel(3)
                notificationManager.notify(3, notificationBuilder
                    .setContentTitle("Completed!")
                    .setContentText("")
                    .setContentIntent(null)
                    .clearActions()
                    .setProgress(0, 0, false)
                    .build())
            }
        }
    }

}