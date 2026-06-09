package com.bitacora.pro.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.bitacora.pro.data.models.AgendaItem
import java.util.Calendar

/**
 * Manages scheduling and cancelling of local reminder notifications for agenda items.
 * Uses AlarmManager to trigger notifications at the specified due date/time.
 * All notifications are local-only, no backend or Firebase integration.
 */
class AgendaNotificationScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val notificationIdGenerator = NotificationIdGenerator()

    companion object {
        private const val TAG = "AgendaNotificationScheduler"
        private const val NOTIFICATION_REQUEST_CODE_BASE = 10000
    }

    /**
     * Schedules a reminder notification for an agenda item.
     * If the item has a dueAt timestamp and reminderEnabled is true,
     * calculates the reminder time based on reminderOffsetDays and schedules an alarm.
     *
     * @param agendaItem The agenda item to schedule a reminder for
     * @return Updated AgendaItem with notificationId and reminderScheduledAt set, or original if not scheduled
     */
    fun scheduleReminder(agendaItem: AgendaItem): AgendaItem {
        // Only schedule if reminder is enabled and item has a due date
        if (!agendaItem.reminderEnabled || agendaItem.dueAt == null) {
            Log.d(TAG, "Reminder not scheduled: enabled=${agendaItem.reminderEnabled}, hasDueDate=${agendaItem.dueAt != null}")
            return agendaItem
        }

        // Check notification permission
        if (!NotificationPermissionHelper.hasNotificationPermission(context)) {
            Log.w(TAG, "Cannot schedule reminder: notification permission not granted")
            return agendaItem
        }

        try {
            // Calculate reminder time: dueAt - (reminderOffsetDays * 24 hours)
            val reminderTimeMs = agendaItem.dueAt!! - (agendaItem.reminderOffsetDays.toLong() * 24 * 60 * 60 * 1000)
            val now = System.currentTimeMillis()

            // Only schedule if reminder time is in the future
            if (reminderTimeMs <= now) {
                Log.d(TAG, "Reminder time is in the past, not scheduling")
                return agendaItem
            }

            // Generate unique notification ID
            val notificationId = notificationIdGenerator.generateId(agendaItem.id)

            // Create intent for the broadcast receiver
            val intent = Intent(context, AgendaReminderReceiver::class.java).apply {
                action = "com.bitacora.pro.AGENDA_REMINDER"
                putExtra("agendaItemId", agendaItem.id)
                putExtra("jobId", agendaItem.jobId)
                putExtra("title", agendaItem.title)
                putExtra("notificationId", notificationId)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                NOTIFICATION_REQUEST_CODE_BASE + notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Schedule the alarm
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    // Android 12+: Check SCHEDULE_EXACT_ALARM permission
                    if (context.checkSelfPermission(android.Manifest.permission.SCHEDULE_EXACT_ALARM) ==
                        android.content.pm.PackageManager.PERMISSION_GRANTED
                    ) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            reminderTimeMs,
                            pendingIntent
                        )
                    } else {
                        // Fall back to inexact alarm
                        alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            reminderTimeMs,
                            pendingIntent
                        )
                    }
                } else {
                    // Android < 12: Use setExactAndAllowWhileIdle
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        reminderTimeMs,
                        pendingIntent
                    )
                }

                Log.d(TAG, "Reminder scheduled: itemId=${agendaItem.id}, notificationId=$notificationId, time=$reminderTimeMs")

                // Return updated agenda item with notification metadata
                return agendaItem.copy(
                    notificationId = notificationId,
                    reminderScheduledAt = System.currentTimeMillis()
                )
            } catch (e: SecurityException) {
                Log.e(TAG, "SecurityException scheduling alarm: ${e.message}")
                return agendaItem
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling reminder: ${e.message}", e)
            return agendaItem
        }
    }

    /**
     * Cancels a scheduled reminder notification for an agenda item.
     * Removes the alarm from AlarmManager.
     *
     * @param agendaItem The agenda item to cancel the reminder for
     */
    fun cancelReminder(agendaItem: AgendaItem) {
        if (agendaItem.notificationId == 0) {
            Log.d(TAG, "No notification ID to cancel for item ${agendaItem.id}")
            return
        }

        try {
            val intent = Intent(context, AgendaReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                NOTIFICATION_REQUEST_CODE_BASE + agendaItem.notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.cancel(pendingIntent)
            Log.d(TAG, "Reminder cancelled: itemId=${agendaItem.id}, notificationId=${agendaItem.notificationId}")
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling reminder: ${e.message}", e)
        }
    }

    /**
     * Reschedules a reminder by cancelling the old one and scheduling a new one.
     * Used when reminder settings are updated.
     *
     * @param oldAgendaItem The previous agenda item state
     * @param newAgendaItem The updated agenda item state
     * @return Updated agenda item with new notification metadata
     */
    fun rescheduleReminder(oldAgendaItem: AgendaItem, newAgendaItem: AgendaItem): AgendaItem {
        // Cancel old reminder if it exists
        if (oldAgendaItem.notificationId != 0) {
            cancelReminder(oldAgendaItem)
        }

        // Schedule new reminder
        return scheduleReminder(newAgendaItem)
    }
}

/**
 * Generates unique notification IDs based on agenda item IDs.
 * Uses a hash of the item ID to create a stable, unique integer.
 */
private class NotificationIdGenerator {
    fun generateId(agendaItemId: String): Int {
        // Use hash code of the item ID, ensure it's positive and within reasonable range
        return Math.abs(agendaItemId.hashCode()) % 100000
    }
}
