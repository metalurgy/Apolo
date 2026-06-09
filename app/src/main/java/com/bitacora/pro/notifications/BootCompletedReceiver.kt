package com.bitacora.pro.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.bitacora.pro.data.storage.StorageManager

/**
 * BroadcastReceiver that handles device boot completion.
 * Reschedules all pending agenda reminders after device restart.
 * This ensures reminders persist across device reboots.
 */
class BootCompletedReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootCompletedReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive: action=${intent.action}")

        // Only handle boot completed action
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) {
            return
        }

        try {
            Log.d(TAG, "Device boot completed, rescheduling reminders")
            rescheduleAllReminders(context)
        } catch (e: Exception) {
            Log.e(TAG, "Error rescheduling reminders: ${e.message}", e)
        }
    }

    /**
     * Reschedules all pending agenda reminders from all jobs.
     * Iterates through all jobs and their agenda items, rescheduling any with reminders enabled.
     */
    private fun rescheduleAllReminders(context: Context) {
        try {
            val storageManager = StorageManager(context)
            val scheduler = AgendaNotificationScheduler(context)

            // Load all jobs
            val allJobs = storageManager.loadAllJobs()
            Log.d(TAG, "Found ${allJobs.size} jobs to check for reminders")

            var rescheduledCount = 0

            // Iterate through all jobs and their agenda items
            for (job in allJobs) {
                for (agendaItem in job.agendaItems) {
                    // Only reschedule if reminder is enabled and has a due date
                    if (agendaItem.reminderEnabled && agendaItem.dueAt != null) {
                        val now = System.currentTimeMillis()
                        val reminderTimeMs = agendaItem.dueAt!! - (agendaItem.reminderOffsetDays.toLong() * 24 * 60 * 60 * 1000)

                        // Only reschedule if reminder time is still in the future
                        if (reminderTimeMs > now) {
                            scheduler.scheduleReminder(agendaItem)
                            rescheduledCount++
                            Log.d(TAG, "Rescheduled reminder for item: ${agendaItem.id}")
                        }
                    }
                }
            }

            Log.d(TAG, "Rescheduled $rescheduledCount reminders")
        } catch (e: Exception) {
            Log.e(TAG, "Error in rescheduleAllReminders: ${e.message}", e)
        }
    }
}
