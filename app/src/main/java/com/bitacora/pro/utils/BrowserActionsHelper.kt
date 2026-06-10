package com.bitacora.pro.utils

import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Helper for browser and web actions via intent.
 * v0.8.0: Intent-based web integration (no API calls)
 *
 * Features:
 * - Open URLs in browser
 * - Search for information
 * - Open maps for location
 * - Open email client for communication
 * - All via standard intents
 */
object BrowserActionsHelper {

    /**
     * Opens a URL in the default browser.
     */
    fun openUrl(context: Context, url: String) {
        val uri = if (url.startsWith("http://") || url.startsWith("https://")) {
            Uri.parse(url)
        } else {
            Uri.parse("https://$url")
        }

        val intent = Intent(Intent.ACTION_VIEW, uri)
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Searches for information using default search engine.
     */
    fun search(context: Context, query: String) {
        val searchUri = Uri.parse("https://www.google.com/search?q=${Uri.encode(query)}")
        val intent = Intent(Intent.ACTION_VIEW, searchUri)
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Opens maps for a location.
     */
    fun openMaps(context: Context, location: String) {
        val mapsUri = Uri.parse("geo:0,0?q=${Uri.encode(location)}")
        val intent = Intent(Intent.ACTION_VIEW, mapsUri)
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Opens email client to send email.
     */
    fun sendEmail(context: Context, email: String, subject: String = "", body: String = "") {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$email")
            if (subject.isNotEmpty()) {
                putExtra(Intent.EXTRA_SUBJECT, subject)
            }
            if (body.isNotEmpty()) {
                putExtra(Intent.EXTRA_TEXT, body)
            }
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Opens phone dialer with a phone number.
     */
    fun callPhone(context: Context, phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Opens WhatsApp with a phone number.
     */
    fun openWhatsApp(context: Context, phoneNumber: String, message: String = "") {
        val cleanPhone = phoneNumber.replace(Regex("[^0-9+]"), "")
        val whatsappUri = if (message.isNotEmpty()) {
            Uri.parse("https://wa.me/$cleanPhone?text=${Uri.encode(message)}")
        } else {
            Uri.parse("https://wa.me/$cleanPhone")
        }

        val intent = Intent(Intent.ACTION_VIEW, whatsappUri)
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Checks if a URL is valid.
     */
    fun isValidUrl(url: String): Boolean {
        return try {
            Uri.parse(url)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Extracts domain from URL.
     */
    fun extractDomain(url: String): String {
        return try {
            val uri = Uri.parse(url)
            uri.host ?: url
        } catch (e: Exception) {
            url
        }
    }

    /**
     * Checks if browser is available.
     */
    fun isBrowserAvailable(context: Context): Boolean {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"))
        return intent.resolveActivity(context.packageManager) != null
    }
}
