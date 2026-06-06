package com.bitacora.pro.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bitacora.pro.assistant.AgendaSuggestionEngine
import com.bitacora.pro.data.models.AgendaItem
import com.bitacora.pro.data.models.AgendaStatus
import com.bitacora.pro.data.models.EvidenceCategory
import com.bitacora.pro.data.models.EvidenceItem
import com.bitacora.pro.data.models.EvidenceType
import com.bitacora.pro.data.models.JobFile
import com.bitacora.pro.data.storage.StorageManager
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

    // Load job on screen composition
    LaunchedEffect(jobId) {
        job.value = storageManager.loadJob(jobId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Job Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
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
                            "Evidence (${job.value!!.evidence.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Group evidence by category
                    val groupedEvidence = job.value!!.evidence.groupBy { it.category }
                    items(groupedEvidence.toList()) { (category, evidenceList) ->
                        Column {
                            Text(
                                category.name,
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
                        Text(
                            "No evidence yet",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
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
            MetadataRow("Client:", job.clientName)
            MetadataRow("Phone:", job.phone)
            MetadataRow("Service:", job.serviceType)
            MetadataRow("Status:", job.status.name)
            MetadataRow("Created:", formatDate(job.createdAt))
            MetadataRow("Updated:", formatDate(job.updatedAt))
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
    val newTitle = remember { mutableStateOf("") }
    val newDescription = remember { mutableStateOf("") }
    val newDueText = remember { mutableStateOf("") }

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
                    Text(if (showAddForm.value) "Cancel" else "+ Add")
                }
            }

            if (showAddForm.value) {
                Spacer(modifier = Modifier.height(12.dp))
                TextField(
                    value = newTitle.value,
                    onValueChange = { newTitle.value = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = newDescription.value,
                    onValueChange = { newDescription.value = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = newDueText.value,
                    onValueChange = { newDueText.value = it },
                    label = { Text("Due date (e.g., mañana, viernes)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        if (newTitle.value.isNotBlank()) {
                            val agendaItem = AgendaItem(
                                jobId = jobId,
                                title = newTitle.value,
                                description = newDescription.value,
                                dueText = newDueText.value,
                                status = AgendaStatus.PENDING
                            )
                            storageManager.addAgendaItemToJob(jobId, agendaItem)
                            newTitle.value = ""
                            newDescription.value = ""
                            newDueText.value = ""
                            showAddForm.value = false
                            onAgendaUpdated()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Agenda Item")
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
                        "Pending",
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
                        "Done",
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
                    "No agenda items yet",
                    style = MaterialTheme.typography.bodySmall
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
                            text = "Due: ${item.dueText}",
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
                        Text(if (item.status == AgendaStatus.PENDING) "Mark Done" else "Reopen")
                    }
                    IconButton(
                        onClick = {
                            storageManager.deleteAgendaItem(jobId, item.id)
                            onDelete()
                        },
                        modifier = Modifier.padding(0.dp)
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete agenda item")
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
                        text = evidence.fileName.ifEmpty { evidence.type.name },
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = evidence.type.name,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.padding(0.dp)) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete evidence")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Category selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Category:",
                    style = MaterialTheme.typography.labelSmall
                )
                Button(
                    onClick = { expandedCategory.value = true },
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(evidence.category.name)
                }
                DropdownMenu(
                    expanded = expandedCategory.value,
                    onDismissRequest = { expandedCategory.value = false }
                ) {
                    EvidenceCategory.values().forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.name) },
                            onClick = {
                                onCategoryChanged(category.name)
                                expandedCategory.value = false
                            }
                        )
                    }
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
                    Text("Suggest Agenda")
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
                            "Suggestions:",
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
                            Text("Close")
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
                    text = "Due: ${suggestion.dueText}",
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
                Text("Add")
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
