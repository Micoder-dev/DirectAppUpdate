package com.micoder.directappupdate.helper

import android.content.Context
import android.content.IntentFilter
import android.os.Build
import com.micoder.directappupdate.listeners.InstallBroadcastReceiver
import com.micoder.directappupdate.viewmodel.NotificationViewModel

/**
 * Helper class to dynamically register broadcast receiver since we can't modify manifest in library
 */
object BroadcastRegisterHelper {

    private var isReceiverRegistered = false
    private var registeredReceiver: InstallBroadcastReceiver? = null

    fun registerInstallReceiver(context: Context) {
        if (!isReceiverRegistered) {
            try {
                val receiver = InstallBroadcastReceiver()
                val filter = IntentFilter(NotificationViewModel.getInstallAction(context.packageName))

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
                } else {
                    context.registerReceiver(receiver, filter)
                }

                registeredReceiver = receiver
                isReceiverRegistered = true
            } catch (e: Exception) {
                // Handle registration error
                e.printStackTrace()
            }
        }
    }

    fun unregisterInstallReceiver(context: Context) {
        if (isReceiverRegistered && registeredReceiver != null) {
            try {
                context.unregisterReceiver(registeredReceiver)
                registeredReceiver = null
                isReceiverRegistered = false
            } catch (e: Exception) {
                // Handle unregistration error
                e.printStackTrace()
            }
        }
    }
}