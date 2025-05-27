package com.micoder.directappupdate.di

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * Updated NotificationModule provides the NotificationCompat.Builder and NotificationManagerCompat instances
 */
@Module
@InstallIn(SingletonComponent::class)
object NotificationModule {

    @Singleton
    @Provides
    @DirectAppUpdateNotificationCompatBuilder
    fun provideNotificationBuilder(@ApplicationContext context: Context): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, "Channel ID")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setColor(0xFFD4AF37.toInt()) // Golden color for notifications
    }

    @Singleton
    @Provides
    @DirectAppUpdateNotificationManager
    fun provideNotificationManager(@ApplicationContext context: Context): NotificationManagerCompat {
        val notificationManager = NotificationManagerCompat.from(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("Channel ID", "App Updates", NotificationManager.IMPORTANCE_LOW)
            channel.description = "Notifications for app update progress"
            notificationManager.createNotificationChannel(channel)
        }
        return notificationManager
    }
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DirectAppUpdateNotificationCompatBuilder

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DirectAppUpdateNotificationManager