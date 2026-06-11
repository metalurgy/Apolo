# Bitacora Pro v0.9.0 - Manual QA Checklist

**Status**: Ready for Testing  
**Date**: June 2026  
**Version**: v0.9.0 Pilot Release  
**Build**: ✅ SUCCESSFUL

---

## Pre-Testing Setup

### Device Requirements
- Android API 26+ (Android 8.0+)
- Minimum 100MB free storage
- Camera permission enabled (for capture tests)
- Contacts permission enabled (for contact import tests)

### Test Data Setup
1. Create 3-5 test activities with various statuses
2. Add evidence to at least 2 activities
3. Add pending items to at least 2 activities
4. Create activities with different client names

---

## Test Flows

### Test 1: LocalAssistantProvider Fuzzy Matching ✅

**Objective**: Verify fuzzy matching works with typos and variations

**Steps**:
1. Navigate to Assistant screen
2. Click "Pregunta al Asistente" (Ask Assistant)
3. Test the following questions:

| Question | Expected Result |
|----------|-----------------|
| "cómo capturar" | Step-by-step capture guide |
| "como captura" | Same guide (fuzzy match) |
| "captura foto" | Capture guide with tips |
| "pendiente tarea" | Pending items guide |
| "privacidad datos" | Privacy explanation |
| "whatsapp chat" | WhatsApp import guide |
| "ayuda" | General help menu |

**Pass Criteria**:
- [ ] All questions return helpful responses
- [ ] Fuzzy matching handles typos
- [ ] Spanish variations work (cómo/como)
- [ ] Tips are displayed with 💡 emoji
- [ ] Responses are formatted clearly

---

### Test 2: File Menu Cleanup ✅

**Objective**: Verify only implemented options appear in menu

**Steps**:
1. Go to Home screen
2. Click ⋮ (More) menu button
3. Verify menu items

**Expected Menu Items**:
- [x] Importar chat de WhatsApp
- [x] 🧹 Limpiar tablero
- [x] Acerca de

**NOT Expected**:
- [ ] Importar imágenes (should NOT appear)
- [ ] Importar PDF o documento (should NOT appear)
- [ ] Importar texto (should NOT appear)
- [ ] Exportar reporte PDF (should NOT appear)

**Pass Criteria**:
- [ ] Only 3 menu items appear
- [ ] No placeholder options visible
- [ ] All items are clickable
- [ ] No dead buttons

---

### Test 3: Board Cleanup ("Limpiar tablero") ✅

**Objective**: Verify board cleanup archives all visible activities

**Setup**:
- Create 5 test activities with ACTIVE status
- Create 2 test activities with COMPLETED status
- Create 1 test activity with ARCHIVED status

**Steps**:
1. Go to Home screen
2. Verify all activities are visible (except archived)
3. Click ⋮ menu → "🧹 Limpiar tablero"
4. Verify confirmation dialog appears
5. Click "Limpiar" button
6. Verify all activities are archived

**Expected Behavior**:
- [ ] Confirmation dialog shows clear warning
- [ ] Dialog explains what will happen
- [ ] All ACTIVE and COMPLETED activities are archived
- [ ] ARCHIVED activities remain archived
- [ ] Dashboard refreshes automatically
- [ ] "Todos" tab shows 0 activities
- [ ] "Archivadas" tab shows all activities

**Pass Criteria**:
- [ ] All visible activities are archived
- [ ] No activities are deleted
- [ ] Activities can be recovered from "Archivadas" tab
- [ ] Dashboard updates correctly

---

### Test 4: Manual Delete Activity ✅

**Objective**: Verify single activity deletion with typed confirmation

**Setup**:
- Create a test activity named "Test Activity Delete"
- Add evidence and pending items to it

**Steps**:
1. Open the test activity
2. Click delete button (🗑️) in top bar
3. Verify confirmation dialog appears
4. Try clicking "Eliminar" without typing (should be disabled)
5. Type "Test Activity Delete" in confirmation field
6. Verify "Eliminar" button becomes enabled
7. Click "Eliminar"
8. Verify activity is deleted
9. Verify return to Home screen

**Expected Behavior**:
- [ ] Delete button is visible in top bar
- [ ] Confirmation dialog shows activity title
- [ ] Dialog explains permanent deletion
- [ ] "Eliminar" button is disabled until correct text is entered
- [ ] Typed confirmation matches activity title exactly
- [ ] Activity is completely deleted (not archived)
- [ ] All evidence is deleted
- [ ] All pending items are deleted
- [ ] Return to Home screen after deletion

**Pass Criteria**:
- [ ] Activity is permanently deleted
- [ ] Cannot be recovered from any tab
- [ ] Typed confirmation prevents accidents
- [ ] No orphaned files remain

---

### Test 5: Dangerous Delete All ✅

**Objective**: Verify delete all activities with typed confirmation

**Setup**:
- Create 5-10 test activities with various statuses
- Add evidence and pending items to several

**Steps**:
1. Go to Home screen
2. Click ⋮ menu → "Acerca de"
3. Scroll down to "⚠️ Acciones Peligrosas" section
4. Click "🗑️ Eliminar TODAS las actividades"
5. Verify confirmation dialog appears
6. Try clicking "Eliminar" without typing (should be disabled)
7. Type "ELIMINAR TODO" in confirmation field
8. Verify "Eliminar" button becomes enabled
9. Click "Eliminar"
10. Verify success dialog appears
11. Click "OK" in success dialog
12. Verify return to Home screen
13. Verify all activities are gone

**Expected Behavior**:
- [ ] Dangerous actions section is visible
- [ ] Button is red and prominent
- [ ] Warning text explains consequences
- [ ] Confirmation dialog requires exact text "ELIMINAR TODO"
- [ ] "Eliminar" button is disabled until correct text is entered
- [ ] Success dialog confirms completion
- [ ] All activities are deleted
- [ ] All evidence is deleted
- [ ] All pending items are deleted
- [ ] Home screen shows empty state

**Pass Criteria**:
- [ ] All activities are permanently deleted
- [ ] Cannot be recovered
- [ ] Typed confirmation prevents accidents
- [ ] Success feedback is clear

---

### Test 6: Dashboard Simplicity ✅

**Objective**: Verify dashboard is simplified with no saturation

**Setup**:
- Create 3 ACTIVE activities
- Create 2 COMPLETED activities
- Add 10+ evidence items across activities

**Steps**:
1. Go to Home screen
2. Observe dashboard section
3. Verify metrics display

**Expected Dashboard**:
- [x] "Activos" card shows count (3)
- [x] "Completados" card shows count (2)
- [x] "Evidencia" card shows count (10+)
- [x] Single insight displayed (if available)
- [x] No saturation or visual clutter
- [x] Clean, minimal design

**Pass Criteria**:
- [ ] Only 3 metric cards visible
- [ ] No extra information or clutter
- [ ] Professional appearance
- [ ] Metrics are accurate
- [ ] Single insight is helpful

---

### Test 7: UI Labels in Spanish ✅

**Objective**: Verify all UI labels are in Spanish

**Steps**:
1. Go to Home screen
2. Check all visible labels
3. Go to Create Activity screen
4. Check form labels
5. Go to Activity Detail screen
6. Check all labels

**Expected Labels** (Sample):
- [x] "Crear Nueva Actividad" (not "Create New Job")
- [x] "Título de la Actividad" (not "Job Title")
- [x] "Nombre del Cliente" (not "Client Name")
- [x] "Número de Teléfono" (not "Phone Number")
- [x] "Tipo de Servicio" (not "Service Type")
- [x] "Sin actividades" (not "No jobs")

**Pass Criteria**:
- [ ] All labels are in Spanish
- [ ] No English placeholders remain
- [ ] Terminology is consistent
- [ ] Labels are clear and professional

---

### Test 8: Capture Flow ✅

**Objective**: Verify "Capturar" button opens capture flow

**Steps**:
1. Go to Home screen
2. Click "📸 Capturar" quick action card
3. Verify CreateJobScreen opens
4. Verify can create new activity
5. Verify can add evidence

**Expected Behavior**:
- [ ] Capturar button navigates to CreateJobScreen
- [ ] Can create new activity
- [ ] Can take photos
- [ ] Can import images
- [ ] Can add text notes
- [ ] Evidence is saved correctly

**Pass Criteria**:
- [ ] Capture flow works end-to-end
- [ ] Evidence is saved to activity
- [ ] No errors or crashes

---

### Test 9: Hard Constraints Verification ✅

**Objective**: Verify all hard constraints are met

**Checks**:
1. [ ] No API keys visible in code
2. [ ] No direct OpenAI/Anthropic/Gemini calls
3. [ ] No WhatsApp scraping (file-based only)
4. [ ] No accessibility service abuse
5. [ ] Build passes without errors
6. [ ] All buttons have implementations
7. [ ] No dead buttons or mystery features
8. [ ] No crashes or exceptions

**Pass Criteria**:
- [ ] All constraints verified
- [ ] No security issues
- [ ] No privacy violations
- [ ] App is safe and honest

---

## Regression Testing

### Core Features Still Work
- [ ] Create activity
- [ ] Add evidence (photos, text)
- [ ] Add pending items
- [ ] Mark pending items as done
- [ ] Complete activity
- [ ] Archive activity
- [ ] Generate PDF report
- [ ] Import WhatsApp chat
- [ ] View activity details
- [ ] Edit activity metadata

### Navigation
- [ ] Home → Create Activity
- [ ] Home → Activity Detail
- [ ] Home → Inbox
- [ ] Home → Agenda
- [ ] Home → Assistant
- [ ] Home → About
- [ ] Back button works everywhere
- [ ] No navigation loops

### Performance
- [ ] App launches quickly
- [ ] No lag in navigation
- [ ] Deletion is fast
- [ ] Dashboard loads quickly
- [ ] No memory leaks
- [ ] No crashes

---

## Bug Report Template

If you find an issue, please document it:

```
**Title**: [Brief description]

**Severity**: Critical / High / Medium / Low

**Steps to Reproduce**:
1. 
2. 
3. 

**Expected Result**:
[What should happen]

**Actual Result**:
[What actually happened]

**Device Info**:
- Android Version: 
- Device Model: 
- Screen Size: 

**Screenshots/Logs**:
[Attach if available]

**Notes**:
[Any additional context]
```

---

## Test Results Summary

### Test Execution

| Test | Status | Notes |
|------|--------|-------|
| 1. Fuzzy Matching | [ ] Pass / [ ] Fail | |
| 2. File Menu | [ ] Pass / [ ] Fail | |
| 3. Board Cleanup | [ ] Pass / [ ] Fail | |
| 4. Manual Delete | [ ] Pass / [ ] Fail | |
| 5. Delete All | [ ] Pass / [ ] Fail | |
| 6. Dashboard | [ ] Pass / [ ] Fail | |
| 7. UI Labels | [ ] Pass / [ ] Fail | |
| 8. Capture Flow | [ ] Pass / [ ] Fail | |
| 9. Constraints | [ ] Pass / [ ] Fail | |

### Regression Testing

| Feature | Status | Notes |
|---------|--------|-------|
| Create Activity | [ ] Pass / [ ] Fail | |
| Add Evidence | [ ] Pass / [ ] Fail | |
| Add Pending | [ ] Pass / [ ] Fail | |
| Complete Activity | [ ] Pass / [ ] Fail | |
| Archive Activity | [ ] Pass / [ ] Fail | |
| PDF Report | [ ] Pass / [ ] Fail | |
| WhatsApp Import | [ ] Pass / [ ] Fail | |
| Navigation | [ ] Pass / [ ] Fail | |
| Performance | [ ] Pass / [ ] Fail | |

### Overall Result

**Total Tests**: 18  
**Passed**: [ ] / 18  
**Failed**: [ ] / 18  
**Blocked**: [ ] / 18  

**Overall Status**: [ ] PASS / [ ] FAIL

---

## Sign-Off

**Tester Name**: ___________________  
**Date**: ___________________  
**Device**: ___________________  
**Android Version**: ___________________  

**Approved for Release**: [ ] YES / [ ] NO

**Comments**:
```
[Add any final comments or recommendations]
```

---

## Next Steps After QA

If all tests pass:
1. ✅ Build final APK
2. ✅ Prepare Play Store submission
3. ✅ Create release notes
4. ✅ Deploy to production

If issues found:
1. ❌ Document all bugs
2. ❌ Prioritize by severity
3. ❌ Fix critical issues
4. ❌ Re-test fixed features
5. ❌ Repeat QA cycle

---

**Version**: v0.9.0 Pilot  
**Status**: Ready for Manual QA  
**Build Status**: ✅ SUCCESSFUL
