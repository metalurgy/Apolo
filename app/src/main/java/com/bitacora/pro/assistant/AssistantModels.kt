package com.bitacora.pro.assistant

/**
 * Structured models for assistant actions and recommendations.
 * All models are local-only with no external API dependencies.
 */

/**
 * Enum representing different types of assistant actions.
 * Each action type has a specific purpose and UI representation.
 */
enum class AssistantActionType {
    // Task management actions
    ADD_TASK,
    REVIEW_PENDING_TASKS,
    MARK_OVERDUE_TASKS,
    
    // Evidence actions
    ADD_EVIDENCE,
    CATEGORIZE_EVIDENCE,
    REVIEW_MISSING_EVIDENCE,
    
    // Report actions
    GENERATE_REPORT,
    ADD_REPORT_NOTES,
    
    // Client information actions
    UPDATE_CLIENT_INFO,
    
    // General actions
    ARCHIVE_JOB,
    COMPLETE_JOB
}

/**
 * Represents a structured assistant action with metadata.
 * Actions are local-only and deterministic based on job analysis.
 *
 * @param type The type of action
 * @param title Human-readable title for the action
 * @param description Detailed description of what the action does
 * @param priority Priority level (1=highest, 5=lowest)
 * @param icon Emoji icon for UI representation
 * @param actionText The text to display on the action button
 * @param isUrgent Whether this action requires immediate attention
 */
data class AssistantAction(
    val type: AssistantActionType,
    val title: String,
    val description: String,
    val priority: Int = 3,
    val icon: String = "📋",
    val actionText: String = "Realizar",
    val isUrgent: Boolean = false
)

/**
 * Represents a keyword detection result from evidence analysis.
 * Used to suggest categories and actions based on content.
 *
 * @param keyword The detected keyword
 * @param category The suggested evidence category
 * @param confidence Confidence level (0.0-1.0)
 * @param context The context where the keyword was found
 */
data class KeywordDetection(
    val keyword: String,
    val category: String,
    val confidence: Double,
    val context: String
)

/**
 * Represents a category suggestion for evidence.
 * Based on keyword detection and content analysis.
 *
 * @param evidenceId The ID of the evidence item
 * @param suggestedCategory The suggested category
 * @param confidence Confidence level (0.0-1.0)
 * @param reason Human-readable reason for the suggestion
 * @param keywords Keywords that triggered this suggestion
 */
data class CategorySuggestion(
    val evidenceId: String,
    val suggestedCategory: String,
    val confidence: Double,
    val reason: String,
    val keywords: List<String>
)

/**
 * Represents a professional report note suggestion.
 * Generated based on job analysis and evidence.
 *
 * @param note The suggested note text
 * @param type The type of note (SUMMARY, RISK, RECOMMENDATION, COMPLETION)
 * @param priority Priority level (1=highest, 5=lowest)
 * @param basedOn What data this note is based on
 */
data class ReportNoteSuggestion(
    val note: String,
    val type: NoteType,
    val priority: Int = 3,
    val basedOn: String = ""
)

enum class NoteType {
    SUMMARY,
    RISK,
    RECOMMENDATION,
    COMPLETION,
    EVIDENCE_SUMMARY
}

/**
 * Represents the complete structured analysis result.
 * Extends AssistantResult with additional structured data.
 *
 * @param summary Text summary of job status
 * @param pendingTasks List of pending tasks
 * @param risks List of detected risks
 * @param suggestedNotes List of suggested report notes
 * @param nextActions List of next action recommendations
 * @param structuredActions List of structured actions with metadata
 * @param categorySuggestions List of category suggestions for evidence
 * @param reportNoteSuggestions List of professional report note suggestions
 * @param keywordDetections List of detected keywords from evidence
 */
data class StructuredAssistantResult(
    val summary: String,
    val pendingTasks: List<String>,
    val risks: List<String>,
    val suggestedNotes: List<String>,
    val nextActions: List<String>,
    val structuredActions: List<AssistantAction> = emptyList(),
    val categorySuggestions: List<CategorySuggestion> = emptyList(),
    val reportNoteSuggestions: List<ReportNoteSuggestion> = emptyList(),
    val keywordDetections: List<KeywordDetection> = emptyList()
)
