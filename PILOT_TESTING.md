# Bitacora Pro v0.4.1 - Pilot Testing Guide

## Overview
This document provides guidance for pilot testing Bitacora Pro v0.4.1 "Pilot Hardening & UX Polish". This is a stabilization release focused on improving the user experience and preparing the app for broader testing.

## What's New in v0.4.1

### 1. Debug Logs Gated
- All debug logs are now controlled by a `DEBUG_SHARE_INTENTS` flag in `MainActivity`
- Set to `false` by default for cleaner production logs
- Can be toggled to `true` for troubleshooting if needed

### 2. Spanish-Friendly UI Labels
All user-facing labels have been translated to Spanish:
- **HomeScreen**: "Sin trabajos aún", "Crear Nuevo Trabajo", "Cliente", "Teléfono", "Servicio", "Evidencia"
- **ShareIntakeScreen**: "Recibir Contenido", "Contenido Recibido", "Agregar al Trabajo Reciente", "Crear Nuevo", "Agregar"
- **JobDetailScreen**: "Detalles del Trabajo", "Evidencia", "Agenda", "Pendiente", "Completado", "Eliminar", "Sugerir Agenda"
- **Evidence Categories**: "Sin clasificar", "Antes", "Durante", "Después", "Material", "Pago", "Mensaje del cliente"
- **Evidence Types**: "Texto", "Imagen", "Audio", "PDF"
- **Agenda Status**: "Pendiente", "Completado", "Cancelado"

### 3. Delete Confirmations
- **Evidence Deletion**: Users are now prompted with a confirmation dialog before deleting evidence
- **Agenda Item Deletion**: Users are now prompted with a confirmation dialog before deleting agenda items
- Prevents accidental data loss

### 4. Improved Empty States
- **HomeScreen**: Enhanced empty state with centered message and call-to-action button
- **JobDetailScreen**: Better empty state messaging for evidence and agenda items
- Clearer guidance on what to do when no data exists

### 5. Polish & UX Improvements
- **ShareIntakeScreen**: Refined labels and button text for clarity
- **JobDetailScreen**: Better visual hierarchy and spacing
- **HomeScreen**: Added app version footer (v0.4.1) for reference

## Testing Checklist

### Core Functionality
- [ ] Create a new job with title, client name, phone, and service type
- [ ] Share content from WhatsApp (text and/or images)
- [ ] Share content from Gallery (single and multiple images)
- [ ] Add shared content to an existing job
- [ ] Create a new job from shared content
- [ ] View job details and evidence
- [ ] View agenda items

### Spanish Labels
- [ ] Verify all UI text is in Spanish
- [ ] Check that category labels display correctly in Spanish
- [ ] Verify evidence type labels are in Spanish
- [ ] Check agenda status labels are in Spanish

### Delete Confirmations
- [ ] Delete evidence - confirm dialog appears
- [ ] Cancel delete evidence - dialog closes without deleting
- [ ] Confirm delete evidence - evidence is removed
- [ ] Delete agenda item - confirm dialog appears
- [ ] Cancel delete agenda item - dialog closes without deleting
- [ ] Confirm delete agenda item - agenda item is removed

### Empty States
- [ ] Launch app with no jobs - see improved empty state message
- [ ] Create a job with no evidence - see "Sin evidencia aún" message
- [ ] Create a job with no agenda items - see "Sin elementos de agenda aún" message

### UI Polish
- [ ] Check app version footer on HomeScreen (v0.4.1)
- [ ] Verify spacing and alignment throughout the app
- [ ] Check that all buttons and text are readable
- [ ] Verify colors and contrast meet accessibility standards

### Share Intent Flow
- [ ] Share text from WhatsApp → app opens with text preview
- [ ] Share image from Gallery → app opens with file preview
- [ ] Share multiple images → app shows file count
- [ ] Share to existing job → files are copied correctly
- [ ] Share to new job → job is created with files

### Performance
- [ ] App launches quickly
- [ ] Job list loads without lag
- [ ] Evidence displays without delay
- [ ] No crashes when deleting items
- [ ] No crashes when sharing content

## Known Limitations (v0.4.1)

This is a stabilization release. The following features are NOT included:
- Cloud sync / Firebase integration
- Login / Authentication
- Billing / Subscription
- OCR / AI-powered features
- Advanced search / filtering
- Export / Backup features

These will be addressed in future releases.

## Bug Reporting

If you encounter any issues during pilot testing:

1. **Note the exact steps to reproduce**
2. **Describe what you expected to happen**
3. **Describe what actually happened**
4. **Include any error messages or logs**
5. **Note your device model and Android version**

## Feedback

We welcome feedback on:
- UI/UX improvements
- Label clarity and translations
- Performance and stability
- Feature requests for future releases
- Any bugs or unexpected behavior

## Version Information

- **App Version**: 0.4.1
- **Build Type**: Debug (for testing)
- **Minimum SDK**: 26 (Android 8.0)
- **Target SDK**: 34 (Android 14)

## Support

For questions or issues during pilot testing, please contact the development team.

---

**Last Updated**: June 2026
**Status**: Pilot Testing Phase
