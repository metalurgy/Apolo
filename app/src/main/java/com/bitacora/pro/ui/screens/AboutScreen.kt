package com.bitacora.pro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bitacora.pro.R
import com.bitacora.pro.data.storage.StorageManager
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBackClick: () -> Unit,
    storageManager: StorageManager? = null
) {
    val context = LocalContext.current
    val actualStorageManager = storageManager ?: StorageManager(context)
    
    val showDeleteAllConfirm = remember { mutableStateOf(false) }
    val deleteAllConfirmText = remember { mutableStateOf("") }
    val showDeleteAllSuccess = remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top bar
        TopAppBar(
            title = { Text(stringResource(R.string.about_title)) },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
            )
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // App Info Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.about_app_name),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.about_version),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = stringResource(R.string.about_build_type),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Privacy Section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.about_privacy_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = stringResource(R.string.about_privacy_description),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Assistant Section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.about_assistant_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "✓ ${stringResource(R.string.about_assistant_local)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "✗ ${stringResource(R.string.about_assistant_remote_disabled)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Permissions Section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.about_permissions_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PermissionItem(stringResource(R.string.about_permission_camera))
                        PermissionItem(stringResource(R.string.about_permission_contacts))
                        PermissionItem(stringResource(R.string.about_permission_notifications))
                        PermissionItem(stringResource(R.string.about_permission_calendar))
                    }
                }
            }

            // Warning Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = stringResource(R.string.about_warning_whatsapp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(12.dp)
                )
            }

            // Dangerous Actions Section (v0.9.0)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "⚠️ Acciones Peligrosas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showDeleteAllConfirm.value = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("🗑️ Eliminar TODAS las actividades")
                        }
                        Text(
                            text = "Esta acción no se puede deshacer. Se eliminarán todas las actividades, evidencia y pendientes.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Delete All Confirmation Dialog
    if (showDeleteAllConfirm.value) {
        AlertDialog(
            onDismissRequest = { showDeleteAllConfirm.value = false },
            title = { Text("⚠️ Eliminar TODAS las actividades") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Esta acción eliminará PERMANENTEMENTE todas las actividades, evidencia y pendientes.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "Para confirmar, escribe: ELIMINAR TODO",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                    TextField(
                        value = deleteAllConfirmText.value,
                        onValueChange = { deleteAllConfirmText.value = it },
                        placeholder = { Text("Escribe aquí") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (deleteAllConfirmText.value == "ELIMINAR TODO") {
                            actualStorageManager.deleteAllJobs()
                            showDeleteAllConfirm.value = false
                            deleteAllConfirmText.value = ""
                            showDeleteAllSuccess.value = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    enabled = deleteAllConfirmText.value == "ELIMINAR TODO"
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showDeleteAllConfirm.value = false
                        deleteAllConfirmText.value = ""
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Success Dialog
    if (showDeleteAllSuccess.value) {
        AlertDialog(
            onDismissRequest = { showDeleteAllSuccess.value = false },
            title = { Text("✓ Completado") },
            text = { Text("Todas las actividades han sido eliminadas.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAllSuccess.value = false
                        onBackClick()
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
private fun PermissionItem(text: String) {
    Text(
        text = "• $text",
        style = MaterialTheme.typography.bodySmall
    )
}
