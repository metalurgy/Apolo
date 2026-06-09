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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import com.bitacora.pro.data.models.JobFile
import com.bitacora.pro.data.storage.StorageManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * HomeScreen displays a list of existing jobs and allows creating new ones.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    storageManager: StorageManager,
    onCreateJobClick: () -> Unit,
    onJobClick: (String) -> Unit
) {
    val jobs = remember { mutableStateOf<List<JobFile>>(emptyList()) }

    // Load jobs on screen composition
    LaunchedEffect(Unit) {
        jobs.value = storageManager.loadAllJobs().sortedByDescending { it.lastUsedAt }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bitacora Pro") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateJobClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Crear nuevo trabajo")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (jobs.value.isEmpty()) {
                EmptyJobsView(Modifier.weight(1f), onCreateJobClick)
            } else {
                JobsList(jobs.value, Modifier.weight(1f), onJobClick)
            }
            // Version footer
            AppVersionFooter()
        }
    }
}

/**
 * Displays an empty state when no jobs exist.
 */
@Composable
private fun EmptyJobsView(modifier: Modifier = Modifier, onCreateJobClick: () -> Unit) {
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
                "Sin trabajos aún",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Crea un nuevo trabajo o comparte contenido desde otra aplicación",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onCreateJobClick) {
                Text("Crear Nuevo Trabajo")
            }
        }
    }
}

/**
 * Displays a list of jobs.
 */
@Composable
private fun JobsList(
    jobs: List<JobFile>,
    modifier: Modifier = Modifier,
    onJobClick: (String) -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(jobs) { job ->
            JobCard(job, onJobClick)
        }
    }
}

/**
 * Displays a single job card.
 */
@Composable
private fun JobCard(job: JobFile, onJobClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .clickable { onJobClick(job.id) },
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
            Text(
                text = job.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Cliente: ${job.clientName}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Teléfono: ${job.phone}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Servicio: ${job.serviceType}",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Evidencia: ${job.evidence.size}",
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = formatDate(job.lastUsedAt),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

/**
 * Displays app version footer.
 */
@Composable
private fun AppVersionFooter() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Bitacora Pro v0.5.1",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Formats a timestamp to a readable date string.
 */
private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
