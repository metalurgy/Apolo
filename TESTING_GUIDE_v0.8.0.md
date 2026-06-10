# Bitacora Pro v0.8.0 Testing Guide
## Comprehensive Manual Testing Procedures

**Version**: 0.8.0
**Date**: June 10, 2026
**Status**: Pilot-ready for testing

---

## Overview

This guide provides comprehensive manual testing procedures for Bitacora Pro v0.8.0. All tests are manual (no automated tests required) and can be performed on Android devices or emulators.

---

## Pre-Testing Setup

### Requirements
- Android device or emulator (API 26+)
- v0.8.0 APK installed
- Test data (optional)
- WhatsApp or Gallery app (for share intent tests)

### Installation
```bash
# Build APK
./gradlew assembleDebug

# Install on device
adb install app/build/outputs/apk/debug/app-debug.apk

# Or use Android Studio: Run → Run 'app'
```

### Device Preparation
1. Clear app data (first time only)
2. Grant necessary permissions
3. Ensure device has storage space
4. Ensure device has internet (for calendar/browser tests)

---

## Test Categories

### Category 1: Core Functionality (5 tests)
### Category 2: Share Intent (4 tests)
### Category 3: Inbox System (3 tests)
### Category 4: Agenda Management (3 tests)
### Category 5: Calendar Integration (2 tests)
### Category 6: Browser Actions (3 tests)
### Category 7: Data Persistence (3 tests)
### Category 8: UI/UX (4 tests)
### Category 9: Privacy & Security (2 tests)
### Category 10: Edge Cases (4 tests)

---

## Category 1: Core Functionality

### Test 1.1: Welcome Screen
**Objective**: Verify welcome screen displays correctly with privacy messaging

**Steps**:
1. Uninstall app completely
2. Install v0.8.0 APK
3. Launch app
4. Observe welcome screen

**Expected Results**:
- ✅ Welcome screen displays
- ✅ "Daily Copilot" branding visible
- ✅ Privacy notice displayed
- ✅ Features listed (Organiza, Captura, Agenda, Privacidad)
- ✅ "Comenzar" button visible
- ✅ Version shows "v0.8.0 - Daily Copilot"

**Pass/Fail**: ___________

---

### Test 1.2: Home Screen - Daily Copilot
**Objective**: Verify home screen redesign with Daily Copilot features

**Steps**:
1. Tap "Comenzar" on welcome screen
2. Observe home screen

**Expected Results**:
- ✅ Title shows "Daily Copilot" and "Bitacora Pro"
- ✅ Greeting displays (Buenos días/tardes/noches)
- ✅ "¿Qué necesitas hacer hoy?" message visible
- ✅ 4 quick action cards visible (Capturar, Inbox, Agenda, Asistente)
- ✅ Dashboard section shows summary cards
- ✅ Filter tabs visible (Todos, Activos, Completados, Archivados)
- ✅ Version footer shows "v0.8.0 - Daily Copilot"

**Pass/Fail**: ___________

---

### Test 1.3: Create Job Manually
**Objective**: Verify job creation works correctly

**Steps**:
1. From home screen, tap FAB (+) button
2. Fill in job details:
   - Title: "Test Job"
   - Cliente: "Test Client"
   - Teléfono: "5551234567"
   - Servicio: "Test Service"
3. Tap "Crear Trabajo"

**Expected Results**:
- ✅ Job created successfully
- ✅ Navigates to job detail screen
- ✅ Job appears in home screen list
- ✅ Job details saved correctly

**Pass/Fail**: ___________

---

### Test 1.4: Contact Import
**Objective**: Verify contact import from device contacts

**Steps**:
1. From create job screen, tap "👥 Importar" button
2. Select a contact from device
3. Observe fields populated

**Expected Results**:
- ✅ Contact picker opens
- ✅ Contact name populated in "Cliente" field
- ✅ Phone number populated in "Teléfono" field
- ✅ Phone number normalized correctly

**Pass/Fail**: ___________

---

### Test 1.5: Job Detail Screen
**Objective**: Verify job detail screen displays correctly

**Steps**:
1. From home screen, tap on a job
2. Observe job detail screen

**Expected Results**:
- ✅ Job title displayed
- ✅ Client name displayed
- ✅ Phone number displayed
- ✅ Service type displayed
- ✅ Evidence list displayed
- ✅ Agenda items displayed
- ✅ Status visible

**Pass/Fail**: ___________

---

## Category 2: Share Intent

### Test 2.1: Share Single Photo from WhatsApp
**Objective**: Verify share intent works with single photo

**Steps**:
1. Open WhatsApp
2. Find a chat with a photo
3. Long-press photo
4. Tap "Share"
5. Select "Bitacora Pro"
6. Observe ShareIntakeScreen

**Expected Results**:
- ✅ App opens to ShareIntakeScreen
- ✅ Photo preview visible
- ✅ "Crear Nuevo Trabajo" button visible
- ✅ "Agregar a Trabajo Existente" button visible
- ✅ Can create new job with photo
- ✅ Photo saved to job folder

**Pass/Fail**: ___________

---

### Test 2.2: Share Multiple Photos from Gallery
**Objective**: Verify share intent works with multiple photos

**Steps**:
1. Open Gallery app
2. Select 3-5 photos
3. Tap "Share"
4. Select "Bitacora Pro"
5. Create new job

**Expected Results**:
- ✅ ShareIntakeScreen shows file count
- ✅ All photos copied to job folder
- ✅ Evidence items created for each photo
- ✅ No temporary folder created

**Pass/Fail**: ___________

---

### Test 2.3: Share Text
**Objective**: Verify share intent works with text

**Steps**:
1. Copy text from any app
2. Open share menu
3. Select "Bitacora Pro"
4. Create new job

**Expected Results**:
- ✅ Text preview visible in ShareIntakeScreen
- ✅ Text saved as TEXT evidence
- ✅ Text content preserved

**Pass/Fail**: ___________

---

### Test 2.4: Add to Existing Job
**Objective**: Verify adding shared content to existing job

**Steps**:
1. Create a job manually
2. Share photo from WhatsApp
3. Tap "Agregar a Trabajo Existente"
4. Select the job created in step 1
5. Verify photo added

**Expected Results**:
- ✅ Job list displayed
- ✅ Can select existing job
- ✅ Photo added to job
- ✅ Evidence count increased
- ✅ No temporary folder created

**Pass/Fail**: ___________

---

## Category 3: Inbox System

### Test 3.1: Inbox Screen Navigation
**Objective**: Verify inbox screen accessible and displays correctly

**Steps**:
1. From home screen, tap "Inbox" quick action card
2. Observe inbox screen

**Expected Results**:
- ✅ Inbox screen opens
- ✅ Title shows "Inbox"
- ✅ Back button visible
- ✅ Empty state message if no items
- ✅ FAB button to add items

**Pass/Fail**: ___________

---

### Test 3.2: Inbox Item Management
**Objective**: Verify inbox items can be created and managed

**Steps**:
1. Share content to app
2. Instead of creating job, save to inbox
3. Navigate to inbox
4. Observe items

**Expected Results**:
- ✅ Items appear in inbox
- ✅ Item type shown (📝, 📸, 🎙️, 📄)
- ✅ Date/time displayed
- ✅ Content preview visible
- ✅ "Asignar a Trabajo" button visible
- ✅ Delete button visible

**Pass/Fail**: ___________

---

### Test 3.3: Assign Inbox Item to Job
**Objective**: Verify inbox items can be assigned to jobs

**Steps**:
1. Have items in inbox
2. Tap "Asignar a Trabajo" on an item
3. Select a job
4. Verify item moved to job

**Expected Results**:
- ✅ Job list displayed
- ✅ Can select job
- ✅ Item added to job
- ✅ Item removed from inbox
- ✅ Evidence count increased in job

**Pass/Fail**: ___________

---

## Category 4: Agenda Management

### Test 4.1: Daily Agenda Screen
**Objective**: Verify daily agenda screen displays today's tasks

**Steps**:
1. From home screen, tap "Agenda" quick action card
2. Observe agenda screen

**Expected Results**:
- ✅ Agenda screen opens
- ✅ Title shows "Agenda de Hoy"
- ✅ Back button visible
- ✅ FAB button to add items
- ✅ Empty state if no tasks today

**Pass/Fail**: ___________

---

### Test 4.2: Create Agenda Item
**Objective**: Verify agenda items can be created

**Steps**:
1. From job detail screen, add agenda item
2. Fill in details:
   - Title: "Test Task"
   - Description: "Test description"
   - Due date: Today
3. Save agenda item

**Expected Results**:
- ✅ Agenda item created
- ✅ Appears in daily agenda
- ✅ Shows in job detail
- ✅ Time displayed correctly

**Pass/Fail**: ___________

---

### Test 4.3: Mark Agenda Item Done
**Objective**: Verify agenda items can be marked as done

**Steps**:
1. From daily agenda, find a pending item
2. Tap checkbox to mark done
3. Observe status change

**Expected Results**:
- ✅ Checkbox toggles
- ✅ Status changes to "Completado"
- ✅ Text shows strikethrough
- ✅ Status persists after app restart

**Pass/Fail**: ___________

---

## Category 5: Calendar Integration

### Test 5.1: Create Calendar Event
**Objective**: Verify calendar event creation via intent

**Steps**:
1. From daily agenda, find an item with due date
2. Tap "Agregar a Calendario" button (if visible)
3. Observe calendar app opens

**Expected Results**:
- ✅ Calendar app opens
- ✅ Event creation dialog shows
- ✅ Title pre-filled
- ✅ Description pre-filled
- ✅ Time pre-filled
- ✅ Can save event

**Pass/Fail**: ___________

---

### Test 5.2: Calendar App Not Installed
**Objective**: Verify graceful fallback if calendar app not installed

**Steps**:
1. Uninstall Google Calendar (if installed)
2. Try to create calendar event
3. Observe behavior

**Expected Results**:
- ✅ App doesn't crash
- ✅ Graceful error handling
- ✅ User informed of missing app
- ✅ Can continue using app

**Pass/Fail**: ___________

---

## Category 6: Browser Actions

### Test 6.1: Open URL
**Objective**: Verify browser action helper works

**Steps**:
1. From job detail, add note with URL
2. Tap URL
3. Observe browser opens

**Expected Results**:
- ✅ Browser app opens
- ✅ URL loaded correctly
- ✅ Can navigate back to app

**Pass/Fail**: ___________

---

### Test 6.2: Search Information
**Objective**: Verify search functionality

**Steps**:
1. From app, trigger search action
2. Search for client information
3. Observe search results

**Expected Results**:
- ✅ Browser opens with search
- ✅ Search query pre-filled
- ✅ Results displayed

**Pass/Fail**: ___________

---

### Test 6.3: Open Maps
**Objective**: Verify maps integration

**Steps**:
1. From job detail, add location
2. Tap location
3. Observe maps opens

**Expected Results**:
- ✅ Maps app opens
- ✅ Location displayed
- ✅ Can navigate back to app

**Pass/Fail**: ___________

---

## Category 7: Data Persistence

### Test 7.1: Job Persistence
**Objective**: Verify jobs persist after app restart

**Steps**:
1. Create a job with evidence
2. Close app completely
3. Reopen app
4. Verify job still exists

**Expected Results**:
- ✅ Job appears in list
- ✅ All details preserved
- ✅ Evidence intact
- ✅ Agenda items intact

**Pass/Fail**: ___________

---

### Test 7.2: Evidence Persistence
**Objective**: Verify evidence files persist

**Steps**:
1. Add evidence to job
2. Close app
3. Reopen app
4. Open job detail
5. Verify evidence visible

**Expected Results**:
- ✅ Evidence list shows all items
- ✅ Evidence files accessible
- ✅ Can view/open evidence
- ✅ No missing files

**Pass/Fail**: ___________

---

### Test 7.3: Inbox Persistence
**Objective**: Verify inbox items persist

**Steps**:
1. Add items to inbox
2. Close app
3. Reopen app
4. Navigate to inbox
5. Verify items still there

**Expected Results**:
- ✅ All inbox items present
- ✅ Item details preserved
- ✅ Can still assign to jobs

**Pass/Fail**: ___________

---

## Category 8: UI/UX

### Test 8.1: Navigation
**Objective**: Verify navigation between screens works

**Steps**:
1. Navigate through all screens
2. Use back button
3. Use FAB buttons
4. Use quick action cards

**Expected Results**:
- ✅ All navigation works
- ✅ Back button works correctly
- ✅ No crashes
- ✅ Smooth transitions

**Pass/Fail**: ___________

---

### Test 8.2: Responsive Layout
**Objective**: Verify UI works on different screen sizes

**Steps**:
1. Test on phone (5-6 inches)
2. Test on tablet (7-10 inches)
3. Test in landscape mode
4. Verify all elements visible

**Expected Results**:
- ✅ UI adapts to screen size
- ✅ All buttons accessible
- ✅ Text readable
- ✅ No overlapping elements

**Pass/Fail**: ___________

---

### Test 8.3: Dark Mode (if supported)
**Objective**: Verify dark mode support

**Steps**:
1. Enable dark mode in system settings
2. Reopen app
3. Verify colors adjusted

**Expected Results**:
- ✅ App uses dark colors
- ✅ Text readable
- ✅ All elements visible
- ✅ Professional appearance

**Pass/Fail**: ___________

---

### Test 8.4: Performance
**Objective**: Verify app performance with large datasets

**Steps**:
1. Create 50+ jobs
2. Add 100+ evidence items
3. Add 50+ agenda items
4. Navigate through app
5. Measure responsiveness

**Expected Results**:
- ✅ App remains responsive
- ✅ No lag or stuttering
- ✅ Smooth scrolling
- ✅ Quick load times

**Pass/Fail**: ___________

---

## Category 9: Privacy & Security

### Test 9.1: No Network Calls
**Objective**: Verify no hidden network calls

**Steps**:
1. Enable airplane mode
2. Use app normally
3. Create jobs, add evidence
4. Navigate screens
5. Verify app works offline

**Expected Results**:
- ✅ App works completely offline
- ✅ No network errors
- ✅ All features available
- ✅ Data saved locally

**Pass/Fail**: ___________

---

### Test 9.2: Data Privacy
**Objective**: Verify data stays local

**Steps**:
1. Create sensitive job data
2. Check device storage
3. Verify data in app-private storage
4. Verify no cloud sync

**Expected Results**:
- ✅ Data in `filesDir/jobs/`
- ✅ No cloud storage used
- ✅ No external API calls
- ✅ User data protected

**Pass/Fail**: ___________

---

## Category 10: Edge Cases

### Test 10.1: Empty Fields
**Objective**: Verify handling of empty fields

**Steps**:
1. Try to create job with empty title
2. Try to create job with empty client
3. Observe validation

**Expected Results**:
- ✅ Form validation works
- ✅ Error messages displayed
- ✅ Can't submit invalid form
- ✅ User guided to fix errors

**Pass/Fail**: ___________

---

### Test 10.2: Large Files
**Objective**: Verify handling of large evidence files

**Steps**:
1. Share large photo (5MB+)
2. Share large PDF (10MB+)
3. Verify files copied correctly

**Expected Results**:
- ✅ Files copied successfully
- ✅ No crashes
- ✅ Files accessible
- ✅ Storage managed properly

**Pass/Fail**: ___________

---

### Test 10.3: Special Characters
**Objective**: Verify handling of special characters

**Steps**:
1. Create job with special characters: "Trabajo #1 - Señor José"
2. Add evidence with special names
3. Verify saved correctly

**Expected Results**:
- ✅ Special characters preserved
- ✅ No encoding issues
- ✅ Files saved correctly
- ✅ Display correct

**Pass/Fail**: ___________

---

### Test 10.4: Concurrent Operations
**Objective**: Verify app handles concurrent operations

**Steps**:
1. Create multiple jobs quickly
2. Add evidence to multiple jobs
3. Navigate between screens rapidly
4. Verify no data loss

**Expected Results**:
- ✅ No crashes
- ✅ No data loss
- ✅ All operations complete
- ✅ Consistent state

**Pass/Fail**: ___________

---

## Test Summary

### Total Tests: 37
- Category 1: 5 tests
- Category 2: 4 tests
- Category 3: 3 tests
- Category 4: 3 tests
- Category 5: 2 tests
- Category 6: 3 tests
- Category 7: 3 tests
- Category 8: 4 tests
- Category 9: 2 tests
- Category 10: 4 tests

### Passing Criteria
- ✅ All 37 tests must pass
- ✅ No crashes or errors
- ✅ All features working
- ✅ Data persists correctly
- ✅ Privacy maintained

---

## Test Results

| Category | Tests | Passed | Failed | Notes |
|----------|-------|--------|--------|-------|
| Core Functionality | 5 | ___ | ___ | |
| Share Intent | 4 | ___ | ___ | |
| Inbox System | 3 | ___ | ___ | |
| Agenda Management | 3 | ___ | ___ | |
| Calendar Integration | 2 | ___ | ___ | |
| Browser Actions | 3 | ___ | ___ | |
| Data Persistence | 3 | ___ | ___ | |
| UI/UX | 4 | ___ | ___ | |
| Privacy & Security | 2 | ___ | ___ | |
| Edge Cases | 4 | ___ | ___ | |
| **TOTAL** | **37** | **___** | **___** | |

---

## Sign-Off

**Tested By**: ___________________
**Date**: ___________________
**Device**: ___________________
**Android Version**: ___________________
**Overall Result**: ☐ PASS ☐ FAIL

**Notes**:
_________________________________________________________________
_________________________________________________________________
_________________________________________________________________

---

**Version**: 0.8.0
**Last Updated**: June 10, 2026
**Status**: Ready for pilot testing
