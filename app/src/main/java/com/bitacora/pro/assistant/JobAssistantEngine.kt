package com.bitacora.pro.assistant

import com.bitacora.pro.data.models.AgendaStatus
import com.bitacora.pro.data.models.EvidenceCategory
import com.bitacora.pro.data.models.JobFile

/**
 * Local rule-based job assistant engine.
 * Analyzes jobs without external AI APIs - all logic is local and deterministic.
 * Provides insights on job status, risks, pending tasks, and suggested actions.
 */
object JobAssistantEngine {

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
     * Notes are cautious and based only on detected data, avoiding speculation.
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
            val categoryList = categories.keys.map { it.name }.joinToString(", ")
            notes.add("Evidencia clasificada en: $categoryList")
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
