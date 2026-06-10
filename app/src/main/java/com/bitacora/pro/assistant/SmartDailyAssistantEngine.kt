package com.bitacora.pro.assistant

import com.bitacora.pro.data.models.AgendaItem
import com.bitacora.pro.data.models.AgendaStatus
import com.bitacora.pro.data.models.EvidenceCategory
import com.bitacora.pro.data.models.JobFile
import com.bitacora.pro.data.models.JobStatus
import java.util.Calendar

/**
 * Smart Daily Assistant Engine for local AI-ready suggestions.
 * v0.8.0: Analyzes job data locally and generates actionable suggestions
 *
 * Features:
 * - Analyze job data locally (no external calls)
 * - Generate suggestions for next steps
 * - Identify patterns in evidence
 * - Recommend agenda items
 * - All processing local, no API calls
 */
object SmartDailyAssistantEngine {

    /**
     * Data class for assistant suggestion.
     */
    data class AssistantSuggestion(
        val id: String = "",
        val title: String = "",
        val description: String = "",
        val priority: Int = 0, // 0=low, 1=medium, 2=high
        val actionType: String = "", // "create_agenda", "add_evidence", "close_job", etc.
        val targetJobId: String? = null
    )

    /**
     * Generates suggestions for a job.
     */
    fun generateSuggestionsForJob(job: JobFile): List<AssistantSuggestion> {
        val suggestions = mutableListOf<AssistantSuggestion>()

        // Suggestion 1: Missing evidence categories
        val missingCategories = identifyMissingEvidenceCategories(job)
        if (missingCategories.isNotEmpty()) {
            suggestions.add(
                AssistantSuggestion(
                    id = "missing_evidence_${job.id}",
                    title = "Evidencia incompleta",
                    description = "Faltan categorías de evidencia: ${missingCategories.joinToString(", ")}",
                    priority = 1,
                    actionType = "add_evidence",
                    targetJobId = job.id
                )
            )
        }

        // Suggestion 2: Pending agenda items
        val pendingItems = job.agendaItems.filter { it.status == AgendaStatus.PENDING }
        if (pendingItems.isNotEmpty()) {
            suggestions.add(
                AssistantSuggestion(
                    id = "pending_agenda_${job.id}",
                    title = "Tareas pendientes",
                    description = "Hay ${pendingItems.size} tareas pendientes en este trabajo",
                    priority = 2,
                    actionType = "view_agenda",
                    targetJobId = job.id
                )
            )
        }

        // Suggestion 3: Old jobs that could be closed
        if (job.status == JobStatus.ACTIVE && isJobOldEnoughToClose(job)) {
            suggestions.add(
                AssistantSuggestion(
                    id = "close_job_${job.id}",
                    title = "Considerar cerrar trabajo",
                    description = "Este trabajo ha estado activo por más de 30 días",
                    priority = 1,
                    actionType = "close_job",
                    targetJobId = job.id
                )
            )
        }

        // Suggestion 4: Jobs with lots of evidence
        if (job.evidence.size > 20) {
            suggestions.add(
                AssistantSuggestion(
                    id = "organize_evidence_${job.id}",
                    title = "Organizar evidencia",
                    description = "Este trabajo tiene ${job.evidence.size} elementos de evidencia. Considera organizarlos mejor",
                    priority = 0,
                    actionType = "organize_evidence",
                    targetJobId = job.id
                )
            )
        }

        return suggestions.sortedByDescending { it.priority }
    }

    /**
     * Generates daily summary suggestions.
     */
    fun generateDailySummary(jobs: List<JobFile>): DailySummary {
        val activeJobs = jobs.filter { it.status == JobStatus.ACTIVE }
        val completedToday = jobs.filter { 
            it.status == JobStatus.COMPLETED && 
            isToday(it.updatedAt)
        }
        val pendingAgendaItems = jobs.flatMap { job ->
            job.agendaItems.filter { 
                it.status == AgendaStatus.PENDING && 
                isToday(it.dueAt ?: 0)
            }
        }

        val suggestions = mutableListOf<String>()

        // Generate contextual suggestions
        if (activeJobs.isEmpty()) {
            suggestions.add("¡Excelente! No hay trabajos activos en este momento")
        } else if (activeJobs.size == 1) {
            suggestions.add("Enfócate en: ${activeJobs.first().title}")
        } else {
            suggestions.add("Tienes ${activeJobs.size} trabajos activos. Prioriza los más urgentes")
        }

        if (pendingAgendaItems.isNotEmpty()) {
            suggestions.add("${pendingAgendaItems.size} tareas pendientes para hoy")
        }

        if (completedToday.isNotEmpty()) {
            suggestions.add("¡Buen trabajo! Completaste ${completedToday.size} trabajos hoy")
        }

        return DailySummary(
            activeJobs = activeJobs.size,
            completedToday = completedToday.size,
            pendingTasks = pendingAgendaItems.size,
            suggestions = suggestions
        )
    }

    /**
     * Data class for daily summary.
     */
    data class DailySummary(
        val activeJobs: Int = 0,
        val completedToday: Int = 0,
        val pendingTasks: Int = 0,
        val suggestions: List<String> = emptyList()
    )

    /**
     * Identifies missing evidence categories in a job.
     */
    private fun identifyMissingEvidenceCategories(job: JobFile): List<String> {
        val presentCategories = job.evidence.map { it.category }.toSet()
        val recommendedCategories = listOf(
            EvidenceCategory.BEFORE,
            EvidenceCategory.DURING,
            EvidenceCategory.AFTER
        )

        return recommendedCategories
            .filter { it !in presentCategories }
            .map {
                when (it) {
                    EvidenceCategory.BEFORE -> "Foto ANTES"
                    EvidenceCategory.DURING -> "Foto DURANTE"
                    EvidenceCategory.AFTER -> "Foto DESPUÉS"
                    else -> it.name
                }
            }
    }

    /**
     * Checks if a job is old enough to consider closing.
     */
    private fun isJobOldEnoughToClose(job: JobFile): Boolean {
        val thirtyDaysAgo = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000)
        return job.createdAt < thirtyDaysAgo
    }

    /**
     * Checks if a timestamp is today.
     */
    private fun isToday(timestamp: Long): Boolean {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val tomorrow = today + (24 * 60 * 60 * 1000)
        return timestamp in today until tomorrow
    }

    /**
     * Analyzes evidence patterns in a job.
     */
    fun analyzeEvidencePatterns(job: JobFile): Map<String, Int> {
        return job.evidence
            .groupingBy { it.type.name }
            .eachCount()
    }

    /**
     * Recommends next steps for a job.
     */
    fun recommendNextSteps(job: JobFile): List<String> {
        val steps = mutableListOf<String>()

        // Check evidence completeness
        val categories = job.evidence.map { it.category }.toSet()
        if (EvidenceCategory.BEFORE !in categories) {
            steps.add("Agregar foto ANTES del trabajo")
        }
        if (EvidenceCategory.DURING !in categories) {
            steps.add("Agregar foto DURANTE el trabajo")
        }
        if (EvidenceCategory.AFTER !in categories) {
            steps.add("Agregar foto DESPUÉS del trabajo")
        }

        // Check for pending tasks
        val pendingTasks = job.agendaItems.filter { it.status == AgendaStatus.PENDING }
        if (pendingTasks.isNotEmpty()) {
            steps.add("Completar ${pendingTasks.size} tareas pendientes")
        }

        // Check if job can be closed
        if (job.status == JobStatus.ACTIVE && job.evidence.size > 0) {
            steps.add("Considerar cerrar el trabajo si está completo")
        }

        return steps
    }
}
