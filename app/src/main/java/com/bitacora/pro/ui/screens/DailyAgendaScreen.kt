package com.bitacora.pro.ui.screens

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import android.provider.CalendarContract
import com.bitacora.pro.data.models.AgendaItem
import com.bitacora.pro.data.models.AgendaStatus
import com.bitacora.pro.data.models.JobFile
import com.bitacora.pro.data.storage.StorageManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * DailyAgendaScreen displays all pending items organized by sections.
 * v0.8.1: "Pendientes" (Pending items) with comprehensive view
 *
 * Sections:
 * - Vencidas (Overdue) - items past due date
 * - Hoy (Today) - items due today
 * - Próximos (Upcoming) - items due in future
 * - Sin fecha (No date) - items without due date
 *
 * Features:
 * - Organized by urgency
 * - Quick status updates (mark done)
 * - Add new agenda item
 * - Integration with reminders
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyAgendaScreen(
    storageManager: StorageManager,
    onBack: () -> Unit,
    onAddAgendaItem: () -> Unit
) {
    val jobs = remember { mutableStateOf<List<JobFile>>(emptyList()) }
    val overduItems = remember { mutableStateOf<List<Pair<JobFile, AgendaItem>>>(emptyList()) }
    val todayItems = remember { mutableStateOf<List<Pair<JobFile, AgendaItem>>>(emptyList()) }
    val upcomingItems = remember { mutableStateOf<List<Pair<JobFile, AgendaItem>>>(emptyList()) }
    val noDateItems = remember { mutableStateOf<List<Pair<JobFile, AgendaItem>>>(emptyList()) }

    // Load jobs and organize agenda items by section
    LaunchedEffect(Unit) {
        val allJobs = storageManager.loadAllJobs()
        jobs.value = allJobs

        val now = System.currentTimeMillis()
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val tomorrow = today + (24 * 60 * 60 * 1000)
        val nextWeek = today + (7 * 24 * 60 * 60 * 1000)

        val overdue = mutableListOf<Pair<JobFile, AgendaItem>>()
        val todayList = mutableListOf<Pair<JobFile, AgendaItem>>()
        val upcoming = mutableListOf<Pair<JobFile, AgendaItem>>()
        val noDate = mutableListOf<Pair<JobFile, AgendaItem>>()

        allJobs.forEach { job ->
            job.agendaItems.forEach { item ->
                // Skip archived items
                if (item.status == AgendaStatus.ARCHIVED) return@forEach

                when {
                    item.dueAt == null -> {
                        // No date items
                        noDate.add(Pair(job, item))
                    }
                    item.dueAt!! < today -> {
                        // Overdue items
                        overdue.add(Pair(job, item))
                    }
                    item.dueAt!! in today until tomorrow -> {
                        // Today items
                        todayList.add(Pair(job, item))
                    }
                    else -> {
                        // Upcoming items
                        upcoming.add(Pair(job, item))
                    }
                }
            }
        }

        overduItems.value = overdue.sortedBy { it.second.dueAt }
        todayItems.value = todayList.sortedBy { it.second.dueAt }
        upcomingItems.value = upcoming.sortedBy { it.second.dueAt }
        noDateItems.value = noDate.sortedByDescending { it.second.createdAt }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Gestión de tareas", fontSize = 12.sp, fontWeight = FontWeight.Light)
                        Text("Pendientes", fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
        },
        floatingActionButton = {
            // FAB for adding new agenda items - currently navigates to home
            // Users can add items through job detail screen
            FloatingActionButton(
                onClick = onAddAgendaItem,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Agregar item de agenda")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val hasAnyItems = overduItems.value.isNotEmpty() || 
                             todayItems.value.isNotEmpty() || 
                             upcomingItems.value.isNotEmpty() || 
                             noDateItems.value.isNotEmpty()

            if (!hasAnyItems) {
                EmptyAgendaView(Modifier.weight(1f))
            } else {
                PendingItemsList(
                    overduItems = overduItems.value,
                    todayItems = todayItems.value,
                    upcomingItems = upcomingItems.value,
                    noDateItems = noDateItems.value,
                    modifier = Modifier.weight(1f),
                    onStatusChange = { job, item, newStatus ->
                        val updatedJob = job.copy(
                            agendaItems = job.agendaItems.map {
                                if (it.id == item.id) it.copy(status = newStatus) else it
                            }
                        )
                        storageManager.saveJobMetadata(updatedJob)
                    }
                )
            }
        }
    }
}

/**
 * Displays list of pending items organized by sections.
 */
@Composable
private fun PendingItemsList(
    overduItems: List<Pair<JobFile, AgendaItem>>,
    todayItems: List<Pair<JobFile, AgendaItem>>,
    upcomingItems: List<Pair<JobFile, AgendaItem>>,
    noDateItems: List<Pair<JobFile, AgendaItem>>,
    modifier: Modifier = Modifier,
    onStatusChange: (JobFile, AgendaItem, AgendaStatus) -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Overdue section
        if (overduItems.isNotEmpty()) {
            item {
                SectionHeader("🔴 Vencidas (${overduItems.size})")
            }
            items(overduItems) { (job, item) ->
                AgendaItemCard(
                    job = job,
                    item = item,
                    isOverdue = true,
                    onStatusChange = { newStatus ->
                        onStatusChange(job, item, newStatus)
                    }
                )
            }
        }

        // Today section
        if (todayItems.isNotEmpty()) {
            item {
                SectionHeader("📅 Hoy (${todayItems.size})")
            }
            items(todayItems) { (job, item) ->
                AgendaItemCard(
                    job = job,
                    item = item,
                    isOverdue = false,
                    onStatusChange = { newStatus ->
                        onStatusChange(job, item, newStatus)
                    }
                )
            }
        }

        // Upcoming section
        if (upcomingItems.isNotEmpty()) {
            item {
                SectionHeader("📆 Próximos (${upcomingItems.size})")
            }
            items(upcomingItems) { (job, item) ->
                AgendaItemCard(
                    job = job,
                    item = item,
                    isOverdue = false,
                    onStatusChange = { newStatus ->
                        onStatusChange(job, item, newStatus)
                    }
                )
            }
        }

        // No date section
        if (noDateItems.isNotEmpty()) {
            item {
                SectionHeader("⏳ Sin fecha (${noDateItems.size})")
            }
            items(noDateItems) { (job, item) ->
                AgendaItemCard(
                    job = job,
                    item = item,
                    isOverdue = false,
                    onStatusChange = { newStatus ->
                        onStatusChange(job, item, newStatus)
                    }
                )
            }
        }
    }
}

/**
 * Section header for pending items.
 */
@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

/**
 * Individual agenda item card with calendar action.
 */
@Composable
private fun AgendaItemCard(
    job: JobFile,
    item: AgendaItem,
    isOverdue: Boolean = false,
    onStatusChange: (AgendaStatus) -> Unit
) {
    val context = LocalContext.current
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOverdue)
                MaterialTheme.colorScheme.errorContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with job name and time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = job.title,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        textDecoration = if (item.status == AgendaStatus.DONE) TextDecoration.LineThrough else TextDecoration.None
                    )
                }
                if (item.dueAt != null) {
                    Text(
                        text = formatDate(item.dueAt!!),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Description
            if (item.description.isNotEmpty()) {
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Status controls and calendar button
             Row(
                 modifier = Modifier.fillMaxWidth(),
                 horizontalArrangement = Arrangement.spacedBy(8.dp),
                 verticalAlignment = Alignment.CenterVertically
             ) {
                 // Status indicator
                 Text(
                     text = if (item.status == AgendaStatus.DONE) "✅" else "⭕",
                     style = MaterialTheme.typography.bodySmall,
                     fontSize = 16.sp
                 )
                 
                 // Task status text
                 Text(
                     text = if (item.status == AgendaStatus.DONE) "Completado" else "Pendiente",
                     style = MaterialTheme.typography.bodySmall,
                     modifier = Modifier.weight(1f)
                 )
                 
                 if (item.reminderEnabled) {
                     Text(
                         text = "🔔",
                         style = MaterialTheme.typography.bodySmall
                     )
                 }
                 
                 // Complete button (only if pending)
                 if (item.status == AgendaStatus.PENDING) {
                     Button(
                         onClick = {
                             onStatusChange(AgendaStatus.DONE)
                         },
                         modifier = Modifier.height(32.dp)
                     ) {
                         Text("Completar", fontSize = 10.sp)
                     }
                 }
                 
                 // Calendar button for dated items
                 if (item.dueAt != null) {
                     IconButton(
                         onClick = {
                            val intent = Intent(Intent.ACTION_INSERT).apply {
                                data = CalendarContract.Events.CONTENT_URI
                                putExtra(CalendarContract.Events.TITLE, "${job.title} - ${item.title}")
                                putExtra(CalendarContract.Events.DESCRIPTION, item.description)
                                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, item.dueAt)
                                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, item.dueAt!! + (60 * 60 * 1000)) // 1 hour duration
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        },
                         modifier = Modifier.size(32.dp)
                     ) {
                         Icon(
                             Icons.Filled.DateRange,
                             contentDescription = "Agregar a calendario",
                             modifier = Modifier.size(18.dp)
                         )
                     }
                 }
             }
        }
    }
}

/**
 * Empty agenda view.
 */
@Composable
private fun EmptyAgendaView(modifier: Modifier = Modifier) {
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
                "Sin tareas pendientes",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "¡Excelente! No hay tareas pendientes. Puedes agregar nuevas tareas cuando las necesites.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

/**
 * Formats a timestamp to date string (MMM dd, HH:mm).
 */
private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
