package com.bitacora.pro.whatsapp

import com.bitacora.pro.data.models.JobFile
import com.bitacora.pro.utils.PhoneNumberUtils

/**
 * Smart job matching from WhatsApp content.
 * Analyzes WhatsApp exports and suggests matching jobs.
 * 
 * Features:
 * - Matches by phone number (primary)
 * - Matches by sender name (secondary)
 * - Matches by content keywords (tertiary)
 * - Confidence scoring
 * - Privacy-first: local processing only
 */
object WhatsAppJobMatcher {

    data class JobMatch(
        val job: JobFile,
        val confidence: Float, // 0.0 to 1.0
        val matchReasons: List<String>
    )

    /**
     * Finds matching jobs for a WhatsApp export.
     * Returns jobs sorted by confidence (highest first).
     */
    fun findMatchingJobs(
        export: WhatsAppExportParser.WhatsAppChatExport,
        availableJobs: List<JobFile>
    ): List<JobMatch> {
        if (availableJobs.isEmpty()) return emptyList()

        val matches = mutableListOf<JobMatch>()

        for (job in availableJobs) {
            val confidence = calculateMatchConfidence(export, job)
            if (confidence > 0f) {
                val reasons = getMatchReasons(export, job)
                matches.add(JobMatch(job, confidence, reasons))
            }
        }

        return matches.sortedByDescending { it.confidence }
    }

    /**
     * Calculates match confidence between a WhatsApp export and a job.
     * Returns a score from 0.0 to 1.0.
     */
    private fun calculateMatchConfidence(
        export: WhatsAppExportParser.WhatsAppChatExport,
        job: JobFile
    ): Float {
        var confidence = 0f

        // Phone number match (highest priority - 0.6 weight)
        if (job.phone.isNotBlank() && export.extractedPhoneNumbers.isNotEmpty()) {
            val jobPhoneNormalized = PhoneNumberUtils.normalizePhoneNumber(job.phone)
            val hasPhoneMatch = export.extractedPhoneNumbers.any { exportPhone ->
                PhoneNumberUtils.arePhoneNumbersEqual(exportPhone, jobPhoneNormalized)
            }
            if (hasPhoneMatch) {
                confidence += 0.6f
            }
        }

        // Sender name match (medium priority - 0.3 weight)
        if (job.clientName.isNotBlank()) {
            val senders = WhatsAppExportParser.getSenders(export)
            val nameMatch = senders.any { sender ->
                sender.contains(job.clientName, ignoreCase = true) ||
                job.clientName.contains(sender, ignoreCase = true)
            }
            if (nameMatch) {
                confidence += 0.3f
            }
        }

        // Content keyword match (lower priority - 0.1 weight)
        if (job.serviceType.isNotBlank() || job.title.isNotBlank()) {
            val textContent = WhatsAppExportParser.extractTextContent(export).lowercase()
            val keywords = listOf(job.serviceType, job.title)
                .filter { it.isNotBlank() }
                .map { it.lowercase() }

            val keywordMatches = keywords.count { keyword ->
                textContent.contains(keyword)
            }

            if (keywordMatches > 0) {
                confidence += 0.1f * minOf(keywordMatches, 1) // Cap at 0.1
            }
        }

        return minOf(confidence, 1.0f) // Cap at 1.0
    }

    /**
     * Gets human-readable reasons for a match.
     */
    private fun getMatchReasons(
        export: WhatsAppExportParser.WhatsAppChatExport,
        job: JobFile
    ): List<String> {
        val reasons = mutableListOf<String>()

        // Phone number match
        if (job.phone.isNotBlank() && export.extractedPhoneNumbers.isNotEmpty()) {
            val jobPhoneNormalized = PhoneNumberUtils.normalizePhoneNumber(job.phone)
            val hasPhoneMatch = export.extractedPhoneNumbers.any { exportPhone ->
                PhoneNumberUtils.arePhoneNumbersEqual(exportPhone, jobPhoneNormalized)
            }
            if (hasPhoneMatch) {
                reasons.add("Número de teléfono coincide")
            }
        }

        // Sender name match
        if (job.clientName.isNotBlank()) {
            val senders = WhatsAppExportParser.getSenders(export)
            val matchingSender = senders.find { sender ->
                sender.contains(job.clientName, ignoreCase = true) ||
                job.clientName.contains(sender, ignoreCase = true)
            }
            if (matchingSender != null) {
                reasons.add("Nombre del cliente coincide: $matchingSender")
            }
        }

        // Content keyword match
        if (job.serviceType.isNotBlank() || job.title.isNotBlank()) {
            val textContent = WhatsAppExportParser.extractTextContent(export).lowercase()
            val keywords = listOf(job.serviceType, job.title)
                .filter { it.isNotBlank() }
                .map { it.lowercase() }

            val matchingKeywords = keywords.filter { keyword ->
                textContent.contains(keyword)
            }

            if (matchingKeywords.isNotEmpty()) {
                reasons.add("Contenido coincide: ${matchingKeywords.joinToString(", ")}")
            }
        }

        return reasons
    }

    /**
     * Suggests a job for a WhatsApp export.
     * Returns the best match if confidence is above threshold (0.5).
     */
    fun suggestJob(
        export: WhatsAppExportParser.WhatsAppChatExport,
        availableJobs: List<JobFile>,
        confidenceThreshold: Float = 0.5f
    ): JobMatch? {
        val matches = findMatchingJobs(export, availableJobs)
        return matches.firstOrNull { it.confidence >= confidenceThreshold }
    }

    /**
     * Extracts suggested metadata from WhatsApp export for creating a new job.
     */
    fun extractSuggestedMetadata(export: WhatsAppExportParser.WhatsAppChatExport): SuggestedMetadata {
        val mainContact = WhatsAppExportParser.getMainContact(export)
        val phoneNumbers = export.extractedPhoneNumbers.toList()
        val isGroup = WhatsAppExportParser.isGroupChat(export)

        return SuggestedMetadata(
            clientName = mainContact ?: "Cliente",
            phone = phoneNumbers.firstOrNull() ?: "",
            chatType = if (isGroup) "Grupo" else "Individual",
            messageCount = export.messages.size,
            mediaCount = export.mediaCount,
            dateRange = export.dateRange
        )
    }

    data class SuggestedMetadata(
        val clientName: String,
        val phone: String,
        val chatType: String,
        val messageCount: Int,
        val mediaCount: Int,
        val dateRange: Pair<String, String>?
    )
}
