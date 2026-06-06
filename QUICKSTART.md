# Bitacora Pro - Quick Start Guide

## 5-Minute Setup

### 1. Prerequisites
- Android Studio installed
- Android SDK 26+ (API level 26)
- Android device or emulator with Android 8.0+

### 2. Build the APK

```bash
# Navigate to project directory
cd /path/to/Bitacora\ Pro

# Build debug APK
./gradlew assembleDebug
```

APK location: `app/build/outputs/apk/debug/app-debug.apk`

### 3. Install on Device/Emulator

```bash
# Using adb
adb install app/build/outputs/apk/debug/app-debug.apk

# Or in Android Studio: Run → Run 'app'
```

### 4. Test Share Intent

**From WhatsApp:**
1. Open WhatsApp
2. Find a photo in a chat
3. Long-press → Share
4. Select "Bitacora Pro"
5. Create a new job or add to existing
6. Photo is saved to app-private storage

**From Gallery:**
1. Open Gallery app
2. Select one or multiple photos
3. Tap Share
4. Select "Bitacora Pro"
5. Create job with metadata
6. Photos are copied and saved

## Files Created

### Core Application
- [`MainActivity.kt`](app/src/main/java/com/bitacora/pro/MainActivity.kt) - Single activity with navigation and share intent handling
- [`AndroidManifest.xml`](app/src/main/AndroidManifest.xml) - App configuration and intent filters

### Data Layer
- [`Models.kt`](app/src/main/java/com/bitacora/pro/data/models/Models.kt) - Data classes (JobFile, EvidenceItem, enums)
- [`StorageManager.kt`](app/src/main/java/com/bitacora/pro/data/storage/StorageManager.kt) - File I/O and storage operations

### UI Layer
- [`HomeScreen.kt`](app/src/main/java/com/bitacora/pro/ui/screens/HomeScreen.kt) - Job list and create button
- [`ShareIntakeScreen.kt`](app/src/main/java/com/bitacora/pro/ui/screens/ShareIntakeScreen.kt) - Share intent handler UI
- [`CreateJobScreen.kt`](app/src/main/java/com/bitacora/pro/ui/screens/CreateJobScreen.kt) - Job creation form
- [`JobDetailScreen.kt`](app/src/main/java/com/bitacora/pro/ui/screens/JobDetailScreen.kt) - Job details and evidence management

### Navigation
- [`NavRoutes.kt`](app/src/main/java/com/bitacora/pro/ui/navigation/NavRoutes.kt) - Navigation route definitions

### Theme
- [`Theme.kt`](app/src/main/java/com/bitacora/pro/ui/theme/Theme.kt) - Material Design 3 theme
- [`Type.kt`](app/src/main/java/com/bitacora/pro/ui/theme/Type.kt) - Typography definitions

### Build Configuration
- [`build.gradle.kts`](build.gradle.kts) - Root build configuration
- [`app/build.gradle.kts`](app/build.gradle.kts) - App-level build configuration
- [`settings.gradle.kts`](settings.gradle.kts) - Project settings
- [`app/proguard-rules.pro`](app/proguard-rules.pro) - ProGuard rules

### Resources
- [`strings.xml`](app/src/main/res/values/strings.xml) - String resources
- [`colors.xml`](app/src/main/res/values/colors.xml) - Color definitions
- [`styles.xml`](app/src/main/res/values/styles.xml) - Style definitions
- [`data_extraction_rules.xml`](app/src/main/res/xml/data_extraction_rules.xml) - Data extraction rules
- [`backup_rules.xml`](app/src/main/res/xml/backup_rules.xml) - Backup rules

### Documentation
- [`README.md`](README.md) - Comprehensive documentation
- [`QUICKSTART.md`](QUICKSTART.md) - This file

## Share Intent Flow

```
User shares content from WhatsApp/Gallery
         ↓
Android shows share sheet
         ↓
User selects "Bitacora Pro"
         ↓
MainActivity receives ACTION_SEND or ACTION_SEND_MULTIPLE
         ↓
MainActivity extracts text and/or file URIs
         ↓
Files are copied to app-private storage
         ↓
ShareIntakeScreen displays incoming content
         ↓
User creates new job or selects existing job
         ↓
Evidence is attached to job
         ↓
Job metadata saved as JSON
         ↓
Evidence files stored in job folder
```

## Storage Structure

```
/data/data/com.bitacora.pro/files/
└── jobs/
    ├── job-id-1/
    │   ├── job.json
    │   └── evidence/
    │       ├── evidence-id-1.jpg
    │       ├── evidence-id-2.jpg
    │       └── evidence-id-3.pdf
    └── job-id-2/
        ├── job.json
        └── evidence/
            └── evidence-id-4.mp3
```

## Key Features Implemented

✅ Share intent handling (ACTION_SEND, ACTION_SEND_MULTIPLE)
✅ Text, image, audio, and PDF support
✅ Job creation with metadata
✅ Evidence categorization (7 categories)
✅ Local-only storage (no backend)
✅ Persistent data (survives app restart)
✅ Single Activity architecture
✅ Jetpack Compose UI
✅ Material Design 3 theme
✅ Navigation between screens

## Testing Checklist

- [ ] App appears in share sheet
- [ ] Single photo share works
- [ ] Multiple photo share works
- [ ] Text share works
- [ ] Audio share works
- [ ] PDF share works
- [ ] Job creation works
- [ ] Evidence categorization works
- [ ] Evidence deletion works
- [ ] Data persists after app restart
- [ ] APK builds without errors

## Troubleshooting

**App doesn't appear in share sheet:**
```bash
adb shell pm clear com.bitacora.pro
adb install app/build/outputs/apk/debug/app-debug.apk
```

**Check app storage:**
```bash
adb shell ls -la /data/data/com.bitacora.pro/files/jobs/
```

**View logs:**
```bash
adb logcat | grep bitacora
```

**Verify installation:**
```bash
adb shell pm list packages | grep bitacora
```

## Next Steps

1. Build and install the APK
2. Test sharing from WhatsApp or Gallery
3. Create a job and verify data persistence
4. Review code and customize as needed
5. Deploy to production when ready

For detailed information, see [`README.md`](README.md).
