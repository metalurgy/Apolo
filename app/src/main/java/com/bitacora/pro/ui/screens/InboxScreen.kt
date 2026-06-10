package com.bitacora.pro.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.unit.sp
import com.bitacora.pro.data.models.EvidenceType
import com.bitacora.pro.data.storage.InboxManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * InboxScreen displays unassigned captured content.
 * v0.8.2: "Sin asignar" (Unassigned) - temporary holding area for captures
 *
 * Product Model:
 * - User captures content quickly (photos, notes, files)
 * - Content goes to "Sin asignar" until assigned to an Actividad
 * - Once assigned, content moves to the Actividad's evidence
 *
 * Features:
 * - List of unassigned captures
 * - Quick preview of content
 * - Assign to job (Actividad)
 * - Delete items
 *
 * CRITICAL FIX v0.8.2:
 * - Renamed from "Por clasificar" to "Sin asignar" (clearer meaning)
 * - Better explanation of purpose
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(
    inboxManager: InboxManager,
    onBack: () -> Unit,
    onAssignToJob: (InboxManager.InboxItem) -> Unit
) {
    val inboxItems = remember { mutableStateOf<List<InboxManager.InboxItem>>(emptyList()) }

    // Load inbox items on screen composition
    LaunchedEffect(Unit) {
        inboxItems.value = inboxManager.loadAllInboxItems()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Capturas sin asignar", fontSize = 12.sp, fontWeight = FontWeight.Light)
                        Text("Sin asignar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                },
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
            if (inboxItems.value.isEmpty()) {
                EmptyInboxView(Modifier.weight(1f))
            } else {
                InboxItemsList(
                    items = inboxItems.value,
                    modifier = Modifier.weight(1f),
                    onDelete = { itemId ->
                        inboxManager.deleteInboxItem(itemId)
                        inboxItems.value = inboxManager.loadAllInboxItems()
                    },
                    onAssignToJob = onAssignToJob
                )
            }
        }
    }
}

/**
 * Displays list of inbox items.
 */
@Composable
private fun InboxItemsList(
    items: List<InboxManager.InboxItem>,
    modifier: Modifier = Modifier,
    onDelete: (String) -> Unit,
    onAssignToJob: (InboxManager.InboxItem) -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items) { item ->
            InboxItemCard(
                item = item,
                onDelete = { onDelete(item.id) },
                onAssignToJob = { onAssignToJob(item) }
            )
        }
    }
}

/**
 * Individual inbox item card.
 */
@Composable
private fun InboxItemCard(
    item: InboxManager.InboxItem,
    onDelete: () -> Unit,
    onAssignToJob: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with type and date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = getTypeEmoji(item.type) + " " + item.type.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formatDate(item.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Content preview
            when (item.type) {
                EvidenceType.TEXT -> {
                    Text(
                        text = item.textContent.take(100) + if (item.textContent.length > 100) "..." else "",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                EvidenceType.IMAGE -> {
                    Text(
                        text = "Imagen: ${item.fileName}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                EvidenceType.AUDIO -> {
                    Text(
                        text = "Audio: ${item.fileName}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                EvidenceType.PDF -> {
                    Text(
                        text = "PDF: ${item.fileName}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onAssignToJob,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                ) {
                    Text("Asignar a actividad")
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.height(40.dp)
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = "Eliminar")
                }
            }
        }
    }
}

/**
 * Empty inbox view - explains the purpose of "Sin asignar"
 * v0.8.2: Clearer explanation of unassigned captures
 */
@Composable
private fun EmptyInboxView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "📥 Sin asignar",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Aquí van las capturas rápidas (fotos, notas, archivos) que aún no asignas a una Actividad.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Usa el botón 'Capturar' en la pantalla principal para agregar contenido.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

/**
 * Gets emoji for evidence type.
 */
private fun getTypeEmoji(type: EvidenceType): String = when (type) {
    EvidenceType.TEXT -> "📝"
    EvidenceType.IMAGE -> "📸"
    EvidenceType.AUDIO -> "🎙️"
    EvidenceType.PDF -> "📄"
}

/**
 * Formats a timestamp to a readable date string.
 */
private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
