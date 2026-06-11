package com.bitacora.pro.assistant

import com.bitacora.pro.data.models.JobFile

/**
 * Local AI assistant provider.
 * Uses built-in rules, fuzzy matching, and local data analysis.
 * No internet required. Works offline.
 * v0.9.0: Improved local assistant with fuzzy matching and better answers
 */
object LocalAssistantProvider {

    /**
     * Calculate Levenshtein distance for fuzzy matching.
     * Lower distance = better match.
     */
    private fun levenshteinDistance(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }
        
        for (i in 0..s1.length) dp[i][0] = i
        for (j in 0..s2.length) dp[0][j] = j
        
        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,      // deletion
                    dp[i][j - 1] + 1,      // insertion
                    dp[i - 1][j - 1] + cost // substitution
                )
            }
        }
        return dp[s1.length][s2.length]
    }

    /**
     * Check if question matches a keyword with fuzzy matching.
     * Returns true if distance is within threshold (3 characters).
     */
    private fun fuzzyMatch(question: String, keyword: String, threshold: Int = 3): Boolean {
        val lowerQuestion = question.lowercase()
        val lowerKeyword = keyword.lowercase()
        
        // Exact substring match (fastest)
        if (lowerQuestion.contains(lowerKeyword)) return true
        
        // Fuzzy match for typos
        val words = lowerQuestion.split(Regex("\\s+"))
        return words.any { word ->
            levenshteinDistance(word, lowerKeyword) <= threshold
        }
    }

    /**
     * Answer a question using local rules, fuzzy matching, and data.
     * Returns a response string or null if unable to answer.
     */
    fun answerQuestion(question: String, job: JobFile? = null): String? {
        val lowerQuestion = question.lowercase()

        // General questions with fuzzy matching
        return when {
            // App usage questions - capture/evidence
            (fuzzyMatch(question, "capturar") || fuzzyMatch(question, "foto") || fuzzyMatch(question, "evidencia")) && 
            (fuzzyMatch(question, "cómo") || fuzzyMatch(question, "como")) ->
                "Para capturar evidencia:\n1. Toca el botón '📸 Capturar' en la pantalla principal\n2. Selecciona una actividad existente o crea una nueva\n3. Toma una foto, importa una imagen, o agrega texto\n4. La evidencia se guardará automáticamente\n\n💡 Tip: Captura fotos antes, durante y después del trabajo para documentar todo"

            // Pending items
            (fuzzyMatch(question, "pendiente") || fuzzyMatch(question, "tarea")) && 
            (fuzzyMatch(question, "cómo") || fuzzyMatch(question, "como") || fuzzyMatch(question, "agregar")) ->
                "Para agregar pendientes:\n1. Abre una actividad\n2. Toca 'Agregar pendiente'\n3. Escribe el título y descripción\n4. Opcionalmente, establece una fecha de vencimiento\n5. Toca 'Guardar'\n\n💡 Tip: Los pendientes te ayudan a organizar tareas dentro de cada actividad"

            // Report generation
            (fuzzyMatch(question, "reporte") || fuzzyMatch(question, "pdf") || fuzzyMatch(question, "exportar")) && 
            (fuzzyMatch(question, "cómo") || fuzzyMatch(question, "como")) ->
                "Para generar un reporte PDF:\n1. Abre una actividad\n2. Toca el botón 'Exportar reporte PDF'\n3. El PDF se guardará en tu dispositivo\n4. Puedes compartirlo por correo, WhatsApp o cualquier app\n\n💡 Tip: Los reportes incluyen toda la evidencia y pendientes de la actividad"

            // Contact import
            (fuzzyMatch(question, "contacto") || fuzzyMatch(question, "cliente")) && 
            (fuzzyMatch(question, "cómo") || fuzzyMatch(question, "como") || fuzzyMatch(question, "importar")) ->
                "Para importar un contacto:\n1. Toca '+ Nueva actividad'\n2. Toca el botón de contacto (ícono de persona)\n3. Selecciona un contacto de tu teléfono\n4. El nombre y teléfono se completarán automáticamente\n\n💡 Tip: Esto te ahorra tiempo al crear actividades para clientes frecuentes"

            // Privacy questions
            fuzzyMatch(question, "privacidad") || fuzzyMatch(question, "datos") || fuzzyMatch(question, "seguridad") ->
                "Bitacora Pro es una aplicación local-first:\n• ✓ Todos tus datos se guardan en tu dispositivo\n• ✓ No se envían a servidores externos sin tu consentimiento\n• ✓ Puedes usar la app completamente sin internet\n• ✓ Tu privacidad es importante para nosotros\n\n💡 Tip: Puedes revisar la sección 'Acerca de' para más detalles sobre permisos"

            // Activity management - completion
            (fuzzyMatch(question, "completar") || fuzzyMatch(question, "terminar") || fuzzyMatch(question, "finalizar")) && 
            (fuzzyMatch(question, "actividad") || fuzzyMatch(question, "trabajo")) ->
                "Para marcar una actividad como completada:\n1. Abre la actividad\n2. Toca el botón 'Completar actividad'\n3. La actividad se moverá a la sección 'Completadas'\n4. Puedes archivarla después si lo deseas\n\n💡 Tip: Las actividades completadas se pueden archivar para mantener la lista limpia"

            // Archiving
            fuzzyMatch(question, "archivar") || fuzzyMatch(question, "archivo") ->
                "Para archivar una actividad:\n1. Abre la actividad completada\n2. Toca el botón 'Archivar actividad'\n3. La actividad se moverá a la sección 'Archivadas'\n4. Las actividades archivadas no aparecen en la vista principal\n\n💡 Tip: Archiva actividades completadas para mantener tu dashboard limpio"

            // Evidence categories
            (fuzzyMatch(question, "evidencia") || fuzzyMatch(question, "foto")) && 
            (fuzzyMatch(question, "categoría") || fuzzyMatch(question, "categoria") || fuzzyMatch(question, "tipo")) ->
                "Las categorías de evidencia son:\n• 📷 Antes: Fotos o documentos previos al trabajo\n• 📷 Durante: Fotos o documentos durante el trabajo\n• 📷 Después: Fotos o documentos después del trabajo\n• 💰 Pago: Recibos o comprobantes de pago\n• 🛠️ Material: Fotos de materiales utilizados\n• 💬 Mensaje del cliente: Chats o mensajes del cliente\n• ❓ Sin clasificar: Evidencia sin categoría asignada\n\n💡 Tip: Categorizar evidencia te ayuda a encontrarla rápidamente"

            // Unassigned/Inbox
            fuzzyMatch(question, "sin asignar") || fuzzyMatch(question, "inbox") || fuzzyMatch(question, "unassigned") ->
                "La sección 'Sin asignar' es un área temporal para:\n• 📸 Fotos capturadas sin actividad específica\n• 💬 Chats importados sin destino\n• 📄 Documentos que aún no están asociados\n\nPuedes mover estos elementos a una actividad existente o crear una nueva.\n\n💡 Tip: Revisa regularmente 'Sin asignar' para mantener todo organizado"

            // Pending items - completion
            (fuzzyMatch(question, "pendiente") || fuzzyMatch(question, "tarea")) && 
            (fuzzyMatch(question, "completar") || fuzzyMatch(question, "marcar")) ->
                "Los pendientes son tareas dentro de una actividad:\n• ✓ Completar un pendiente no completa la actividad\n• ✓ Tú decides cuándo marcar la actividad como completada\n• ✓ Los pendientes ayudan a organizar el trabajo dentro de cada actividad\n\n💡 Tip: Usa pendientes para desglosar trabajos grandes en tareas pequeñas"

            // WhatsApp
            fuzzyMatch(question, "whatsapp") || fuzzyMatch(question, "chat") ->
                "Bitacora Pro no lee WhatsApp automáticamente:\n• ✓ Solo importa chats que compartas manualmente\n• ✓ Puedes exportar un chat de WhatsApp como archivo .txt\n• ✓ Luego importarlo en Bitacora Pro\n• ✓ Esto te da control total sobre qué datos compartir\n\n💡 Tip: En WhatsApp, abre el chat → Más → Exportar chat → Sin multimedia"

            // General help
            fuzzyMatch(question, "ayuda") || fuzzyMatch(question, "help") || fuzzyMatch(question, "qué") || fuzzyMatch(question, "que") ->
                "Bitacora Pro te ayuda a:\n• 📋 Organizar actividades y trabajos\n• 📸 Capturar evidencia (fotos, documentos, notas)\n• ✓ Gestionar pendientes y recordatorios\n• 📄 Generar reportes profesionales en PDF\n• 🔒 Mantener todo privado en tu dispositivo\n\n¿Hay algo específico que necesites? Pregunta sobre:\n• Capturar evidencia\n• Agregar pendientes\n• Generar reportes\n• Privacidad y seguridad"

            // Default response
            else -> null
        }
    }

    /**
     * Generate suggestions for an activity.
     */
    fun generateActivitySuggestions(job: JobFile): List<String> {
        val suggestions = mutableListOf<String>()

        // Suggest adding evidence if missing
        if (job.evidence.isEmpty()) {
            suggestions.add("📸 Considera agregar evidencia (fotos, documentos) a esta actividad")
        }

        // Suggest adding pending items if missing
        if (job.agendaItems.isEmpty()) {
            suggestions.add("✓ Agrega pendientes para organizar las tareas de esta actividad")
        }

        // Suggest completing if old
        val daysSinceCreation = (System.currentTimeMillis() - job.createdAt) / (1000 * 60 * 60 * 24)
        if (daysSinceCreation > 7 && job.status.name == "ACTIVE") {
            suggestions.add("⏰ Esta actividad tiene más de una semana. ¿Está completada?")
        }

        // Suggest generating report if has evidence
        if (job.evidence.isNotEmpty()) {
            suggestions.add("📄 Puedes generar un reporte PDF con toda la evidencia")
        }

        return suggestions
    }

    /**
     * Analyze evidence patterns in an activity.
     */
    fun analyzeEvidencePatterns(job: JobFile): String {
        val evidenceCount = job.evidence.size
        val categories = job.evidence.groupingBy { it.category }.eachCount()

        return buildString {
            append("Análisis de evidencia:\n")
            append("• Total: $evidenceCount elementos\n")
            if (categories.isNotEmpty()) {
                append("• Categorías: ${categories.keys.joinToString(", ")}\n")
            }
            if (evidenceCount == 0) {
                append("• Sin evidencia aún. Considera capturar fotos o documentos.\n")
            }
        }
    }
}
