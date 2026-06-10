# Bitacora Pro v0.8.2 - Camera Feature Test Guide

**Quick Reference for Manual Testing**  
**Date**: 2026-06-10

---

## Pre-Test Setup

### Requirements
- Android device or emulator with camera
- Bitacora Pro v0.8.2 installed
- At least one Activity created

### Permissions
- Camera permission will be requested on first use
- Grant permission when prompted

---

## Test Scenario 1: Basic Camera Capture

### Steps
1. Open Bitacora Pro app
2. Navigate to an Activity (tap on any activity in the list)
3. Scroll down to "Evidencia" section
4. Verify "📸 Tomar foto" button is visible
5. Tap "📸 Tomar foto" button
6. Grant camera permission if prompted
7. Camera app opens
8. Take a photo (tap capture button)
9. Confirm/save photo
10. Return to app

### Expected Results
- ✅ Photo appears in Evidence section
- ✅ Photo has timestamp
- ✅ Photo is categorized as "UNCLASSIFIED"
- ✅ Evidence count increases
- ✅ No error messages

---

## Test Scenario 2: Multiple Photos

### Steps
1. From Activity Detail screen
2. Tap "📸 Tomar foto" again
3. Take another photo
4. Return to app
5. Repeat 2-4 one more time (3 photos total)

### Expected Results
- ✅ All 3 photos appear in Evidence section
- ✅ Each has unique timestamp
- ✅ Evidence count shows "3"
- ✅ Photos are grouped by category

---

## Test Scenario 3: Photo Categorization

### Steps
1. From Activity Detail screen with photos
2. Find a photo in Evidence section
3. Tap category button (shows "UNCLASSIFIED")
4. Select "BEFORE" from dropdown
5. Verify category changed
6. Repeat with another photo, select "DURING"

### Expected Results
- ✅ Category dropdown appears
- ✅ Category changes immediately
- ✅ Photos grouped by new category
- ✅ Changes persist after reload

---

## Test Scenario 4: Photo Viewing

### Steps
1. From Activity Detail screen with photos
2. Find a photo in Evidence section
3. Tap "Abrir" button
4. Photo opens in image viewer
5. Close image viewer
6. Return to Activity Detail

### Expected Results
- ✅ Photo opens in system image viewer
- ✅ Photo displays correctly
- ✅ Can zoom/pan if supported
- ✅ Returns to app without issues

---

## Test Scenario 5: Photo Deletion

### Steps
1. From Activity Detail screen with photos
2. Find a photo in Evidence section
3. Tap delete icon (trash can)
4. Confirm deletion
5. Verify photo removed

### Expected Results
- ✅ Confirmation dialog appears
- ✅ Photo removed from Evidence section
- ✅ Evidence count decreases
- ✅ No error messages

---

## Test Scenario 6: Cancel Camera Capture

### Steps
1. From Activity Detail screen
2. Tap "📸 Tomar foto" button
3. Camera app opens
4. Tap back button or close camera
5. Return to app

### Expected Results
- ✅ Error message: "Captura de foto cancelada"
- ✅ No photo added to Evidence
- ✅ Evidence count unchanged
- ✅ Can tap "Tomar foto" again

---

## Test Scenario 7: No Evidence State

### Steps
1. Create a new Activity
2. Navigate to Activity Detail
3. Scroll to Evidence section
4. Verify "Sin evidencia aún" message
5. Tap "📸 Tomar foto" button
6. Take a photo
7. Return to app

### Expected Results
- ✅ "Sin evidencia aún" message shown
- ✅ "📸 Tomar foto" button visible
- ✅ Photo captured successfully
- ✅ Evidence section now shows photo
- ✅ "Sin evidencia aún" message gone

---

## Test Scenario 8: Evidence Help Button

### Steps
1. From Activity Detail screen with evidence
2. Tap "ℹ️ Categorías" button
3. Read help text
4. Tap "Entendido" button

### Expected Results
- ✅ Help dialog appears
- ✅ Shows evidence category explanations
- ✅ Dialog closes on "Entendido"
- ✅ Button label is clear (not "?")

---

## Test Scenario 9: Label Consistency

### Steps
1. Open Activity Detail screen
2. Check top bar title
3. Check archive button tooltip
4. Check all visible text

### Expected Results
- ✅ Title: "Detalles de la Actividad" (not "Trabajo")
- ✅ Archive button: "Archivar actividad" (not "trabajo")
- ✅ All labels use "Actividad" consistently
- ✅ No "Trabajo" labels visible

---

## Test Scenario 10: Persistence

### Steps
1. Take a photo in Activity
2. Close app completely
3. Reopen app
4. Navigate to same Activity
5. Check Evidence section

### Expected Results
- ✅ Photo still appears
- ✅ Timestamp preserved
- ✅ Category preserved
- ✅ No data loss

---

## Test Scenario 11: Multiple Activities

### Steps
1. Create Activity A
2. Take photo in Activity A
3. Create Activity B
4. Take photo in Activity B
5. Navigate back to Activity A
6. Check Evidence

### Expected Results
- ✅ Activity A shows only its photo
- ✅ Activity B shows only its photo
- ✅ No cross-contamination
- ✅ Each activity independent

---

## Test Scenario 12: Share Evidence (Regression Test)

### Steps
1. From Activity Detail with photos
2. Tap "Compartir" button (if available)
3. Select share target
4. Verify photo shared

### Expected Results
- ✅ Share intent works
- ✅ Photo shared successfully
- ✅ No regression in existing feature

---

## Test Scenario 13: PDF/Audio Evidence (Regression Test)

### Steps
1. Share a PDF or audio file to app
2. Add to Activity
3. Navigate to Activity Detail
4. Find PDF/audio in Evidence
5. Tap "Abrir" button

### Expected Results
- ✅ PDF/audio opens in appropriate app
- ✅ No regression in existing feature
- ✅ Camera photos don't interfere

---

## Test Scenario 14: Text Evidence (Regression Test)

### Steps
1. Share text to app
2. Add to Activity
3. Navigate to Activity Detail
4. Find text in Evidence
5. Verify text preview shown

### Expected Results
- ✅ Text evidence displays
- ✅ Preview shown correctly
- ✅ No regression in existing feature

---

## Test Scenario 15: Agenda Integration

### Steps
1. From Activity Detail with photos
2. Find photo in Evidence
3. Tap "Sugerir Agenda" button (if available)
4. Accept suggestion

### Expected Results
- ✅ Agenda item created
- ✅ No regression in existing feature
- ✅ Camera photos work with agenda

---

## Test Scenario 16: PDF Report Generation

### Steps
1. From Activity Detail with photos
2. Scroll to "Reporte PDF" section
3. Tap "Generar Reporte PDF"
4. Wait for generation
5. Verify report includes photos

### Expected Results
- ✅ Report generates successfully
- ✅ Photos included in report
- ✅ No regression in existing feature

---

## Test Scenario 17: Permission Denial

### Steps
1. Revoke camera permission in Settings
2. From Activity Detail
3. Tap "📸 Tomar foto" button
4. Deny camera permission when prompted

### Expected Results
- ✅ Camera intent fails gracefully
- ✅ Error message shown
- ✅ App doesn't crash
- ✅ Can retry after granting permission

---

## Test Scenario 18: Low Storage

### Steps
1. Fill device storage to near capacity
2. From Activity Detail
3. Tap "📸 Tomar foto" button
4. Try to take photo

### Expected Results
- ✅ Camera handles gracefully
- ✅ Error message if save fails
- ✅ App doesn't crash
- ✅ Temp files cleaned up

---

## Quick Checklist

### Functionality
- [ ] Camera launches
- [ ] Photo captured
- [ ] Photo saved to Evidence
- [ ] UI refreshes immediately
- [ ] Multiple photos work
- [ ] Cancel handled gracefully

### UI/UX
- [ ] "📸 Tomar foto" button visible
- [ ] "ℹ️ Categorías" button clear
- [ ] Error messages helpful
- [ ] Labels use "Actividad"
- [ ] No "Trabajo" labels visible

### Data
- [ ] Photos persist
- [ ] Timestamps correct
- [ ] Categories work
- [ ] No data loss
- [ ] No cross-contamination

### Regression
- [ ] Share evidence works
- [ ] PDF/audio works
- [ ] Text evidence works
- [ ] Agenda integration works
- [ ] PDF reports work

---

## Known Limitations

- Photos are JPEG format (not configurable)
- No photo editing before save
- No batch capture
- No photo compression options
- Single photo per capture (not multi-select)

---

## Troubleshooting

### Camera doesn't open
- Check camera permission granted
- Restart app
- Restart device
- Check device has camera

### Photo not saved
- Check storage space available
- Check file permissions
- Check app has write access
- Check temp files cleaned up

### UI doesn't refresh
- Try navigating away and back
- Try closing and reopening app
- Check for errors in logcat

### Permission issues
- Go to Settings > Apps > Bitacora Pro > Permissions
- Enable Camera permission
- Restart app

---

## Test Results Template

```
Test Date: ___________
Tester: ___________
Device: ___________
Android Version: ___________

Scenario 1 (Basic Capture): [ ] PASS [ ] FAIL
Scenario 2 (Multiple Photos): [ ] PASS [ ] FAIL
Scenario 3 (Categorization): [ ] PASS [ ] FAIL
Scenario 4 (Photo Viewing): [ ] PASS [ ] FAIL
Scenario 5 (Photo Deletion): [ ] PASS [ ] FAIL
Scenario 6 (Cancel Capture): [ ] PASS [ ] FAIL
Scenario 7 (No Evidence): [ ] PASS [ ] FAIL
Scenario 8 (Help Button): [ ] PASS [ ] FAIL
Scenario 9 (Label Consistency): [ ] PASS [ ] FAIL
Scenario 10 (Persistence): [ ] PASS [ ] FAIL
Scenario 11 (Multiple Activities): [ ] PASS [ ] FAIL
Scenario 12 (Share Regression): [ ] PASS [ ] FAIL
Scenario 13 (PDF/Audio Regression): [ ] PASS [ ] FAIL
Scenario 14 (Text Regression): [ ] PASS [ ] FAIL
Scenario 15 (Agenda Integration): [ ] PASS [ ] FAIL
Scenario 16 (PDF Report): [ ] PASS [ ] FAIL
Scenario 17 (Permission Denial): [ ] PASS [ ] FAIL
Scenario 18 (Low Storage): [ ] PASS [ ] FAIL

Overall Result: [ ] PASS [ ] FAIL

Notes:
_________________________________
_________________________________
_________________________________
```

---

## Sign-Off

**Test Guide**: Complete ✅  
**Ready for Testing**: Yes ✅  
**Expected Duration**: 30-45 minutes

---

**Last Updated**: 2026-06-10  
**Version**: v0.8.2
