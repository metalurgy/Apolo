package com.bitacora.pro.ui.screens

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bitacora.pro.assistant.AgendaSuggestionEngine
import com.bitacora.pro.assistant.AssistantResult
import com.bitacora.pro.assistant.JobAssistantEngine
import com.bitacora.pro.data.models.AgendaItem
import com.bitacora.pro.data.models.AgendaStatus
import com.bitacora.pro.data.models.EvidenceCategory
import com.bitacora.pro.data.models.EvidenceItem
import com.bitacora.pro.data.models.EvidenceType
import com.bitacora.pro.data.models.JobFile
import com.bitacora.pro.data.models.JobStatus
import com.bitacora.pro.data.models.getSpanishLabel
import com.bitacora.pro.data.storage.StorageManager
import com.bitacora.pro.notifications.AgendaNotificationScheduler
import com.bitacora.pro.reports.JobPdfReportGenerator
import com.bitacora.pro.whatsapp.WhatsAppUIHelper
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * JobDetailScreen displays job metadata and evidence items.
 * Users can change evidence categories and delete evidence.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobDetailScreen(
    jobId: String,
    storageManager: StorageManager,
    onBack: () -> Unit
) {
    val job = remember { mutableStateOf<JobFile?>(null) }
    val generatedReportFile = remember { mutableStateOf<File?>(null) }
    val isGeneratingReport = remember { mutableStateOf(false) }
    val reportErrorMessage = remember { mutableStateOf("") }
    val assistantResult = remember { mutableStateOf<AssistantResult?>(null) }
    val isAnalyzingJob = remember { mutableStateOf(false) }
    val assistantErrorMessage = remember { mutableStateOf("") }
    val showArchiveConfirm = remember { mutableStateOf(false) }
    val showDeleteConfirm = remember { mutableStateOf(false) }
    val deleteConfirmText = remember { mutableStateOf("") }
    val context = LocalContext.current
    val cameraErrorMessage = remember { mutableStateOf("") }
    val pendingCameraFile = remember { mutableStateOf<File?>(null) }

    // Camera capture launcher using ActivityResultContracts.TakePicture()
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && pendingCameraFile.value != null) {
            // Photo was taken successfully - save it as evidence
            val tempFile = pendingCameraFile.value!!
            val evidence = storageManager.saveCameraPhotoAsEvidence(jobId, tempFile)
            if (evidence != null) {
                // Add evidence to job
                storageManager.addEvidenceToJob(jobId, evidence)
                // Reload job to show new evidence
                job.value = storageManager.loadJob(jobId)
                cameraErrorMessage.value = "" // Clear any previous errors
            } else {
                cameraErrorMessage.value = "Error al guardar la foto"
                storageManager.cleanupTemporaryCameraFiles()
            }
            pendingCameraFile.value = null
        } else {
            // User cancelled or camera failed
            cameraErrorMessage.value = "Captura de foto cancelada"
            storageManager.cleanupTemporaryCameraFiles()
            pendingCameraFile.value = null
        }
    }

    // Load job on screen composition
    LaunchedEffect(jobId) {
        job.value = storageManager.loadJob(jobId)
    }

    if (showArchiveConfirm.value && job.value != null) {
        AlertDialog(
            onDismissRequest = { showArchiveConfirm.value = false },
            title = { Text("Archivar Actividad") },
            text = { Text("¿Estás seguro de que deseas archivar esta actividad? Se cancelarán todos los recordatorios programados.") },
            confirmButton = {
                Button(
                    onClick = {
                        storageManager.archiveJob(jobId)
                        showArchiveConfirm.value = false
                        onBack()
                    }
                ) {
                    Text("Archivar")
                }
            },
            dismissButton = {
                Button(onClick = { showArchiveConfirm.value = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Delete confirmation dialog (v0.9.0)
    if (showDeleteConfirm.value && job.value != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm.value = false },
            title = { Text("🗑️ Eliminar Actividad") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Esta acción eliminará PERMANENTEMENTE esta actividad, toda su evidencia y pendientes.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "Para confirmar, escribe: ${job.value!!.title}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                    TextField(
                        value = deleteConfirmText.value,
                        onValueChange = { deleteConfirmText.value = it },
                        placeholder = { Text("Escribe aquí") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (deleteConfirmText.value == job.value!!.title) {
                            storageManager.deleteJob(jobId)
                            showDeleteConfirm.value = false
                            deleteConfirmText.value = ""
                            onBack()
                        }
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    enabled = deleteConfirmText.value == job.value!!.title
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                Button(onClick = {
                    showDeleteConfirm.value = false
                    deleteConfirmText.value = ""
                }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalles de la Actividad") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    if (job.value != null) {
                        // Delete button
                        IconButton(onClick = { showDeleteConfirm.value = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Eliminar actividad")
                        }
                        // Archive button (only for non-archived activities)
                        if (job.value!!.status != JobStatus.ARCHIVED) {
                            IconButton(onClick = { showArchiveConfirm.value = true }) {
                                Icon(Icons.Filled.Edit, contentDescription = "Archivar actividad")
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        if (job.value != null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Job metadata section
                item {
                    JobMetadataCard(job.value!!)
                }

                // Activity status explanation section
                item {
                    ActivityStatusExplanationCard(job.value!!.status)
                }

                // Report notes section
                item {
                    val scope = androidx.compose.runtime.rememberCoroutineScope()
                    ReportNotesSection(
                        job = job.value!!,
                        jobId = jobId,
                        storageManager = storageManager,
                        onReportNotesUpdated = {
                            job.value = storageManager.loadJob(jobId)
                        }
                    )
                }

                // PDF Report section
                item {
                    val scope = androidx.compose.runtime.rememberCoroutineScope()
                    PdfReportSection(
                        jobId = jobId,
                        job = job.value!!,
                        storageManager = storageManager,
                        isGenerating = isGeneratingReport.value,
                        generatedFile = generatedReportFile.value,
                        errorMessage = reportErrorMessage.value,
                        onGenerateReport = {
                            isGeneratingReport.value = true
                            reportErrorMessage.value = ""
                            scope.launch {
                                try {
                                    val reportFile = withContext(Dispatchers.Default) {
                                        JobPdfReportGenerator.generateReport(context, job.value!!, storageManager)
                                    }
                                    generatedReportFile.value = reportFile
                                    if (reportFile == null) {
                                        reportErrorMessage.value = "Error al generar el reporte"
                                    }
                                } catch (e: Exception) {
                                    reportErrorMessage.value = "Error: ${e.message}"
                                    e.printStackTrace()
                                } finally {
                                    isGeneratingReport.value = false
                                }
                            }
                        },
                        onOpenReport = { file ->
                            val uri = storageManager.getReportFileUri(jobId, file.name)
                            if (uri != null) {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(uri, "application/pdf")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        },
                        onShareReport = { file ->
                            val uri = storageManager.getReportFileUri(jobId, file.name)
                            if (uri != null) {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/pdf"
                                    putExtra(Intent.EXTRA_STREAM, uri as android.os.Parcelable)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                try {
                                    context.startActivity(Intent.createChooser(intent, "Compartir Reporte"))
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    )
                }

                // Agenda section
                item {
                    AgendaSection(
                        job = job.value!!,
                        jobId = jobId,
                        storageManager = storageManager,
                        onAgendaUpdated = {
                            job.value = storageManager.loadJob(jobId)
                        }
                    )
                }

                // Assistant section (v0.7.3 - compact action-based)
                item {
                    val scope = androidx.compose.runtime.rememberCoroutineScope()
                    com.bitacora.pro.ui.screens.AssistantSection(
                        isAnalyzing = isAnalyzingJob.value,
                        result = assistantResult.value,
                        errorMessage = assistantErrorMessage.value,
                        onAnalyze = {
                            isAnalyzingJob.value = true
                            assistantErrorMessage.value = ""
                            scope.launch {
                                try {
                                    val result = withContext(Dispatchers.Default) {
                                        JobAssistantEngine.analyzeJob(job.value!!)
                                    }
                                    assistantResult.value = result
                                } catch (e: Exception) {
                                    assistantErrorMessage.value = "Error al analizar: ${e.message}"
                                    e.printStackTrace()
                                } finally {
                                    isAnalyzingJob.value = false
                                }
                            }
                        },
                        onAddTaskFromAction = { actionText ->
                            // Extract task title from action text (remove emoji and prefix)
                            val taskTitle = actionText
                                .replace(Regex("^[🔴📋📸💰✏️📦📄]\\s*"), "")
                                .replace(Regex(":\\s*\\d+\\s*"), ": ")
                            
                            // Check if task already exists
                            val existingTask = job.value!!.agendaItems.find {
                                it.title.equals(taskTitle, ignoreCase = true) &&
                                it.status != AgendaStatus.ARCHIVED
                            }
                            
                            if (existingTask == null) {
                                val newAgendaItem = AgendaItem(
                                    jobId = jobId,
                                    title = taskTitle,
                                    description = "Sugerido por el asistente",
                                    status = AgendaStatus.PENDING
                                )
                                storageManager.addAgendaItemToJob(jobId, newAgendaItem)
                                job.value = storageManager.loadJob(jobId)
                            }
                        }
                    )
                }

                // Evidence section
                if (job.value!!.evidence.isNotEmpty()) {
                    item {
                        val showEvidenceHelp = remember { mutableStateOf(false) }
                        
                        if (showEvidenceHelp.value) {
                            AlertDialog(
                                onDismissRequest = { showEvidenceHelp.value = false },
                                title = { Text("Clasificación de Evidencia") },
                                text = { Text(WhatsAppUIHelper.getEvidenceClassificationHelpText()) },
                                confirmButton = {
                                    Button(onClick = { showEvidenceHelp.value = false }) {
                                        Text("Entendido")
                                    }
                                }
                            )
                        }

                        // Show camera error message if any
                        if (cameraErrorMessage.value.isNotEmpty()) {
                            AlertDialog(
                                onDismissRequest = { cameraErrorMessage.value = "" },
                                title = { Text("Información") },
                                text = { Text(cameraErrorMessage.value) },
                                confirmButton = {
                                    Button(onClick = { cameraErrorMessage.value = "" }) {
                                        Text("Entendido")
                                    }
                                }
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Evidencia (${job.value!!.evidence.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Button(
                                    onClick = { showEvidenceHelp.value = true },
                                    modifier = Modifier
                                        .height(36.dp)
                                        .padding(0.dp)
                                ) {
                                    Text("ℹ️ Categorías")
                                }
                                Button(
                                    onClick = {
                                        // Launch camera to capture photo
                                        val (tempFile, uri) = storageManager.createTemporaryCameraFile()
                                        pendingCameraFile.value = tempFile
                                        takePictureLauncher.launch(uri)
                                    },
                                    modifier = Modifier
                                        .height(36.dp)
                                        .padding(0.dp)
                                ) {
                                    Text("📸 Tomar foto")
                                }
                            }
                        }
                    }

                    // Group evidence by category
                    val groupedEvidence = job.value!!.evidence.groupBy { it.category }
                    items(groupedEvidence.toList()) { (category, evidenceList) ->
                        Column {
                            Text(
                                category.getSpanishLabel(),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                            evidenceList.forEach { evidence ->
                                EvidenceCard(
                                    evidence = evidence,
                                    jobId = jobId,
                                    storageManager = storageManager,
                                    onCategoryChanged = { newCategory ->
                                        storageManager.updateEvidenceCategory(jobId, evidence.id, newCategory)
                                        job.value = storageManager.loadJob(jobId)
                                    },
                                    onDelete = {
                                        storageManager.deleteEvidence(jobId, evidence.id)
                                        job.value = storageManager.loadJob(jobId)
                                    },
                                    onSuggestAgenda = { evidenceId ->
                                        job.value = storageManager.loadJob(jobId)
                                    }
                                )
                            }
                        }
                    }
                } else {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Sin evidencia aún",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    // Launch camera to capture photo
                                    val (tempFile, uri) = storageManager.createTemporaryCameraFile()
                                    pendingCameraFile.value = tempFile
                                    takePictureLauncher.launch(uri)
                                }
                            ) {
                                Text("📸 Tomar foto")
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Displays job metadata in a card.
 */
@Composable
private fun JobMetadataCard(job: JobFile) {
    val context = LocalContext.current
    val showWhatsAppHelp = remember { mutableStateOf(false) }
    val whatsAppError = remember { mutableStateOf("") }

    if (showWhatsAppHelp.value) {
        AlertDialog(
            onDismissRequest = { showWhatsAppHelp.value = false },
            title = { Text("Abrir WhatsApp") },
            text = { Text(WhatsAppUIHelper.getOpenWhatsAppHelpText()) },
            confirmButton = {
                Button(onClick = { showWhatsAppHelp.value = false }) {
                    Text("Entendido")
                }
            }
        )
    }

    if (whatsAppError.value.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { whatsAppError.value = "" },
            title = { Text("Información") },
            text = { Text(whatsAppError.value) },
            confirmButton = {
                Button(onClick = { whatsAppError.value = "" }) {
                    Text("Entendido")
                }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = job.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            MetadataRow("Cliente:", job.clientName)
            MetadataRow("Teléfono:", job.phone)
            MetadataRow("Servicio:", job.serviceType)
            MetadataRow("Estado:", job.status.name)
            MetadataRow("Creado:", formatDate(job.createdAt))
            MetadataRow("Actualizado:", formatDate(job.updatedAt))
            
            // WhatsApp buttons (v0.7.3 - with phone validation and error handling)
            if (job.phone.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (WhatsAppUIHelper.isWhatsAppInstalled(context)) {
                        Button(
                            onClick = {
                                val (success, message) = WhatsAppUIHelper.openWhatsApp(context, job.phone)
                                if (!success) {
                                    whatsAppError.value = message
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                        ) {
                            Text("💬 WhatsApp")
                        }
                    }
                    if (WhatsAppUIHelper.isWhatsAppBusinessInstalled(context)) {
                        Button(
                            onClick = {
                                val (success, message) = WhatsAppUIHelper.openWhatsAppBusiness(context, job.phone)
                                if (!success) {
                                    whatsAppError.value = message
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                        ) {
                            Text("💼 Business")
                        }
                    }
                    Button(
                        onClick = { showWhatsAppHelp.value = true },
                        modifier = Modifier
                            .height(40.dp)
                            .padding(0.dp)
                    ) {
                        Text("?")
                    }
                }
            }
        }
    }
}

/**
 * Activity Status Explanation Card - v0.8.2
 * Clarifies the meaning of each activity status and relationship to pending items
 */
@Composable
private fun ActivityStatusExplanationCard(status: JobStatus) {
    val (icon, title, description, note) = when (status) {
        JobStatus.ACTIVE -> {
            val desc = "Esta actividad está en progreso. Puedes agregar evidencia, tareas y notas."
            val n = "Nota: Completar todas las tareas pendientes NO marca automáticamente la actividad como completada. Debes hacerlo manualmente."
            Quadruple(
                "🟢",
                "Activo",
                desc,
                n
            )
        }
        JobStatus.COMPLETED -> {
            val desc = "Esta actividad ha sido finalizada. Puedes revisar la evidencia y generar reportes."
            val n = "Nota: Las tareas pendientes no se eliminan. Puedes seguir agregando evidencia si es necesario."
            Quadruple(
                "✅",
                "Completado",
                desc,
                n
            )
        }
        JobStatus.ARCHIVED -> {
            val desc = "Esta actividad está archivada. No puedes hacer cambios, pero puedes revisar el historial."
            val n = "Nota: Archiva cuando ya no necesites trabajar en esta actividad."
            Quadruple(
                "📦",
                "Archivado",
                desc,
                n
            )
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(icon, style = MaterialTheme.typography.headlineSmall)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = note,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }
        }
    }
}

/**
 * Helper data class for status explanation
 */
private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

/**
 * Displays a metadata row with label and value.
 */
@Composable
private fun MetadataRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

/**
 * Displays the Agenda section with pending/done items and manual creation UI.
 */
@Composable
private fun AgendaSection(
    job: JobFile,
    jobId: String,
    storageManager: StorageManager,
    onAgendaUpdated: () -> Unit
) {
    val showAddForm = remember { mutableStateOf(false) }
    val showDatePicker = remember { mutableStateOf(false) }
    val showArchivedItems = remember { mutableStateOf(false) }
    val newTitle = remember { mutableStateOf("") }
    val newDescription = remember { mutableStateOf("") }
    val newDueText = remember { mutableStateOf("") }
    val newDueAt = remember { mutableStateOf<Long?>(null) }
    val newReminderEnabled = remember { mutableStateOf(false) }
    val newReminderOffsetDays = remember { mutableStateOf(0) }
    val context = LocalContext.current

    if (showDatePicker.value) {
        DatePickerDialog(
            onDateSelected = { timestamp, dateText ->
                newDueAt.value = timestamp
                newDueText.value = dateText
                showDatePicker.value = false
            },
            onDismiss = { showDatePicker.value = false }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Agenda (${job.agendaItems.filter { it.status != AgendaStatus.ARCHIVED }.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = { showAddForm.value = !showAddForm.value },
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(if (showAddForm.value) "Cancelar" else "+ Agregar")
                }
            }

            if (showAddForm.value) {
                Spacer(modifier = Modifier.height(12.dp))
                TextField(
                    value = newTitle.value,
                    onValueChange = { newTitle.value = it },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = newDescription.value,
                    onValueChange = { newDescription.value = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = newDueText.value,
                        onValueChange = { newDueText.value = it },
                        label = { Text("Fecha de vencimiento") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Button(
                        onClick = { showDatePicker.value = true },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("📅")
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                // Reminder section
                ReminderDropdown(
                    reminderEnabled = newReminderEnabled.value,
                    reminderOffsetDays = newReminderOffsetDays.value,
                    onReminderEnabledChange = { newReminderEnabled.value = it },
                    onReminderOffsetChange = { newReminderOffsetDays.value = it }
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        if (newTitle.value.isNotBlank()) {
                            var agendaItem = AgendaItem(
                                jobId = jobId,
                                title = newTitle.value,
                                description = newDescription.value,
                                dueText = newDueText.value,
                                dueAt = newDueAt.value,
                                status = AgendaStatus.PENDING,
                                reminderEnabled = newReminderEnabled.value,
                                reminderOffsetDays = newReminderOffsetDays.value
                            )
                            // Schedule reminder if enabled
                            if (newReminderEnabled.value && newDueAt.value != null) {
                                val scheduler = AgendaNotificationScheduler(context)
                                agendaItem = scheduler.scheduleReminder(agendaItem)
                            }
                            storageManager.addAgendaItemToJob(jobId, agendaItem)
                            newTitle.value = ""
                            newDescription.value = ""
                            newDueText.value = ""
                            newDueAt.value = null
                            newReminderEnabled.value = false
                            newReminderOffsetDays.value = 0
                            showAddForm.value = false
                            onAgendaUpdated()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Agregar Elemento")
                }
            }

            if (job.agendaItems.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                // Separate pending, done, and archived items
                val pendingItems = job.agendaItems.filter { it.status == AgendaStatus.PENDING }
                val doneItems = job.agendaItems.filter { it.status == AgendaStatus.DONE }
                val archivedItems = job.agendaItems.filter { it.status == AgendaStatus.ARCHIVED }

                // Show pending items first
                if (pendingItems.isNotEmpty()) {
                    Text(
                        "Pendiente",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                    pendingItems.forEach { item ->
                        AgendaItemCard(
                            item = item,
                            jobId = jobId,
                            storageManager = storageManager,
                            onStatusChanged = { onAgendaUpdated() },
                            onDelete = { onAgendaUpdated() }
                        )
                    }
                }

                // Show done items
                if (doneItems.isNotEmpty()) {
                    Text(
                        "Completado",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                    doneItems.forEach { item ->
                        AgendaItemCard(
                            item = item,
                            jobId = jobId,
                            storageManager = storageManager,
                            onStatusChanged = { onAgendaUpdated() },
                            onDelete = { onAgendaUpdated() }
                        )
                    }
                }

                // Show archived items in collapsible section
                if (archivedItems.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { showArchivedItems.value = !showArchivedItems.value },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("${if (showArchivedItems.value) "▼" else "▶"} Tareas Archivadas (${archivedItems.size})")
                    }
                    if (showArchivedItems.value) {
                        archivedItems.forEach { item ->
                            AgendaItemCard(
                                item = item,
                                jobId = jobId,
                                storageManager = storageManager,
                                onStatusChanged = { onAgendaUpdated() },
                                onDelete = { onAgendaUpdated() }
                            )
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Sin elementos de agenda aún",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Displays a single agenda item with status toggle, edit button, and delete button.
 */
@Composable
private fun AgendaItemCard(
    item: AgendaItem,
    jobId: String,
    storageManager: StorageManager,
    onStatusChanged: () -> Unit,
    onDelete: () -> Unit
) {
    val showDeleteConfirm = remember { mutableStateOf(false) }
    val showArchiveConfirm = remember { mutableStateOf(false) }
    val showEditDialog = remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (showEditDialog.value) {
        EditAgendaDialog(
            item = item,
            onSave = { updatedItem ->
                val scheduler = AgendaNotificationScheduler(context)
                storageManager.updateAgendaItem(jobId, updatedItem, scheduler)
                showEditDialog.value = false
                onStatusChanged()
            },
            onDismiss = { showEditDialog.value = false }
        )
    }

    if (showDeleteConfirm.value) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm.value = false },
            title = { Text("Eliminar Elemento") },
            text = { Text("¿Estás seguro de que deseas eliminar este elemento de agenda?") },
            confirmButton = {
                Button(
                    onClick = {
                        storageManager.deleteAgendaItem(jobId, item.id)
                        showDeleteConfirm.value = false
                        onDelete()
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteConfirm.value = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showArchiveConfirm.value) {
        AlertDialog(
            onDismissRequest = { showArchiveConfirm.value = false },
            title = { Text("Archivar Elemento") },
            text = { Text("¿Deseas archivar este elemento de agenda completado?") },
            confirmButton = {
                Button(
                    onClick = {
                        storageManager.archiveAgendaItem(jobId, item.id)
                        showArchiveConfirm.value = false
                        onStatusChanged()
                    }
                ) {
                    Text("Archivar")
                }
            },
            dismissButton = {
                Button(onClick = { showArchiveConfirm.value = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (item.dueText.isNotEmpty()) {
                        Text(
                            text = "Vencimiento: ${item.dueText}",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    // Display reminder information if enabled
                    if (item.reminderEnabled) {
                        Text(
                            text = "🔔 Recordatorio: " + when (item.reminderOffsetDays) {
                                0 -> "El día del vencimiento"
                                1 -> "1 día antes"
                                2 -> "2 días antes"
                                3 -> "3 días antes"
                                7 -> "1 semana antes"
                                else -> "${item.reminderOffsetDays} días antes"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Row {
                    // Show different buttons based on status
                    if (item.status == AgendaStatus.PENDING) {
                        Button(
                            onClick = {
                                storageManager.updateAgendaItemStatus(jobId, item.id, "DONE")
                                onStatusChanged()
                            },
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Text("Completar")
                        }
                    } else if (item.status == AgendaStatus.DONE) {
                        Button(
                            onClick = { showArchiveConfirm.value = true },
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Text("Archivar")
                        }
                    } else if (item.status == AgendaStatus.ARCHIVED) {
                        Button(
                            onClick = {
                                storageManager.updateAgendaItemStatus(jobId, item.id, "DONE")
                                onStatusChanged()
                            },
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Text("Restaurar")
                        }
                    }
                    // Edit button (enabled for PENDING and DONE, disabled for ARCHIVED)
                    if (item.status != AgendaStatus.ARCHIVED) {
                        IconButton(
                            onClick = { showEditDialog.value = true },
                            modifier = Modifier.padding(0.dp)
                        ) {
                            Icon(Icons.Filled.Edit, contentDescription = "Editar elemento de agenda")
                        }
                    }
                    IconButton(
                        onClick = { showDeleteConfirm.value = true },
                        modifier = Modifier.padding(0.dp)
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = "Eliminar elemento de agenda")
                    }
                }
            }
            if (item.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.description.take(100) + if (item.description.length > 100) "..." else "",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

/**
 * Displays a single evidence item with category selector, delete button, and suggestion button.
 */
@Composable
private fun EvidenceCard(
    evidence: EvidenceItem,
    jobId: String,
    storageManager: StorageManager,
    onCategoryChanged: (String) -> Unit,
    onDelete: () -> Unit,
    onSuggestAgenda: (String) -> Unit
) {
    val expandedCategory = remember { mutableStateOf(false) }
    val showSuggestions = remember { mutableStateOf(false) }
    val suggestions = remember { mutableStateOf<List<AgendaItem>>(emptyList()) }
    val showDeleteConfirm = remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (showDeleteConfirm.value) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm.value = false },
            title = { Text("Eliminar Evidencia") },
            text = { Text("¿Estás seguro de que deseas eliminar esta evidencia?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm.value = false
                        onDelete()
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteConfirm.value = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = evidence.fileName.ifEmpty { evidence.type.getSpanishLabel() },
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = evidence.type.getSpanishLabel(),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                IconButton(onClick = { showDeleteConfirm.value = true }, modifier = Modifier.padding(0.dp)) {
                    Icon(Icons.Filled.Delete, contentDescription = "Eliminar evidencia")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Image thumbnail for IMAGE evidence
            if (evidence.type == EvidenceType.IMAGE) {
                ImageThumbnail(
                    jobId = jobId,
                    evidence = evidence,
                    storageManager = storageManager,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .padding(bottom = 8.dp)
                )
            }

            // Category selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Categoría:",
                    style = MaterialTheme.typography.labelSmall
                )
                Button(
                    onClick = { expandedCategory.value = true },
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(evidence.category.getSpanishLabel())
                }
                DropdownMenu(
                    expanded = expandedCategory.value,
                    onDismissRequest = { expandedCategory.value = false }
                ) {
                    EvidenceCategory.values().forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.getSpanishLabel()) },
                            onClick = {
                                onCategoryChanged(category.name)
                                expandedCategory.value = false
                            }
                        )
                    }
                }
            }

            // Open button for file-based evidence (IMAGE, PDF, AUDIO)
            if (evidence.type in listOf(EvidenceType.IMAGE, EvidenceType.PDF, EvidenceType.AUDIO)) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        val fileUri = storageManager.getEvidenceFileUri(jobId, evidence.id)
                        if (fileUri != null) {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(fileUri, evidence.mimeType)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.OpenInNew, contentDescription = "Abrir", modifier = Modifier.padding(end = 4.dp))
                    Text("Abrir")
                }
            }

            // Text content preview and suggestion button
            if (evidence.textContent.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = evidence.textContent.take(200) + if (evidence.textContent.length > 200) "..." else "",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        suggestions.value = AgendaSuggestionEngine.suggestFromText(
                            jobId = jobId,
                            evidenceId = evidence.id,
                            text = evidence.textContent
                        )
                        showSuggestions.value = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Sugerir Agenda")
                }

                // Show suggestions if available
                if (showSuggestions.value && suggestions.value.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Text(
                            "Sugerencias:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        suggestions.value.forEach { suggestion ->
                            SuggestionCard(
                                suggestion = suggestion,
                                jobId = jobId,
                                storageManager = storageManager,
                                onAdd = {
                                    storageManager.addAgendaItemToJob(jobId, suggestion)
                                    showSuggestions.value = false
                                    suggestions.value = emptyList()
                                    onSuggestAgenda(evidence.id)
                                }
                            )
                        }
                        Button(
                            onClick = { showSuggestions.value = false },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Text("Cerrar")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatDate(evidence.createdAt),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

/**
 * Displays an image thumbnail for IMAGE evidence.
 */
@Composable
private fun ImageThumbnail(
    jobId: String,
    evidence: EvidenceItem,
    storageManager: StorageManager,
    modifier: Modifier = Modifier
) {
    val bitmap = remember { mutableStateOf<android.graphics.Bitmap?>(null) }

    LaunchedEffect(evidence.id) {
        val file = storageManager.getEvidenceFile(jobId, evidence.id)
        if (file != null && file.exists()) {
            try {
                bitmap.value = BitmapFactory.decodeFile(file.absolutePath)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    if (bitmap.value != null) {
        Card(
            modifier = modifier,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Image(
                bitmap = bitmap.value!!.asImageBitmap(),
                contentDescription = "Miniatura de evidencia",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    } else {
        Card(
            modifier = modifier,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Cargando imagen...",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

/**
 * Displays a single suggestion card with preview and add button.
 */
@Composable
private fun SuggestionCard(
    suggestion: AgendaItem,
    jobId: String,
    storageManager: StorageManager,
    onAdd: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = suggestion.title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )
            if (suggestion.dueText.isNotEmpty()) {
                Text(
                    text = "Vencimiento: ${suggestion.dueText}",
                    style = MaterialTheme.typography.labelSmall
                )
            }
            if (suggestion.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = suggestion.description.take(80) + if (suggestion.description.length > 80) "..." else "",
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onAdd,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Agregar")
            }
        }
    }
}

/**
 * Formats a timestamp to a readable date string.
 */
private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

/**
 * Displays PDF report generation section with open/share/save actions.
 */
@Composable
private fun PdfReportSection(
    jobId: String,
    job: JobFile,
    storageManager: StorageManager,
    isGenerating: Boolean,
    generatedFile: File?,
    errorMessage: String = "",
    onGenerateReport: () -> Unit,
    onOpenReport: (File) -> Unit,
    onShareReport: (File) -> Unit
) {
    val context = LocalContext.current
    val saveMessage = remember { mutableStateOf("") }
    
    val saveFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        if (uri != null && generatedFile != null) {
            try {
                generatedFile.inputStream().use { input ->
                    context.contentResolver.openOutputStream(uri)?.use { output ->
                        input.copyTo(output)
                        saveMessage.value = "Archivo guardado exitosamente"
                    }
                }
            } catch (e: Exception) {
                saveMessage.value = "Error al guardar: ${e.message}"
                e.printStackTrace()
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                "Reporte PDF",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Show error message if generation failed
            if (errorMessage.isNotEmpty()) {
                Text(
                    errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            // Show save message feedback
            if (saveMessage.value.isNotEmpty()) {
                Text(
                    saveMessage.value,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (saveMessage.value.contains("exitosamente"))
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            if (generatedFile != null) {
                Text(
                    "Reporte generado: ${generatedFile.name}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onOpenReport(generatedFile) },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                    ) {
                        Text("Abrir")
                    }
                    Button(
                        onClick = { onShareReport(generatedFile) },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                    ) {
                        Text("Compartir")
                    }
                    Button(
                        onClick = {
                            val fileName = generatedFile.name
                            saveFileLauncher.launch(fileName)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                    ) {
                        Text("Guardar")
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Button(
                onClick = onGenerateReport,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = !isGenerating
            ) {
                Text(if (isGenerating) "Generando..." else "Generar Reporte PDF")
            }
        }
    }
}

/**
 * Displays the Report Notes section where users can add/edit notes for the PDF report.
 */
@Composable
private fun ReportNotesSection(
    job: JobFile,
    jobId: String,
    storageManager: StorageManager,
    onReportNotesUpdated: () -> Unit
) {
    val showEditForm = remember { mutableStateOf(false) }
    val reportNotesText = remember { mutableStateOf(job.reportNotes) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Notas del Reporte",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = { showEditForm.value = !showEditForm.value },
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(if (showEditForm.value) "Cancelar" else "✏️ Editar")
                }
            }

            if (showEditForm.value) {
                Spacer(modifier = Modifier.height(12.dp))
                TextField(
                    value = reportNotesText.value,
                    onValueChange = { reportNotesText.value = it },
                    label = { Text("Notas para el reporte PDF") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 6,
                    placeholder = { Text("Agregue notas que aparecerán en el reporte PDF...") }
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val updatedJob = job.copy(reportNotes = reportNotesText.value)
                            storageManager.saveJobMetadata(updatedJob)
                            showEditForm.value = false
                            onReportNotesUpdated()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                    ) {
                        Text("Guardar")
                    }
                    Button(
                        onClick = {
                            reportNotesText.value = job.reportNotes
                            showEditForm.value = false
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                    ) {
                        Text("Cancelar")
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                if (job.reportNotes.isNotBlank()) {
                    Text(
                        job.reportNotes,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    Text(
                        "Sin notas de reporte aún",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Reminder dropdown selector for agenda items.
 * Allows users to enable/disable reminders and select offset days.
 */
@Composable
fun ReminderDropdown(
    reminderEnabled: Boolean,
    reminderOffsetDays: Int,
    onReminderEnabledChange: (Boolean) -> Unit,
    onReminderOffsetChange: (Int) -> Unit
) {
    val showOffsetMenu = remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Enable/Disable reminder toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Recordatorio",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )
            Button(
                onClick = { onReminderEnabledChange(!reminderEnabled) },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(if (reminderEnabled) "Habilitado" else "Deshabilitado")
            }
        }

        // Offset days dropdown (only show if reminder is enabled)
        if (reminderEnabled) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { showOffsetMenu.value = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        when (reminderOffsetDays) {
                            0 -> "El Día del Vencimiento"
                            1 -> "1 Día Antes"
                            2 -> "2 Días Antes"
                            3 -> "3 Días Antes"
                            7 -> "1 Semana Antes"
                            else -> "$reminderOffsetDays Días Antes"
                        }
                    )
                }

                DropdownMenu(
                    expanded = showOffsetMenu.value,
                    onDismissRequest = { showOffsetMenu.value = false }
                ) {
                    listOf(0, 1, 2, 3, 7).forEach { days ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    when (days) {
                                        0 -> "El Día del Vencimiento"
                                        1 -> "1 Día Antes"
                                        2 -> "2 Días Antes"
                                        3 -> "3 Días Antes"
                                        7 -> "1 Semana Antes"
                                        else -> "$days Días Antes"
                                    }
                                )
                            },
                            onClick = {
                                onReminderOffsetChange(days)
                                showOffsetMenu.value = false
                            }
                        )
                    }
                }
            }
        }
    }
}
