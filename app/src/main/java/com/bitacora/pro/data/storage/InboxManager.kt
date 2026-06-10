package com.bitacora.pro.data.storage

import android.content.Context
import android.net.Uri
import com.bitacora.pro.data.models.EvidenceItem
import com.bitacora.pro.data.models.EvidenceType
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import java.io.File
import java.util.UUID

/**
 * Manages local inbox for captured content before job assignment.
 * v0.8.0: Provides temporary storage for unassigned captures
 *
 * Features:
 * - Store captured content locally
 * - List unassigned captures
 * - Assign captures to jobs
 * - Delete/archive inbox items
 * - Persistent storage in app-private storage
 */
class InboxManager(private val context: Context) {

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val inboxDir: File
        get() = File(context.filesDir, "inbox").apply { mkdirs() }

    /**
     * Data class representing an inbox item.
     */
    data class InboxItem(
        val id: String = UUID.randomUUID().toString(),
        val type: EvidenceType = EvidenceType.TEXT,
        val fileName: String = "",
        val textContent: String = "",
        val mimeType: String = "",
        val createdAt: Long = System.currentTimeMillis(),
        val notes: String = ""
    )

    /**
     * Saves a captured item to the inbox.
     */
    fun saveToInbox(item: InboxItem): InboxItem {
        val itemFile = File(inboxDir, "${item.id}.json")
        itemFile.writeText(gson.toJson(item))
        return item
    }

    /**
     * Loads all inbox items.
     */
    fun loadAllInboxItems(): List<InboxItem> {
        return try {
            inboxDir.listFiles()?.filter { it.extension == "json" }?.mapNotNull { file ->
                try {
                    val jsonText = file.readText()
                    gson.fromJson(jsonText, InboxItem::class.java)
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }?.sortedByDescending { it.createdAt } ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Loads a specific inbox item by ID.
     */
    fun loadInboxItem(itemId: String): InboxItem? {
        return try {
            val itemFile = File(inboxDir, "$itemId.json")
            if (!itemFile.exists()) return null
            val jsonText = itemFile.readText()
            gson.fromJson(jsonText, InboxItem::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Deletes an inbox item.
     */
    fun deleteInboxItem(itemId: String): Boolean {
        return try {
            val itemFile = File(inboxDir, "$itemId.json")
            itemFile.delete()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Converts an inbox item to an evidence item for a job.
     */
    fun convertToEvidence(inboxItem: InboxItem): EvidenceItem {
        return EvidenceItem(
            id = inboxItem.id,
            type = inboxItem.type,
            fileName = inboxItem.fileName,
            textContent = inboxItem.textContent,
            mimeType = inboxItem.mimeType,
            createdAt = inboxItem.createdAt,
            notes = inboxItem.notes
        )
    }

    /**
     * Clears all inbox items.
     */
    fun clearInbox(): Boolean {
        return try {
            inboxDir.listFiles()?.forEach { it.delete() }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Gets inbox item count.
     */
    fun getInboxItemCount(): Int {
        return loadAllInboxItems().size
    }
}
