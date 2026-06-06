# Bitacora Pro - Field Work Evidence Organizer

A field-work evidence organizer Android app built with Kotlin and Jetpack Compose. The app allows users to receive shared content (text, images, audio, PDFs) from WhatsApp or any Android app via share intents, organize evidence into jobs, and store everything locally in app-private storage.

## Features

### MVP (Minimum Viable Product)

- **Share Intent Integration**: Appears in Android share sheet for:
  - Text (ACTION_SEND)
  - Single images (ACTION_SEND)
  - Multiple images (ACTION_SEND_MULTIPLE)
  - Audio files (ACTION_SEND)
  - PDF files (ACTION_SEND)

- **Job Management**:
  - Create new jobs with metadata (title, client name, phone, service type)
  - View list of all jobs
  - View detailed job information with evidence

- **Evidence Management**:
  - Attach shared content to new or existing jobs
  - Categorize evidence (UNCLASSIFIED, BEFORE, DURING, AFTER, MATERIAL, PAYMENT, CLIENT_MESSAGE)
  - Delete evidence items
  - View evidence grouped by category

- **Local Storage**:
  - All data stored in app-private storage (no backend required)
  - Job metadata stored as JSON files
  - Evidence files stored in job-specific folders
  - Persistent storage survives app restarts

### Architecture

- **Single Activity**: MainActivity with Jetpack Compose navigation
- **Data Models**:
  - `JobFile`: Represents a job with metadata and evidence list
  - `EvidenceItem`: Represents a single evidence item
  - `JobStatus`: Enum for job status (ACTIVE, COMPLETED, ARCHIVED)
  - `EvidenceType`: Enum for evidence type (TEXT, IMAGE, AUDIO, PDF)
  - `EvidenceCategory`: Enum for evidence category

- **Storage**:
  - `StorageManager`: Handles all file I/O operations
  - Storage structure:
    ```
    filesDir/
      jobs/
        <job-id>/
          job.json
          evidence/
            <evidence-id>.<ext>
    ```

- **UI Screens**:
  - `HomeScreen`: Lists all jobs with FAB to create new job
  - `ShareIntakeScreen`: Displays incoming shared content, allows creating new job or adding to existing
  - `CreateJobScreen`: Form to create new job with metadata
  - `JobDetailScreen`: Shows job details and evidence grouped by category

## Project Structure

```
Bitacora Pro/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/bitacora/pro/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── data/
│   │   │   │   │   ├── models/
│   │   │   │   │   │   └── Models.kt
│   │   │   │   │   └── storage/
│   │   │   │   │       └── StorageManager.kt
│   │   │   │   └── ui/
│   │   │   │       ├── navigation/
│   │   │   │       │   └── NavRoutes.kt
│   │   │   │       ├── screens/
│   │   │   │       │   ├── HomeScreen.kt
│   │   │   │       │   ├── ShareIntakeScreen.kt
│   │   │   │       │   ├── CreateJobScreen.kt
│   │   │   │       │   └── JobDetailScreen.kt
│   │   │   │       └── theme/
│   │   │   │           ├── Theme.kt
│   │   │   │           └── Type.kt
│   │   │   ├── AndroidManifest.xml
│   │   │   └── res/
│   │   │       ├── values/
│   │   │       │   ├── strings.xml
│   │   │       │   ├── colors.xml
│   │   │       │   └── styles.xml
│   │   │       └── xml/
│   │   │           ├── data_extraction_rules.xml
│   │   │           └── backup_rules.xml
│   │   └── test/
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

## Requirements

- Android SDK 26+ (API level 26)
- Android Studio Flamingo or later
- Kotlin 1.9.10+
- Gradle 8.1.0+

## Dependencies

- Jetpack Compose (Material 3)
- Jetpack Navigation Compose
- Jetpack Activity Compose
- Jetpack Lifecycle
- Gson (JSON serialization)

## Building the APK

### Prerequisites

1. Install Android Studio from [developer.android.com](https://developer.android.com/studio)
2. Install Android SDK (API 34 recommended)
3. Clone or download this project

### Build Steps

#### Using Android Studio GUI

1. Open the project in Android Studio
2. Wait for Gradle sync to complete
3. Go to **Build** → **Build Bundle(s) / APK(s)** → **Build APK(s)**
4. The APK will be generated at: `app/build/outputs/apk/debug/app-debug.apk`

#### Using Command Line

```bash
# Navigate to project directory
cd /path/to/Bitacora\ Pro

# Build debug APK
./gradlew assembleDebug

# Build release APK (requires signing configuration)
./gradlew assembleRelease
```

The debug APK will be located at:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Installing on Device/Emulator

```bash
# Using adb (Android Debug Bridge)
adb install app/build/outputs/apk/debug/app-debug.apk

# Or using Android Studio: Run → Run 'app'
```

## Testing the Share Intent

### Test Scenario 1: Share Photo from WhatsApp

1. **Setup**:
   - Install the debug APK on an Android device or emulator
   - Have WhatsApp installed with at least one photo in a chat

2. **Steps**:
   - Open WhatsApp and find a photo in a chat
   - Long-press the photo
   - Tap "Share"
   - Look for "Bitacora Pro" in the share sheet
   - Tap "Bitacora Pro"

3. **Expected Result**:
   - App opens to ShareIntakeScreen
   - Shows the shared photo in "Incoming Content"
   - User can create a new job or add to existing job
   - Photo is copied to app-private storage

### Test Scenario 2: Share Multiple Photos

1. **Setup**:
   - Have multiple photos available (WhatsApp, Gallery, etc.)

2. **Steps**:
   - Select multiple photos
   - Tap "Share"
   - Select "Bitacora Pro"

3. **Expected Result**:
   - ShareIntakeScreen shows all photos
   - All photos are copied when job is created/updated

### Test Scenario 3: Share Text

1. **Setup**:
   - Have text content to share (message, note, etc.)

2. **Steps**:
   - Select and copy text
   - Open share menu
   - Select "Bitacora Pro"

3. **Expected Result**:
   - ShareIntakeScreen shows text preview
   - Text is stored as evidence item

### Test Scenario 4: Create Job Manually

1. **Steps**:
   - Open Bitacora Pro
   - Tap FAB (+ button)
   - Fill in job details:
     - Title: "Inspection Job"
     - Client: "John Doe"
     - Phone: "555-1234"
     - Service: "Home Inspection"
   - Tap "Create Job"

2. **Expected Result**:
   - Job is created and saved
   - App navigates to JobDetailScreen
   - Job appears in HomeScreen list

### Test Scenario 5: Manage Evidence

1. **Steps**:
   - Open a job from HomeScreen
   - View evidence grouped by category
   - Tap category dropdown on evidence item
   - Select new category (e.g., "BEFORE")
   - Tap delete icon to remove evidence

2. **Expected Result**:
   - Evidence category updates immediately
   - Evidence is deleted from job
   - Job metadata is updated

### Test Scenario 6: Persistence

1. **Steps**:
   - Create a job with evidence
   - Close the app completely
   - Reopen the app

2. **Expected Result**:
   - Job and evidence still exist
   - All data is intact

## Storage Details

### Job Metadata (job.json)

```json
{
  "id": "uuid-string",
  "title": "Job Title",
  "clientName": "Client Name",
  "phone": "555-1234",
  "serviceType": "Service Type",
  "status": "ACTIVE",
  "createdAt": 1234567890000,
  "updatedAt": 1234567890000,
  "evidence": [
    {
      "id": "evidence-uuid",
      "type": "IMAGE",
      "category": "BEFORE",
      "fileName": "evidence-uuid.jpg",
      "textContent": "",
      "mimeType": "image/jpeg",
      "createdAt": 1234567890000,
      "notes": ""
    }
  ],
  "notes": ""
}
```

### File Storage

- All files stored in `context.filesDir` (app-private storage)
- No external storage permissions required
- Files are not accessible to other apps
- Survives app uninstall (backed up by Android)

## Share Intent Flow

1. **User shares content** from WhatsApp or another app
2. **Android system** shows share sheet with installed apps
3. **Bitacora Pro** appears as share target (via intent filters in AndroidManifest.xml)
4. **MainActivity** receives intent with:
   - `Intent.ACTION_SEND` or `Intent.ACTION_SEND_MULTIPLE`
   - `Intent.EXTRA_TEXT` (for text content)
   - `Intent.EXTRA_STREAM` (for file URIs)
5. **MainActivity** processes intent:
   - Extracts text and/or file URIs
   - Copies files from URI to temporary location
   - Creates `SharedContent` object
   - Navigates to `ShareIntakeScreen`
6. **User** creates new job or selects existing job
7. **Evidence** is attached to job and saved to app-private storage

## Intent Filters

The app registers for the following intent filters in AndroidManifest.xml:

```xml
<!-- Single text -->
<action android:name="android.intent.action.SEND" />
<data android:mimeType="text/plain" />

<!-- Single image -->
<action android:name="android.intent.action.SEND" />
<data android:mimeType="image/*" />

<!-- Multiple images -->
<action android:name="android.intent.action.SEND_MULTIPLE" />
<data android:mimeType="image/*" />

<!-- Audio -->
<action android:name="android.intent.action.SEND" />
<data android:mimeType="audio/*" />

<!-- PDF -->
<action android:name="android.intent.action.SEND" />
<data android:mimeType="application/pdf" />
```

## Known Limitations (MVP)

- No cloud sync
- No login/authentication
- No PDF generation
- No OCR
- No AI summaries
- No multi-user support
- No WhatsApp Business API integration
- Single activity architecture (no fragments)

## Future Enhancements

- Cloud backup and sync
- User authentication
- PDF report generation
- OCR for text extraction
- AI-powered summaries
- Multi-user support
- Offline-first sync
- Advanced search and filtering
- Evidence annotations
- Signature capture

## Troubleshooting

### App doesn't appear in share sheet

1. Ensure app is installed: `adb shell pm list packages | grep bitacora`
2. Check intent filters in AndroidManifest.xml
3. Restart device or clear app cache: `adb shell pm clear com.bitacora.pro`

### Files not being copied

1. Check app permissions: Settings → Apps → Bitacora Pro → Permissions
2. Verify file URI is valid
3. Check app-private storage: `adb shell ls -la /data/data/com.bitacora.pro/files/`

### App crashes on share

1. Check logcat: `adb logcat | grep bitacora`
2. Verify MIME type handling in MainActivity
3. Ensure ContentResolver.openInputStream() succeeds

### Jobs not persisting

1. Verify storage location: `adb shell ls -la /data/data/com.bitacora.pro/files/jobs/`
2. Check job.json file format
3. Ensure StorageManager.saveJobMetadata() is called

## License

This project is provided as-is for demonstration purposes.

## Contact

For questions or issues, please refer to the project documentation.
