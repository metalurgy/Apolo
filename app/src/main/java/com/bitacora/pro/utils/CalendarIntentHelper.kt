package com.bitacora.pro.utils

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import com.bitacora.pro.data.models.AgendaItem

/**
 * Helper for creating calendar events via intent.
 * v0.8.0: Intent-based calendar integration (no API calls)
 *
 * Features:
 * - Create calendar event from agenda item
 * - Pre-fill event details
 * - Works with Google Calendar and other calendar apps
 * - Graceful fallback if app not installed
 */
object CalendarIntentHelper {

    /**
     * Creates a calendar event from an agenda item.
     * Uses intent to open calendar app with pre-filled details.
     */
    fun createCalendarEvent(context: Context, agendaItem: AgendaItem, jobTitle: String) {
        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, agendaItem.title)
            putExtra(CalendarContract.Events.DESCRIPTION, buildEventDescription(agendaItem, jobTitle))
            
            // Set time if available
            if (agendaItem.dueAt != null) {
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, agendaItem.dueAt)
                // Set end time to 1 hour after start
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, agendaItem.dueAt + (60 * 60 * 1000))
            }
            
            // Set as all-day event if no specific time
            if (agendaItem.dueAt == null) {
                putExtra(CalendarContract.Events.ALL_DAY, true)
            }
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Calendar app not installed or error occurred
            e.printStackTrace()
        }
    }

    /**
     * Builds event description from agenda item and job title.
     */
    private fun buildEventDescription(agendaItem: AgendaItem, jobTitle: String): String {
        val sb = StringBuilder()
        sb.append("Trabajo: $jobTitle\n")
        if (agendaItem.description.isNotEmpty()) {
            sb.append("Descripción: ${agendaItem.description}\n")
        }
        sb.append("Creado en: Bitacora Pro")
        return sb.toString()
    }

    /**
     * Opens calendar app to view a specific date.
     */
    fun openCalendarForDate(context: Context, timestamp: Long) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = CalendarContract.CONTENT_URI.buildUpon()
                .appendPath("time")
                .appendPath(timestamp.toString())
                .build()
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Checks if calendar app is available.
     */
    fun isCalendarAppAvailable(context: Context): Boolean {
        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
        }
        return intent.resolveActivity(context.packageManager) != null
    }
}
