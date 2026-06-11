package com.bitacora.pro.assistant

import com.bitacora.pro.data.models.JobFile
import kotlinx.coroutines.delay

/**
 * Remote AI assistant provider.
 * Calls Bitacora backend proxy for LLM responses.
 * v0.9.0: Placeholder for future backend integration
 *
 * IMPORTANT SECURITY NOTES:
 * - No API keys are stored in this APK
 * - All calls go through Bitacora backend proxy only
 * - Never calls OpenAI/Anthropic/Gemini directly from Android
 * - User consent required before sending activity data
 * - Backend URL must be configured externally
 */
object RemoteAssistantProvider {

    /**
     * Request an answer from the remote backend.
     * Returns null if backend is not configured or network error occurs.
     *
     * @param question User's question
     * @param job Optional activity context
     * @param backendUrl Bitacora backend URL (e.g., https://api.bitacora.example.com)
     * @return Response from backend or null
     */
    suspend fun answerQuestion(
        question: String,
        job: JobFile? = null,
        backendUrl: String?
    ): String? {
        // v0.9.0: Placeholder implementation
        // In production, this would:
        // 1. Validate backendUrl is configured
        // 2. Build request payload with question and optional job summary
        // 3. Call POST /api/v1/assistant/ask
        // 4. Parse response
        // 5. Return answer or error message

        if (backendUrl.isNullOrEmpty()) {
            return null
        }

        // TODO: Implement actual HTTP call to backend
        // For now, return null to indicate not configured
        return null
    }

    /**
     * Generate activity suggestions using remote AI.
     * Returns empty list if backend is not configured.
     */
    suspend fun generateActivitySuggestions(
        job: JobFile,
        backendUrl: String?
    ): List<String> {
        // v0.9.0: Placeholder implementation
        // In production, this would call backend to generate suggestions

        if (backendUrl.isNullOrEmpty()) {
            return emptyList()
        }

        // TODO: Implement actual HTTP call to backend
        return emptyList()
    }

    /**
     * Analyze activity using remote AI.
     * Returns null if backend is not configured.
     */
    suspend fun analyzeActivity(
        job: JobFile,
        backendUrl: String?
    ): String? {
        // v0.9.0: Placeholder implementation
        // In production, this would call backend for analysis

        if (backendUrl.isNullOrEmpty()) {
            return null
        }

        // TODO: Implement actual HTTP call to backend
        return null
    }

    /**
     * Build a safe summary of activity for sending to backend.
     * Excludes sensitive data, only sends what's necessary.
     */
    fun buildActivitySummary(job: JobFile): String {
        return buildString {
            append("Actividad: ${job.title}\n")
            append("Cliente: ${job.clientName}\n")
            if (job.phone.isNotEmpty()) {
                append("Teléfono: ${job.phone}\n")
            }
            append("Estado: ${job.status}\n")
            append("Evidencia: ${job.evidence.size} elementos\n")
            append("Pendientes: ${job.agendaItems.size} tareas\n")
            if (job.reportNotes.isNotEmpty()) {
                append("Notas: ${job.reportNotes.take(200)}...\n")
            }
        }
    }

    /**
     * Validate backend URL format.
     * Returns true if URL looks valid (https://).
     */
    fun isValidBackendUrl(url: String?): Boolean {
        if (url.isNullOrEmpty()) return false
        return url.startsWith("https://") && url.length > 10
    }
}
