package com.bitacora.pro.whatsapp

import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Helper for WhatsApp-related UI operations and intents.
 * 
 * Features:
 * - Opens WhatsApp or WhatsApp Business with a specific contact
 * - Generates WhatsApp share intents
 * - Provides privacy-first UI explanations
 * - No automatic scraping or background access
 * - User-initiated only
 */
object WhatsAppUIHelper {

    /**
     * Opens WhatsApp with a specific phone number.
     * Uses the standard WhatsApp intent with phone validation.
     *
     * v0.7.3: Added phone validation and friendly error handling
     *
     * @param context Android context
     * @param phoneNumber Normalized phone number (e.g., "+525551234567")
     * @return Pair<Boolean, String> - (success, message)
     */
    fun openWhatsApp(context: Context, phoneNumber: String): Pair<Boolean, String> {
        return try {
            // Validate phone number
            if (phoneNumber.isBlank()) {
                return Pair(false, "Por favor, ingresa un número de teléfono")
            }
            
            // Remove common formatting characters
            val cleanPhone = phoneNumber.replace(Regex("[\\s\\-().]"), "")
            if (cleanPhone.length < 7) {
                return Pair(false, "El número de teléfono parece incompleto")
            }
            
            // Ensure it starts with + for international format
            val formattedPhone = if (cleanPhone.startsWith("+")) cleanPhone else "+$cleanPhone"
            
            val uri = Uri.parse("https://wa.me/$formattedPhone")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(intent)
            Pair(true, "WhatsApp abierto")
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(false, "No se pudo abrir WhatsApp. ¿Está instalado?")
        }
    }

    /**
     * Opens WhatsApp Business with a specific phone number.
     * Uses the WhatsApp Business intent with phone validation.
     *
     * v0.7.3: Added phone validation and friendly error handling
     *
     * @param context Android context
     * @param phoneNumber Normalized phone number (e.g., "+525551234567")
     * @return Pair<Boolean, String> - (success, message)
     */
    fun openWhatsAppBusiness(context: Context, phoneNumber: String): Pair<Boolean, String> {
        return try {
            // Validate phone number
            if (phoneNumber.isBlank()) {
                return Pair(false, "Por favor, ingresa un número de teléfono")
            }
            
            // Remove common formatting characters
            val cleanPhone = phoneNumber.replace(Regex("[\\s\\-().]"), "")
            if (cleanPhone.length < 7) {
                return Pair(false, "El número de teléfono parece incompleto")
            }
            
            // Ensure it starts with + for international format
            val formattedPhone = if (cleanPhone.startsWith("+")) cleanPhone else "+$cleanPhone"
            
            val uri = Uri.parse("https://wa.me/$formattedPhone")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage("com.whatsapp.w4b")
            }
            context.startActivity(intent)
            Pair(true, "WhatsApp Business abierto")
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(false, "No se pudo abrir WhatsApp Business. ¿Está instalado?")
        }
    }

    /**
     * Checks if WhatsApp is installed on the device.
     */
    fun isWhatsAppInstalled(context: Context): Boolean {
        return try {
            context.packageManager.getApplicationInfo("com.whatsapp", 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Checks if WhatsApp Business is installed on the device.
     */
    fun isWhatsAppBusinessInstalled(context: Context): Boolean {
        return try {
            context.packageManager.getApplicationInfo("com.whatsapp.w4b", 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Gets the privacy explanation text for WhatsApp workflow.
     * Explains to users that:
     * - No automatic scraping occurs
     * - User must manually export and share
     * - Data is stored locally only
     * - No internet permission is used
     */
    fun getPrivacyExplanation(): String {
        return """
            🔒 Privacidad y Seguridad

            Bitacora Pro NO:
            • Accede automáticamente a WhatsApp
            • Lee mensajes sin tu permiso
            • Usa servicios de internet
            • Almacena datos en la nube
            • Requiere permisos especiales

            Cómo funciona:
            1. Exporta el chat desde WhatsApp
            2. Comparte el archivo con Bitacora Pro
            3. Los datos se guardan localmente en tu dispositivo
            4. Puedes editar y clasificar la evidencia

            Todos los datos permanecen en tu dispositivo.
        """.trimIndent()
    }

    /**
     * Gets the help text for importing WhatsApp evidence.
     */
    fun getImportHelpText(): String {
        return """
            📱 Importar Evidencia de WhatsApp

            Pasos:
            1. Abre WhatsApp
            2. Abre el chat que deseas exportar
            3. Toca el menú (⋮) → Más → Exportar chat
            4. Elige "Sin archivos multimedia" (más rápido)
            5. Comparte el archivo con Bitacora Pro
            6. El chat se importará como evidencia de texto

            Consejos:
            • Exporta sin multimedia para archivos más pequeños
            • Puedes exportar múltiples chats
            • Los números de teléfono se extraen automáticamente
            • La evidencia se clasifica automáticamente
        """.trimIndent()
    }

    /**
     * Gets the help text for opening WhatsApp from a job.
     */
    fun getOpenWhatsAppHelpText(): String {
        return """
            💬 Abrir WhatsApp

            Toca el botón para abrir WhatsApp con el número del cliente.
            
            Esto te permite:
            • Enviar mensajes rápidamente
            • Compartir fotos o documentos
            • Hacer seguimiento del trabajo
            • Documentar cambios en tiempo real

            Después de la conversación, puedes exportar el chat
            y agregarlo como evidencia.
        """.trimIndent()
    }

    /**
     * Gets the help text for contact import.
     */
    fun getContactImportHelpText(): String {
        return """
            👥 Importar Contacto

            Toca el botón para seleccionar un contacto de tu dispositivo.
            
            Esto te permite:
            • Llenar automáticamente el nombre del cliente
            • Usar el número de teléfono del contacto
            • Evitar errores de tipeo
            • Mantener consistencia

            Los contactos se importan localmente sin acceso a internet.
        """.trimIndent()
    }

    /**
     * Gets the help text for WhatsApp evidence classification.
     */
    fun getEvidenceClassificationHelpText(): String {
        return """
            🏷️ Clasificación de Evidencia

            Después de importar un chat de WhatsApp, clasifica los mensajes:

            • Antes: Mensajes previos al trabajo
            • Durante: Mensajes mientras se realizaba el trabajo
            • Después: Mensajes posteriores al trabajo
            • Mensaje del Cliente: Comunicación directa del cliente
            • Pago: Información sobre pagos o presupuestos
            • Material: Detalles sobre materiales o suministros

            La clasificación ayuda a organizar la evidencia
            y generar reportes profesionales.
        """.trimIndent()
    }

    /**
     * Gets a formatted message for sharing job details via WhatsApp.
     */
    fun getShareJobMessage(jobTitle: String, clientName: String, serviceType: String): String {
        return """
            Trabajo: $jobTitle
            Cliente: $clientName
            Servicio: $serviceType
            
            Documentado con Bitacora Pro
        """.trimIndent()
    }
}
