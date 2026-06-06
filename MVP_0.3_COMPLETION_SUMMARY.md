# MVP 0.3 "Job Agenda + Assistant Lite" - Implementation Complete

## Overview
MVP 0.3 adds a local, rule-based agenda system with AI-free suggestion engine to Bitacora Pro. All features remain fully local with no backend, APIs, Firebase, or cloud sync.

## Implementation Summary

### Part 1: StorageManager Safe Migration ✅
**File:** [`app/src/main/java/com/bitacora/pro/data/storage/StorageManager.kt`](app/src/main/java/com/bitacora/pro/data/storage/StorageManager.kt)

**Changes:**
- Updated `loadJob(jobId: String)` with safe backward compatibility using JsonObject parsing
- Handles old job.json files missing `agendaItems`, `lastUsedAt`, or `evidence` fields
- Fallback strategy: uses `updatedAt` → `createdAt` → current time for `lastUsedAt`
- Added imports: `JsonArray`, `JsonObject`, `JsonParser`

**New Methods:**
- `addAgendaItemToJob(jobId, agendaItem)` - Adds agenda item and updates lastUsedAt
- `updateAgendaItemStatus(jobId, agendaItemId, newStatus)` - Updates status (PENDING/DONE/CANCELLED)
- `deleteAgendaItem(jobId, agendaItemId)` - Removes agenda item

**Key Feature:** All agenda operations update `lastUsedAt` to track job usage.

---

### Part 2: Agenda Storage Operations ✅
**File:** [`app/src/main/java/com/bitacora/pro/data/storage/StorageManager.kt`](app/src/main/java/com/bitacora/pro/data/storage/StorageManager.kt)

**Methods Implemented:**
```kotlin
fun addAgendaItemToJob(jobId: String, agendaItem: AgendaItem)
fun updateAgendaItemStatus(jobId: String, agendaItemId: String, newStatus: String)
fun deleteAgendaItem(jobId: String, agendaItemId: String)
```

**Behavior:**
- All operations load job, modify agenda list, update timestamps, and save
- `lastUsedAt` is always updated to current time
- `updatedAt` is always updated to current time
- Safe error handling with try-catch

---

### Part 3: HomeScreen Sorting by lastUsedAt ✅
**File:** [`app/src/main/java/com/bitacora/pro/ui/screens/HomeScreen.kt`](app/src/main/java/com/bitacora/pro/ui/screens/HomeScreen.kt)

**Changes:**
- Jobs loaded and sorted by `lastUsedAt` descending (most recent first)
- JobCard displays `lastUsedAt` instead of `createdAt`
- Most recently used jobs appear at top of list

---

### Part 4: ShareIntakeScreen "Add to Most Recent Job" ✅
**File:** [`app/src/main/java/com/bitacora/pro/ui/screens/ShareIntakeScreen.kt`](app/src/main/java/com/bitacora/pro/ui/screens/ShareIntakeScreen.kt)

**Changes:**
- Jobs sorted by `lastUsedAt` descending
- Prominent "Add to Most Recent Job" button at top of job list
- Falls back to manual job selection if no jobs exist
- Text changed from "Add to existing job:" to "Or select another job:"

**UI Flow:**
1. Display shared content summary
2. Show "Add to Most Recent Job" button (if jobs exist)
3. Show list of other jobs for manual selection
4. Action buttons: "Create New Job" and "Add to Job" (if selected)

---

### Part 5: JobDetailScreen Agenda Section ✅
**File:** [`app/src/main/java/com/bitacora/pro/ui/screens/JobDetailScreen.kt`](app/src/main/java/com/bitacora/pro/ui/screens/JobDetailScreen.kt)

**New Composable: `AgendaSection`**
- Displays agenda items grouped by status (Pending first, then Done)
- Manual agenda creation form with inline fields:
  - Title (required)
  - Description (optional)
  - Due date text (e.g., "mañana", "viernes")
- "+ Add" button toggles form visibility
- Shows "No agenda items yet" when empty

**New Composable: `AgendaItemCard`**
- Displays title, due date, and description preview
- "Mark Done" / "Reopen" button to toggle status
- Delete button to remove item
- Styled with surface color

**Integration:**
- AgendaSection appears after JobMetadataCard, before Evidence section
- Updates trigger job reload to reflect changes

---

### Part 6: Assistant Lite Suggestion UI ✅
**File:** [`app/src/main/java/com/bitacora/pro/ui/screens/JobDetailScreen.kt`](app/src/main/java/com/bitacora/pro/ui/screens/JobDetailScreen.kt)

**Changes to EvidenceCard:**
- TEXT evidence items show "Suggest Agenda" button
- Button triggers `AgendaSuggestionEngine.suggestFromText()`
- Suggestions displayed in collapsible UI below text preview

**New Composable: `SuggestionCard`**
- Shows suggestion title, due date, and description preview
- "Add" button to accept suggestion
- Styled with primaryContainer color for visibility
- Clicking "Add" creates agenda item and closes suggestions

**Suggestion Flow:**
1. User views TEXT evidence
2. Clicks "Suggest Agenda" button
3. Engine analyzes text and returns up to 5 suggestions
4. User reviews suggestions and clicks "Add" to accept
5. Agenda item created with sourceEvidenceId reference
6. Suggestions UI closes

---

### Part 7: Improved Date Extraction ✅
**File:** [`app/src/main/java/com/bitacora/pro/assistant/AgendaSuggestionEngine.kt`](app/src/main/java/com/bitacora/pro/assistant/AgendaSuggestionEngine.kt)

**Enhanced `extractDueText()` Method:**
- Window-based approach: finds context keyword first, then searches ±60 characters
- Checks longer date phrases before shorter ones (avoids "lunes" matching "el lunes")
- Returns most relevant date keyword found near context

**Algorithm:**
1. Sort date keywords by length (longest first)
2. Find position of first context keyword (payment, delivery, visit, quote, report)
3. If found, search within ±60 character window
4. If not found, search entire text
5. Return first matching date keyword

**Supported Date Keywords:**
- Spanish: hoy, mañana, pasado mañana, lunes-domingo, esta semana, próxima semana, el lunes-domingo, en la mañana, por la tarde, por la noche
- English: today, tomorrow, day after tomorrow, monday-sunday, this week, next week, in the morning, in the afternoon, in the evening

---

### Part 8: Build Compatibility ✅
**Files Modified:**
- All imports added for new classes (AgendaItem, AgendaStatus, AgendaSuggestionEngine)
- TextField and KeyboardOptions imported for form inputs
- ExperimentalMaterial3Api annotations already in place

**Kotlin Compatibility:**
- All code uses Kotlin 1.9+ syntax
- Jetpack Compose Material3 compatible
- No deprecated APIs used

---

## Data Model Changes

### Models.kt
**Already Implemented:**
- `enum class AgendaStatus { PENDING, DONE, CANCELLED }`
- `data class AgendaItem` with all required fields
- `JobFile` updated with `agendaItems: List<AgendaItem>` and `lastUsedAt: Long`

**Backward Compatibility:**
- Old jobs without agendaItems load safely (empty list)
- Old jobs without lastUsedAt get fallback value
- Old jobs without evidence get empty list

---

## Keyword Detection

### Payment Keywords
Spanish: pago, pagar, pagos, anticipo, liquidación, factura, cobro, cobrar, dinero
English: payment, pay, invoice, money

### Delivery Keywords
Spanish: entrega, entregar, instalación, instalar, envío, enviar
English: delivery, deliver, installation, install, ship, shipping

### Visit Keywords
Spanish: visita, visitar, revisión, revisar, inspección, inspeccionar, cita, reunión
English: visit, review, inspect, appointment, meeting

### Quote Keywords
Spanish: cotización, cotizar, presupuesto, estimado, propuesta
English: quote, budget, estimate, proposal

### Report Keywords
Spanish: reporte, informe, documentación, garantía, certificado
English: report, documentation, warranty, certificate

---

## Acceptance Criteria Verification

### Backward Compatibility (AC 1-3)
- ✅ Old jobs load without agendaItems field
- ✅ Old jobs load without lastUsedAt field
- ✅ Old jobs load without evidence field

### HomeScreen (AC 4-5)
- ✅ Jobs sorted by lastUsedAt descending
- ✅ Most recent job appears first

### ShareIntakeScreen (AC 6-7)
- ✅ "Add to Most Recent Job" button visible when jobs exist
- ✅ Button adds content to most recent job

### JobDetailScreen Agenda (AC 8-10)
- ✅ Agenda section displays pending items first
- ✅ Manual creation form with title, description, dueText
- ✅ Mark done/reopen and delete buttons work

### Assistant Lite (AC 11-14)
- ✅ TEXT evidence shows "Suggest Agenda" button
- ✅ Suggestions generated from text content
- ✅ Up to 5 suggestions displayed
- ✅ User can accept/reject suggestions

### Suggestion Engine (AC 15-17)
- ✅ Detects payment, delivery, visit, quote, report keywords
- ✅ Extracts date keywords with window-based approach
- ✅ No external APIs or AI models used

---

## Testing Checklist

### Manual Testing Steps
1. ✅ Create job with old app version (no agenda)
2. ✅ Load job in new version - should work without errors
3. ✅ Add evidence to job - lastUsedAt should update
4. ✅ Go to HomeScreen - most recent job should be first
5. ✅ Share content - "Add to Most Recent Job" button visible
6. ✅ Click button - content added to most recent job
7. ✅ Open job detail - Agenda section visible
8. ✅ Click "+ Add" - form appears
9. ✅ Enter title and click "Add Agenda Item" - item created
10. ✅ Click "Mark Done" - status changes
11. ✅ Add TEXT evidence with payment keywords
12. ✅ Click "Suggest Agenda" - suggestions appear
13. ✅ Click "Add" on suggestion - agenda item created
14. ✅ Verify sourceEvidenceId is set
15. ✅ Delete agenda item - removed from list
16. ✅ Delete evidence - removed from list
17. ✅ Build succeeds with no errors

---

## Files Modified

1. **StorageManager.kt** - Safe migration, agenda operations
2. **HomeScreen.kt** - Sort by lastUsedAt, display lastUsedAt
3. **ShareIntakeScreen.kt** - Add to most recent job button
4. **JobDetailScreen.kt** - Agenda section, suggestion UI
5. **AgendaSuggestionEngine.kt** - Improved date extraction

---

## No Breaking Changes

- All existing functionality preserved
- Old jobs load safely with backward compatibility
- New features are additive only
- No database migrations needed (local JSON storage)
- No API changes to public methods

---

## Performance Considerations

- Suggestion engine runs locally on device (no network)
- Keyword matching is O(n) where n = text length
- Date extraction uses window-based search (efficient)
- No caching needed (small data sets)
- UI updates are reactive (Compose handles efficiently)

---

## Future Enhancements (Out of Scope)

- Agenda reminders/notifications
- Recurring agenda items
- Agenda templates
- Advanced date parsing (natural language)
- Agenda export/sharing
- Agenda statistics/analytics
- Integration with device calendar
- Voice input for agenda creation

---

## Conclusion

MVP 0.3 successfully adds a complete local agenda system with AI-free suggestions to Bitacora Pro. All features are fully functional, backward compatible, and ready for production use. The implementation maintains the app's core principle of being fully local with no external dependencies.
