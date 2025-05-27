package com.micoder.directappupdate.viewmodel

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.micoder.directappupdate.di.DirectAppUpdateNotificationCompatBuilder
import com.micoder.directappupdate.di.DirectAppUpdateNotificationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Fixed ViewModel that handles detailed notification progress with click-to-install
 */
@SuppressLint("MissingPermission")
@HiltViewModel
class NotificationViewModel @Inject constructor(
    @DirectAppUpdateNotificationCompatBuilder private val notificationBuilder: NotificationCompat.Builder,
    @DirectAppUpdateNotificationManager private val notificationManager: NotificationManagerCompat,
    @ApplicationContext private val context: Context // Inject context directly
) : ViewModel() {

    companion object {
        private const val PROGRESS_NOTIFICATION_ID = 3
        private const val COMPLETE_NOTIFICATION_ID = 4
        fun getInstallAction(packageName: String) = "${packageName}.INSTALL_APK"
    }

    fun showProgress(progress: Int, icon: Int) {
        val max = 100
        viewModelScope.launch {
            // Create detailed progress notification
            val progressNotification = notificationBuilder
                .setContentTitle("App Update Download")
                .setContentText("Downloading update... $progress% complete")
                .setSubText("${progress}% of ${max}%")
                .setSmallIcon(icon)
                .setProgress(max, progress, false)
                .setOngoing(true)
                .setAutoCancel(false)
                .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                // Remove BigTextStyle from progress notification - it conflicts with progress bar
                .build()

            notificationManager.notify(PROGRESS_NOTIFICATION_ID, progressNotification)

            if (progress >= max) {
                delay(1000)
                notificationManager.cancel(PROGRESS_NOTIFICATION_ID)
                showCompletionNotification(icon)
            }
        }
    }

    private fun showCompletionNotification(icon: Int) {
        // Create pending intent for opening the app
        val openAppIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            1, // Different request code
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Create pending intent for installation
        val installIntent = Intent(getInstallAction(context.packageName)).apply {
            setPackage(context.packageName)
        }

        val installPendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            installIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Create completion notification with proper styling
        val completionNotification = notificationBuilder
            .setContentTitle("Update Download Complete")
            .setContentText("Tap to open app or install update")
            .setSubText("Ready to install new version")
            .setSmallIcon(icon)
            .setProgress(0, 0, false)
            .setOngoing(false)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            // Only one action button for installation
            .addAction(
                icon,
                "Install Now",
                installPendingIntent
            )
            // Notification click opens the app
            .setContentIntent(openAppPendingIntent)
            // Use BigTextStyle for always expanded notification
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Update download completed successfully!\nTap this notification to open the app or use 'Install Now' button to install the update.")
                .setBigContentTitle("Ready to Install Update"))
            .build()

        notificationManager.notify(COMPLETE_NOTIFICATION_ID, completionNotification)
    }

    fun cancelNotifications() {
        notificationManager.cancel(PROGRESS_NOTIFICATION_ID)
        notificationManager.cancel(COMPLETE_NOTIFICATION_ID)
    }
}