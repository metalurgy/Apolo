package com.bitacora.pro.data.storage

import android.content.Context
import android.net.Uri
import com.bitacora.pro.data.models.AgendaItem
import com.bitacora.pro.data.models.EvidenceItem
import com.bitacora.pro.data.models.EvidenceType
import com.bitacora.pro.data.models.JobFile
import com.bitacora.pro.data.models.JobStatus
import com.bitacora.pro.notifications.AgendaNotificationScheduler
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
     * Handles old job.json files that may be missing:
     * - agendaItems (v0.6 and earlier)
     * - lastUsedAt (v0.6 and earlier)
     * - evidence (v0.6 and earlier)
     * - reportNotes (v0.6 and earlier)
     * - status (v0.7 and earlier - defaults to ACTIVE)
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

            // Ensure reportNotes field exists (for backward compatibility with v0.6 and earlier)
            if (!jsonObject.has("reportNotes") || jsonObject.get("reportNotes").isJsonNull) {
                jsonObject.addProperty("reportNotes", "")
            }

            // Ensure status field exists (for backward compatibility with v0.7 and earlier)
            // Default to ACTIVE for old jobs
            if (!jsonObject.has("status") || jsonObject.get("status").isJsonNull) {
                jsonObject.addProperty("status", JobStatus.ACTIVE.name)
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
     * Cancels reminder notifications when item is marked as DONE.
     */
    fun updateAgendaItemStatus(jobId: String, agendaItemId: String, newStatus: String) {
        try {
            val job = loadJob(jobId) ?: return
            val now = System.currentTimeMillis()
            val scheduler = AgendaNotificationScheduler(context)
            
            val updatedAgendaItems = job.agendaItems.map { item ->
                if (item.id == agendaItemId) {
                    val updatedItem = item.copy(
                        status = enumValueOf(newStatus),
                        updatedAt = now
                    )
                    // Cancel reminder if item is marked as DONE
                    if (newStatus == "DONE" && item.notificationId != 0) {
                        scheduler.cancelReminder(item)
                    }
                    updatedItem
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
     * Archives an agenda item (changes status to ARCHIVED).
     * Also updates lastUsedAt to current time.
     * Cancels any scheduled reminder notifications for the item.
     */
    fun archiveAgendaItem(jobId: String, agendaItemId: String) {
        try {
            val job = loadJob(jobId) ?: return
            val now = System.currentTimeMillis()
            val scheduler = AgendaNotificationScheduler(context)
            
            val updatedAgendaItems = job.agendaItems.map { item ->
                if (item.id == agendaItemId) {
                    val updatedItem = item.copy(
                        status = com.bitacora.pro.data.models.AgendaStatus.ARCHIVED,
                        updatedAt = now
                    )
                    // Cancel reminder if item has one scheduled
                    if (item.notificationId != 0) {
                        scheduler.cancelReminder(item)
                    }
                    updatedItem
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
     * Updates an existing agenda item in a job.
     * Handles reminder rescheduling if reminder settings changed.
     * Preserves sourceEvidenceId and createdAt.
     * Updates job.updatedAt and job.lastUsedAt.
     *
     * @param jobId The ID of the job containing the agenda item
     * @param updatedItem The updated agenda item
     * @param scheduler Optional AgendaNotificationScheduler for reminder handling
     */
    fun updateAgendaItem(
        jobId: String,
        updatedItem: AgendaItem,
        scheduler: AgendaNotificationScheduler? = null
    ) {
        try {
            val job = loadJob(jobId) ?: return
            val now = System.currentTimeMillis()
            
            val updatedAgendaItems = job.agendaItems.map { item ->
                if (item.id == updatedItem.id) {
                    // Preserve sourceEvidenceId and createdAt
                    val preservedItem = updatedItem.copy(
                        sourceEvidenceId = item.sourceEvidenceId,
                        createdAt = item.createdAt,
                        updatedAt = now
                    )
                    
                    // Handle reminder rescheduling if scheduler is provided
                    if (scheduler != null) {
                        // Check if reminder settings changed
                        val reminderChanged = (item.reminderEnabled != preservedItem.reminderEnabled) ||
                                (item.reminderOffsetDays != preservedItem.reminderOffsetDays) ||
                                (item.dueAt != preservedItem.dueAt)
                        
                        if (reminderChanged) {
                            // Reschedule reminder if item is PENDING and reminder is enabled
                            if (preservedItem.status == com.bitacora.pro.data.models.AgendaStatus.PENDING &&
                                preservedItem.reminderEnabled && preservedItem.dueAt != null) {
                                scheduler.rescheduleReminder(item, preservedItem)
                            } else {
                                // Cancel reminder if item is DONE/ARCHIVED or reminder disabled
                                if (item.notificationId != 0) {
                                    scheduler.cancelReminder(item)
                                }
                            }
                        }
                    }
                    
                    preservedItem
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
     * Cancels any scheduled reminder notifications for the item.
     */
    fun deleteAgendaItem(jobId: String, agendaItemId: String) {
        try {
            val job = loadJob(jobId) ?: return
            val now = System.currentTimeMillis()
            val scheduler = AgendaNotificationScheduler(context)
            
            // Find the item to delete and cancel its reminder
            val itemToDelete = job.agendaItems.find { it.id == agendaItemId }
            if (itemToDelete != null && itemToDelete.notificationId != 0) {
                scheduler.cancelReminder(itemToDelete)
            }
            
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

    /**
     * Creates a temporary camera file for capturing photos.
     * Returns the file URI for use with ActivityResultContracts.TakePicture().
     * The file is created in the cache directory and will be moved to evidence folder after capture.
     *
     * @return Pair of (File, Uri) for the temporary camera file
     */
    fun createTemporaryCameraFile(): Pair<File, Uri> {
        val cameraDir = File(context.cacheDir, "camera").apply { mkdirs() }
        val timeStamp = System.currentTimeMillis()
        val imageFile = File(cameraDir, "IMG_$timeStamp.jpg")
        
        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )
        
        return Pair(imageFile, uri)
    }

    /**
     * Saves a camera-captured photo as evidence.
     * Moves the temporary file from cache to the job's evidence folder.
     * Returns the EvidenceItem if successful, null if the move fails.
     *
     * @param jobId The ID of the job to save the photo to
     * @param tempFile The temporary file created by the camera
     * @return EvidenceItem if successful, null if move fails
     */
    fun saveCameraPhotoAsEvidence(jobId: String, tempFile: File): EvidenceItem? {
        return try {
            if (!tempFile.exists()) {
                return null
            }

            val jobDir = File(jobsDir, jobId)
            val evidenceDir = File(jobDir, "evidence").apply { mkdirs() }

            val evidenceId = java.util.UUID.randomUUID().toString()
            val fileName = "$evidenceId.jpg"
            val targetFile = File(evidenceDir, fileName)

            // Move the temporary file to the evidence folder
            if (tempFile.renameTo(targetFile)) {
                // Create and return EvidenceItem
                EvidenceItem(
                    id = evidenceId,
                    type = EvidenceType.IMAGE,
                    fileName = fileName,
                    mimeType = "image/jpeg",
                    createdAt = System.currentTimeMillis()
                )
            } else {
                // If rename fails, try copying instead
                tempFile.inputStream().use { input ->
                    targetFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                
                // Verify the copy was successful
                if (targetFile.exists() && targetFile.length() > 0) {
                    tempFile.delete() // Clean up temp file
                    EvidenceItem(
                        id = evidenceId,
                        type = EvidenceType.IMAGE,
                        fileName = fileName,
                        mimeType = "image/jpeg",
                        createdAt = System.currentTimeMillis()
                    )
                } else {
                    targetFile.delete()
                    null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Cleans up temporary camera files from the cache directory.
     * Called when the user cancels the camera capture.
     */
    fun cleanupTemporaryCameraFiles() {
        try {
            val cameraDir = File(context.cacheDir, "camera")
            if (cameraDir.exists() && cameraDir.isDirectory) {
                cameraDir.listFiles()?.forEach { file ->
                    file.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Gets the reports directory for a job.
     * Creates it if it doesn't exist.
     */
    fun getReportsDir(jobId: String): File {
        val jobDir = File(jobsDir, jobId)
        return File(jobDir, "reports").apply { mkdirs() }
    }

    /**
     * Gets a specific report file.
     */
    fun getReportFile(jobId: String, fileName: String): File {
        return File(getReportsDir(jobId), fileName)
    }

    /**
     * Gets the URI for a report file using FileProvider.
     * This is used to safely share reports with other apps.
     */
     fun getReportFileUri(jobId: String, fileName: String): Uri? {
         return try {
             val file = getReportFile(jobId, fileName)
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
      * Updates the status of a job.
      * Also updates job.updatedAt to current time.
      * Cancels all reminder notifications when job is archived.
      */
     fun updateJobStatus(jobId: String, newStatus: JobStatus) {
         try {
             val job = loadJob(jobId) ?: return
             val now = System.currentTimeMillis()
             val scheduler = AgendaNotificationScheduler(context)
             
             // If archiving the job, cancel all reminders
             if (newStatus == JobStatus.ARCHIVED) {
                 job.agendaItems.forEach { item ->
                     if (item.notificationId != 0) {
                         scheduler.cancelReminder(item)
                     }
                 }
             }
             
             val updatedJob = job.copy(
                 status = newStatus,
                 updatedAt = now,
                 lastUsedAt = now
             )
             saveJobMetadata(updatedJob)
         } catch (e: Exception) {
             e.printStackTrace()
         }
     }

     /**
      * Archives an entire job and all its reminders.
      * This is a convenience method that calls updateJobStatus with ARCHIVED.
      */
     fun archiveJob(jobId: String) {
         updateJobStatus(jobId, JobStatus.ARCHIVED)
     }

     /**
      * Marks a job as completed.
      * This is a convenience method that calls updateJobStatus with COMPLETED.
      */
     fun completeJob(jobId: String) {
         updateJobStatus(jobId, JobStatus.COMPLETED)
     }

     /**
      * Reactivates a job (sets status back to ACTIVE).
      * This is a convenience method that calls updateJobStatus with ACTIVE.
      */
     fun reactivateJob(jobId: String) {
         updateJobStatus(jobId, JobStatus.ACTIVE)
     }

     /**
      * Saves WhatsApp chat export as text evidence (v0.7.2).
      * Parses the export and creates a TEXT evidence item.
      */
     fun saveWhatsAppExportAsEvidence(
         jobId: String,
         exportText: String,
         senderName: String = "WhatsApp Chat"
     ): EvidenceItem? {
         return try {
             val evidence = EvidenceItem(
                 type = EvidenceType.TEXT,
                 fileName = "whatsapp_export.txt",
                 textContent = exportText,
                 mimeType = "text/plain",
                 notes = "Importado desde WhatsApp - $senderName"
             )
             addEvidenceToJob(jobId, evidence)
             evidence
         } catch (e: Exception) {
             e.printStackTrace()
             null
         }
     }

     /**
      * Extracts phone numbers from WhatsApp export and updates job metadata.
      * Useful for auto-filling contact information (v0.7.2).
      */
     fun extractPhoneNumbersFromWhatsAppExport(
         jobId: String,
         exportText: String
     ): List<String> {
         return try {
             val phonePattern = Regex(
                 "(?:\\+?\\d{1,3})?[\\s.-]?\\(?\\d{3}\\)?[\\s.-]?\\d{3}[\\s.-]?\\d{4,6}|\\d{10,15}"
             )
             phonePattern.findAll(exportText)
                 .map { it.value }
                 .distinct()
                 .toList()
         } catch (e: Exception) {
             e.printStackTrace()
             emptyList()
         }
     }

     /**
      * Creates a summary of WhatsApp export for display.
      * Returns a formatted string with message count, participants, etc.
      */
     fun getWhatsAppExportSummary(exportText: String): String {
         return try {
             val lines = exportText.split("\n").filter { it.isNotBlank() }
             val messagePattern = Regex("^\\[.*?\\]\\s+(.+?):\\s")
             val senders = mutableSetOf<String>()
             
             lines.forEach { line ->
                 val match = messagePattern.find(line)
                 if (match != null) {
                     senders.add(match.groupValues[1])
                 }
             }

             buildString {
                 append("Mensajes: ${lines.size}\n")
                 append("Participantes: ${senders.size}\n")
                 if (senders.isNotEmpty()) {
                     append("Contactos: ${senders.joinToString(", ")}")
                 }
             }
         } catch (e: Exception) {
             e.printStackTrace()
             "Error al procesar el chat"
         }
     }

     /**
      * Deletes a single job and all its associated files.
      * v0.9.0: Manual delete activity with typed confirmation
      */
     fun deleteJob(jobId: String): Boolean {
         return try {
             val jobDir = File(jobsDir, jobId)
             if (jobDir.exists()) {
                 jobDir.deleteRecursively()
             }
             true
         } catch (e: Exception) {
             e.printStackTrace()
             false
         }
     }

     /**
      * Deletes all jobs and their associated files.
      * v0.9.0: Dangerous delete all from About screen
      */
     fun deleteAllJobs(): Boolean {
         return try {
             val allJobs = loadAllJobs()
             allJobs.forEach { job ->
                 deleteJob(job.id)
             }
             true
         } catch (e: Exception) {
             e.printStackTrace()
             false
         }
     }
 }
