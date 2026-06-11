package com.bitacora.pro.assistant

/**
 * Defines the mode of operation for the AI assistant.
 * v0.9.0: Local-first with optional remote backend
 */
enum class AssistantMode {
    /**
     * Local mode: Uses built-in rules and local data analysis.
     * No internet required. Limited capabilities.
     */
    LOCAL,

    /**
     * Remote mode: Calls Bitacora backend proxy for LLM responses.
     * Requires internet and backend configuration.
     * User consent required before sending activity data.
     */
    REMOTE
}

/**
 * Configuration for the AI assistant.
 */
data class AssistantConfig(
    val mode: AssistantMode = AssistantMode.LOCAL,
    val remoteBackendUrl: String? = null,
    val isRemoteEnabled: Boolean = remoteBackendUrl != null
)
