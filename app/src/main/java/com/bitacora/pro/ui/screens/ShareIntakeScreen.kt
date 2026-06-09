package com.bitacora.pro.ui.screens

import android.net.Uri
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bitacora.pro.data.models.SharedFileDescriptor
import com.bitacora.pro.data.storage.StorageManager

/**
 * Data class to hold shared content information.
 * Contains pending shared files (URIs) and text content.
 * Files are NOT copied until the user creates/selects a job.
 *
 * @param textContent Text content shared by the user.
 * @param sharedFiles List of pending shared file descriptors (URI + MIME type).
 */
data class SharedContent(
    val textContent: String = "",
    val sharedFiles: List<SharedFileDescriptor> = emptyList()
)

/**
 * ShareIntakeScreen displays incoming shared content and allows the user to
 * create a new job or add to an existing job.
 *
 * Files are NOT copied in this screen; they are copied when the user
 * creates a new job or selects an existing job.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareIntakeScreen(
    storageManager: StorageManager,
    sharedContent: SharedContent,
    onCreateNewJob: (SharedContent) -> Unit,
    onAddToExistingJob: (String, SharedContent) -> Unit,
    onBack: () -> Unit
) {
    val jobs = remember { mutableStateOf(storageManager.loadAllJobs().sortedByDescending { it.lastUsedAt }) }
    val selectedJobId = remember { mutableStateOf<String?>(null) }
    val mostRecentJobId = remember { mutableStateOf(jobs.value.firstOrNull()?.id) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recibir Contenido") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Display shared content summary
                item {
                    SharedContentSummary(sharedContent)
                }

                // Quick add to most recent job button
                if (mostRecentJobId.value != null) {
                    item {
                        Button(
                            onClick = {
                                onAddToExistingJob(mostRecentJobId.value!!, sharedContent)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text("Agregar al Trabajo Reciente")
                        }
                    }
                }

                // Display existing jobs for selection
                if (jobs.value.isNotEmpty()) {
                    item {
                        Text(
                            "O selecciona otro trabajo:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    items(jobs.value) { job ->
                        JobSelectionCard(
                            job = job,
                            isSelected = selectedJobId.value == job.id,
                            onSelect = { selectedJobId.value = job.id }
                        )
                    }
                }
            }

            // Action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onCreateNewJob(sharedContent) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text("Crear Nuevo")
                }

                if (selectedJobId.value != null) {
                    Button(
                        onClick = {
                            onAddToExistingJob(selectedJobId.value!!, sharedContent)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Agregar")
                    }
                }
            }
        }
    }
}

/**
 * Displays a summary of the shared content.
 * Shows text preview and file count (files are not copied yet).
 */
@Composable
private fun SharedContentSummary(sharedContent: SharedContent) {
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
                "Contenido Recibido:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (sharedContent.textContent.isNotEmpty()) {
                Text(
                    "Texto: ${sharedContent.textContent.take(100)}${if (sharedContent.textContent.length > 100) "..." else ""}",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            if (sharedContent.sharedFiles.isNotEmpty()) {
                Text(
                    "Archivos: ${sharedContent.sharedFiles.size}",
                    style = MaterialTheme.typography.bodySmall
                )
                sharedContent.sharedFiles.forEach { fileDescriptor ->
                    val fileName = getFileNameFromUri(fileDescriptor.uri)
                    Text(
                        "  • $fileName (${fileDescriptor.mimeType})",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

/**
 * Displays a job card for selection.
 */
@Composable
private fun JobSelectionCard(
    job: com.bitacora.pro.data.models.JobFile,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = job.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = job.clientName,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Button(
                onClick = onSelect,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(if (isSelected) "Seleccionado" else "Seleccionar")
            }
        }
    }
}

/**
 * Extracts a simple file name from a URI.
 * Used for display purposes only.
 */
private fun getFileNameFromUri(uri: Uri): String {
    return uri.lastPathSegment ?: "file"
}
