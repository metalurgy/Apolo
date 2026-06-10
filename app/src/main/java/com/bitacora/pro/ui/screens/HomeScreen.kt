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
import androidx.compose.ui.unit.sp
import com.bitacora.pro.assistant.DashboardAssistantEngine
import com.bitacora.pro.data.models.JobFile
import com.bitacora.pro.data.models.JobStatus
import com.bitacora.pro.data.storage.StorageManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * HomeScreen displays the Daily Copilot dashboard with job summary and quick actions.
 *
 * Features (v0.8.0 - Daily Copilot):
 * - Daily Copilot branding and messaging
 * - Quick action cards (Capture, Inbox, Agenda, Assistant)
 * - Smart dashboard with summary cards
 * - Today's/this week's jobs highlighted
 * - Actionable insights
 * - Filter tabs (Todos excludes archived by default)
 * - Professional, clean layout
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    storageManager: StorageManager,
    onCreateJobClick: () -> Unit,
    onJobClick: (String) -> Unit,
    onCaptureClick: () -> Unit = {},
    onInboxClick: () -> Unit = {},
    onAgendaClick: () -> Unit = {},
    onAssistantClick: () -> Unit = {}
) {
    val jobs = remember { mutableStateOf<List<JobFile>>(emptyList()) }
    val selectedFilter = remember { mutableStateOf<JobStatus?>(null) }
    val dashboardSummary = remember { mutableStateOf(DashboardAssistantEngine.DashboardSummary()) }

    // Load jobs on screen composition
    LaunchedEffect(Unit) {
        val allJobs = storageManager.loadAllJobs().sortedByDescending { it.lastUsedAt }
        jobs.value = allJobs
        dashboardSummary.value = DashboardAssistantEngine.generateDashboardSummary(allJobs)
    }

    // Filter jobs based on selected status
    // "Todos" (null filter) excludes archived by default
    val filteredJobs = when (selectedFilter.value) {
        null -> jobs.value.filter { it.status != JobStatus.ARCHIVED } // Todos: exclude archived
        else -> jobs.value.filter { it.status == selectedFilter.value }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Daily Copilot", fontSize = 13.sp, fontWeight = FontWeight.Light)
                        Text("Bitacora Pro", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                },
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
                Icon(Icons.Filled.Add, contentDescription = "Crear nueva actividad")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Daily Copilot greeting and quick actions
             DailyCopilotGreeting()
             Spacer(modifier = Modifier.height(8.dp))

             // Quick action cards
             QuickActionCards(
                 onCaptureClick = onCaptureClick,
                 onInboxClick = onInboxClick,
                 onAgendaClick = onAgendaClick,
                 onAssistantClick = onAssistantClick
             )
             Spacer(modifier = Modifier.height(16.dp))

            // Smart Dashboard Section (v0.8.0 - simplified, less clutter)
            if (jobs.value.isNotEmpty()) {
                SmartDashboardSection(dashboardSummary.value)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Status filter tabs
            if (jobs.value.isNotEmpty()) {
                StatusFilterTabs(
                    selectedFilter = selectedFilter.value,
                    onFilterSelected = { selectedFilter.value = it },
                    jobs = jobs.value
                )
            }

            if (filteredJobs.isEmpty()) {
                if (jobs.value.isEmpty()) {
                    EmptyJobsView(Modifier.weight(1f), onCreateJobClick)
                } else {
                    EmptyFilteredJobsView(Modifier.weight(1f), selectedFilter.value)
                }
            } else {
                JobsList(filteredJobs, Modifier.weight(1f), onJobClick)
            }

            // Version footer
            AppVersionFooter()
        }
    }
}

/**
 * Daily Copilot greeting section with time-based messaging.
 * v0.8.2: Personalized greeting and daily focus messaging with improved spacing
 */
@Composable
private fun DailyCopilotGreeting() {
    val hour = remember {
        val calendar = java.util.Calendar.getInstance()
        calendar.get(java.util.Calendar.HOUR_OF_DAY)
    }
    
    val greeting = when (hour) {
        in 5..11 -> "Buenos días"
        in 12..17 -> "Buenas tardes"
        else -> "Buenas noches"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Text(
            greeting,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "¿Qué necesitas hacer hoy?",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Quick action cards for common tasks.
 * v0.8.2: Capture, Sin asignar, Pendientes, Asistente
 * Product model: Actividad (job) → Pendientes (agenda) → Sin asignar (inbox) → Asistente (workflows)
 *
 * CRITICAL FIX v0.8.2:
 * - Renamed "Por clasificar" to "Sin asignar" (clearer meaning)
 * - Capturar now opens capture UI, not CreateJob
 * - All actions match user expectations
 */
@Composable
private fun QuickActionCards(
    onCaptureClick: () -> Unit,
    onInboxClick: () -> Unit,
    onAgendaClick: () -> Unit,
    onAssistantClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        QuickActionCard(
            icon = "📸",
            label = "Capturar",
            modifier = Modifier.weight(1f),
            onClick = onCaptureClick,
            description = "Captura rápida"
        )
        QuickActionCard(
            icon = "📥",
            label = "Sin asignar",
            modifier = Modifier.weight(1f),
            onClick = onInboxClick,
            description = "Capturas sin clasificar"
        )
        QuickActionCard(
            icon = "📋",
            label = "Pendientes",
            modifier = Modifier.weight(1f),
            onClick = onAgendaClick,
            description = "Tareas pendientes"
        )
        QuickActionCard(
            icon = "🤖",
            label = "Asistente",
            modifier = Modifier.weight(1f),
            onClick = onAssistantClick,
            description = "Acciones guiadas"
        )
    }
}

/**
 * Individual quick action card.
 * v0.8.2: Compact, minimal design with tooltip
 */
@Composable
private fun QuickActionCard(
    icon: String,
    label: String,
    modifier: Modifier = Modifier,
    description: String = "",
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .height(70.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(icon, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
            if (description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    description,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 7.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Smart Dashboard Section with summary cards and insights.
 * v0.8.1: Ultra-simplified, minimal clutter, premium feel
 */
@Composable
private fun SmartDashboardSection(summary: DashboardAssistantEngine.DashboardSummary) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Summary Cards Row - Compact and minimal
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CompactSummaryCard(
                label = "Activos",
                value = summary.activeJobs.toString(),
                modifier = Modifier.weight(1f)
            )
            CompactSummaryCard(
                label = "Completados",
                value = summary.completedJobs.toString(),
                modifier = Modifier.weight(1f)
            )
            CompactSummaryCard(
                label = "Evidencia",
                value = summary.totalEvidence.toString(),
                modifier = Modifier.weight(1f)
            )
        }

        // Insights Section - Only show top insight if available
        if (summary.insights.isNotEmpty()) {
            Text(
                "💡 ${summary.insights.first()}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

/**
 * Compact summary card for dashboard metrics.
 * v0.8.1: Ultra-minimal, no elevation, clean design
 */
@Composable
private fun CompactSummaryCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Summary card for dashboard metrics (legacy - kept for compatibility).
 * v0.7.3: Minimal elevation, premium feel
 */
@Composable
private fun SummaryCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    CompactSummaryCard(label, value, modifier)
}

/**
 * Displays status filter tabs for job organization.
 */
@Composable
private fun StatusFilterTabs(
    selectedFilter: JobStatus?,
    onFilterSelected: (JobStatus?) -> Unit,
    jobs: List<JobFile>
) {
    val activeCount = jobs.count { it.status == JobStatus.ACTIVE }
    val completedCount = jobs.count { it.status == JobStatus.COMPLETED }
    val archivedCount = jobs.count { it.status == JobStatus.ARCHIVED }
    val todosCount = jobs.count { it.status != JobStatus.ARCHIVED } // Todos excludes archived

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        FilterButton(
            label = "Todos ($todosCount)",
            isSelected = selectedFilter == null,
            onClick = { onFilterSelected(null) }
        )
        FilterButton(
            label = "Activos ($activeCount)",
            isSelected = selectedFilter == JobStatus.ACTIVE,
            onClick = { onFilterSelected(JobStatus.ACTIVE) }
        )
        FilterButton(
            label = "Completados ($completedCount)",
            isSelected = selectedFilter == JobStatus.COMPLETED,
            onClick = { onFilterSelected(JobStatus.COMPLETED) }
        )
        FilterButton(
            label = "Archivados ($archivedCount)",
            isSelected = selectedFilter == JobStatus.ARCHIVED,
            onClick = { onFilterSelected(JobStatus.ARCHIVED) }
        )
    }
}

/**
 * Displays a single filter button.
 */
@Composable
private fun FilterButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .height(36.dp)
            .padding(horizontal = 2.dp),
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10f.sp
        )
    }
}

/**
 * Displays empty state when filter returns no results.
 */
@Composable
private fun EmptyFilteredJobsView(modifier: Modifier = Modifier, filter: JobStatus?) {
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
                "Sin actividades en esta categoría",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "No hay actividades con estado ${filter?.name?.lowercase() ?: "desconocido"}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
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
                "Empieza tu primera actividad",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Organiza evidencia, pendientes, contactos y reportes en un solo lugar.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onCreateJobClick) {
                Text("Crear actividad")
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
 * Displays a single job card with professional styling.
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
            // Title and Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = job.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = job.status.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = when (job.status) {
                        JobStatus.ACTIVE -> MaterialTheme.colorScheme.primary
                        JobStatus.COMPLETED -> MaterialTheme.colorScheme.secondary
                        JobStatus.ARCHIVED -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Client and Phone
            Text(
                text = "Cliente: ${job.clientName}",
                style = MaterialTheme.typography.bodySmall
            )
            if (job.phone.isNotEmpty()) {
                Text(
                    text = "Teléfono: ${job.phone}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = "Servicio: ${job.serviceType}",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Evidence and Date
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
            text = "Bitacora Pro v0.8.2 - P0 UX Action Fix",
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
