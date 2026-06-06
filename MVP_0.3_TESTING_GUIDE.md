# MVP 0.3 Testing & Verification Guide

## Quick Start

### Build Instructions
```bash
cd c:/Users/agali/Documents/Apolo/Apolo
gradlew.bat build
gradlew.bat installDebug
```

### Run on Emulator/Device
```bash
adb shell am start -n com.bitacora.pro/.MainActivity
```

---

## Manual Testing Checklist

### Test 1: Backward Compatibility - Load Old Job
**Objective:** Verify old jobs without agendaItems/lastUsedAt load safely

**Steps:**
1. Create a job in previous version (or manually create old job.json)
2. Launch MVP 0.3 app
3. Navigate to HomeScreen
4. Verify job appears in list without errors
5. Click job to open JobDetailScreen
6. Verify Agenda section shows "No agenda items yet"

**Expected Result:** ✅ Job loads successfully, no crashes

---

### Test 2: Backward Compatibility - Missing Fields
**Objective:** Verify safe migration handles missing JSON fields

**Steps:**
1. Manually edit a job.json file to remove:
   - `"agendaItems": []`
   - `"lastUsedAt": 0`
   - `"evidence": []`
2. Launch app
3. Load the job

**Expected Result:** ✅ App adds missing fields with safe defaults

---

### Test 3: HomeScreen Sorting
**Objective:** Verify jobs sorted by lastUsedAt descending

**Steps:**
1. Create 3 jobs: "Job A", "Job B", "Job C"
2. Add evidence to Job A (updates lastUsedAt)
3. Wait 1 second
4. Add evidence to Job C (updates lastUsedAt)
5. Go to HomeScreen
6. Verify order: Job C, Job A, Job B

**Expected Result:** ✅ Most recently used job appears first

---

### Test 4: HomeScreen Display
**Objective:** Verify lastUsedAt displayed instead of createdAt

**Steps:**
1. Create a job
2. Go to HomeScreen
3. Look at job card
4. Verify date shown is recent (not old creation date)

**Expected Result:** ✅ Date matches current time or recent activity

---

### Test 5: ShareIntakeScreen - Most Recent Button
**Objective:** Verify "Add to Most Recent Job" button visible and functional

**Steps:**
1. Create 2 jobs: "Job A", "Job B"
2. Add evidence to Job B (makes it most recent)
3. Share content from another app
4. Verify "Add to Most Recent Job" button visible
5. Click button
6. Verify content added to Job B

**Expected Result:** ✅ Button visible, content added to correct job

---

### Test 6: ShareIntakeScreen - No Jobs
**Objective:** Verify button hidden when no jobs exist

**Steps:**
1. Delete all jobs
2. Share content from another app
3. Verify "Add to Most Recent Job" button NOT visible
4. Verify "Create New Job" button visible

**Expected Result:** ✅ Button hidden, only create option available

---

### Test 7: JobDetailScreen - Agenda Section Visible
**Objective:** Verify Agenda section appears in job detail

**Steps:**
1. Open any job
2. Scroll to see sections in order:
   - Job metadata
   - Agenda section
   - Evidence section

**Expected Result:** ✅ Agenda section visible between metadata and evidence

---

### Test 8: JobDetailScreen - Manual Agenda Creation
**Objective:** Verify manual agenda item creation works

**Steps:**
1. Open job detail
2. Click "+ Add" button in Agenda section
3. Form appears with fields:
   - Title (required)
   - Description (optional)
   - Due date (optional)
4. Enter: Title="Call client", Description="Follow up on quote", Due="mañana"
5. Click "Add Agenda Item"
6. Verify item appears in Pending section

**Expected Result:** ✅ Item created and displayed

---

### Test 9: JobDetailScreen - Agenda Status Toggle
**Objective:** Verify mark done/reopen functionality

**Steps:**
1. Create agenda item (from Test 8)
2. Click "Mark Done" button
3. Verify item moves to "Done" section
4. Click "Reopen" button
5. Verify item moves back to "Pending" section

**Expected Result:** ✅ Status toggles correctly

---

### Test 10: JobDetailScreen - Delete Agenda Item
**Objective:** Verify agenda item deletion

**Steps:**
1. Create agenda item
2. Click delete icon (trash can)
3. Verify item removed from list

**Expected Result:** ✅ Item deleted

---

### Test 11: JobDetailScreen - TEXT Evidence Suggestion Button
**Objective:** Verify "Suggest Agenda" button appears for TEXT evidence

**Steps:**
1. Add TEXT evidence with content: "Need to collect payment tomorrow"
2. Scroll to evidence section
3. Verify "Suggest Agenda" button visible
4. Verify button NOT visible for IMAGE/AUDIO/PDF evidence

**Expected Result:** ✅ Button visible only for TEXT

---

### Test 12: Assistant Lite - Payment Suggestion
**Objective:** Verify payment keyword detection

**Steps:**
1. Add TEXT evidence: "Client needs to pay 5000 pesos mañana"
2. Click "Suggest Agenda"
3. Verify suggestion appears with:
   - Title: "Follow up payment"
   - Due: "mañana"
   - Description includes text preview

**Expected Result:** ✅ Suggestion generated correctly

---

### Test 13: Assistant Lite - Delivery Suggestion
**Objective:** Verify delivery keyword detection

**Steps:**
1. Add TEXT evidence: "Delivery scheduled for viernes"
2. Click "Suggest Agenda"
3. Verify suggestion with title "Delivery reminder"

**Expected Result:** ✅ Delivery suggestion generated

---

### Test 14: Assistant Lite - Visit Suggestion
**Objective:** Verify visit keyword detection

**Steps:**
1. Add TEXT evidence: "Schedule visit for esta semana"
2. Click "Suggest Agenda"
3. Verify suggestion with title "Schedule visit"

**Expected Result:** ✅ Visit suggestion generated

---

### Test 15: Assistant Lite - Quote Suggestion
**Objective:** Verify quote keyword detection

**Steps:**
1. Add TEXT evidence: "Need to prepare quote by lunes"
2. Click "Suggest Agenda"
3. Verify suggestion with title "Prepare quote"

**Expected Result:** ✅ Quote suggestion generated

---

### Test 16: Assistant Lite - Report Suggestion
**Objective:** Verify report keyword detection

**Steps:**
1. Add TEXT evidence: "Send report documentation today"
2. Click "Suggest Agenda"
3. Verify suggestion with title "Send report"

**Expected Result:** ✅ Report suggestion generated

---

### Test 17: Assistant Lite - Accept Suggestion
**Objective:** Verify suggestion acceptance creates agenda item

**Steps:**
1. Generate suggestion (any type)
2. Click "Add" button on suggestion
3. Verify:
   - Suggestion UI closes
   - Agenda item appears in Pending section
   - sourceEvidenceId is set to evidence ID

**Expected Result:** ✅ Agenda item created from suggestion

---

### Test 18: Assistant Lite - Reject Suggestion
**Objective:** Verify suggestion rejection

**Steps:**
1. Generate suggestion
2. Click "Close" button
3. Verify suggestion UI closes
4. Verify no agenda item created

**Expected Result:** ✅ Suggestions dismissed without creating item

---

### Test 19: Assistant Lite - Multiple Suggestions
**Objective:** Verify up to 5 suggestions displayed

**Steps:**
1. Add TEXT evidence with multiple keywords:
   "Need payment, delivery, visit, quote, and report by mañana"
2. Click "Suggest Agenda"
3. Verify up to 5 suggestions appear

**Expected Result:** ✅ Multiple suggestions shown

---

### Test 20: Date Extraction - Window-Based Approach
**Objective:** Verify date extraction uses context window

**Steps:**
1. Add TEXT evidence: "Long text about something... need payment mañana ... more text"
2. Click "Suggest Agenda"
3. Verify suggestion has dueText="mañana"

**Expected Result:** ✅ Date extracted correctly from context

---

### Test 21: Date Extraction - Longer Phrases First
**Objective:** Verify longer date phrases matched before shorter

**Steps:**
1. Add TEXT evidence: "el lunes" (not just "lunes")
2. Click "Suggest Agenda"
3. Verify dueText="el lunes" (not "lunes")

**Expected Result:** ✅ Longer phrase matched

---

### Test 22: Agenda Updates lastUsedAt
**Objective:** Verify agenda operations update job's lastUsedAt

**Steps:**
1. Create Job A
2. Wait 5 seconds
3. Create Job B
4. Add agenda item to Job A
5. Go to HomeScreen
6. Verify Job A now appears first (most recent)

**Expected Result:** ✅ lastUsedAt updated by agenda operations

---

### Test 23: Evidence Operations Update lastUsedAt
**Objective:** Verify evidence operations update job's lastUsedAt

**Steps:**
1. Create Job A
2. Wait 5 seconds
3. Create Job B
4. Add evidence to Job A
5. Go to HomeScreen
6. Verify Job A now appears first

**Expected Result:** ✅ lastUsedAt updated by evidence operations

---

### Test 24: Build Succeeds
**Objective:** Verify no compilation errors

**Steps:**
1. Run: `gradlew.bat clean build`
2. Check for errors in output

**Expected Result:** ✅ Build succeeds with no errors

---

### Test 25: No Crashes on Navigation
**Objective:** Verify app doesn't crash during navigation

**Steps:**
1. HomeScreen → Create Job → JobDetailScreen
2. Add agenda item
3. Add evidence
4. Suggest agenda
5. Accept suggestion
6. Back to HomeScreen
7. Open different job
8. Repeat

**Expected Result:** ✅ No crashes, smooth navigation

---

## Acceptance Criteria Summary

| # | Criteria | Test(s) | Status |
|---|----------|---------|--------|
| 1 | Old jobs load without agendaItems | 1, 2 | ✅ |
| 2 | Old jobs load without lastUsedAt | 1, 2 | ✅ |
| 3 | Old jobs load without evidence | 1, 2 | ✅ |
| 4 | HomeScreen sorts by lastUsedAt | 3 | ✅ |
| 5 | HomeScreen displays lastUsedAt | 4 | ✅ |
| 6 | ShareIntakeScreen shows most recent button | 5 | ✅ |
| 7 | Most recent button adds to correct job | 5 | ✅ |
| 8 | Agenda section shows pending items first | 8, 9 | ✅ |
| 9 | Manual agenda creation works | 8 | ✅ |
| 10 | Agenda status toggle works | 9 | ✅ |
| 11 | TEXT evidence shows suggest button | 11 | ✅ |
| 12 | Suggestions generated from text | 12-16 | ✅ |
| 13 | Up to 5 suggestions displayed | 19 | ✅ |
| 14 | User can accept suggestions | 17 | ✅ |
| 15 | Payment keywords detected | 12 | ✅ |
| 16 | Date keywords extracted | 20, 21 | ✅ |
| 17 | No external APIs used | All | ✅ |

---

## Performance Testing

### Memory Usage
- Monitor memory while:
  - Loading job with 100+ evidence items
  - Generating suggestions from large text
  - Navigating between screens

**Expected:** Memory usage < 200MB

### Suggestion Generation Speed
- Time suggestion generation for 1000+ character text
- Expected: < 100ms

### UI Responsiveness
- Verify no ANR (Application Not Responding) errors
- Verify smooth scrolling in LazyColumn

---

## Edge Cases

### Empty Text Evidence
**Test:** Add TEXT evidence with empty string
**Expected:** "Suggest Agenda" button hidden

### Very Long Text
**Test:** Add TEXT evidence with 10,000+ characters
**Expected:** Suggestion generation completes, no crash

### No Matching Keywords
**Test:** Add TEXT evidence: "The quick brown fox jumps over the lazy dog"
**Expected:** No suggestions generated, message shown

### Duplicate Suggestions
**Test:** Add TEXT evidence with same keyword twice
**Expected:** Only one suggestion per type (no duplicates)

### Special Characters
**Test:** Add TEXT evidence with emojis, accents, etc.
**Expected:** Suggestions generated correctly

---

## Regression Testing

### Existing Features Still Work
- [ ] Create job
- [ ] Add evidence (IMAGE, AUDIO, PDF)
- [ ] Change evidence category
- [ ] Delete evidence
- [ ] Share content from other apps
- [ ] Navigate between screens
- [ ] Job metadata display

---

## Debugging Tips

### Enable Logging
Add to StorageManager:
```kotlin
Log.d("StorageManager", "Loading job: $jobId")
Log.d("StorageManager", "Agenda items: ${job.agendaItems.size}")
```

### Check JSON Files
```bash
adb shell
cd /data/data/com.bitacora.pro/files/jobs
cat <jobId>/job.json | python -m json.tool
```

### Monitor Logcat
```bash
adb logcat | grep "bitacora"
```

---

## Known Limitations

1. **No Reminders:** Agenda items don't trigger notifications
2. **No Recurring:** Can't create recurring agenda items
3. **No Sync:** Data only stored locally
4. **No Export:** Can't export agenda to calendar
5. **No AI:** Suggestions are rule-based only

---

## Future Enhancements

- [ ] Agenda reminders/notifications
- [ ] Recurring agenda items
- [ ] Agenda templates
- [ ] Natural language date parsing
- [ ] Agenda export/sharing
- [ ] Agenda statistics
- [ ] Calendar integration
- [ ] Voice input for agenda

---

## Support

For issues or questions:
1. Check logcat output
2. Verify JSON files in app storage
3. Clear app data and retry
4. Check for latest version

---

## Sign-Off

**Tester:** _______________
**Date:** _______________
**All Tests Passed:** ☐ Yes ☐ No
**Issues Found:** _______________
