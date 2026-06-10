package com.bitacora.pro.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.bitacora.pro.R

/**
 * BroadcastReceiver that handles agenda reminder notifications.
 * Triggered by AlarmManager when a reminder time is reached.
 * Creates and displays a local notification to the user.
 */
class AgendaReminderReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "AgendaReminderReceiver"
        private const val NOTIFICATION_CHANNEL_ID = "agenda_reminders"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive: action=${intent.action}")

        // Only handle our custom agenda reminder action
        if (intent.action != "com.bitacora.pro.AGENDA_REMINDER") {
            return
        }

        try {
            // Extract data from intent
            val agendaItemId = intent.getStringExtra("agendaItemId") ?: return
            val jobId = intent.getStringExtra("jobId") ?: return
            val title = intent.getStringExtra("title") ?: "Recordatorio de Agenda"
            val notificationId = intent.getIntExtra("notificationId", 0)

            Log.d(TAG, "Showing notification: itemId=$agendaItemId, jobId=$jobId, notificationId=$notificationId")

            // Check if notification permission is granted
            if (!NotificationPermissionHelper.hasNotificationPermission(context)) {
                Log.w(TAG, "Notification permission not granted, cannot show notification")
                return
            }

            // Create and show the notification
            showNotification(context, agendaItemId, jobId, title, notificationId)
        } catch (e: Exception) {
            Log.e(TAG, "Error handling reminder: ${e.message}", e)
        }
    }

    /**
     * Creates and displays a notification for the agenda reminder.
     * Ensures notification channel is created on Android 8+.
     */
    private fun showNotification(
        context: Context,
        agendaItemId: String,
        jobId: String,
        title: String,
        notificationId: Int
    ) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Create notification channel for Android 8+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "Recordatorios de Agenda",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notificaciones de recordatorio para elementos de agenda"
                    enableVibration(true)
                    enableLights(true)
                }
                notificationManager.createNotificationChannel(channel)
                Log.d(TAG, "Notification channel created: $NOTIFICATION_CHANNEL_ID")
            }

            // Create the notification
            val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("🔔 Recordatorio: $title")
                .setContentText("Tienes un elemento de agenda pendiente")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setVibrate(longArrayOf(0, 500, 250, 500))
                .build()

            // Show the notification
            notificationManager.notify(notificationId, notification)
            Log.d(TAG, "Notification displayed: id=$notificationId, title=$title")
        } catch (e: Exception) {
            Log.e(TAG, "Error showing notification: ${e.message}", e)
        }
    }
}
