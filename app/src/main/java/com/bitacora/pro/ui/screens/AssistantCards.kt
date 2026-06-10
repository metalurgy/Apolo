package com.bitacora.pro.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bitacora.pro.assistant.AssistantResult

/**
 * Displays job summary information.
 */
@Composable
private fun SummaryCard(summary: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                "📊 Resumen",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                summary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

/**
 * Displays pending tasks.
 */
@Composable
private fun PendingTasksCard(pendingTasks: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                "📋 Tareas Pendientes",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            pendingTasks.forEach { task ->
                Text(
                    task,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}

/**
 * Displays detected risks and missing information.
 */
@Composable
private fun RisksCard(risks: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                "⚠️ Riesgos y Faltantes",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            risks.forEach { risk ->
                Text(
                    risk,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}

/**
 * Displays suggested report notes.
 */
@Composable
private fun SuggestedNotesCard(suggestedNotes: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                "📝 Notas Sugeridas",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            suggestedNotes.forEach { note ->
                Text(
                    "• $note",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}

/**
 * Displays next actions with 4 clear action buttons.
 * Improved UI with better button organization and visual hierarchy.
 */
@Composable
private fun NextActionsCard(
    nextActions: List<String>,
    onAddTask: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                "🎯 Próximas Acciones",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Display actions with improved button layout
            nextActions.forEach { action ->
                ActionItemRow(
                    action = action,
                    onAddTask = onAddTask
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

/**
 * Displays a single action item with appropriate button.
 * Shows 4 clear action buttons based on action type.
 */
@Composable
private fun ActionItemRow(
    action: String,
    onAddTask: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            action,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )

        // Show appropriate button based on action type
        when {
            action.contains("URGENTE") || action.contains("vencidas") -> {
                Button(
                    onClick = { onAddTask(action) },
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .height(32.dp)
                ) {
                    Text("Revisar", style = MaterialTheme.typography.labelSmall)
                }
            }
            action.contains("tareas pendientes") -> {
                Button(
                    onClick = { onAddTask(action) },
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .height(32.dp)
                ) {
                    Text("Revisar", style = MaterialTheme.typography.labelSmall)
                }
            }
            action.contains("evidencia") -> {
                Button(
                    onClick = { onAddTask(action) },
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .height(32.dp)
                ) {
                    Text("Agregar", style = MaterialTheme.typography.labelSmall)
                }
            }
            action.contains("información") -> {
                Button(
                    onClick = { onAddTask(action) },
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .height(32.dp)
                ) {
                    Text("Completar", style = MaterialTheme.typography.labelSmall)
                }
            }
            action.contains("Generar reporte") -> {
                // No button for report generation - user should use PDF Report section
            }
            else -> {
                Button(
                    onClick = { onAddTask(action) },
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .height(32.dp)
                ) {
                    Text("Agregar", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
