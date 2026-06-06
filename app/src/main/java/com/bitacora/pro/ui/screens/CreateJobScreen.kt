package com.bitacora.pro.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bitacora.pro.data.models.EvidenceType
import com.bitacora.pro.data.models.JobFile
import com.bitacora.pro.data.storage.StorageManager

/**
 * CreateJobScreen allows the user to manually create a new job with metadata.
 *
 * If shared content is provided, files are copied to the newly created job folder
 * after the job is created. This ensures all evidence files are self-contained
 * within the job folder.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateJobScreen(
    storageManager: StorageManager,
    sharedContent: SharedContent? = null,
    onJobCreated: (String) -> Unit,
    onBack: () -> Unit
) {
    val title = remember { mutableStateOf("") }
    val clientName = remember { mutableStateOf("") }
    val phone = remember { mutableStateOf("") }
    val serviceType = remember { mutableStateOf("") }
    val isLoading = remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create New Job") },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Title field
                    OutlinedTextField(
                        value = title.value,
                        onValueChange = { title.value = it },
                        label = { Text("Job Title") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Client name field
                    OutlinedTextField(
                        value = clientName.value,
                        onValueChange = { clientName.value = it },
                        label = { Text("Client Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Phone field
                    OutlinedTextField(
                        value = phone.value,
                        onValueChange = { phone.value = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Service type field
                    OutlinedTextField(
                        value = serviceType.value,
                        onValueChange = { serviceType.value = it },
                        label = { Text("Service Type") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // Display shared content info if available
                    if (sharedContent != null && (sharedContent.textContent.isNotEmpty() || sharedContent.sharedFiles.isNotEmpty())) {
                        Text(
                            "Shared Content to Attach:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        if (sharedContent.textContent.isNotEmpty()) {
                            Text(
                                "Text: ${sharedContent.textContent.take(100)}${if (sharedContent.textContent.length > 100) "..." else ""}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        if (sharedContent.sharedFiles.isNotEmpty()) {
                            Text(
                                "Files: ${sharedContent.sharedFiles.size}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = onBack,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            enabled = !isLoading.value
                        ) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.padding(8.dp))
                        Button(
                            onClick = {
                                if (title.value.isNotEmpty() && clientName.value.isNotEmpty()) {
                                    isLoading.value = true
                                    
                                    // Create the new job
                                    val newJob = JobFile(
                                        title = title.value,
                                        clientName = clientName.value,
                                        phone = phone.value,
                                        serviceType = serviceType.value
                                    )
                                    val createdJob = storageManager.createJob(newJob)

                                    // Copy shared content to the newly created job
                                    if (sharedContent != null) {
                                        copySharedContentToJob(storageManager, createdJob.id, sharedContent)
                                    }

                                    isLoading.value = false
                                    onJobCreated(createdJob.id)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            enabled = !isLoading.value && title.value.isNotEmpty() && clientName.value.isNotEmpty()
                        ) {
                            Text("Create Job")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Copies all shared content (files and text) to the specified job.
 * Files are copied from their URIs to the job's evidence folder.
 * Text is stored as TEXT evidence metadata.
 */
private fun copySharedContentToJob(
    storageManager: StorageManager,
    jobId: String,
    sharedContent: SharedContent
) {
    // Copy all shared files to the job
    sharedContent.sharedFiles.forEach { fileDescriptor ->
        val evidenceType = getEvidenceTypeFromMimeType(fileDescriptor.mimeType)
        val evidence = storageManager.copyEvidenceFromUri(
            jobId = jobId,
            uri = fileDescriptor.uri,
            evidenceType = evidenceType,
            mimeType = fileDescriptor.mimeType
        )
        if (evidence != null) {
            storageManager.addEvidenceToJob(jobId, evidence)
        }
    }

    // Store shared text as TEXT evidence
    if (sharedContent.textContent.isNotEmpty()) {
        val textEvidence = storageManager.saveTextEvidence(jobId, sharedContent.textContent)
        storageManager.addEvidenceToJob(jobId, textEvidence)
    }
}

/**
 * Determines EvidenceType from MIME type.
 */
private fun getEvidenceTypeFromMimeType(mimeType: String): EvidenceType {
    return when {
        mimeType.startsWith("image/") -> EvidenceType.IMAGE
        mimeType.startsWith("audio/") -> EvidenceType.AUDIO
        mimeType.contains("pdf") -> EvidenceType.PDF
        else -> EvidenceType.TEXT
    }
}
