package com.bitacora.pro.notifications

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/**
 * Helper class for managing notification permissions on Android 13+.
 * On Android 13 and above, POST_NOTIFICATIONS permission is required.
 * On earlier versions, no runtime permission is needed.
 */
object NotificationPermissionHelper {

    /**
     * Checks if the app has permission to post notifications.
     * Returns true if:
     * - Android version < 13 (permission not required)
     * - Android version >= 13 AND POST_NOTIFICATIONS permission is granted
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Permission not required on Android < 13
            true
        }
    }

    /**
     * Returns the permission string needed for notifications.
     * Returns POST_NOTIFICATIONS on Android 13+, null on earlier versions.
     */
    fun getNotificationPermission(): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.POST_NOTIFICATIONS
        } else {
            null
        }
    }
}
