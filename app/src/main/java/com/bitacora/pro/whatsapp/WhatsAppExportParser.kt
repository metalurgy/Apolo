package com.bitacora.pro.whatsapp

import com.bitacora.pro.utils.PhoneNumberUtils
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Parses WhatsApp chat exports (text format).
 * Supports both individual and group chat exports.
 *
 * v0.7.3: Simplified, minimal, non-blocking
 * - Fast parsing without heavy processing
 * - Basic message extraction
 * - Phone number detection
 * - Privacy-first: local processing only, no external APIs
 *
 * WhatsApp export format:
 * [MM/DD/YYYY, HH:MM:SS AM/PM] Sender: Message text
 * or
 * [MM/DD/YYYY, HH:MM:SS] Sender: Message text (24-hour format)
 */
object WhatsAppExportParser {

    data class WhatsAppMessage(
        val timestamp: String,
        val sender: String,
        val content: String,
        val type: MessageType,
        val extractedPhoneNumbers: List<String> = emptyList()
    )

    enum class MessageType {
        TEXT,
        IMAGE,
        AUDIO,
        VIDEO,
        DOCUMENT,
        LOCATION,
        CONTACT,
        SYSTEM,
        UNKNOWN
    }

    data class WhatsAppChatExport(
        val messages: List<WhatsAppMessage>,
        val senderCount: Int,
        val extractedPhoneNumbers: Set<String>,
        val mediaCount: Int,
        val dateRange: Pair<String, String>? = null
    )

    /**
     * Parses a WhatsApp chat export text.
     * Returns a WhatsAppChatExport with all extracted information.
     */
    fun parseExport(exportText: String): WhatsAppChatExport {
        if (exportText.isBlank()) {
            return WhatsAppChatExport(
                messages = emptyList(),
                senderCount = 0,
                extractedPhoneNumbers = emptySet(),
                mediaCount = 0
            )
        }

        val messages = mutableListOf<WhatsAppMessage>()
        val senders = mutableSetOf<String>()
        val phoneNumbers = mutableSetOf<String>()
        var mediaCount = 0

        val lines = exportText.split("\n")
        var currentMessage: WhatsAppMessage? = null

        for (line in lines) {
            if (line.isBlank()) continue

            // Try to parse as a new message
            val parsedMessage = parseMessageLine(line)
            if (parsedMessage != null) {
                // Save previous message if exists
                if (currentMessage != null) {
                    messages.add(currentMessage)
                    senders.add(currentMessage.sender)
                    phoneNumbers.addAll(currentMessage.extractedPhoneNumbers)
                    if (currentMessage.type != MessageType.TEXT && currentMessage.type != MessageType.SYSTEM) {
                        mediaCount++
                    }
                }
                currentMessage = parsedMessage
            } else if (currentMessage != null) {
                // This is a continuation of the previous message (multi-line)
                currentMessage = currentMessage.copy(
                    content = currentMessage.content + "\n" + line
                )
            }
        }

        // Don't forget the last message
        if (currentMessage != null) {
            messages.add(currentMessage)
            senders.add(currentMessage.sender)
            phoneNumbers.addAll(currentMessage.extractedPhoneNumbers)
            if (currentMessage.type != MessageType.TEXT && currentMessage.type != MessageType.SYSTEM) {
                mediaCount++
            }
        }

        val dateRange = if (messages.isNotEmpty()) {
            Pair(messages.first().timestamp, messages.last().timestamp)
        } else {
            null
        }

        return WhatsAppChatExport(
            messages = messages,
            senderCount = senders.size,
            extractedPhoneNumbers = phoneNumbers,
            mediaCount = mediaCount,
            dateRange = dateRange
        )
    }

    /**
     * Parses a single message line from WhatsApp export.
     * Returns null if the line is not a valid message start.
     */
    private fun parseMessageLine(line: String): WhatsAppMessage? {
        // Match pattern: [MM/DD/YYYY, HH:MM:SS AM/PM] Sender: Message
        // or: [MM/DD/YYYY, HH:MM:SS] Sender: Message
        val messagePattern = Regex(
            "^\\[(\\d{1,2}/\\d{1,2}/\\d{4},\\s\\d{1,2}:\\d{2}(?::\\d{2})?(?:\\s[AP]M)?)\\]\\s+(.+?):\\s(.*)$"
        )

        val match = messagePattern.find(line) ?: return null

        val timestamp = match.groupValues[1]
        val sender = match.groupValues[2]
        val content = match.groupValues[3]

        // Detect message type
        val type = detectMessageType(content)

        // Extract phone numbers from content
        val phoneNumbers = PhoneNumberUtils.extractPhoneNumbers(content)

        return WhatsAppMessage(
            timestamp = timestamp,
            sender = sender,
            content = content,
            type = type,
            extractedPhoneNumbers = phoneNumbers
        )
    }

    /**
     * Detects the type of message based on content.
     */
    private fun detectMessageType(content: String): MessageType {
        return when {
            content.contains("<image omitted>", ignoreCase = true) -> MessageType.IMAGE
            content.contains("<audio omitted>", ignoreCase = true) -> MessageType.AUDIO
            content.contains("<video omitted>", ignoreCase = true) -> MessageType.VIDEO
            content.contains("<document omitted>", ignoreCase = true) -> MessageType.DOCUMENT
            content.contains("<location omitted>", ignoreCase = true) -> MessageType.LOCATION
            content.contains("<contact omitted>", ignoreCase = true) -> MessageType.CONTACT
            content.contains("Messages and calls are encrypted") -> MessageType.SYSTEM
            content.contains("You created this group") -> MessageType.SYSTEM
            content.contains("added") && content.contains("to this group") -> MessageType.SYSTEM
            content.contains("left") -> MessageType.SYSTEM
            content.contains("changed the subject") -> MessageType.SYSTEM
            content.contains("changed this group's icon") -> MessageType.SYSTEM
            content.isEmpty() -> MessageType.SYSTEM
            else -> MessageType.TEXT
        }
    }

    /**
     * Extracts a summary of the chat export.
     * Useful for displaying preview information.
     */
    fun getSummary(export: WhatsAppChatExport): String {
        return buildString {
            append("Mensajes: ${export.messages.size}\n")
            append("Participantes: ${export.senderCount}\n")
            append("Archivos multimedia: ${export.mediaCount}\n")
            if (export.extractedPhoneNumbers.isNotEmpty()) {
                append("Números de teléfono encontrados: ${export.extractedPhoneNumbers.size}\n")
            }
            if (export.dateRange != null) {
                append("Período: ${export.dateRange.first} a ${export.dateRange.second}")
            }
        }
    }

    /**
     * Filters messages by sender.
     */
    fun filterBySender(export: WhatsAppChatExport, sender: String): List<WhatsAppMessage> {
        return export.messages.filter { it.sender.equals(sender, ignoreCase = true) }
    }

    /**
     * Filters messages by type.
     */
    fun filterByType(export: WhatsAppChatExport, type: MessageType): List<WhatsAppMessage> {
        return export.messages.filter { it.type == type }
    }

    /**
     * Extracts all text content from messages (excluding system messages).
     * Useful for creating text evidence from chat.
     */
    fun extractTextContent(export: WhatsAppChatExport): String {
        return export.messages
            .filter { it.type == MessageType.TEXT }
            .joinToString("\n") { "[${it.timestamp}] ${it.sender}: ${it.content}" }
    }

    /**
     * Gets all unique senders from the export.
     */
    fun getSenders(export: WhatsAppChatExport): List<String> {
        return export.messages.map { it.sender }.distinct()
    }

    /**
     * Detects if this is a group chat (multiple senders) or individual chat.
     */
    fun isGroupChat(export: WhatsAppChatExport): Boolean {
        return export.senderCount > 2 // More than 2 because system messages might have different sender
    }

    /**
     * Extracts the main contact/sender (the one with most messages, excluding system).
     */
    fun getMainContact(export: WhatsAppChatExport): String? {
        return export.messages
            .filter { it.type != MessageType.SYSTEM }
            .groupingBy { it.sender }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key
    }
}
