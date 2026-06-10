package com.bitacora.pro.data.models

import android.net.Uri
import java.io.Serializable
import java.util.UUID

/**
 * Enum representing the status of a job.
 */
enum class JobStatus {
    ACTIVE,
    COMPLETED,
    ARCHIVED
}

/**
 * Enum representing the type of evidence.
 */
enum class EvidenceType {
    TEXT,
    IMAGE,
    AUDIO,
    PDF
}

/**
 * Enum representing the category of evidence.
 */
enum class EvidenceCategory {
    UNCLASSIFIED,
    BEFORE,
    DURING,
    AFTER,
    MATERIAL,
    PAYMENT,
    CLIENT_MESSAGE
}

/**
 * Enum representing the status of an agenda item.
 */
enum class AgendaStatus {
    PENDING,
    DONE,
    CANCELLED,
    ARCHIVED
}

/**
 * Data class representing a single evidence item.
 *
 * @param id Unique identifier for the evidence item.
 * @param type The type of evidence (TEXT, IMAGE, AUDIO, PDF).
 * @param category The category of evidence.
 * @param fileName The name of the file (for non-text evidence).
 * @param textContent The text content (for TEXT type evidence).
 * @param mimeType The MIME type of the evidence.
 * @param createdAt Timestamp when the evidence was created.
 * @param notes Optional notes about the evidence.
 */
data class EvidenceItem(
    val id: String = UUID.randomUUID().toString(),
    val type: EvidenceType = EvidenceType.TEXT,
    val category: EvidenceCategory = EvidenceCategory.UNCLASSIFIED,
    val fileName: String = "",
    val textContent: String = "",
    val mimeType: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val notes: String = ""
) : Serializable

/**
 * Data class representing a shared file descriptor.
 * Used to hold pending shared content before it is copied to a job folder.
 * This allows us to defer file copying until the final job is selected/created.
 *
 * @param uri The content URI of the shared file.
 * @param mimeType The MIME type of the file.
 */
data class SharedFileDescriptor(
    val uri: Uri,
    val mimeType: String
)

/**
 * Data class representing an agenda item for a job.
 * Agenda items are tasks, reminders, or follow-ups related to a job.
 *
 * @param id Unique identifier for the agenda item.
 * @param jobId The ID of the job this agenda item belongs to.
 * @param title Short title of the agenda item.
 * @param description Detailed description of the agenda item.
 * @param dueAt Optional timestamp for when the item is due (null if not set).
 * @param dueText Human-readable due date text (e.g., "mañana", "viernes").
 * @param status The current status of the agenda item.
 * @param sourceEvidenceId Optional reference to the evidence item that generated this agenda item.
 * @param createdAt Timestamp when the agenda item was created.
 * @param updatedAt Timestamp when the agenda item was last updated.
 */
data class AgendaItem(
    val id: String = UUID.randomUUID().toString(),
    val jobId: String = "",
    val title: String = "",
    val description: String = "",
    val dueAt: Long? = null,
    val dueText: String = "",
    val status: AgendaStatus = AgendaStatus.PENDING,
    val sourceEvidenceId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    // Reminder notification fields
    val reminderEnabled: Boolean = false,
    val reminderOffsetDays: Int = 0,
    val reminderScheduledAt: Long? = null,
    val notificationId: Int = 0
) : Serializable

/**
 * Data class representing a job file.
 *
 * @param id Unique identifier for the job.
 * @param title The title of the job.
 * @param clientName The name of the client.
 * @param phone The phone number of the client.
 * @param serviceType The type of service provided.
 * @param status The current status of the job.
 * @param createdAt Timestamp when the job was created.
 * @param updatedAt Timestamp when the job was last updated.
 * @param lastUsedAt Timestamp when the job was last accessed or modified.
 * @param evidence List of evidence items associated with the job.
 * @param agendaItems List of agenda items (tasks/reminders) for the job.
 * @param notes Optional notes about the job.
 * @param reportNotes Optional notes specifically for the PDF report (v0.7+).
 */
data class JobFile(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val clientName: String = "",
    val phone: String = "",
    val serviceType: String = "",
    val status: JobStatus = JobStatus.ACTIVE,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastUsedAt: Long = System.currentTimeMillis(),
    val evidence: List<EvidenceItem> = emptyList(),
    val agendaItems: List<AgendaItem> = emptyList(),
    val notes: String = "",
    val reportNotes: String = ""
) : Serializable

/**
 * Helper function to get Spanish label for EvidenceCategory.
 */
fun EvidenceCategory.getSpanishLabel(): String = when (this) {
    EvidenceCategory.UNCLASSIFIED -> "Sin clasificar"
    EvidenceCategory.BEFORE -> "Antes"
    EvidenceCategory.DURING -> "Durante"
    EvidenceCategory.AFTER -> "Después"
    EvidenceCategory.MATERIAL -> "Material"
    EvidenceCategory.PAYMENT -> "Pago"
    EvidenceCategory.CLIENT_MESSAGE -> "Mensaje del cliente"
}

/**
 * Helper function to get Spanish label for EvidenceType.
 */
fun EvidenceType.getSpanishLabel(): String = when (this) {
    EvidenceType.TEXT -> "Texto"
    EvidenceType.IMAGE -> "Imagen"
    EvidenceType.AUDIO -> "Audio"
    EvidenceType.PDF -> "PDF"
}

/**
 * Helper function to get Spanish label for AgendaStatus.
 */
fun AgendaStatus.getSpanishLabel(): String = when (this) {
    AgendaStatus.PENDING -> "Pendiente"
    AgendaStatus.DONE -> "Completado"
    AgendaStatus.CANCELLED -> "Cancelado"
    AgendaStatus.ARCHIVED -> "Archivado"
}

/**
 * Helper function to get Spanish label for JobStatus.
 */
fun JobStatus.getSpanishLabel(): String = when (this) {
    JobStatus.ACTIVE -> "Activo"
    JobStatus.COMPLETED -> "Completado"
    JobStatus.ARCHIVED -> "Archivado"
}
