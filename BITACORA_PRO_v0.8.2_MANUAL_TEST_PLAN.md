# Bitacora Pro v0.8.2 - Manual Test Plan (34 Steps)

**Version**: v0.8.2 - P0 UX Action Fix
**Date**: 2026-06-10
**Target**: Real Device Testing
**Duration**: ~45 minutes

---

## Pre-Test Setup

1. **Clean Install**: Uninstall previous version, install v0.8.2 fresh
2. **Device**: Real Android device (not emulator)
3. **Network**: Connected to internet
4. **Permissions**: Grant all requested permissions
5. **Contacts**: Have at least 2 contacts with phone numbers

---

## Test Sequence (34 Steps)

### Section 1: Home Screen & Navigation (Steps 1-5)

**Step 1: Verify Home Screen Layout**
- [ ] Open app
- [ ] See "Daily Copilot" header
- [ ] See greeting (Buenos días/tardes/noches)
- [ ] See 4 quick action cards: Capturar, Sin asignar, Pendientes, Asistente
- [ ] Verify improved spacing and whitespace
- [ ] **Expected**: Clean, professional layout with good spacing

**Step 2: Verify Quick Action Cards Have Descriptions**
- [ ] Look at each quick action card
- [ ] Capturar: See "Captura rápida" description
- [ ] Sin asignar: See "Capturas sin clasificar" description
- [ ] Pendientes: See "Tareas pendientes" description
- [ ] Asistente: See "Acciones guiadas" description
- [ ] **Expected**: All cards have clear descriptions

**Step 3: Verify Version Footer**
- [ ] Scroll to bottom of Home screen
- [ ] See "Bitacora Pro v0.8.2 - P0 UX Action Fix"
- [ ] **Expected**: Correct version displayed

**Step 4: Verify FAB (Floating Action Button)**
- [ ] See "+" button in bottom right
- [ ] Tap it
- [ ] Navigate to CreateJob screen
- [ ] Go back to Home
- [ ] **Expected**: FAB works correctly

**Step 5: Verify Empty State**
- [ ] If no jobs exist, see empty state message
- [ ] See "Empieza tu primera actividad"
- [ ] See "Crear actividad" button
- [ ] **Expected**: Clear empty state with action

---

### Section 2: Create Activity & Contact Import (Steps 6-10)

**Step 6: Create New Activity**
- [ ] Tap "Crear actividad" or FAB
- [ ] Fill in: Title, Client Name, Phone, Service Type
- [ ] Tap "Importar" button
- [ ] Select a contact from device
- [ ] **Expected**: Contact name and phone populate

**Step 7: Verify Contact Import**
- [ ] Confirm name field populated
- [ ] Confirm phone field populated
- [ ] Verify phone is in correct format
- [ ] **Expected**: Both fields populated correctly

**Step 8: Save Activity**
- [ ] Tap "Guardar"
- [ ] Wait for activity to be created
- [ ] Confirm navigation to JobDetailScreen
- [ ] **Expected**: Activity created successfully

**Step 9: Verify Activity Metadata**
- [ ] See activity title
- [ ] See client name
- [ ] See phone number
- [ ] See service type
- [ ] See status (Activo)
- [ ] **Expected**: All metadata displayed correctly

**Step 10: Verify Status Explanation Card**
- [ ] See status explanation card
- [ ] See "🟢 Activo" with description
- [ ] See note: "Completar todas las tareas pendientes NO marca automáticamente..."
- [ ] **Expected**: Clear explanation of status and pending relationship

---

### Section 3: Pendientes (Pending Items) - Part B (Steps 11-15)

**Step 11: Add Pending Item**
- [ ] In JobDetailScreen, scroll to Agenda section
- [ ] Tap "Agregar tarea"
- [ ] Fill in title and description
- [ ] Set due date (today)
- [ ] Tap "Guardar"
- [ ] **Expected**: Pending item added

**Step 12: Verify Pending Item Display**
- [ ] See pending item in Agenda section
- [ ] See status indicator: ⭕ (pending)
- [ ] See status text: "Pendiente"
- [ ] See "Completar" button
- [ ] See calendar icon (for dated items)
- [ ] **Expected**: Clear status display with action button

**Step 13: Complete Pending Item**
- [ ] Tap "Completar" button
- [ ] Confirm status changes to "Completado"
- [ ] Confirm status indicator changes to ✅
- [ ] **Expected**: Status changes immediately

**Step 14: Verify Completed Item**
- [ ] See completed item with ✅ indicator
- [ ] See "Completado" text
- [ ] See task title with strikethrough
- [ ] **Expected**: Completed state clearly visible

**Step 15: Verify Activity Status NOT Auto-Changed**
- [ ] Go back to Home
- [ ] Open activity again
- [ ] Confirm status is still "Activo" (not auto-completed)
- [ ] **Expected**: Activity status unchanged despite completing pending item

---

### Section 4: Sin asignar (Unassigned Captures) - Part D (Steps 16-18)

**Step 16: Navigate to Sin asignar**
- [ ] Go to Home
- [ ] Tap "Sin asignar" quick action card
- [ ] **Expected**: Navigate to InboxScreen

**Step 17: Verify Sin asignar Screen**
- [ ] See title: "Sin asignar"
- [ ] See subtitle: "Capturas sin clasificar"
- [ ] If empty, see explanation: "Aquí van las capturas rápidas..."
- [ ] See instruction: "Usa el botón 'Capturar' en la pantalla principal..."
- [ ] **Expected**: Clear purpose and instructions

**Step 18: Verify Sin asignar is Different from Por clasificar**
- [ ] Confirm label is "Sin asignar" (not "Por clasificar")
- [ ] Confirm subtitle is "Capturas sin clasificar"
- [ ] **Expected**: Renamed correctly

---

### Section 5: Capturar Menu - Part E (Steps 19-22)

**Step 19: Open Capturar Menu**
- [ ] Go to Home
- [ ] Tap "Capturar" quick action card
- [ ] See menu with 4 options
- [ ] **Expected**: Menu opens with 4 items

**Step 20: Verify Capturar Menu Items**
- [ ] See "📸 Tomar foto" (Captura rápida)
- [ ] See "🎙️ Grabar audio" (Captura rápida)
- [ ] See "📝 Escribir nota" (Captura rápida)
- [ ] See "✨ Nueva actividad" (Crear trabajo)
- [ ] **Expected**: All 4 items with correct labels and descriptions

**Step 21: Verify Separation of Capture vs Job Creation**
- [ ] Confirm first 3 items are capture actions
- [ ] Confirm last item is job creation (different description)
- [ ] **Expected**: Clear separation between capture and job creation

**Step 22: Test Capture Actions**
- [ ] Tap "Tomar foto"
- [ ] Confirm camera or photo picker opens
- [ ] Go back
- [ ] Tap "Escribir nota"
- [ ] Confirm text input opens
- [ ] Go back
- [ ] **Expected**: Each action opens correct interface

---

### Section 6: Evidence & Camera Capture - Part F (Steps 23-25)

**Step 23: Navigate to Evidence Section**
- [ ] Open activity detail
- [ ] Scroll to Evidence section
- [ ] If evidence exists, see "Evidencia (X)" header
- [ ] **Expected**: Evidence section visible

**Step 24: Verify Tomar foto Button in Evidence Header**
- [ ] See "📸 Tomar foto" button next to "?" button
- [ ] Button is clickable
- [ ] **Expected**: Camera button visible and accessible

**Step 25: Verify Tomar foto in Empty Evidence**
- [ ] If no evidence, see "Sin evidencia aún"
- [ ] See "📸 Tomar foto" button below message
- [ ] Button is clickable
- [ ] **Expected**: Camera button visible in empty state

---

### Section 7: Asistente (Assistant) - Part A (Steps 26-28)

**Step 26: Navigate to Asistente**
- [ ] Go to Home
- [ ] Tap "Asistente" quick action card
- [ ] **Expected**: Navigate to AssistantScreen (not Pendientes)

**Step 27: Verify Asistente Screen**
- [ ] See "🤖 Asistente" title
- [ ] See 4 action cards:
  - "🔍 Revisar Faltantes" → "Ir a Pendientes"
  - "📸 Capturar Evidencia" → "Capturar ahora"
  - "📊 Preparar Reporte" → "Seleccionar actividad"
  - "✅ Cerrar Actividad" → "Ir a actividades"
- [ ] **Expected**: All 4 workflows visible

**Step 28: Test Asistente Actions**
- [ ] Tap "Revisar Faltantes"
- [ ] Confirm navigate to Pendientes
- [ ] Go back
- [ ] Tap "Cerrar Actividad"
- [ ] Confirm navigate to Home
- [ ] **Expected**: Each action navigates correctly

---

### Section 8: Pendientes Screen - Part B (Steps 29-31)

**Step 29: Navigate to Pendientes**
- [ ] Go to Home
- [ ] Tap "Pendientes" quick action card
- [ ] **Expected**: Navigate to DailyAgendaScreen

**Step 30: Verify Pendientes Organization**
- [ ] See sections: Vencidas, Hoy, Próximos, Sin fecha
- [ ] Each section shows count
- [ ] Items organized by urgency
- [ ] **Expected**: Clear organization by date

**Step 31: Verify Completar Button**
- [ ] For each pending item, see "Completar" button
- [ ] Tap "Completar"
- [ ] Confirm status changes to "Completado"
- [ ] Confirm ✅ indicator appears
- [ ] **Expected**: Clear completion action

---

### Section 9: Layout & Colors - Part H (Steps 32-33)

**Step 32: Verify Improved Spacing**
- [ ] Open Home screen
- [ ] Check spacing between sections
- [ ] Verify whitespace is adequate
- [ ] Confirm no visual clutter
- [ ] **Expected**: Professional, clean layout

**Step 33: Verify Color Consistency**
- [ ] Check all screens for color consistency
- [ ] Verify no excessive saturation
- [ ] Confirm readable text contrast
- [ ] **Expected**: Professional color scheme

---

### Section 10: Final Verification (Step 34)

**Step 34: Comprehensive Action Audit**
- [ ] Verify NO dead buttons visible
- [ ] Verify all primary actions work
- [ ] Verify no misleading icons
- [ ] Verify all menu items functional
- [ ] Verify all navigation works
- [ ] Verify all text is clear and understandable
- [ ] **Expected**: All actions match user expectations

---

## Test Results Summary

### Critical Issues (Must Pass)
- [ ] Part A: Assistant opens AssistantScreen
- [ ] Part B: Pendientes checkboxes/buttons work
- [ ] Part C: Sugerir Agenda is clear
- [ ] Part D: Sin asignar is clear
- [ ] Part E: Capturar menu is clear
- [ ] Part F: Tomar foto button visible
- [ ] Part G: Nueva actividad is separate
- [ ] Part H: Layout is clean
- [ ] Part I: Status relationship is clear
- [ ] Part J: No dead actions

### Pass/Fail
- **Overall Result**: [ ] PASS [ ] FAIL
- **Critical Issues**: [ ] 0 [ ] 1+ 
- **Minor Issues**: [ ] 0 [ ] 1+

---

## Issues Found

### Critical Issues
| # | Issue | Screen | Severity | Status |
|---|-------|--------|----------|--------|
| | | | | |

### Minor Issues
| # | Issue | Screen | Severity | Status |
|---|-------|--------|----------|--------|
| | | | | |

---

## Notes

**Tester Name**: ___________________
**Device**: ___________________
**OS Version**: ___________________
**Test Date**: ___________________
**Test Time**: ___________________

**Additional Notes**:
```
[Space for additional observations]
```

---

## Sign-Off

- **Tester**: ___________________
- **Date**: ___________________
- **Result**: [ ] PASS [ ] FAIL
- **Ready for Release**: [ ] YES [ ] NO

---

**Test Plan Version**: v0.8.2
**Last Updated**: 2026-06-10 15:25 UTC
**Prepared By**: Roo (AI Engineer)
