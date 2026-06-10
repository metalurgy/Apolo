# Bitacora Pro Backend Contract
## v0.8.0 - AI-Ready Architecture

**Status**: Specification (not implemented)
**Purpose**: Define API contract for future cloud sync and AI integration
**Constraint**: No external AI calls from Android app

---

## Overview

This document defines the backend API contract for Bitacora Pro. The backend is designed to:
- Sync local data to cloud (future)
- Provide AI processing capabilities (future)
- Maintain user authentication (future)
- Enable multi-device sync (future)

**Current Status**: Local-first, no backend required
**Future**: Optional cloud sync with this contract

---

## Base URL

```
https://api.bitacora.pro/v1
```

---

## Authentication

### Header Format
```
Authorization: Bearer {jwt_token}
```

### Login Endpoint
```
POST /auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password"
}

Response:
{
  "token": "jwt_token",
  "user": {
    "id": "user_id",
    "email": "user@example.com",
    "name": "User Name"
  }
}
```

---

## Job Management

### Create Job
```
POST /jobs
Authorization: Bearer {token}
Content-Type: application/json

{
  "id": "uuid",
  "title": "Job Title",
  "clientName": "Client Name",
  "phone": "+525551234567",
  "serviceType": "Service Type",
  "status": "ACTIVE",
  "createdAt": 1234567890000,
  "updatedAt": 1234567890000,
  "lastUsedAt": 1234567890000,
  "notes": "Job notes"
}

Response: 201 Created
{
  "id": "uuid",
  "title": "Job Title",
  ...
}
```

### Get Job
```
GET /jobs/{jobId}
Authorization: Bearer {token}

Response: 200 OK
{
  "id": "uuid",
  "title": "Job Title",
  ...
}
```

### List Jobs
```
GET /jobs?status=ACTIVE&limit=50&offset=0
Authorization: Bearer {token}

Response: 200 OK
{
  "jobs": [
    {
      "id": "uuid",
      "title": "Job Title",
      ...
    }
  ],
  "total": 100,
  "limit": 50,
  "offset": 0
}
```

### Update Job
```
PUT /jobs/{jobId}
Authorization: Bearer {token}
Content-Type: application/json

{
  "title": "Updated Title",
  "status": "COMPLETED",
  ...
}

Response: 200 OK
{
  "id": "uuid",
  ...
}
```

### Delete Job
```
DELETE /jobs/{jobId}
Authorization: Bearer {token}

Response: 204 No Content
```

---

## Evidence Management

### Upload Evidence
```
POST /jobs/{jobId}/evidence
Authorization: Bearer {token}
Content-Type: multipart/form-data

Form Data:
- file: (binary file)
- type: "IMAGE" | "AUDIO" | "PDF" | "TEXT"
- category: "BEFORE" | "DURING" | "AFTER" | "MATERIAL" | "PAYMENT" | "CLIENT_MESSAGE"
- notes: "Optional notes"

Response: 201 Created
{
  "id": "evidence_id",
  "type": "IMAGE",
  "category": "BEFORE",
  "fileName": "evidence_id.jpg",
  "mimeType": "image/jpeg",
  "createdAt": 1234567890000,
  "notes": "Optional notes"
}
```

### Get Evidence
```
GET /jobs/{jobId}/evidence/{evidenceId}
Authorization: Bearer {token}

Response: 200 OK
{
  "id": "evidence_id",
  "type": "IMAGE",
  ...
}
```

### List Evidence
```
GET /jobs/{jobId}/evidence?category=BEFORE&limit=50
Authorization: Bearer {token}

Response: 200 OK
{
  "evidence": [
    {
      "id": "evidence_id",
      ...
    }
  ],
  "total": 100
}
```

### Delete Evidence
```
DELETE /jobs/{jobId}/evidence/{evidenceId}
Authorization: Bearer {token}

Response: 204 No Content
```

---

## Agenda Management

### Create Agenda Item
```
POST /jobs/{jobId}/agenda
Authorization: Bearer {token}
Content-Type: application/json

{
  "id": "uuid",
  "title": "Task Title",
  "description": "Task description",
  "dueAt": 1234567890000,
  "status": "PENDING",
  "reminderEnabled": true,
  "reminderOffsetDays": 1
}

Response: 201 Created
{
  "id": "uuid",
  ...
}
```

### Update Agenda Item
```
PUT /jobs/{jobId}/agenda/{agendaId}
Authorization: Bearer {token}
Content-Type: application/json

{
  "status": "DONE",
  ...
}

Response: 200 OK
{
  "id": "uuid",
  ...
}
```

### List Agenda Items
```
GET /jobs/{jobId}/agenda?status=PENDING
Authorization: Bearer {token}

Response: 200 OK
{
  "agenda": [
    {
      "id": "uuid",
      ...
    }
  ]
}
```

---

## AI Processing (Future)

### Generate Job Summary
```
POST /jobs/{jobId}/ai/summary
Authorization: Bearer {token}

Response: 200 OK
{
  "summary": "AI-generated summary of job",
  "keyPoints": ["point1", "point2"],
  "recommendations": ["recommendation1"]
}
```

### Extract Text from Evidence
```
POST /jobs/{jobId}/evidence/{evidenceId}/ai/extract-text
Authorization: Bearer {token}

Response: 200 OK
{
  "text": "Extracted text from image/PDF",
  "confidence": 0.95
}
```

### Generate Suggestions
```
POST /jobs/{jobId}/ai/suggestions
Authorization: Bearer {token}

Response: 200 OK
{
  "suggestions": [
    {
      "title": "Suggestion title",
      "description": "Suggestion description",
      "priority": 2
    }
  ]
}
```

---

## Error Responses

### 400 Bad Request
```json
{
  "error": "INVALID_REQUEST",
  "message": "Request validation failed",
  "details": {
    "field": "error message"
  }
}
```

### 401 Unauthorized
```json
{
  "error": "UNAUTHORIZED",
  "message": "Invalid or expired token"
}
```

### 403 Forbidden
```json
{
  "error": "FORBIDDEN",
  "message": "Access denied"
}
```

### 404 Not Found
```json
{
  "error": "NOT_FOUND",
  "message": "Resource not found"
}
```

### 500 Internal Server Error
```json
{
  "error": "INTERNAL_ERROR",
  "message": "Server error occurred"
}
```

---

## Rate Limiting

- **Limit**: 1000 requests per hour per user
- **Headers**:
  - `X-RateLimit-Limit`: 1000
  - `X-RateLimit-Remaining`: 999
  - `X-RateLimit-Reset`: 1234567890

---

## Data Models

### Job
```json
{
  "id": "uuid",
  "title": "string",
  "clientName": "string",
  "phone": "string",
  "serviceType": "string",
  "status": "ACTIVE|COMPLETED|ARCHIVED",
  "createdAt": "timestamp",
  "updatedAt": "timestamp",
  "lastUsedAt": "timestamp",
  "evidence": ["evidence_id"],
  "agendaItems": ["agenda_id"],
  "notes": "string",
  "reportNotes": "string"
}
```

### Evidence
```json
{
  "id": "uuid",
  "type": "TEXT|IMAGE|AUDIO|PDF",
  "category": "UNCLASSIFIED|BEFORE|DURING|AFTER|MATERIAL|PAYMENT|CLIENT_MESSAGE",
  "fileName": "string",
  "textContent": "string",
  "mimeType": "string",
  "createdAt": "timestamp",
  "notes": "string"
}
```

### AgendaItem
```json
{
  "id": "uuid",
  "jobId": "uuid",
  "title": "string",
  "description": "string",
  "dueAt": "timestamp|null",
  "dueText": "string",
  "status": "PENDING|DONE|CANCELLED|ARCHIVED",
  "sourceEvidenceId": "uuid|null",
  "createdAt": "timestamp",
  "updatedAt": "timestamp",
  "reminderEnabled": "boolean",
  "reminderOffsetDays": "number",
  "reminderScheduledAt": "timestamp|null",
  "notificationId": "number"
}
```

---

## Implementation Notes

1. **No External AI Calls from Android**: All AI processing happens on backend
2. **Local-First**: App works completely offline
3. **Optional Sync**: Backend is optional, not required
4. **Privacy**: No data collection without explicit user consent
5. **Backward Compatible**: New backend doesn't break existing local-only usage

---

## Future Enhancements

- [ ] Real-time sync
- [ ] Offline queue for sync
- [ ] Conflict resolution
- [ ] Multi-device sync
- [ ] AI-powered summaries
- [ ] OCR for evidence
- [ ] Advanced search
- [ ] Analytics

---

**Version**: 0.8.0
**Status**: Specification
**Last Updated**: June 10, 2026
