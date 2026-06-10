package com.bitacora.pro.assistant

import com.bitacora.pro.data.models.JobFile
import com.bitacora.pro.data.models.JobStatus
import java.util.Calendar

/**
 * DashboardAssistantEngine provides local-only insights for the smart dashboard.
 * 
 * Features (v0.7.2):
 * - NO external AI or internet calls
 * - Local-only analysis using rule-based heuristics
 * - Provides summary statistics and insights
 * - Identifies priority jobs and overdue items
 * - Calculates productivity metrics
 * 
 * This engine is designed to work entirely offline and provide
 * actionable insights based on local job data.
 */
object DashboardAssistantEngine {

    /**
     * Data class for dashboard summary statistics.
     */
    data class DashboardSummary(
        val totalJobs: Int = 0,
        val activeJobs: Int = 0,
        val completedJobs: Int = 0,
        val archivedJobs: Int = 0,
        val totalEvidence: Int = 0,
        val averageEvidencePerJob: Float = 0f,
        val jobsWithoutEvidence: Int = 0,
        val recentlyUpdatedCount: Int = 0,
        val priorityJobs: List<JobFile> = emptyList(),
        val insights: List<String> = emptyList()
    )

    /**
     * Generates a comprehensive dashboard summary from all jobs.
     * All analysis is local and rule-based.
     */
    fun generateDashboardSummary(jobs: List<JobFile>): DashboardSummary {
        val activeJobs = jobs.filter { it.status == JobStatus.ACTIVE }
        val completedJobs = jobs.filter { it.status == JobStatus.COMPLETED }
        val archivedJobs = jobs.filter { it.status == JobStatus.ARCHIVED }
        
        val totalEvidence = jobs.sumOf { it.evidence.size }
        val averageEvidence = if (jobs.isNotEmpty()) totalEvidence.toFloat() / jobs.size else 0f
        val jobsWithoutEvidence = jobs.count { it.evidence.isEmpty() }
        
        val now = System.currentTimeMillis()
        val oneDayAgo = now - (24 * 60 * 60 * 1000)
        val recentlyUpdated = jobs.count { it.lastUsedAt > oneDayAgo }
        
        val priorityJobs = identifyPriorityJobs(activeJobs)
        val insights = generateInsights(jobs, activeJobs, completedJobs, jobsWithoutEvidence)
        
        return DashboardSummary(
            totalJobs = jobs.size,
            activeJobs = activeJobs.size,
            completedJobs = completedJobs.size,
            archivedJobs = archivedJobs.size,
            totalEvidence = totalEvidence,
            averageEvidencePerJob = averageEvidence,
            jobsWithoutEvidence = jobsWithoutEvidence,
            recentlyUpdatedCount = recentlyUpdated,
            priorityJobs = priorityJobs,
            insights = insights
        )
    }

    /**
     * Identifies priority jobs based on local heuristics.
     * Priority is determined by:
     * - Jobs with no evidence (need documentation)
     * - Jobs with pending agenda items
     * - Jobs not updated in 7+ days
     */
    private fun identifyPriorityJobs(activeJobs: List<JobFile>): List<JobFile> {
        val now = System.currentTimeMillis()
        val sevenDaysAgo = now - (7 * 24 * 60 * 60 * 1000)
        
        return activeJobs
            .sortedWith(compareBy(
                // First: jobs with no evidence
                { it.evidence.isNotEmpty() },
                // Second: jobs with pending agenda items
                { it.agendaItems.isEmpty() },
                // Third: jobs not updated recently
                { it.lastUsedAt }
            ))
            .take(5) // Return top 5 priority jobs
    }

    /**
     * Generates actionable insights based on job data.
     * All insights are generated locally without external services.
     */
    private fun generateInsights(
        allJobs: List<JobFile>,
        activeJobs: List<JobFile>,
        completedJobs: List<JobFile>,
        jobsWithoutEvidence: Int
    ): List<String> {
        val insights = mutableListOf<String>()
        
        // Insight 1: Jobs without evidence
        if (jobsWithoutEvidence > 0) {
            insights.add("📸 $jobsWithoutEvidence trabajo(s) sin evidencia - Agrega fotos o documentos")
        }
        
        // Insight 2: Completion rate
        if (allJobs.isNotEmpty()) {
            val completionRate = (completedJobs.size * 100) / allJobs.size
            if (completionRate > 50) {
                insights.add("✅ Excelente tasa de finalización: $completionRate%")
            } else if (completionRate > 25) {
                insights.add("📈 Tasa de finalización: $completionRate% - Sigue adelante")
            }
        }
        
        // Insight 3: Active jobs status
        if (activeJobs.isEmpty() && allJobs.isNotEmpty()) {
            insights.add("🎉 Todos los trabajos están completados o archivados")
        } else if (activeJobs.size > 10) {
            insights.add("⚠️ Tienes ${activeJobs.size} trabajos activos - Considera archivar los completados")
        }
        
        // Insight 4: Recent activity
        val now = System.currentTimeMillis()
        val oneDayAgo = now - (24 * 60 * 60 * 1000)
        val recentlyActive = allJobs.count { it.lastUsedAt > oneDayAgo }
        if (recentlyActive == 0 && allJobs.isNotEmpty()) {
            insights.add("📅 Sin actividad reciente - Abre un trabajo para continuar")
        }
        
        // Insight 5: Evidence distribution
        val jobsWithMultipleEvidence = allJobs.count { it.evidence.size > 5 }
        if (jobsWithMultipleEvidence > 0) {
            insights.add("📦 $jobsWithMultipleEvidence trabajo(s) con evidencia abundante")
        }
        
        return insights.take(3) // Return top 3 insights
    }

    /**
     * Calculates productivity metrics for a time period.
     * All calculations are local and based on job timestamps.
     */
    fun calculateProductivityMetrics(jobs: List<JobFile>): ProductivityMetrics {
        val now = System.currentTimeMillis()
        val oneDayAgo = now - (24 * 60 * 60 * 1000)
        val sevenDaysAgo = now - (7 * 24 * 60 * 60 * 1000)
        val thirtyDaysAgo = now - (30 * 24 * 60 * 60 * 1000)
        
        val jobsCreatedToday = jobs.count { it.createdAt > oneDayAgo }
        val jobsCreatedThisWeek = jobs.count { it.createdAt > sevenDaysAgo }
        val jobsCreatedThisMonth = jobs.count { it.createdAt > thirtyDaysAgo }
        
        val jobsCompletedToday = jobs.count { 
            it.status == JobStatus.COMPLETED && it.updatedAt > oneDayAgo 
        }
        val jobsCompletedThisWeek = jobs.count { 
            it.status == JobStatus.COMPLETED && it.updatedAt > sevenDaysAgo 
        }
        
        val averageEvidencePerJob = if (jobs.isNotEmpty()) {
            jobs.sumOf { it.evidence.size }.toFloat() / jobs.size
        } else {
            0f
        }
        
        return ProductivityMetrics(
            jobsCreatedToday = jobsCreatedToday,
            jobsCreatedThisWeek = jobsCreatedThisWeek,
            jobsCreatedThisMonth = jobsCreatedThisMonth,
            jobsCompletedToday = jobsCompletedToday,
            jobsCompletedThisWeek = jobsCompletedThisWeek,
            averageEvidencePerJob = averageEvidencePerJob
        )
    }

    /**
     * Data class for productivity metrics.
     */
    data class ProductivityMetrics(
        val jobsCreatedToday: Int = 0,
        val jobsCreatedThisWeek: Int = 0,
        val jobsCreatedThisMonth: Int = 0,
        val jobsCompletedToday: Int = 0,
        val jobsCompletedThisWeek: Int = 0,
        val averageEvidencePerJob: Float = 0f
    )

    /**
     * Suggests next actions based on job status.
     * All suggestions are generated locally.
     */
    fun suggestNextActions(jobs: List<JobFile>): List<String> {
        val suggestions = mutableListOf<String>()
        
        // Find jobs with no evidence
        val jobsNeedingEvidence = jobs.filter { 
            it.status == JobStatus.ACTIVE && it.evidence.isEmpty() 
        }
        if (jobsNeedingEvidence.isNotEmpty()) {
            suggestions.add("Agrega evidencia a ${jobsNeedingEvidence.size} trabajo(s)")
        }
        
        // Find jobs with pending agenda items
        val jobsWithPendingItems = jobs.filter { job ->
            job.agendaItems.any { it.status.name == "PENDING" }
        }
        if (jobsWithPendingItems.isNotEmpty()) {
            suggestions.add("Revisa ${jobsWithPendingItems.size} trabajo(s) con tareas pendientes")
        }
        
        // Find old jobs that haven't been updated
        val now = System.currentTimeMillis()
        val thirtyDaysAgo = now - (30 * 24 * 60 * 60 * 1000)
        val oldJobs = jobs.filter { 
            it.status == JobStatus.ACTIVE && it.lastUsedAt < thirtyDaysAgo 
        }
        if (oldJobs.isNotEmpty()) {
            suggestions.add("Considera archivar ${oldJobs.size} trabajo(s) inactivo(s)")
        }
        
        return suggestions.take(3)
    }
}
