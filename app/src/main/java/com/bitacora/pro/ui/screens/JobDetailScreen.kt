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
import com.bitacora.pro.data.models.AgendaItem
import com.bitacora.pro.data.models.AgendaStatus
import com.bitacora.pro.data.models.EvidenceCategory
import com.bitacora.pro.data.models.EvidenceItem
import com.bitacora.pro.data.models.EvidenceType
import com.bitacora.pro.data.models.JobFile
import com.bitacora.pro.data.models.getSpanishLabel
import com.bitacora.pro.data.storage.StorageManager
import com.bitacora.pro.reports.JobPdfReportGenerator
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
    val context = LocalContext.current

    // Load job on screen composition
    LaunchedEffect(jobId) {
        job.value = storageManager.loadJob(jobId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalles del Trabajo") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Atrás")
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

                // Evidence section
                if (job.value!!.evidence.isNotEmpty()) {
                    item {
                        Text(
                            "Evidencia (${job.value!!.evidence.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
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
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Sin evidencia aún",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
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
        }
    }
}

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
    val newTitle = remember { mutableStateOf("") }
    val newDescription = remember { mutableStateOf("") }
    val newDueText = remember { mutableStateOf("") }
    val newDueAt = remember { mutableStateOf<Long?>(null) }

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
                    "Agenda (${job.agendaItems.size})",
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
                Button(
                    onClick = {
                        if (newTitle.value.isNotBlank()) {
                            val agendaItem = AgendaItem(
                                jobId = jobId,
                                title = newTitle.value,
                                description = newDescription.value,
                                dueText = newDueText.value,
                                dueAt = newDueAt.value,
                                status = AgendaStatus.PENDING
                            )
                            storageManager.addAgendaItemToJob(jobId, agendaItem)
                            newTitle.value = ""
                            newDescription.value = ""
                            newDueText.value = ""
                            newDueAt.value = null
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
                // Separate pending and done items
                val pendingItems = job.agendaItems.filter { it.status == AgendaStatus.PENDING }
                val doneItems = job.agendaItems.filter { it.status == AgendaStatus.DONE }

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
 * Displays a single agenda item with status toggle and delete button.
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
                }
                Row {
                    Button(
                        onClick = {
                            val newStatus = if (item.status == AgendaStatus.PENDING) "DONE" else "PENDING"
                            storageManager.updateAgendaItemStatus(jobId, item.id, newStatus)
                            onStatusChanged()
                        },
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Text(if (item.status == AgendaStatus.PENDING) "Completar" else "Reabrir")
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
