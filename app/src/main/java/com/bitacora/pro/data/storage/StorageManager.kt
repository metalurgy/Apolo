package com.bitacora.pro.data.storage

import android.content.Context
import android.net.Uri
import com.bitacora.pro.data.models.AgendaItem
import com.bitacora.pro.data.models.EvidenceItem
import com.bitacora.pro.data.models.EvidenceType
import com.bitacora.pro.data.models.JobFile
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.File
import java.util.UUID

/**
 * Manages all file storage operations for jobs and evidence.
 * All data is stored in app-private storage (filesDir).
 * No temporary job folders are created; files are copied directly to their final job folder.
 */
class StorageManager(private val context: Context) {

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val jobsDir: File
        get() = File(context.filesDir, "jobs").apply { mkdirs() }

    /**
     * Creates a new job directory and saves the job metadata.
     */
    fun createJob(job: JobFile): JobFile {
        val jobDir = File(jobsDir, job.id).apply { mkdirs() }
        File(jobDir, "evidence").mkdirs()
        saveJobMetadata(job)
        return job
    }

    /**
     * Loads a job by its ID with safe backward compatibility.
     * Handles old job.json files that may be missing agendaItems, lastUsedAt, or evidence fields.
     */
    fun loadJob(jobId: String): JobFile? {
        return try {
            val jobFile = File(File(jobsDir, jobId), "job.json")
            if (!jobFile.exists()) {
                return null
            }

            val jsonText = jobFile.readText()
            val jsonObject = JsonParser.parseString(jsonText).asJsonObject

            // Ensure agendaItems field exists (for backward compatibility)
            if (!jsonObject.has("agendaItems") || jsonObject.get("agendaItems").isJsonNull) {
                jsonObject.add("agendaItems", JsonArray())
            }

            // Ensure lastUsedAt field exists (for backward compatibility)
            if (!jsonObject.has("lastUsedAt") || jsonObject.get("lastUsedAt").isJsonNull) {
                // Use updatedAt if available, otherwise createdAt, otherwise current time
                val fallbackTime = when {
                    jsonObject.has("updatedAt") && !jsonObject.get("updatedAt").isJsonNull ->
                        jsonObject.get("updatedAt").asLong
                    jsonObject.has("createdAt") && !jsonObject.get("createdAt").isJsonNull ->
                        jsonObject.get("createdAt").asLong
                    else -> System.currentTimeMillis()
                }
                jsonObject.addProperty("lastUsedAt", fallbackTime)
            }

            // Ensure evidence field exists (for backward compatibility)
            if (!jsonObject.has("evidence") || jsonObject.get("evidence").isJsonNull) {
                jsonObject.add("evidence", JsonArray())
            }

            // Deserialize the corrected JSON object
            gson.fromJson(jsonObject, JobFile::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Loads all jobs from storage.
     */
    fun loadAllJobs(): List<JobFile> {
        return try {
            jobsDir.listFiles()?.mapNotNull { jobDir ->
                if (jobDir.isDirectory) {
                    loadJob(jobDir.name)
                } else {
                    null
                }
            }?.sortedByDescending { it.createdAt } ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Saves job metadata to job.json.
     */
    fun saveJobMetadata(job: JobFile) {
        try {
            val jobDir = File(jobsDir, job.id)
            jobDir.mkdirs()
            val jobFile = File(jobDir, "job.json")
            jobFile.writeText(gson.toJson(job))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Copies a file from a URI directly to the specified job's evidence folder.
     * Returns the EvidenceItem with the copied file information.
     * Returns null if the file copy fails.
     *
     * @param jobId The ID of the job to copy the file to.
     * @param uri The content URI of the file to copy.
     * @param evidenceType The type of evidence.
     * @param mimeType The MIME type of the file.
     * @return EvidenceItem if successful, null if copy fails.
     */
    fun copyEvidenceFromUri(
        jobId: String,
        uri: Uri,
        evidenceType: EvidenceType,
        mimeType: String
    ): EvidenceItem? {
        return try {
            val jobDir = File(jobsDir, jobId)
            val evidenceDir = File(jobDir, "evidence").apply { mkdirs() }

            // Determine file extension from MIME type
            val extension = getExtensionFromMimeType(mimeType)
            val evidenceId = UUID.randomUUID().toString()
            val fileName = "$evidenceId.$extension"
            val targetFile = File(evidenceDir, fileName)

            // Copy file from URI to target location
            val inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream == null) {
                // Failed to open input stream
                return null
            }

            inputStream.use { input ->
                targetFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            // Verify file was created and has content
            if (!targetFile.exists() || targetFile.length() == 0L) {
                // File copy failed or resulted in empty file
                targetFile.delete()
                return null
            }

            // Create and return EvidenceItem
            return EvidenceItem(
                id = evidenceId,
                type = evidenceType,
                fileName = fileName,
                mimeType = mimeType,
                createdAt = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Saves text evidence to the job.
     * Text is stored as metadata only, not as a file.
     */
    fun saveTextEvidence(jobId: String, text: String): EvidenceItem {
        val evidenceId = UUID.randomUUID().toString()
        return EvidenceItem(
            id = evidenceId,
            type = EvidenceType.TEXT,
            textContent = text,
            mimeType = "text/plain",
            createdAt = System.currentTimeMillis()
        )
    }

    /**
     * Adds evidence to a job and updates the job metadata.
     * Also updates lastUsedAt to current time.
     */
    fun addEvidenceToJob(jobId: String, evidence: EvidenceItem) {
        try {
            val job = loadJob(jobId) ?: return
            val now = System.currentTimeMillis()
            val updatedJob = job.copy(
                evidence = job.evidence + evidence,
                updatedAt = now,
                lastUsedAt = now
            )
            saveJobMetadata(updatedJob)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Adds an agenda item to a job and updates the job metadata.
     * Also updates lastUsedAt to current time.
     */
    fun addAgendaItemToJob(jobId: String, agendaItem: AgendaItem) {
        try {
            val job = loadJob(jobId) ?: return
            val now = System.currentTimeMillis()
            val updatedJob = job.copy(
                agendaItems = job.agendaItems + agendaItem,
                updatedAt = now,
                lastUsedAt = now
            )
            saveJobMetadata(updatedJob)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Updates the status of an agenda item.
     * Also updates lastUsedAt to current time.
     */
    fun updateAgendaItemStatus(jobId: String, agendaItemId: String, newStatus: String) {
        try {
            val job = loadJob(jobId) ?: return
            val now = System.currentTimeMillis()
            val updatedAgendaItems = job.agendaItems.map { item ->
                if (item.id == agendaItemId) {
                    item.copy(
                        status = enumValueOf(newStatus),
                        updatedAt = now
                    )
                } else {
                    item
                }
            }
            val updatedJob = job.copy(
                agendaItems = updatedAgendaItems,
                updatedAt = now,
                lastUsedAt = now
            )
            saveJobMetadata(updatedJob)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Deletes an agenda item from a job.
     * Also updates lastUsedAt to current time.
     */
    fun deleteAgendaItem(jobId: String, agendaItemId: String) {
        try {
            val job = loadJob(jobId) ?: return
            val now = System.currentTimeMillis()
            val updatedAgendaItems = job.agendaItems.filter { it.id != agendaItemId }
            val updatedJob = job.copy(
                agendaItems = updatedAgendaItems,
                updatedAt = now,
                lastUsedAt = now
            )
            saveJobMetadata(updatedJob)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Updates the category of an evidence item.
     */
    fun updateEvidenceCategory(jobId: String, evidenceId: String, newCategory: String) {
        try {
            val job = loadJob(jobId) ?: return
            val now = System.currentTimeMillis()
            val updatedEvidence = job.evidence.map { evidence ->
                if (evidence.id == evidenceId) {
                    evidence.copy(category = enumValueOf(newCategory))
                } else {
                    evidence
                }
            }
            val updatedJob = job.copy(
                evidence = updatedEvidence,
                updatedAt = now,
                lastUsedAt = now
            )
            saveJobMetadata(updatedJob)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Deletes evidence from a job.
     * Removes both the metadata and the physical file.
     */
    fun deleteEvidence(jobId: String, evidenceId: String) {
        try {
            val job = loadJob(jobId) ?: return
            val evidence = job.evidence.find { it.id == evidenceId } ?: return

            // Delete the physical file if it exists
            if (evidence.fileName.isNotEmpty()) {
                val evidenceFile = File(File(jobsDir, jobId), "evidence/${evidence.fileName}")
                if (evidenceFile.exists()) {
                    evidenceFile.delete()
                }
            }

            // Update job metadata
            val now = System.currentTimeMillis()
            val updatedEvidence = job.evidence.filter { it.id != evidenceId }
            val updatedJob = job.copy(
                evidence = updatedEvidence,
                updatedAt = now,
                lastUsedAt = now
            )
            saveJobMetadata(updatedJob)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Gets the file for an evidence item.
     */
    fun getEvidenceFile(jobId: String, evidenceId: String): File? {
        return try {
            val job = loadJob(jobId) ?: return null
            val evidence = job.evidence.find { it.id == evidenceId } ?: return null
            if (evidence.fileName.isNotEmpty()) {
                File(File(jobsDir, jobId), "evidence/${evidence.fileName}")
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Gets the URI for an evidence file using FileProvider.
     * This is used to safely share files with other apps.
     */
    fun getEvidenceFileUri(jobId: String, evidenceId: String): Uri? {
        return try {
            val file = getEvidenceFile(jobId, evidenceId) ?: return null
            if (file.exists()) {
                androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Determines file extension from MIME type.
     * Uses safe defaults for unknown types.
     */
    private fun getExtensionFromMimeType(mimeType: String): String {
        return when {
            mimeType.startsWith("image/") -> {
                when {
                    mimeType.contains("jpeg") || mimeType.contains("jpg") -> "jpg"
                    mimeType.contains("png") -> "png"
                    mimeType.contains("gif") -> "gif"
                    mimeType.contains("webp") -> "webp"
                    else -> "jpg"
                }
            }
            mimeType.startsWith("audio/") -> {
                when {
                    mimeType.contains("mpeg") || mimeType.contains("mp3") -> "mp3"
                    mimeType.contains("wav") -> "wav"
                    mimeType.contains("ogg") -> "ogg"
                    mimeType.contains("m4a") -> "m4a"
                    else -> "mp3"
                }
            }
            mimeType.contains("pdf") -> "pdf"
            else -> "bin"
        }
    }
}
