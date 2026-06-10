# Bitacora Pro v0.8.0 Implementation Plan
## "Daily Copilot + AI-Ready Assistant"

**Status**: Pilot-ready, AI-ready, backend-ready (not production-ready)
**Release Date**: June 2026
**Target**: 17 implementation parts + verification

---

## Hard Constraints (Non-Negotiable)

✅ **MUST HAVE:**
- NO API keys hardcoded
- NO external AI calls from Android
- NO Firebase, Room, cloud sync
- NO WhatsApp scraping
- NO accessibility service abuse
- NO hidden network calls
- Calendar/browser intents are OK
- Local-first architecture maintained
- 100% backward compatible

---

## Implementation Parts Overview

### Part 0: Verify Contact Import Phone Number Fix (Precondition)
**Status**: Pending
**Objective**: Ensure phone number import from contacts works correctly
**Files to Check**:
- `PhoneNumberUtils.kt` - Phone number parsing and validation
- `CreateJobScreen.kt` - Contact picker integration
- `ShareIntakeScreen.kt` - Phone number display

**Acceptance Criteria**:
- [ ] Phone numbers imported from contacts are properly formatted
- [ ] Phone number validation works for various formats
- [ ] No crashes when importing contacts with missing phone numbers

---

### Part A: Daily Copilot Home Redesign
**Status**: Pending
**Objective**: Transform HomeScreen into a Daily Copilot dashboard
**Current State**: v0.7.3 with smart dashboard
**Changes**:
- Add "Daily Copilot" branding and messaging
- Reorganize dashboard for daily productivity focus
- Add quick action cards (Capture, Inbox, Agenda, Assistant)
- Simplify job list to show only today's/this week's jobs
- Add "What's next?" section with AI-ready placeholders

**Files to Modify**:
- `HomeScreen.kt` - Main redesign
- `Theme.kt` - Color scheme updates if needed

**Acceptance Criteria**:
- [ ] Home shows "Daily Copilot" branding
- [ ] Quick action cards visible and clickable
- [ ] Dashboard shows relevant jobs for today/week
- [ ] Professional, clean layout

---

### Part B: Universal Capture Button and Menu
**Status**: Pending
**Objective**: Add floating capture button with menu for quick actions
**Features**:
- Floating action button with menu
- Options: Capture Photo, Capture Audio, Capture Text, Share Intent
- Quick job selection
- Accessible from any screen

**Files to Create**:
- `CaptureButton.kt` - Reusable capture button component
- `CaptureMenu.kt` - Menu UI

**Files to Modify**:
- `MainActivity.kt` - Add capture button to nav
- `HomeScreen.kt` - Integrate capture button

**Acceptance Criteria**:
- [ ] Capture button visible on home screen
- [ ] Menu opens with 4 options
- [ ] Each option navigates correctly
- [ ] Works from any screen

---

### Part C: Universal Inbox System
**Status**: Pending
**Objective**: Create local inbox for captured content before job assignment
**Features**:
- Inbox screen showing unassigned captures
- Quick preview of content
- Batch operations (assign to job, delete, archive)
- Persistent storage in local inbox folder

**Files to Create**:
- `InboxScreen.kt` - Inbox UI
- `InboxManager.kt` - Inbox storage logic

**Files to Modify**:
- `StorageManager.kt` - Add inbox methods
- `NavRoutes.kt` - Add inbox route
- `MainActivity.kt` - Add inbox navigation

**Acceptance Criteria**:
- [ ] Inbox screen displays unassigned captures
- [ ] Can assign captures to jobs
- [ ] Can delete/archive inbox items
- [ ] Data persists across app restarts

---

### Part D: Daily Agenda Screen
**Status**: Pending
**Objective**: Create dedicated agenda view for daily tasks and reminders
**Features**:
- List of today's agenda items
- Grouped by job
- Quick status updates (mark done, reschedule)
- Add new agenda item
- Integration with reminders

**Files to Create**:
- `DailyAgendaScreen.kt` - Agenda UI

**Files to Modify**:
- `NavRoutes.kt` - Add agenda route
- `MainActivity.kt` - Add agenda navigation
- `HomeScreen.kt` - Link to agenda

**Acceptance Criteria**:
- [ ] Agenda screen shows today's items
- [ ] Can mark items as done
- [ ] Can reschedule items
- [ ] Reminders still work

---

### Part E: Google Calendar Intent Integration
**Status**: Pending
**Objective**: Allow creating calendar events from agenda items
**Features**:
- Intent to Google Calendar app
- Pre-fill event details from agenda item
- No API calls, just intent-based
- Fallback to generic calendar intent

**Files to Create**:
- `CalendarIntentHelper.kt` - Calendar intent logic

**Files to Modify**:
- `DailyAgendaScreen.kt` - Add "Add to Calendar" button
- `AgendaItem` model - Add calendar-related fields if needed

**Acceptance Criteria**:
- [ ] Can create calendar event from agenda item
- [ ] Event details pre-filled correctly
- [ ] Works with Google Calendar app
- [ ] Graceful fallback if app not installed

---

### Part F: Smart Daily Assistant Engine
**Status**: Pending
**Objective**: Create local AI-ready assistant engine (no external calls)
**Features**:
- Analyze job data locally
- Generate suggestions for next steps
- Identify patterns in evidence
- Recommend agenda items
- All processing local, no API calls

**Files to Create**:
- `SmartDailyAssistantEngine.kt` - Core assistant logic
- `AssistantSuggestion.kt` - Suggestion data model

**Files to Modify**:
- `Models.kt` - Add suggestion models
- `HomeScreen.kt` - Display suggestions

**Acceptance Criteria**:
- [ ] Engine generates suggestions locally
- [ ] No external API calls
- [ ] Suggestions are relevant and actionable
- [ ] Performance is acceptable

---

### Part G: Guided Assistant Workflows
**Status**: Pending
**Objective**: Create step-by-step workflows for common tasks
**Features**:
- Job closure workflow
- Evidence collection workflow
- Client follow-up workflow
- Payment tracking workflow
- All local, no external calls

**Files to Create**:
- `AssistantWorkflow.kt` - Workflow base class
- `JobClosureWorkflow.kt` - Job closure steps
- `EvidenceCollectionWorkflow.kt` - Evidence collection steps
- `WorkflowScreen.kt` - Workflow UI

**Files to Modify**:
- `NavRoutes.kt` - Add workflow routes
- `MainActivity.kt` - Add workflow navigation

**Acceptance Criteria**:
- [ ] Workflows guide users through tasks
- [ ] Each step is clear and actionable
- [ ] Can save progress and resume
- [ ] All local processing

---

### Part H: Web/Browser Actions Helper
**Status**: Pending
**Objective**: Create helper for opening web links and browser actions
**Features**:
- Open URLs in browser
- Search for client info
- Open maps for location
- Open email client for communication
- All via standard intents

**Files to Create**:
- `BrowserActionsHelper.kt` - Browser intent logic

**Files to Modify**:
- `JobDetailScreen.kt` - Add browser action buttons
- `AgendaItem` model - Add URL fields if needed

**Acceptance Criteria**:
- [ ] Can open URLs in browser
- [ ] Can search for information
- [ ] Can open maps
- [ ] Can open email client
- [ ] All via standard intents

---

### Part I: AI-Ready Backend Architecture
**Status**: Pending
**Objective**: Design backend contract for future AI integration
**Features**:
- Define API contract (no implementation)
- Document expected endpoints
- Define request/response formats
- Plan for future cloud sync
- Maintain local-first architecture

**Files to Create**:
- `BackendContract.md` - API specification
- `BackendModels.kt` - Data models for backend
- `BackendClient.kt` - Stub implementation (no-op)

**Acceptance Criteria**:
- [ ] Backend contract documented
- [ ] Models defined for all entities
- [ ] Stub client created (no-op)
- [ ] Ready for future implementation

---

### Part J: Backend Contract Documentation
**Status**: Pending
**Objective**: Document the backend API contract
**Features**:
- REST API endpoints
- Request/response formats
- Authentication (future)
- Error handling
- Rate limiting (future)

**Files to Create**:
- `BACKEND_CONTRACT.md` - Full API documentation

**Acceptance Criteria**:
- [ ] All endpoints documented
- [ ] Request/response examples provided
- [ ] Error codes defined
- [ ] Clear and comprehensive

---

### Part K: Privacy Messaging
**Status**: Pending
**Objective**: Add privacy notices and data handling information
**Features**:
- Privacy notice on welcome screen
- Data handling explanation
- Local-first messaging
- No cloud sync messaging
- GDPR/privacy compliance info

**Files to Modify**:
- `WelcomeScreen.kt` - Add privacy notice
- `strings.xml` - Add privacy strings
- `README.md` - Add privacy section

**Acceptance Criteria**:
- [ ] Privacy notice displayed
- [ ] Clear data handling explanation
- [ ] Users understand local-first approach
- [ ] Compliant with privacy regulations

---

### Part L: Event and Reminder Upgrades
**Status**: Pending
**Objective**: Enhance reminders and event scheduling
**Features**:
- Improved reminder UI
- Recurring reminders
- Smart reminder timing
- Event categorization
- Better notification handling

**Files to Modify**:
- `AgendaNotificationScheduler.kt` - Enhanced scheduling
- `AgendaReminderReceiver.kt` - Better notification handling
- `DailyAgendaScreen.kt` - Enhanced reminder UI
- `Models.kt` - Add reminder fields

**Acceptance Criteria**:
- [ ] Reminders work reliably
- [ ] Can set recurring reminders
- [ ] Smart timing suggestions
- [ ] Notifications are clear

---

### Part M: Job Closure Workflow
**Status**: Pending
**Objective**: Create guided workflow for closing jobs
**Features**:
- Step-by-step closure process
- Evidence checklist
- Final notes
- Status update to COMPLETED
- Archive option
- PDF report generation option

**Files to Create**:
- `JobClosureWorkflow.kt` - Closure workflow
- `JobClosureScreen.kt` - Closure UI

**Files to Modify**:
- `JobDetailScreen.kt` - Add closure button
- `NavRoutes.kt` - Add closure route

**Acceptance Criteria**:
- [ ] Workflow guides job closure
- [ ] Evidence checklist works
- [ ] Job status updates correctly
- [ ] Can generate report

---

### Part N: UI Simplification Pass
**Status**: Pending
**Objective**: Simplify UI across entire app
**Features**:
- Remove unnecessary elements
- Improve whitespace
- Cleaner typography
- Consistent spacing
- Better visual hierarchy

**Files to Modify**:
- All screen files
- `Theme.kt` - Simplify theme
- `Type.kt` - Simplify typography

**Acceptance Criteria**:
- [ ] UI is cleaner and simpler
- [ ] Better whitespace
- [ ] Consistent styling
- [ ] Professional appearance

---

### Part O: Documentation
**Status**: Pending
**Objective**: Create comprehensive documentation
**Files to Create**:
- `BITACORA_PRO_v0.8.0_RELEASE_NOTES.md` - Release notes
- `BITACORA_PRO_v0.8.0_USER_GUIDE.md` - User guide
- `BITACORA_PRO_v0.8.0_DEVELOPER_GUIDE.md` - Developer guide
- `BITACORA_PRO_v0.8.0_ARCHITECTURE.md` - Architecture overview

**Acceptance Criteria**:
- [ ] Release notes complete
- [ ] User guide comprehensive
- [ ] Developer guide clear
- [ ] Architecture documented

---

### Part P: Hard Constraints Verification
**Status**: Pending
**Objective**: Verify all hard constraints are met
**Checks**:
- [ ] No API keys in code
- [ ] No external AI calls from Android
- [ ] No Firebase usage
- [ ] No Room database usage
- [ ] No cloud sync
- [ ] No WhatsApp scraping
- [ ] No accessibility service abuse
- [ ] No hidden network calls
- [ ] Calendar/browser intents only
- [ ] Local-first architecture
- [ ] 100% backward compatible

**Files to Check**:
- All source files
- `AndroidManifest.xml`
- `build.gradle.kts`
- All dependencies

---

### Part Q: Manual Testing
**Status**: Pending
**Objective**: Comprehensive manual testing
**Test Scenarios**:
- [ ] Share intent from WhatsApp
- [ ] Share intent from Gallery
- [ ] Create job manually
- [ ] Capture photo
- [ ] Capture audio
- [ ] Capture text
- [ ] Inbox operations
- [ ] Agenda operations
- [ ] Calendar integration
- [ ] Job closure workflow
- [ ] Reminders and notifications
- [ ] Data persistence
- [ ] App restart
- [ ] Multiple jobs
- [ ] Evidence management

---

## Implementation Order

1. **Part 0**: Verify phone number fix (precondition)
2. **Part A**: Daily Copilot Home redesign
3. **Part B**: Universal Capture button
4. **Part C**: Inbox system
5. **Part D**: Daily Agenda screen
6. **Part E**: Google Calendar intent
7. **Part F**: Smart Assistant engine
8. **Part G**: Guided workflows
9. **Part H**: Browser actions helper
10. **Part I**: Backend architecture
11. **Part J**: Backend documentation
12. **Part K**: Privacy messaging
13. **Part L**: Reminder upgrades
14. **Part M**: Job closure workflow
15. **Part N**: UI simplification
16. **Part O**: Documentation
17. **Part P**: Constraints verification
18. **Part Q**: Manual testing

---

## Key Principles

1. **Local-First**: All processing happens locally
2. **No External Calls**: No API calls from Android app
3. **Intent-Based**: Use Android intents for external actions
4. **Backward Compatible**: All changes maintain compatibility
5. **User Privacy**: No data collection or transmission
6. **Simple UI**: Clean, professional, easy to use
7. **Reliable Storage**: All data persists correctly

---

## Success Criteria

- ✅ All 17 parts implemented
- ✅ All hard constraints verified
- ✅ Manual testing passed
- ✅ No crashes or errors
- ✅ Professional UI
- ✅ Comprehensive documentation
- ✅ Pilot-ready for testing
- ✅ AI-ready for future integration
- ✅ Backend-ready for future cloud sync

---

## Timeline

- **Phase 1** (Parts 0-3): Core infrastructure (2-3 hours)
- **Phase 2** (Parts 4-7): Daily features (2-3 hours)
- **Phase 3** (Parts 8-12): Assistant and privacy (2-3 hours)
- **Phase 4** (Parts 13-17): Polish and verification (2-3 hours)

**Total Estimated Time**: 8-12 hours

---

## Notes

- All code follows Kotlin best practices
- All UI uses Jetpack Compose
- All storage uses local files (no Room, no Firebase)
- All intents are standard Android intents
- All documentation is comprehensive and clear
- All testing is manual (no automated tests required)

---

**Version**: 0.8.0
**Status**: Implementation Plan
**Last Updated**: June 10, 2026
