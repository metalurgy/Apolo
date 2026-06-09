# Bitacora Pro MVP 0.5.2 - Quick Reference Guide

## Feature: Local Agenda Notifications

### What's New
Users can now set local reminder notifications for agenda items with due dates. Reminders are triggered using Android's AlarmManager and displayed as local notifications. All functionality is local-only with no backend integration.

### User Flow

#### Creating an Agenda Item with Reminder
1. Open a job detail screen
2. Click "+ Agregar" in the Agenda section
3. Fill in title, description, and due date
4. **NEW**: Toggle "Recordatorio" button to enable reminders
5. **NEW**: Select offset days (0, 1, 2, 3, or 7 days before due date)
6. Click "Agregar Elemento"
7. Reminder is scheduled automatically

#### Viewing Reminder Status
- Open job detail screen
- Look for 🔔 bell emoji in agenda item card
- Shows reminder offset (e.g., "1 día antes")

#### Cancelling a Reminder
- Mark item as DONE → reminder automatically cancelled
- Delete item → reminder automatically cancelled

### Key Components

| Component | File | Purpose |
|-----------|------|---------|
| **Data Model** | `Models.kt` | Extended `AgendaItem` with reminder fields |
| **Permission Helper** | `NotificationPermissionHelper.kt` | Handles Android 13+ notification permission |
| **Scheduler** | `AgendaNotificationScheduler.kt` | Schedules/cancels alarms with AlarmManager |
| **Receiver** | `AgendaReminderReceiver.kt` | Displays notification when alarm triggers |
| **Boot Receiver** | `BootCompletedReceiver.kt` | Reschedules reminders after device restart |
| **Storage** | `StorageManager.kt` | Cancels reminders on delete/done |
| **UI** | `JobDetailScreen.kt` | Reminder dropdown and display |
| **Manifest** | `AndroidManifest.xml` | Permissions and receiver registration |
| **MainActivity** | `MainActivity.kt` | Creates notification channel |
| **Strings** | `strings.xml` | Spanish labels and text |

### Reminder Offset Options
- **0 días** → "El Día del Vencimiento" (on due date)
- **1 día** → "1 Día Antes"
- **2 días** → "2 Días Antes"
- **3 días** → "3 Días Antes"
- **7 días** → "1 Semana Antes"

### Technical Details

#### Reminder Calculation
```
Reminder Time = Due Date - (Offset Days × 24 hours)
```

#### Notification ID Generation
```
Stable ID = abs(agendaItemId.hashCode()) % 100000
```

#### Alarm Scheduling
- **Android 12+**: Uses `setExactAndAllowWhileIdle()` if permission granted, falls back to `setAndAllowWhileIdle()`
- **Android < 12**: Uses `setExactAndAllowWhileIdle()`
- **All versions**: Respects Doze mode with `AllowWhileIdle` flag

#### Permission Handling
- **Android 13+**: Requires `POST_NOTIFICATIONS` permission
- **Android < 13**: No runtime permission needed
- **Android 12+**: Requires `SCHEDULE_EXACT_ALARM` permission (with fallback)

### Data Persistence

#### AgendaItem Fields
```kotlin
reminderEnabled: Boolean = false          // Is reminder active?
reminderOffsetDays: Int = 0               // Days before due date
reminderScheduledAt: Long? = null         // When was it scheduled?
notificationId: Int = 0                   // Unique alarm ID
```

#### Saved in JSON
All reminder fields are automatically saved in `job.json` when agenda item is created/updated.

### Manifest Permissions
```xml
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
```

### Manifest Receivers
```xml
<receiver android:name=".notifications.AgendaReminderReceiver" />
<receiver android:name=".notifications.BootCompletedReceiver" />
```

### Notification Channel
- **Channel ID**: `agenda_reminders`
- **Name**: "Recordatorios de Agenda"
- **Importance**: HIGH
- **Features**: Vibration, Lights enabled

### Testing Checklist

#### Basic Functionality
- [ ] Create agenda item with reminder enabled
- [ ] Verify reminder shows in UI with bell emoji
- [ ] Verify reminder offset displays correctly
- [ ] Disable reminder and verify bell emoji disappears

#### Notification Triggering
- [ ] Wait for reminder time (or use adb to trigger)
- [ ] Verify notification appears on device
- [ ] Verify notification title includes agenda item title
- [ ] Verify notification is dismissible

#### Reminder Cancellation
- [ ] Mark item as DONE → verify notification cancelled
- [ ] Delete item → verify notification cancelled
- [ ] Verify no orphaned alarms in AlarmManager

#### Device Restart
- [ ] Create reminder with future trigger time
- [ ] Restart device
- [ ] Verify reminder is rescheduled
- [ ] Verify reminder still triggers at correct time

#### Permission Handling
- [ ] Test on Android 13+ device (notification permission)
- [ ] Test on Android 12 device (exact alarm permission)
- [ ] Test on Android 11 device (no special permissions)
- [ ] Deny notification permission and verify graceful handling

#### Edge Cases
- [ ] Create reminder with past due date (should not schedule)
- [ ] Create reminder with due date in 1 hour (should schedule)
- [ ] Create multiple reminders for same job
- [ ] Test all 5 offset options (0, 1, 2, 3, 7)

### Debugging

#### Enable Logging
All components use `Log.d()` with TAG:
- `NotificationPermissionHelper` - Permission checks
- `AgendaNotificationScheduler` - Scheduling/cancellation
- `AgendaReminderReceiver` - Notification display
- `BootCompletedReceiver` - Boot reschedule

#### View Logs
```bash
adb logcat | grep -E "NotificationPermissionHelper|AgendaNotificationScheduler|AgendaReminderReceiver|BootCompletedReceiver"
```

#### Trigger Reminder Manually
```bash
adb shell am broadcast -a com.bitacora.pro.AGENDA_REMINDER \
  -e agendaItemId "YOUR_ITEM_ID" \
  -e jobId "YOUR_JOB_ID" \
  -e title "Test Reminder" \
  -e notificationId 12345
```

#### Check Scheduled Alarms
```bash
adb shell dumpsys alarm | grep "com.bitacora.pro"
```

### Known Limitations
- No snooze functionality
- No reminder editing (must delete and recreate)
- No custom notification sounds
- No reminder history/logs
- Single reminder per item (not recurring)

### Future Enhancements
- Reminder editing UI
- Snooze functionality (5, 10, 15 minutes)
- Custom notification sounds
- Reminder history/logs
- Multiple reminders per item
- Recurring reminders
- Notification actions (Mark Done, Snooze)

### Version Info
- **MVP**: 0.5.2
- **Build**: ✅ SUCCESSFUL
- **Errors**: 0
- **Warnings**: 0 (Gradle deprecation only)

### Support
For issues or questions:
1. Check logs using adb logcat
2. Verify permissions in AndroidManifest.xml
3. Ensure notification channel is created in MainActivity
4. Check AgendaItem has valid dueAt timestamp
5. Verify reminder offset is 0-7 days

---
**Last Updated**: June 9, 2026
**Status**: Ready for Testing
