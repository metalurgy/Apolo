package com.bitacora.pro.assistant

import com.bitacora.pro.data.models.JobFile

/**
 * Main AI assistant provider.
 * Coordinates between local and remote assistant modes.
 * v0.9.0: Local-first with optional remote backend
 *
 * ARCHITECTURE:
 * - Local mode: Always available, works offline
 * - Remote mode: Optional, requires backend configuration
 * - Fallback: Remote errors fall back to local mode
 * - No API keys in APK
 * - No direct calls to LLM providers
 */
object AiAssistantProvider {

    /**
     * Answer a question using the configured assistant mode.
     * Falls back to local if remote fails or is not configured.
     */
    suspend fun answerQuestion(
        question: String,
        config: AssistantConfig,
        job: JobFile? = null
    ): String {
        return when {
            // Try remote if enabled
            config.isRemoteEnabled && config.mode == AssistantMode.REMOTE -> {
                val remoteAnswer = RemoteAssistantProvider.answerQuestion(
                    question,
                    job,
                    config.remoteBackendUrl
                )
                remoteAnswer ?: fallbackToLocal(question, job)
            }

            // Use local mode
            else -> LocalAssistantProvider.answerQuestion(question, job)
                ?: "No tengo una respuesta para esa pregunta. Intenta preguntar sobre cómo usar Bitacora Pro."
        }
    }

    /**
     * Generate suggestions for an activity.
     */
    suspend fun generateActivitySuggestions(
        job: JobFile,
        config: AssistantConfig
    ): List<String> {
        return when {
            config.isRemoteEnabled && config.mode == AssistantMode.REMOTE -> {
                val remoteSuggestions = RemoteAssistantProvider.generateActivitySuggestions(
                    job,
                    config.remoteBackendUrl
                )
                if (remoteSuggestions.isNotEmpty()) {
                    remoteSuggestions
                } else {
                    LocalAssistantProvider.generateActivitySuggestions(job)
                }
            }

            else -> LocalAssistantProvider.generateActivitySuggestions(job)
        }
    }

    /**
     * Analyze an activity using the configured assistant mode.
     */
    suspend fun analyzeActivity(
        job: JobFile,
        config: AssistantConfig
    ): String {
        return when {
            config.isRemoteEnabled && config.mode == AssistantMode.REMOTE -> {
                val remoteAnalysis = RemoteAssistantProvider.analyzeActivity(
                    job,
                    config.remoteBackendUrl
                )
                remoteAnalysis ?: LocalAssistantProvider.analyzeEvidencePatterns(job)
            }

            else -> LocalAssistantProvider.analyzeEvidencePatterns(job)
        }
    }

    /**
     * Get the current assistant mode description.
     */
    fun getModeDescription(config: AssistantConfig): String {
        return when {
            config.isRemoteEnabled && config.mode == AssistantMode.REMOTE ->
                "IA en línea activa (con fallback local)"

            config.isRemoteEnabled ->
                "IA local activa (remota disponible pero deshabilitada)"

            else -> "IA local activa"
        }
    }

    /**
     * Check if remote assistant is properly configured.
     */
    fun isRemoteConfigured(config: AssistantConfig): Boolean {
        return config.isRemoteEnabled &&
                RemoteAssistantProvider.isValidBackendUrl(config.remoteBackendUrl)
    }

    /**
     * Fallback to local assistant when remote fails.
     */
    private fun fallbackToLocal(question: String, job: JobFile?): String {
        val localAnswer = LocalAssistantProvider.answerQuestion(question, job)
        return if (localAnswer != null) {
            localAnswer
        } else {
            "El asistente en línea no está disponible. Usando modo local.\n\n" +
                    "Intenta preguntar sobre cómo usar Bitacora Pro."
        }
    }

    /**
     * Build a safe activity summary for remote analysis.
     * Only includes necessary data, excludes sensitive information.
     */
    fun buildActivitySummaryForRemote(job: JobFile): String {
        return RemoteAssistantProvider.buildActivitySummary(job)
    }
}
