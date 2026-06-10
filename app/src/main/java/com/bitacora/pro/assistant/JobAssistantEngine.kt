package com.bitacora.pro.assistant

import com.bitacora.pro.data.models.AgendaStatus
import com.bitacora.pro.data.models.EvidenceCategory
import com.bitacora.pro.data.models.JobFile
import com.bitacora.pro.data.models.getSpanishLabel

/**
 * Local rule-based job assistant engine.
 * Analyzes jobs without external AI APIs - all logic is local and deterministic.
 * Provides insights on job status, risks, pending tasks, and suggested actions.
 * 
 * Features:
 * - Keyword detection for payment, material, scheduling, completion, problems
 * - Evidence category suggestions based on content analysis
 * - Professional report note generation in Spanish
 * - Structured action recommendations
 * - Privacy-first: no external APIs, no internet, no API keys
 */
object JobAssistantEngine {

    /**
     * Keywords for detecting payment-related evidence
     */
    private val PAYMENT_KEYWORDS = listOf(
        "pago", "pagado", "pagar", "precio", "costo", "cobro", "cobrado",
        "factura", "recibo", "comprobante", "transferencia", "depósito",
        "efectivo", "tarjeta", "dinero", "cantidad", "monto", "total",
        "invoice", "payment", "paid", "charge", "cost", "receipt"
    )

    /**
     * Keywords for detecting material/supply-related evidence
     */
    private val MATERIAL_KEYWORDS = listOf(
        "material", "materiales", "suministro", "producto", "productos",
        "herramienta", "herramientas", "equipo", "equipos", "pieza", "piezas",
        "compra", "comprado", "adquirido", "adquisición", "stock", "inventario",
        "supplies", "materials", "tools", "equipment", "parts", "purchase"
    )

    /**
     * Keywords for detecting scheduling/timeline-related evidence
     */
    private val SCHEDULING_KEYWORDS = listOf(
        "fecha", "fechas", "horario", "hora", "tiempo", "plazo", "vencimiento",
        "programado", "agendado", "cita", "reunión", "próximo", "siguiente",
        "mañana", "hoy", "ayer", "semana", "mes", "día", "lunes", "martes",
        "miércoles", "jueves", "viernes", "sábado", "domingo",
        "schedule", "date", "time", "appointment", "meeting", "deadline"
    )

    /**
     * Keywords for detecting completion/progress-related evidence
     */
    private val COMPLETION_KEYWORDS = listOf(
        "completado", "completar", "terminado", "terminar", "finalizado",
        "finalizar", "listo", "hecho", "realizado", "realizado", "acabado",
        "progreso", "avance", "porcentaje", "100%", "done", "completed",
        "finished", "ready", "progress", "complete"
    )

    /**
     * Keywords for detecting problems/issues
     */
    private val PROBLEM_KEYWORDS = listOf(
        "problema", "problemas", "error", "errores", "fallo", "fallos",
        "defecto", "defectos", "daño", "dañado", "roto", "rotura",
        "issue", "issue", "problem", "error", "failure", "broken",
        "damaged", "defect", "complaint", "queja", "reclamo"
    )

    /**
     * Analyzes a job and returns comprehensive assistant results.
     * All analysis is performed locally using rule-based logic.
     */
    fun analyzeJob(job: JobFile): AssistantResult {
        val summary = generateJobSummary(job)
        val pendingTasks = detectPendingTasks(job)
        val risks = detectRisksAndMissing(job)
        val suggestedNotes = generateSuggestedReportNotes(job, risks)
        val nextActions = generateNextActions(job, pendingTasks, risks)

        return AssistantResult(
            summary = summary,
            pendingTasks = pendingTasks,
            risks = risks,
            suggestedNotes = suggestedNotes,
            nextActions = nextActions
        )
    }

    /**
     * Detects keywords in evidence and returns categorized results.
     * Used for category suggestions and content analysis.
     */
    fun detectKeywordsInEvidence(job: JobFile): List<KeywordDetection> {
        val detections = mutableListOf<KeywordDetection>()

        job.evidence.forEach { evidence ->
            val textToAnalyze = (evidence.textContent + " " + evidence.fileName).lowercase()

            // Check for payment keywords
            PAYMENT_KEYWORDS.forEach { keyword ->
                if (textToAnalyze.contains(keyword)) {
                    detections.add(
                        KeywordDetection(
                            keyword = keyword,
                            category = EvidenceCategory.PAYMENT.name,
                            confidence = calculateConfidence(keyword, textToAnalyze),
                            context = evidence.id
                        )
                    )
                }
            }

            // Check for material keywords
            MATERIAL_KEYWORDS.forEach { keyword ->
                if (textToAnalyze.contains(keyword)) {
                    detections.add(
                        KeywordDetection(
                            keyword = keyword,
                            category = EvidenceCategory.MATERIAL.name,
                            confidence = calculateConfidence(keyword, textToAnalyze),
                            context = evidence.id
                        )
                    )
                }
            }

            // Check for scheduling keywords
            SCHEDULING_KEYWORDS.forEach { keyword ->
                if (textToAnalyze.contains(keyword)) {
                    detections.add(
                        KeywordDetection(
                            keyword = keyword,
                            category = "SCHEDULING",
                            confidence = calculateConfidence(keyword, textToAnalyze),
                            context = evidence.id
                        )
                    )
                }
            }

            // Check for completion keywords
            COMPLETION_KEYWORDS.forEach { keyword ->
                if (textToAnalyze.contains(keyword)) {
                    detections.add(
                        KeywordDetection(
                            keyword = keyword,
                            category = "COMPLETION",
                            confidence = calculateConfidence(keyword, textToAnalyze),
                            context = evidence.id
                        )
                    )
                }
            }

            // Check for problem keywords
            PROBLEM_KEYWORDS.forEach { keyword ->
                if (textToAnalyze.contains(keyword)) {
                    detections.add(
                        KeywordDetection(
                            keyword = keyword,
                            category = "PROBLEMS",
                            confidence = calculateConfidence(keyword, textToAnalyze),
                            context = evidence.id
                        )
                    )
                }
            }
        }

        return detections
    }

    /**
     * Suggests evidence categories based on keyword detection.
     */
    fun suggestEvidenceCategories(job: JobFile): List<CategorySuggestion> {
        val suggestions = mutableListOf<CategorySuggestion>()
        val keywordDetections = detectKeywordsInEvidence(job)

        // Group detections by evidence ID
        val detectionsByEvidence = keywordDetections.groupBy { it.context }

        job.evidence.forEach { evidence ->
            val detections = detectionsByEvidence[evidence.id] ?: emptyList()

            if (detections.isNotEmpty()) {
                // Find the most confident category
                val categoryScores = mutableMapOf<String, Double>()
                detections.forEach { detection ->
                    val currentScore = categoryScores[detection.category] ?: 0.0
                    categoryScores[detection.category] = currentScore + detection.confidence
                }

                val bestCategory = categoryScores.maxByOrNull { it.value }
                if (bestCategory != null && bestCategory.value > 0.3) {
                    val categoryName = when (bestCategory.key) {
                        EvidenceCategory.PAYMENT.name -> EvidenceCategory.PAYMENT.name
                        EvidenceCategory.MATERIAL.name -> EvidenceCategory.MATERIAL.name
                        else -> evidence.category.name
                    }

                    val keywords = detections
                        .filter { it.category == bestCategory.key }
                        .map { it.keyword }
                        .distinct()

                    suggestions.add(
                        CategorySuggestion(
                            evidenceId = evidence.id,
                            suggestedCategory = categoryName,
                            confidence = bestCategory.value / detections.size,
                            reason = "Detectados ${keywords.size} indicadores de ${bestCategory.key.lowercase()}",
                            keywords = keywords
                        )
                    )
                }
            }
        }

        return suggestions
    }

    /**
     * Calculates confidence score for a keyword match.
     * Higher confidence for exact matches and longer keywords.
     */
    private fun calculateConfidence(keyword: String, text: String): Double {
        val occurrences = text.split(keyword).size - 1
        val baseConfidence = minOf(0.9, 0.3 + (keyword.length * 0.05))
        val occurrenceBonus = minOf(0.3, occurrences * 0.1)
        return baseConfidence + occurrenceBonus
    }

    /**
     * Generates a summary of the job status.
     */
    private fun generateJobSummary(job: JobFile): String {
        val evidenceCount = job.evidence.size
        val activeAgendaCount = job.agendaItems.filter { it.status != AgendaStatus.ARCHIVED }.size
        val completedAgendaCount = job.agendaItems.filter { it.status == AgendaStatus.DONE }.size
        val pendingAgendaCount = job.agendaItems.filter { it.status == AgendaStatus.PENDING }.size

        val summaryParts = mutableListOf<String>()
        summaryParts.add("Trabajo: ${job.title}")
        summaryParts.add("Cliente: ${job.clientName}")
        summaryParts.add("Evidencia: $evidenceCount elementos")
        if (activeAgendaCount > 0) {
            summaryParts.add("Agenda: $activeAgendaCount activos ($pendingAgendaCount pendientes, $completedAgendaCount completados)")
        } else {
            summaryParts.add("Agenda: Sin elementos activos")
        }

        return summaryParts.joinToString("\n")
    }

    /**
     * Detects pending tasks that need attention.
     */
    private fun detectPendingTasks(job: JobFile): List<String> {
        val pendingTasks = mutableListOf<String>()

        // Get pending agenda items (exclude archived)
        val pendingAgendaItems = job.agendaItems.filter { it.status == AgendaStatus.PENDING }
        if (pendingAgendaItems.isNotEmpty()) {
            pendingTasks.add("${pendingAgendaItems.size} tareas pendientes en la agenda")
            pendingAgendaItems.forEach { item ->
                val dueInfo = if (item.dueText.isNotEmpty()) " (vencimiento: ${item.dueText})" else ""
                pendingTasks.add("  • ${item.title}$dueInfo")
            }
        }

        // Check for overdue items
        val now = System.currentTimeMillis()
        val overdueItems = pendingAgendaItems.filter { it.dueAt != null && it.dueAt!! < now }
        if (overdueItems.isNotEmpty()) {
            pendingTasks.add("⚠️ ${overdueItems.size} tareas vencidas")
        }

        return pendingTasks
    }

    /**
     * Detects risks and missing information.
     */
    private fun detectRisksAndMissing(job: JobFile): List<String> {
        val risks = mutableListOf<String>()

        // Check for missing evidence
        if (job.evidence.isEmpty()) {
            risks.add("❌ Sin evidencia registrada")
        } else {
            // Check for missing evidence categories
            val hasPaymentEvidence = job.evidence.any { it.category == EvidenceCategory.PAYMENT }
            if (!hasPaymentEvidence) {
                risks.add("⚠️ Sin evidencia de pago")
            }

            val hasBeforeEvidence = job.evidence.any { it.category == EvidenceCategory.BEFORE }
            val hasDuringEvidence = job.evidence.any { it.category == EvidenceCategory.DURING }
            val hasAfterEvidence = job.evidence.any { it.category == EvidenceCategory.AFTER }

            if (!hasBeforeEvidence && !hasDuringEvidence && !hasAfterEvidence) {
                risks.add("⚠️ Sin evidencia de proceso (Antes/Durante/Después)")
            }
        }

        // Check for missing agenda items with due dates
        val agendaWithoutDates = job.agendaItems.filter { 
            it.status == AgendaStatus.PENDING && it.dueAt == null 
        }
        if (agendaWithoutDates.isNotEmpty()) {
            risks.add("⚠️ ${agendaWithoutDates.size} tareas sin fecha de vencimiento")
        }

        // Check for missing client information
        if (job.clientName.isBlank()) {
            risks.add("⚠️ Nombre del cliente no especificado")
        }
        if (job.phone.isBlank()) {
            risks.add("⚠️ Teléfono del cliente no especificado")
        }

        // Check for missing service type
        if (job.serviceType.isBlank()) {
            risks.add("⚠️ Tipo de servicio no especificado")
        }

        // Check for missing notes
        if (job.notes.isBlank()) {
            risks.add("ℹ️ Sin notas adicionales")
        }

        return risks
    }

    /**
     * Generates suggested report notes based on job analysis.
     * Notes are professional, cautious, and based only on detected data.
     * Generated in Spanish for professional reports.
     */
    private fun generateSuggestedReportNotes(job: JobFile, risks: List<String>): List<String> {
        val notes = mutableListOf<String>()

        // Summary note - factual only
        val evidenceCount = job.evidence.size
        val completedTasks = job.agendaItems.filter { it.status == AgendaStatus.DONE }.size
        val pendingCount = job.agendaItems.filter { it.status == AgendaStatus.PENDING }.size
        
        if (evidenceCount > 0 || completedTasks > 0) {
            notes.add("Se han registrado $evidenceCount elementos de evidencia y $completedTasks tareas completadas.")
        }

        // Evidence summary - only if evidence exists
        if (job.evidence.isNotEmpty()) {
            val categories = job.evidence.groupBy { it.category }
            val categoryDescriptions = mutableListOf<String>()
            
            categories.forEach { (category, items) ->
                categoryDescriptions.add("${category.getSpanishLabel()} (${items.size})")
            }
            
            notes.add("Evidencia clasificada en: ${categoryDescriptions.joinToString(", ")}")
        }

        // Risk mitigation notes - only suggest if risks detected
        if (risks.any { it.contains("Sin evidencia") }) {
            notes.add("Se sugiere recopilar evidencia adicional para completar el registro.")
        }

        if (risks.any { it.contains("Sin evidencia de pago") }) {
            notes.add("Se sugiere documentar el comprobante de pago si está disponible.")
        }

        if (risks.any { it.contains("tareas sin fecha") }) {
            notes.add("Se sugiere establecer fechas de vencimiento para las tareas pendientes.")
        }

        // Completion status - factual
        if (pendingCount == 0 && completedTasks > 0) {
            notes.add("Todas las tareas de agenda han sido completadas.")
        }

        // Professional closing note
        if (notes.isNotEmpty()) {
            notes.add("Reporte generado automáticamente por Bitacora Pro v0.7.3.")
        }

        return notes
    }

    /**
     * Generates suggested next actions based on job analysis.
     * Actions are cautious and only suggest what can be verified from the data.
     */
    private fun generateNextActions(
        job: JobFile,
        pendingTasks: List<String>,
        risks: List<String>
    ): List<String> {
        val actions = mutableListOf<String>()

        // Priority 1: Handle overdue tasks
        val now = System.currentTimeMillis()
        val overdueItems = job.agendaItems.filter {
            it.status == AgendaStatus.PENDING && it.dueAt != null && it.dueAt!! < now
        }
        if (overdueItems.isNotEmpty()) {
            actions.add("🔴 URGENTE: Revisar ${overdueItems.size} tareas vencidas")
        }

        // Priority 2: Complete pending tasks
        val pendingItems = job.agendaItems.filter { it.status == AgendaStatus.PENDING }
        if (pendingItems.isNotEmpty()) {
            actions.add("📋 Revisar ${pendingItems.size} tareas pendientes")
        }

        // Priority 3: Address missing information
        if (risks.any { it.contains("Sin evidencia") }) {
            actions.add("📸 Considerar recopilar evidencia adicional")
        }

        if (risks.any { it.contains("Sin evidencia de pago") }) {
            actions.add("💰 Considerar documentar comprobante de pago")
        }

        if (risks.any { it.contains("no especificado") }) {
            actions.add("✏️ Considerar completar información del cliente")
        }

        // Priority 4: Generate report
        if (job.evidence.isNotEmpty() || job.agendaItems.isNotEmpty()) {
            actions.add("📄 Generar reporte PDF del trabajo")
        }

        return actions
    }
}

/**
 * Data class representing the complete analysis result from the assistant.
 */
data class AssistantResult(
    val summary: String,
    val pendingTasks: List<String>,
    val risks: List<String>,
    val suggestedNotes: List<String>,
    val nextActions: List<String>
)
