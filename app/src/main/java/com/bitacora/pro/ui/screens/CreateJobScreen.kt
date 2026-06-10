package com.bitacora.pro.ui.screens

import android.app.Activity
import android.content.Intent
import android.provider.ContactsContract
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bitacora.pro.data.models.EvidenceType
import com.bitacora.pro.data.models.JobFile
import com.bitacora.pro.data.storage.StorageManager
import com.bitacora.pro.utils.PhoneNumberUtils
import com.bitacora.pro.whatsapp.WhatsAppUIHelper

/**
 * CreateJobScreen allows the user to manually create a new activity with metadata.
 *
 * If shared content is provided, files are copied to the newly created activity folder
 * after the activity is created. This ensures all evidence files are self-contained
 * within the activity folder.
 * 
 * Features (v0.8.1):
 * - Contact import from device contacts with phone number population
 * - Phone number normalization
 * - WhatsApp workflow integration
 * - UI labels use "Actividad" instead of "Trabajo"
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
    val errorMessage = remember { mutableStateOf("") }
    val context = LocalContext.current

    // Phone-specific contact picker launcher
    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                Log.d("ContactImport", "Selected URI: $uri")
                parsePhoneContact(
                    context = context,
                    uri = uri,
                    onContactSelected = { name, phoneNumber ->
                        clientName.value = name
                        phone.value = phoneNumber
                        errorMessage.value = ""
                    },
                    onError = { error ->
                        errorMessage.value = error
                    }
                )
            } ?: run {
                Log.w("ContactImport", "No URI returned from contact picker")
                errorMessage.value = "No se seleccionó contacto"
            }
        } else {
            Log.d("ContactImport", "Contact picker cancelled")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva actividad") },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Error message display
                    if (errorMessage.value.isNotEmpty()) {
                        Text(
                            errorMessage.value,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    // Title field
                    OutlinedTextField(
                        value = title.value,
                        onValueChange = { title.value = it },
                        label = { Text("Nombre de la actividad") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Client name field with contact import
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = clientName.value,
                                onValueChange = { clientName.value = it },
                                label = { Text("Persona o cliente") },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp),
                                singleLine = true
                            )
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
                                    contactPickerLauncher.launch(intent)
                                },
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .height(56.dp)
                            ) {
                                Text("👥 Importar")
                            }
                        }
                        Text(
                            "Toca 'Importar' para seleccionar un contacto de tu dispositivo",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Phone field
                    OutlinedTextField(
                        value = phone.value,
                        onValueChange = { phone.value = it },
                        label = { Text("Teléfono") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Service type field
                    OutlinedTextField(
                        value = serviceType.value,
                        onValueChange = { serviceType.value = it },
                        label = { Text("Tipo de actividad") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // Display shared content info if available
                    if (sharedContent != null && (sharedContent.textContent.isNotEmpty() || sharedContent.sharedFiles.isNotEmpty())) {
                        Text(
                            "Contenido recibido para adjuntar:",
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
                            Text("Cancelar")
                        }
                        Spacer(modifier = Modifier.padding(8.dp))
                        Button(
                            onClick = {
                                if (title.value.isNotEmpty() && clientName.value.isNotEmpty()) {
                                    isLoading.value = true
                                    
                                    // Create the new activity
                                    val newJob = JobFile(
                                        title = title.value,
                                        clientName = clientName.value,
                                        phone = phone.value,
                                        serviceType = serviceType.value
                                    )
                                    val createdJob = storageManager.createJob(newJob)

                                    // Copy shared content to the newly created activity
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
                            Text("Crear actividad")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Parse the selected contact URI to extract name and phone.
 * This handles the phone-specific picker result.
 */
private fun parsePhoneContact(
    context: android.content.Context,
    uri: android.net.Uri,
    onContactSelected: (name: String, phone: String) -> Unit,
    onError: (message: String) -> Unit
) {
    try {
        val contentResolver = context.contentResolver

        // Query the phone-specific URI directly
        val cursor = contentResolver.query(
            uri,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER
            ),
            null,
            null,
            null
        )

        cursor?.use { c ->
            if (c.moveToFirst()) {
                val displayNameIndex = c.getColumnIndex(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                )
                val numberIndex = c.getColumnIndex(
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                )
                val normalizedIndex = c.getColumnIndex(
                    ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER
                )

                val displayName = c.getString(displayNameIndex) ?: ""
                val rawNumber = c.getString(numberIndex) ?: ""
                val normalizedNumber = c.getString(normalizedIndex) ?: ""

                Log.d("ContactImport", "Display name: $displayName")
                Log.d("ContactImport", "Raw number: $rawNumber")
                Log.d("ContactImport", "Normalized number: $normalizedNumber")

                // Use normalized number if available, otherwise use raw number
                val finalPhone = normalizedNumber.takeIf { it.isNotBlank() } ?: rawNumber

                Log.d("ContactImport", "Final phone: $finalPhone")

                if (displayName.isBlank()) {
                    onError("No se pudo leer el nombre del contacto")
                } else if (finalPhone.isBlank()) {
                    onError("No se pudo leer el teléfono del contacto. Puedes escribirlo manualmente.")
                } else {
                    onContactSelected(displayName, finalPhone)
                }
            } else {
                Log.w("ContactImport", "Cursor is empty")
                onError("No se pudo leer el contacto")
            }
        } ?: run {
            Log.w("ContactImport", "Cursor is null")
            onError("No se pudo acceder al contacto")
        }
    } catch (e: Exception) {
        Log.e("ContactImport", "Error parsing contact", e)
        onError("Error al leer el contacto: ${e.message}")
    }
}

/**
 * Copies all shared content (files and text) to the specified activity.
 * Files are copied from their URIs to the activity's evidence folder.
 * Text is stored as TEXT evidence metadata.
 */
private fun copySharedContentToJob(
    storageManager: StorageManager,
    jobId: String,
    sharedContent: SharedContent
) {
    // Copy all shared files to the activity
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
