# AI Backend Contract v0.9.0

**Status**: Specification for Future Implementation  
**Version**: v0.9.0  
**Audience**: Backend developers, DevOps, Security team

---

## Overview

This document specifies the contract between Bitacora Pro Android app and the Bitacora backend for AI assistant functionality.

**Key Principle**: The Android app NEVER calls LLM providers directly. All LLM calls go through the Bitacora backend proxy.

---

## Architecture

```
┌─────────────────────┐
│  Bitacora Pro App   │
│   (Android)         │
└──────────┬──────────┘
           │ HTTPS REST
           ▼
┌─────────────────────┐
│ Bitacora Backend    │
│ (Proxy)             │
└──────────┬──────────┘
           │ (Internal)
           ▼
┌─────────────────────┐
│ LLM Provider        │
│ (OpenAI/Anthropic)  │
└─────────────────────┘
```

---

## Security Requirements

### Android App
- ❌ No API keys stored in APK
- ❌ No direct calls to LLM providers
- ❌ No hardcoded backend URLs (configurable)
- ✅ All calls through HTTPS only
- ✅ User consent before sending activity data
- ✅ Graceful fallback to local assistant

### Backend
- ✅ Validate all requests from app
- ✅ Rate limit per user/device
- ✅ Log all LLM requests for audit
- ✅ Encrypt activity data in transit
- ✅ Never store activity data permanently
- ✅ Implement request signing/verification

---

## API Endpoints

### 1. Ask Question

**Endpoint**: `POST /api/v1/assistant/ask`

**Request**:
```json
{
  "question": "¿Cómo capturo evidencia?",
  "activity_summary": "Actividad: Reparación de puerta\nCliente: Juan Pérez\n...",
  "mode": "local_with_remote_fallback"
}
```

**Response (Success)**:
```json
{
  "status": "success",
  "answer": "Para capturar evidencia:\n1. Toca el botón '+ Capturar'...",
  "mode_used": "remote",
  "confidence": 0.95
}
```

**Response (Fallback)**:
```json
{
  "status": "fallback",
  "message": "Backend unavailable. Using local assistant.",
  "mode_used": "local"
}
```

**Response (Error)**:
```json
{
  "status": "error",
  "error": "Invalid question format",
  "code": "INVALID_REQUEST"
}
```

### 2. Generate Suggestions

**Endpoint**: `POST /api/v1/assistant/suggestions`

**Request**:
```json
{
  "activity_id": "job_12345",
  "activity_summary": "...",
  "evidence_count": 5,
  "pending_count": 3
}
```

**Response**:
```json
{
  "status": "success",
  "suggestions": [
    "Considera agregar más evidencia de la fase 'Después'",
    "Puedes generar un reporte PDF con la evidencia actual"
  ]
}
```

### 3. Analyze Activity

**Endpoint**: `POST /api/v1/assistant/analyze`

**Request**:
```json
{
  "activity_id": "job_12345",
  "activity_summary": "...",
  "include_recommendations": true
}
```

**Response**:
```json
{
  "status": "success",
  "analysis": "Análisis de evidencia:\n• Total: 5 elementos\n...",
  "recommendations": [
    "Agregar fotos de la fase 'Después'",
    "Documentar pagos realizados"
  ]
}
```

### 4. Health Check

**Endpoint**: `GET /api/v1/health`

**Response**:
```json
{
  "status": "healthy",
  "version": "v0.9.0",
  "llm_provider": "openai",
  "llm_status": "operational"
}
```

---

## Request Format

### Headers
```
Content-Type: application/json
Authorization: Bearer {device_token}
X-App-Version: v0.9.0
X-Device-Id: {unique_device_id}
```

### Activity Summary Format
```
Actividad: {title}
Cliente: {client_name}
Teléfono: {phone_number}
Estado: {status}
Evidencia: {count} elementos
Pendientes: {count} tareas
Notas: {first_200_chars_of_notes}
```

---

## Response Format

### Success Response
```json
{
  "status": "success",
  "data": { ... },
  "timestamp": "2026-06-10T17:00:00Z",
  "request_id": "req_abc123"
}
```

### Error Response
```json
{
  "status": "error",
  "error": "Error message",
  "code": "ERROR_CODE",
  "timestamp": "2026-06-10T17:00:00Z",
  "request_id": "req_abc123"
}
```

---

## Error Codes

| Code | Meaning | Action |
|------|---------|--------|
| INVALID_REQUEST | Malformed request | Retry with valid format |
| UNAUTHORIZED | Invalid token | Re-authenticate |
| RATE_LIMITED | Too many requests | Exponential backoff |
| BACKEND_ERROR | Server error | Fallback to local |
| LLM_ERROR | LLM provider error | Fallback to local |
| TIMEOUT | Request timeout | Retry with backoff |

---

## Rate Limiting

- **Per user**: 100 requests/hour
- **Per device**: 50 requests/hour
- **Burst**: 10 requests/minute
- **Response**: 429 Too Many Requests with Retry-After header

---

## Data Privacy

### What Gets Sent
- Question text
- Activity summary (title, client, phone, status, counts)
- First 200 characters of notes
- Evidence categories (not content)
- Pending item titles (not descriptions)

### What Never Gets Sent
- ❌ Full evidence content
- ❌ Full note text
- ❌ Photo data
- ❌ Document content
- ❌ Full WhatsApp chat
- ❌ Personal identifiable information beyond what user provides

### Data Retention
- Backend: Delete after 24 hours
- Logs: Anonymize after 30 days
- Never store activity data permanently

---

## Authentication

### Device Token Flow
1. App generates unique device ID (UUID)
2. App requests device token from backend
3. Backend validates app signature
4. Backend returns device token (valid 30 days)
5. App includes token in all requests
6. Backend validates token on each request

### Token Refresh
- Automatic refresh 7 days before expiry
- Manual refresh on 401 response
- Graceful fallback if refresh fails

---

## Timeout & Retry

### Timeouts
- Connection timeout: 10 seconds
- Read timeout: 30 seconds
- Write timeout: 10 seconds

### Retry Strategy
- Exponential backoff: 1s, 2s, 4s, 8s, 16s
- Max retries: 3
- Jitter: ±10%
- Don't retry on 4xx errors (except 429)

---

## Monitoring & Logging

### Backend Logs
- All requests logged with timestamp, device_id, request_id
- All errors logged with error code and context
- All LLM calls logged with prompt and response length
- Anonymize logs after 30 days

### Metrics
- Request latency (p50, p95, p99)
- Error rate by error code
- LLM provider latency
- Cache hit rate

---

## Future Enhancements

### v0.9.1+
- Streaming responses for long answers
- WebSocket for real-time suggestions
- Caching of common questions
- Multi-language support
- Custom model selection

### v1.0+
- User preferences for LLM model
- Activity-specific context learning
- Offline mode with sync
- Advanced analytics

---

## Implementation Checklist

### Backend Team
- [ ] Implement `/api/v1/assistant/ask` endpoint
- [ ] Implement `/api/v1/assistant/suggestions` endpoint
- [ ] Implement `/api/v1/assistant/analyze` endpoint
- [ ] Implement `/api/v1/health` endpoint
- [ ] Implement device token authentication
- [ ] Implement rate limiting
- [ ] Implement request logging
- [ ] Implement error handling
- [ ] Implement LLM provider integration
- [ ] Implement data retention policy
- [ ] Set up monitoring and alerts
- [ ] Security audit

### Android Team
- [ ] Implement RemoteAssistantProvider HTTP calls
- [ ] Implement device token management
- [ ] Implement retry logic
- [ ] Implement user consent dialog
- [ ] Implement error handling
- [ ] Implement fallback to local assistant
- [ ] Test with backend
- [ ] Security review

---

## Testing

### Unit Tests
- Request/response serialization
- Error handling
- Retry logic
- Token refresh

### Integration Tests
- End-to-end question answering
- Fallback behavior
- Rate limiting
- Authentication

### Load Tests
- 1000 concurrent users
- 100 requests/second
- Latency under 2 seconds (p95)

---

## Deployment

### Prerequisites
- Backend infrastructure ready
- LLM provider account configured
- SSL certificates installed
- Rate limiting configured
- Monitoring set up

### Rollout
1. Deploy backend in staging
2. Test with staging app
3. Deploy to production
4. Monitor for errors
5. Gradually increase traffic

---

## Support & Escalation

### Issues
- Backend errors: Escalate to backend team
- LLM provider errors: Check provider status
- Rate limiting: Increase limits if needed
- Data privacy concerns: Escalate to security team

---

**Version**: v0.9.0  
**Status**: Specification for Future Implementation  
**Last Updated**: June 2026
