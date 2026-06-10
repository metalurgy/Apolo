package com.bitacora.pro.utils

/**
 * Phone number normalization and validation utilities.
 * Supports international phone numbers with focus on Latin American formats.
 * 
 * Features:
 * - Removes common formatting characters (spaces, dashes, parentheses)
 * - Normalizes country codes (adds +52 for Mexico if missing)
 * - Validates basic phone number structure
 * - Extracts phone numbers from text
 * - Privacy-first: no external APIs, local processing only
 */
object PhoneNumberUtils {

    /**
     * Normalizes a phone number by removing formatting and adding country code if needed.
     * 
     * Examples:
     * - "555-1234567" -> "+525551234567"
     * - "5551234567" -> "+525551234567"
     * - "+525551234567" -> "+525551234567"
     * - "(555) 123-4567" -> "+525551234567"
     */
    fun normalizePhoneNumber(input: String): String {
        if (input.isBlank()) return ""
        
        // Remove common formatting characters
        var cleaned = input.trim()
            .replace(Regex("[\\s\\-().]"), "")
            .replace("+", "")
        
        // If it doesn't start with country code, assume Mexico (+52)
        if (!cleaned.startsWith("52") && cleaned.length >= 10) {
            // If it starts with 0, remove it (common in some formats)
            if (cleaned.startsWith("0")) {
                cleaned = cleaned.substring(1)
            }
            // Add Mexico country code if not present
            if (!cleaned.startsWith("52")) {
                cleaned = "52$cleaned"
            }
        }
        
        return "+$cleaned"
    }

    /**
     * Validates if a phone number has a reasonable format.
     * Returns true if the number has at least 10 digits after normalization.
     */
    fun isValidPhoneNumber(input: String): Boolean {
        if (input.isBlank()) return false
        val normalized = normalizePhoneNumber(input)
        val digitsOnly = normalized.replace(Regex("[^0-9]"), "")
        return digitsOnly.length >= 10
    }

    /**
     * Extracts phone numbers from text.
     * Looks for patterns like:
     * - 10+ consecutive digits
     * - Numbers with dashes/spaces (555-1234567)
     * - Numbers with parentheses ((555) 123-4567)
     * - Numbers with + prefix (+525551234567)
     */
    fun extractPhoneNumbers(text: String): List<String> {
        if (text.isBlank()) return emptyList()
        
        val phonePattern = Regex(
            "(?:\\+?\\d{1,3})?[\\s.-]?\\(?\\d{3}\\)?[\\s.-]?\\d{3}[\\s.-]?\\d{4,6}|\\d{10,15}"
        )
        
        return phonePattern.findAll(text)
            .map { it.value }
            .filter { isValidPhoneNumber(it) }
            .map { normalizePhoneNumber(it) }
            .distinct()
            .toList()
    }

    /**
     * Formats a normalized phone number for display.
     * Example: "+525551234567" -> "+52 555 123 4567"
     */
    fun formatForDisplay(normalizedNumber: String): String {
        if (normalizedNumber.isBlank()) return ""
        
        val digitsOnly = normalizedNumber.replace(Regex("[^0-9]"), "")
        
        return when {
            digitsOnly.startsWith("52") && digitsOnly.length == 12 -> {
                // Mexico format: +52 555 123 4567
                val areaCode = digitsOnly.substring(2, 5)
                val firstPart = digitsOnly.substring(5, 8)
                val secondPart = digitsOnly.substring(8, 12)
                "+52 $areaCode $firstPart $secondPart"
            }
            digitsOnly.length >= 10 -> {
                // Generic format: +52 XXXXXXXXXX
                "+${digitsOnly.substring(0, 2)} ${digitsOnly.substring(2)}"
            }
            else -> normalizedNumber
        }
    }

    /**
     * Compares two phone numbers for equality after normalization.
     * Useful for matching contacts from different sources.
     */
    fun arePhoneNumbersEqual(phone1: String, phone2: String): Boolean {
        if (phone1.isBlank() || phone2.isBlank()) return false
        val normalized1 = normalizePhoneNumber(phone1)
        val normalized2 = normalizePhoneNumber(phone2)
        return normalized1 == normalized2
    }

    /**
     * Extracts the last 10 digits of a phone number (useful for matching).
     * Example: "+525551234567" -> "5551234567"
     */
    fun getLastTenDigits(phoneNumber: String): String {
        val digitsOnly = phoneNumber.replace(Regex("[^0-9]"), "")
        return if (digitsOnly.length >= 10) {
            digitsOnly.takeLast(10)
        } else {
            digitsOnly
        }
    }
}
