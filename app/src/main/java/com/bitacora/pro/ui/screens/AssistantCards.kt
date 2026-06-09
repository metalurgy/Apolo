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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bitacora.pro.assistant.AssistantResult

/**
 * Displays the job assistant analysis section with results.
 */
@Composable
fun AssistantSection(
    isAnalyzing: Boolean,
    result: AssistantResult?,
    errorMessage: String = "",
    onAnalyze: () -> Unit,
    onAddTaskFromAction: (String) -> Unit
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
                .padding(16.dp)
        ) {
            Text(
                "Asistente de Trabajo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Show error message if analysis failed
            if (errorMessage.isNotEmpty()) {
                Text(
                    errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            // Show loading state
            if (isAnalyzing) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .height(24.dp)
                            .padding(end = 8.dp)
                    )
                    Text(
                        "Analizando trabajo...",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Show results if available
            if (result != null && !isAnalyzing) {
                // Summary card
                SummaryCard(result.summary)
                Spacer(modifier = Modifier.height(12.dp))

                // Pending tasks card
                if (result.pendingTasks.isNotEmpty()) {
                    PendingTasksCard(result.pendingTasks)
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Risks card
                if (result.risks.isNotEmpty()) {
                    RisksCard(result.risks)
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Suggested notes card
                if (result.suggestedNotes.isNotEmpty()) {
                    SuggestedNotesCard(result.suggestedNotes)
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Next actions card
                if (result.nextActions.isNotEmpty()) {
                    NextActionsCard(
                        result.nextActions,
                        onAddTask = onAddTaskFromAction
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // Analyze button
            Button(
                onClick = onAnalyze,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = !isAnalyzing
            ) {
                Text(if (isAnalyzing) "Analizando..." else "Analizar Trabajo")
            }
        }
    }
}

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
 * Displays next actions with buttons to add tasks.
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
            Spacer(modifier = Modifier.height(8.dp))
            nextActions.forEach { action ->
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
                    // Only show "Agregar" button for task-related actions, not for archive/report actions
                    if (!action.contains("Archivar") && !action.contains("Generar reporte")) {
                        Button(
                            onClick = { onAddTask(action) },
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .height(32.dp)
                        ) {
                            Text(
                                "Agregar",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }
}
