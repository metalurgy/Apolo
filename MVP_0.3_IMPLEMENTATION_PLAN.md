# Bitacora Pro MVP 0.3 - Job Agenda + Assistant Lite Implementation Plan

## Overview
MVP 0.3 adds an internal agenda/task system per job and a simple local "Assistant Lite" that suggests agenda items from text evidence. This prepares the app for future AI integration while remaining fully local and backend-free.

## Files Created
1. **`app/src/main/java/com/bitacora/pro/assistant/AgendaSuggestionEngine.kt`** ✅
   - Local rule-based suggestion engine
   - Analyzes Spanish/English text
   - Detects payment, delivery, visit, quote, and report keywords
   - Returns max 5 suggestions per text
   - No API calls, no backend

## Files to Modify

### 1. Models.kt ✅
**Changes:**
- Added `enum class AgendaStatus { PENDING, DONE, CANCELLED }`
- Added `data class AgendaItem` with fields:
  - `id`, `jobId`, `title`, `description`
  - `dueAt` (nullable), `dueText` (human-readable)
  - `status`, `sourceEvidenceId`, `createdAt`, `updatedAt`
- Updated `JobFile` to include:
  - `agendaItems: List<AgendaItem> = emptyList()`
  - `lastUsedAt: Long = System.currentTimeMillis()`

**Backward Compatibility:**
- Old job.json files without `agendaItems` and `lastUsedAt` will load safely
- Missing fields default to empty list and current timestamp

### 2. StorageManager.kt (To be modified)
**New Methods to Add:**

```kotlin
fun addAgendaItemToJob(jobId: String, item: AgendaItem)
// - Load job
// - Add item with jobId set
// - Update job.updatedAt and job.lastUsedAt
// - Save job metadata

fun updateAgendaItemStatus(jobId: String, agendaItemId: String, status: AgendaStatus)
// - Load job
// - Find and update agenda item status
// - Update job.updatedAt and job.lastUsedAt
// - Save job metadata

fun deleteAgendaItem(jobId: String, agendaItemId: String)
// - Load job
// - Remove agenda item
// - Update job.updatedAt and job.lastUsedAt
// - Save job metadata
```

**Existing Method Updates:**
- `addEvidenceToJob()` - Also update `job.lastUsedAt` when evidence is added
- `loadJob()` - Add safe migration for missing `agendaItems` and `lastUsedAt` fields

**Safe Migration in loadJob():**
```kotlin
// Parse as JsonObject first
val jsonObject = JsonParser.parseString(jobFile.readText()).asJsonObject

// Add missing fields if needed
if (!jsonObject.has("agendaItems")) {
    jsonObject.add("agendaItems", JsonArray())
}
if (!jsonObject.has("lastUsedAt")) {
    jsonObject.addProperty("lastUsedAt", jsonObject.get("updatedAt").asLong)
}

// Now deserialize
return gson.fromJson(jsonObject, JobFile::class.java)
```

### 3. HomeScreen.kt (To be modified)
**Changes:**
- Sort jobs by `lastUsedAt` descending instead of `createdAt`
- Keep UI simple, no redesign
- Most recently used jobs appear first

### 4. ShareIntakeScreen.kt (To be modified)
**Changes:**
- Sort existing jobs by `lastUsedAt` descending
- Add button: "Add to most recent job"
- This button attaches incoming content to the first job in sorted list
- Keep existing "Create New Job" and job selection flow

### 5. JobDetailScreen.kt (To be modified)
**Major Changes:**

**New Agenda Section:**
- Display below job metadata
- Show agenda items grouped:
  - Pending items first
  - Done items below
  - Cancelled hidden or at bottom
- For each agenda item show:
  - `title`
  - `dueText` if present
  - `description` preview
  - `status`
  - `createdAt` formatted

**New UI Elements:**
- Button: "+ Add agenda item"
- Simple inline form or dialog:
  - Fields: `title`, `description`, `dueText`
  - Save button
- For each agenda item:
  - "Mark done" action
  - "Delete" action
- For TEXT evidence items:
  - "Suggest agenda" button
  - When tapped:
    - Call `AgendaSuggestionEngine.suggestFromText()`
    - Show suggestions in a confirmation UI
    - User can add all or one by one
    - Do NOT silently create agenda items

**Suggestion Confirmation UI:**
- Show list of suggested agenda items
- Each suggestion shows: title, dueText, description
- Each has an "Add" button
- User confirms before adding

## Data Storage
**No changes to storage layout:**
```
filesDir/jobs/<jobId>/
  job.json (now includes agendaItems and lastUsedAt)
  evidence/
    <evidence-id>.<ext>
```

## AgendaSuggestionEngine Details

**Keyword Detection:**
- **Payment:** pago, pagar, payment, pay, anticipo, liquidación, factura, invoice, cobro, dinero
- **Delivery:** entrega, entregar, delivery, instalación, instalar, install, envío, enviar, ship
- **Visit:** visita, visitar, visit, revisión, revisar, review, inspección, inspect, cita, appointment, reunión, meeting
- **Quote:** cotización, cotizar, quote, presupuesto, budget, estimado, estimate, proposal, propuesta
- **Report:** reporte, report, informe, documentación, documentation, garantía, warranty, certificado, certificate

**Date Keywords:**
- Spanish: hoy, mañana, pasado mañana, lunes-domingo, esta semana, próxima semana, el lunes, en la mañana, por la tarde, por la noche
- English: today, tomorrow, day after tomorrow, monday-sunday, this week, next week, in the morning, in the afternoon, in the evening

**Suggestion Logic:**
- Max 5 suggestions per text
- Avoid duplicates (one payment, one delivery, etc.)
- `dueAt` remains null (exact date parsing deferred)
- `dueText` preserves human-readable phrase
- `sourceEvidenceId` links back to text evidence
- `title` is short and actionable
- `description` includes text excerpt

## Manual Testing Steps

1. **Test Backward Compatibility:**
   - Open app with existing jobs
   - Confirm old jobs load without crashes
   - Confirm `agendaItems` is empty list
   - Confirm `lastUsedAt` is set to `updatedAt`

2. **Test Manual Agenda Item:**
   - Open a job
   - Tap "+ Add agenda item"
   - Enter: Title "Enviar cotización", Due text "mañana"
   - Save
   - Close and reopen app
   - Confirm agenda item persists

3. **Test Agenda Item Status:**
   - Mark agenda item as DONE
   - Confirm it moves to done section
   - Delete an agenda item
   - Confirm it's removed

4. **Test lastUsedAt Update:**
   - Share text from WhatsApp
   - Attach to a job
   - Confirm job moves to top of HomeScreen list

5. **Test Assistant Lite:**
   - Share text: "Te pago mañana y la instalación queda para el viernes"
   - Attach to job
   - Open job
   - Tap "Suggest agenda" on text evidence
   - Confirm suggestions appear:
     - "Follow up payment" with dueText "mañana"
     - "Delivery reminder" with dueText "viernes"
   - Add suggestions
   - Confirm they appear in Agenda section

6. **Test Share Intent:**
   - Share photo from WhatsApp
   - Confirm app appears in share sheet
   - Confirm "Add to most recent job" button works
   - Confirm photo is copied to job folder

7. **Test Build:**
   - Run `./gradlew clean assembleDebug`
   - Confirm BUILD SUCCESSFUL
   - Install APK on device
   - Confirm app runs without crashes

## Implementation Notes

**Backward Compatibility:**
- Use Gson's JsonObject parsing to safely add missing fields
- Default values ensure old jobs work immediately
- No migration script needed

**UI Complexity:**
- Keep agenda section simple
- Inline form for manual agenda items
- Simple confirmation dialog for suggestions
- No complex animations or transitions

**Performance:**
- AgendaSuggestionEngine is synchronous (fast keyword matching)
- No network calls
- No database queries
- Suitable for main thread

**Future Extensibility:**
- AgendaSuggestionEngine can be replaced with AI API later
- Agenda items structure supports future fields (reminders, notifications, etc.)
- `sourceEvidenceId` enables traceability

## Acceptance Criteria Status

- [ ] Existing jobs still load after model change
- [ ] User can open a job and add a manual agenda item
- [ ] Agenda item persists after app restart
- [ ] User can mark an agenda item as DONE
- [ ] User can delete an agenda item
- [ ] Adding evidence to a job updates lastUsedAt
- [ ] HomeScreen shows most recently used/updated jobs first
- [ ] Text evidence shows a "Suggest agenda" action
- [ ] Assistant Lite suggests agenda items from simple text
- [ ] Suggestions are not added silently; user must confirm
- [ ] Sharing content from WhatsApp still works
- [ ] Evidence storage still works under filesDir/jobs/<jobId>/evidence
- [ ] App builds and runs from Android Studio

## Build Constraints Met

- ✅ No backend
- ✅ No external AI API
- ✅ No Firebase
- ✅ No Room database
- ✅ No cloud sync
- ✅ No login
- ✅ No billing
- ✅ No OCR
- ✅ No PDF generation
- ✅ No package name change
- ✅ No share-intent architecture change
- ✅ No storage layout change
- ✅ No AGP Upgrade Assistant
- ✅ No Gradle version changes
- ✅ All code and comments in English

## Next Steps

1. Implement StorageManager changes with safe migration
2. Update HomeScreen to sort by lastUsedAt
3. Update ShareIntakeScreen with "Add to most recent job" button
4. Implement JobDetailScreen agenda section and UI
5. Test all acceptance criteria
6. Build and verify APK
