package com.daycountapp.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.daycountapp.MainActivity
import com.daycountapp.R

object NotificationHelper {
    private const val CHANNEL_ID = "daycount_reminder"
    private const val CHANNEL_NAME = "\u4E8B\u4EF6\u63D0\u9192"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT,
                ).apply {
                    description = "\u5012\u6570\u65E5\u4E8B\u4EF6\u5230\u671F\u63D0\u9192"
                }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun showEventReminder(
        context: Context,
        eventTitle: String,
        daysLeft: Long,
    ) {
        val intent =
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        val pendingIntent =
            PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

        val title =
            if (daysLeft == 0L) {
                "\u4E8B\u4EF6\u5230\u671F\uFF01"
            } else {
                "\u8DDD\u79BB $eventTitle \u8FD8\u6709 $daysLeft \u5929"
            }

        val notification =
            NotificationCompat
                .Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(eventTitle)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
