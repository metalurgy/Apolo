package com.bitacora.pro.assistant

import com.bitacora.pro.data.models.AgendaItem
import com.bitacora.pro.data.models.AgendaStatus
import java.util.Calendar

/**
 * AgendaSuggestionEngine is a local, rule-based suggestion engine that analyzes text
 * and suggests agenda items for common field-work situations.
 *
 * No external APIs, no AI models, no backend calls.
 * Supports Spanish and English keywords.
 */
object AgendaSuggestionEngine {

    /**
     * Analyzes text and suggests agenda items.
     *
     * @param jobId The ID of the job these suggestions are for.
     * @param evidenceId Optional ID of the evidence item that generated these suggestions.
     * @param text The text to analyze.
     * @param now Current timestamp for relative date calculations.
     * @return List of suggested agenda items (max 5).
     */
    fun suggestFromText(
        jobId: String,
        evidenceId: String?,
        text: String,
        now: Long = System.currentTimeMillis()
    ): List<AgendaItem> {
        if (text.isBlank()) return emptyList()

        val suggestions = mutableListOf<AgendaItem>()
        val lowerText = text.lowercase()

        // Detect payment-related tasks
        if (hasPaymentKeywords(lowerText)) {
            val dueText = extractDueText(lowerText, paymentKeywords)
            suggestions.add(
                AgendaItem(
                    jobId = jobId,
                    title = "Follow up payment",
                    description = "Payment follow-up: ${text.take(100)}",
                    dueText = dueText,
                    dueAt = null,
                    status = AgendaStatus.PENDING,
                    sourceEvidenceId = evidenceId
                )
            )
        }

        // Detect delivery/installation tasks
        if (hasDeliveryKeywords(lowerText)) {
            val dueText = extractDueText(lowerText, deliveryKeywords)
            suggestions.add(
                AgendaItem(
                    jobId = jobId,
                    title = "Delivery reminder",
                    description = "Delivery/Installation: ${text.take(100)}",
                    dueText = dueText,
                    dueAt = null,
                    status = AgendaStatus.PENDING,
                    sourceEvidenceId = evidenceId
                )
            )
        }

        // Detect visit/inspection tasks
        if (hasVisitKeywords(lowerText)) {
            val dueText = extractDueText(lowerText, visitKeywords)
            suggestions.add(
                AgendaItem(
                    jobId = jobId,
                    title = "Schedule visit",
                    description = "Visit/Inspection: ${text.take(100)}",
                    dueText = dueText,
                    dueAt = null,
                    status = AgendaStatus.PENDING,
                    sourceEvidenceId = evidenceId
                )
            )
        }

        // Detect quote/budget tasks
        if (hasQuoteKeywords(lowerText)) {
            val dueText = extractDueText(lowerText, quoteKeywords)
            suggestions.add(
                AgendaItem(
                    jobId = jobId,
                    title = "Prepare quote",
                    description = "Quote/Budget: ${text.take(100)}",
                    dueText = dueText,
                    dueAt = null,
                    status = AgendaStatus.PENDING,
                    sourceEvidenceId = evidenceId
                )
            )
        }

        // Detect report/documentation tasks
        if (hasReportKeywords(lowerText)) {
            val dueText = extractDueText(lowerText, reportKeywords)
            suggestions.add(
                AgendaItem(
                    jobId = jobId,
                    title = "Send report",
                    description = "Report/Documentation: ${text.take(100)}",
                    dueText = dueText,
                    dueAt = null,
                    status = AgendaStatus.PENDING,
                    sourceEvidenceId = evidenceId
                )
            )
        }

        // Return max 5 suggestions
        return suggestions.take(5)
    }

    // Keyword lists for different task types
    private val paymentKeywords = listOf(
        "pago", "pagar", "pagos", "payment", "pay", "anticipo", "liquidación",
        "factura", "invoice", "cobro", "cobrar", "dinero", "money"
    )

    private val deliveryKeywords = listOf(
        "entrega", "entregar", "delivery", "deliver", "instalación", "instalar",
        "install", "installation", "envío", "enviar", "ship", "shipping"
    )

    private val visitKeywords = listOf(
        "visita", "visitar", "visit", "revisión", "revisar", "review", "inspección",
        "inspeccionar", "inspect", "cita", "appointment", "reunión", "meeting"
    )

    private val quoteKeywords = listOf(
        "cotización", "cotizar", "quote", "presupuesto", "budget", "estimado",
        "estimate", "proposal", "propuesta"
    )

    private val reportKeywords = listOf(
        "reporte", "report", "informe", "documentación", "documentation", "garantía",
        "warranty", "certificado", "certificate"
    )

    private val dateKeywords = listOf(
        "hoy", "today", "mañana", "tomorrow", "pasado mañana", "day after tomorrow",
        "lunes", "monday", "martes", "tuesday", "miércoles", "wednesday",
        "jueves", "thursday", "viernes", "friday", "sábado", "saturday",
        "domingo", "sunday", "esta semana", "this week", "próxima semana", "next week",
        "el lunes", "el martes", "el miércoles", "el jueves", "el viernes",
        "el sábado", "el domingo", "en la mañana", "in the morning", "por la tarde",
        "in the afternoon", "por la noche", "in the evening"
    )

    private fun hasPaymentKeywords(text: String): Boolean {
        return paymentKeywords.any { text.contains(it) }
    }

    private fun hasDeliveryKeywords(text: String): Boolean {
        return deliveryKeywords.any { text.contains(it) }
    }

    private fun hasVisitKeywords(text: String): Boolean {
        return visitKeywords.any { text.contains(it) }
    }

    private fun hasQuoteKeywords(text: String): Boolean {
        return quoteKeywords.any { text.contains(it) }
    }

    private fun hasReportKeywords(text: String): Boolean {
        return reportKeywords.any { text.contains(it) }
    }

    /**
     * Extracts human-readable due date text from the input text.
     * Uses a window-based approach: finds context keywords first, then looks for date keywords
     * within +/- 60 characters of the context keyword.
     * Checks longer phrases before shorter ones to avoid partial matches.
     */
    private fun extractDueText(text: String, contextKeywords: List<String>): String {
        // Sort date keywords by length (longest first) to match longer phrases first
        val sortedDateKeywords = dateKeywords.sortedByDescending { it.length }

        // Find the position of the first context keyword
        var contextPos = -1
        for (keyword in contextKeywords) {
            val pos = text.indexOf(keyword)
            if (pos >= 0 && (contextPos < 0 || pos < contextPos)) {
                contextPos = pos
            }
        }

        // If no context keyword found, search entire text
        if (contextPos < 0) {
            for (dateKeyword in sortedDateKeywords) {
                if (text.contains(dateKeyword)) {
                    return dateKeyword
                }
            }
            return ""
        }

        // Search for date keywords within +/- 60 characters of context keyword
        val windowStart = maxOf(0, contextPos - 60)
        val windowEnd = minOf(text.length, contextPos + 60)
        val window = text.substring(windowStart, windowEnd)

        for (dateKeyword in sortedDateKeywords) {
            if (window.contains(dateKeyword)) {
                return dateKeyword
            }
        }

        return ""
    }
}
