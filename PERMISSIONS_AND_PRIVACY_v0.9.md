# Permissions and Privacy v0.9.0

**Status**: Pilot Release  
**Version**: v0.9.0  
**Audience**: Users, Privacy advocates, Security auditors

---

## Privacy Philosophy

Bitacora Pro is built on a **local-first, privacy-first** philosophy:

- **Your data stays on your device** by default
- **No cloud sync** without your explicit action
- **No tracking** of your activities
- **No ads** or analytics
- **No third-party data sharing**
- **You control** what gets sent where

---

## Data Storage

### Local Storage
- All activities, evidence, and pending items stored on device
- Stored in app-private directory (not accessible to other apps)
- Encrypted at rest using Android's built-in encryption
- Survives app uninstall only if you export manually

### Cloud Storage
- **None by default**
- Optional: You can export PDF reports to share
- Optional: You can send WhatsApp chats manually
- Optional: Future remote assistant requires explicit consent

---

## Permissions Explained

### 1. CAMERA Permission

**Why**: To take photos of evidence directly in the app

**What it does**:
- Allows app to access device camera
- Only when you tap "Tomar foto" button
- Photos saved to app-private storage
- You can review before saving

**What it doesn't do**:
- ❌ Doesn't access photos you already took
- ❌ Doesn't upload photos anywhere
- ❌ Doesn't run in background
- ❌ Doesn't access camera without your action

**How to revoke**:
- Settings → Apps → Bitacora Pro → Permissions → Camera → Deny

---

### 2. READ_CONTACTS Permission

**Why**: To import contact name and phone number

**What it does**:
- Allows app to read your contacts
- Only when you tap "Importar contacto" button
- Only reads name and phone number
- Other contact data is ignored

**What it doesn't do**:
- ❌ Doesn't upload contacts anywhere
- ❌ Doesn't modify your contacts
- ❌ Doesn't access contacts without your action
- ❌ Doesn't run in background

**How to revoke**:
- Settings → Apps → Bitacora Pro → Permissions → Contacts → Deny

---

### 3. POST_NOTIFICATIONS Permission

**Why**: To send you reminders about pending tasks

**What it does**:
- Allows app to send notifications
- Only for pending item reminders
- Only if you set a due date and enable reminder
- You can disable in app settings

**What it doesn't do**:
- ❌ Doesn't send marketing notifications
- ❌ Doesn't send unsolicited messages
- ❌ Doesn't track notification opens
- ❌ Doesn't share notification data

**How to revoke**:
- Settings → Apps → Bitacora Pro → Permissions → Notifications → Deny

---

### 4. SCHEDULE_EXACT_ALARM Permission

**Why**: To schedule exact reminder times

**What it does**:
- Allows app to set exact alarm times
- Used for pending item reminders
- Respects device battery saver mode
- Can be disabled in app settings

**What it doesn't do**:
- ❌ Doesn't run other background tasks
- ❌ Doesn't drain battery excessively
- ❌ Doesn't track your schedule
- ❌ Doesn't share timing data

**How to revoke**:
- Settings → Apps → Bitacora Pro → Permissions → Alarms → Deny

---

### 5. RECEIVE_BOOT_COMPLETED Permission

**Why**: To restore reminders after device restart

**What it does**:
- Allows app to run when device boots
- Only restores pending item reminders
- Doesn't run any other background tasks
- Minimal battery impact

**What it doesn't do**:
- ❌ Doesn't run other background services
- ❌ Doesn't track boot events
- ❌ Doesn't send data anywhere
- ❌ Doesn't slow down boot

**How to revoke**:
- Settings → Apps → Bitacora Pro → Permissions → (usually can't revoke, but app respects if disabled)

---

### 6. INTERNET Permission (Optional)

**Why**: Only if you enable remote AI assistant

**What it does**:
- Allows app to connect to Bitacora backend
- Only for AI assistant questions
- Only when you explicitly ask a question
- Requires your consent before sending activity data

**What it doesn't do**:
- ❌ Doesn't connect without your action
- ❌ Doesn't send data without consent
- ❌ Doesn't run in background
- ❌ Doesn't track your usage

**How to revoke**:
- Settings → Apps → Bitacora Pro → Permissions → Internet → Deny
- Or disable remote assistant in app settings

**Status in v0.9.0**: Not added (local assistant only)

---

## Permissions NOT Requested

### Why We Don't Ask For These

| Permission | Why Not |
|-----------|---------|
| MANAGE_EXTERNAL_STORAGE | We use app-private storage only |
| READ_EXTERNAL_STORAGE | We use file picker (user chooses files) |
| WRITE_EXTERNAL_STORAGE | We use app-private storage only |
| ACCESS_FINE_LOCATION | We don't track location |
| ACCESS_COARSE_LOCATION | We don't track location |
| RECORD_AUDIO | We don't record audio |
| BODY_SENSORS | We don't access sensors |
| CALENDAR | We use system calendar intent (no permission needed) |
| ACCESSIBILITY_SERVICE | We don't need accessibility access |
| NOTIFICATION_LISTENER_SERVICE | We don't listen to other notifications |

---

## Data Sharing

### What We Share
- **Nothing** by default
- **Only what you explicitly export**:
  - PDF reports (you choose who to send to)
  - WhatsApp chats (you choose to share)
  - Text notes (you choose to copy/share)

### What We Never Share
- ❌ Your activities with anyone
- ❌ Your evidence with anyone
- ❌ Your pending items with anyone
- ❌ Your phone number with anyone
- ❌ Your contact information with anyone
- ❌ Your usage patterns with anyone
- ❌ Your location with anyone
- ❌ Your device information with anyone

### Third-Party Services
- **None** in v0.9.0
- **Optional in future**: Remote AI assistant (with your consent)
- **Never**: Analytics, ads, tracking, or marketing

---

## Remote AI Assistant (Future)

### How It Works
1. You ask a question in the app
2. App shows consent dialog:
   - "Para responder mejor, se enviará un resumen de esta actividad al asistente en línea. ¿Continuar?"
3. You choose "Continuar" or "Cancelar"
4. If you continue:
   - Activity summary sent to Bitacora backend
   - Backend sends to LLM provider
   - Response returned to app
   - Summary deleted from backend after 24 hours

### What Gets Sent
- Activity title
- Client name
- Phone number
- Status
- Evidence count
- Pending count
- First 200 characters of notes

### What Never Gets Sent
- ❌ Full evidence content
- ❌ Full note text
- ❌ Photos or documents
- ❌ WhatsApp chats
- ❌ Personal data beyond what you provide

### Your Control
- ✅ You can disable remote assistant
- ✅ You can see what's being sent
- ✅ You can choose not to send
- ✅ You can delete activity anytime
- ✅ You can export your data anytime

---

## Data Deletion

### How to Delete Your Data

**Delete Single Activity**:
1. Open activity
2. Tap "Archivar actividad"
3. Go to Archivadas tab
4. Swipe to delete (or long-press)

**Delete All Data**:
1. Settings → Apps → Bitacora Pro
2. Tap "Storage" → "Clear Data"
3. Confirm deletion
4. All data permanently deleted

**Export Before Deleting**:
1. Open activity
2. Tap "Exportar reporte PDF"
3. Save PDF to your device
4. Then delete activity

---

## Security

### Device Security
- Data stored in app-private directory
- Only accessible to Bitacora Pro app
- Protected by Android's permission system
- Encrypted at rest on modern Android devices

### Network Security
- All backend communication uses HTTPS
- SSL/TLS encryption in transit
- Certificate pinning (future enhancement)
- No data sent over unencrypted connections

### Authentication
- Device token for backend access
- Token expires after 30 days
- Automatic refresh before expiry
- No passwords or API keys stored

---

## Compliance

### GDPR (EU)
- ✅ Data stored locally by default
- ✅ User consent for remote processing
- ✅ Right to access data (export PDF)
- ✅ Right to delete data (clear app data)
- ✅ No third-party data sharing

### CCPA (California)
- ✅ No personal information collection
- ✅ No data selling
- ✅ User control over data
- ✅ Right to delete data

### LGPD (Brazil)
- ✅ Data stored locally
- ✅ User consent for processing
- ✅ Right to access and delete
- ✅ No unauthorized sharing

---

## Privacy Settings

### In-App Settings
- [ ] Enable/disable remote assistant
- [ ] Enable/disable notifications
- [ ] Enable/disable reminders
- [ ] Clear app data
- [ ] Export all data

### System Settings
- Settings → Apps → Bitacora Pro → Permissions
- Disable any permission you don't want to grant
- App will work with reduced functionality

---

## Transparency

### What We Track
- ❌ Nothing (no analytics)
- ❌ No usage statistics
- ❌ No crash reports
- ❌ No user behavior
- ❌ No device information

### What We Log
- ✅ Backend API calls (for debugging)
- ✅ Error messages (for fixing bugs)
- ✅ Anonymized logs (no personal data)
- ✅ Logs deleted after 30 days

---

## Questions & Support

### Privacy Questions
- Email: privacy@bitacora.example.com
- Response time: 48 hours

### Data Access Requests
- Email: data@bitacora.example.com
- Include: Device ID, activity ID
- Response time: 7 days

### Data Deletion Requests
- Email: delete@bitacora.example.com
- Include: Device ID
- Response time: 24 hours

---

## Changes to This Policy

- We may update this policy
- We will notify you in-app
- You can review changes anytime
- You can delete app if you disagree

---

## Summary

**Bitacora Pro v0.9.0 is designed with privacy as a core principle:**

- Your data stays on your device
- You control what gets shared
- You can delete everything anytime
- We don't track or sell your data
- We're transparent about what we do
- You can revoke permissions anytime

---

**Version**: v0.9.0 Pilot  
**Last Updated**: June 2026  
**Status**: Ready for Review
