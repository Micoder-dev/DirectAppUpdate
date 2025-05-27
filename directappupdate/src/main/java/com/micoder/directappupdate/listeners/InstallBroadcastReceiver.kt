package com.micoder.directappupdate.listeners

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.micoder.directappupdate.DirectAppUpdateManager
import com.micoder.directappupdate.viewmodel.NotificationViewModel

/**
 * Fixed broadcast receiver to handle notification clicks and trigger installation
 */
class InstallBroadcastReceiver : BroadcastReceiver() {

    companion object {
        var directAppUpdateManager: DirectAppUpdateManager? = null
    }

    override fun onReceive(context: Context, intent: Intent) {
        // Check if action matches the dynamic package name pattern
        val expectedAction = NotificationViewModel.getInstallAction(context.packageName)

        when (intent.action) {
            expectedAction -> {
                // Trigger installation when notification is clicked
                directAppUpdateManager?.installApk()

                // Cancel the notification after clicking
                val notificationManager = NotificationManagerCompat.from(context)
                notificationManager.cancel(4) // COMPLETE_NOTIFICATION_ID
            }
        }
    }
}