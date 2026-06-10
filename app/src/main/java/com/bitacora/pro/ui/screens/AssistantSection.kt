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
 * Compact Assistant Section for JobDetailScreen.
 * 
 * Features (v0.7.3):
 * - Action-based, not text-based
 * - Progressive disclosure (collapsed by default)
 * - Shows only actionable items
 * - Minimal clutter
 * - Professional appearance
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "🤖 Asistente",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

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

            // Show results if available
            if (result != null) {
                // Show next actions (most important)
                if (result.nextActions.isNotEmpty()) {
                    Text(
                        "Acciones Sugeridas",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        result.nextActions.take(3).forEach { action ->
                            ActionButton(
                                action = action,
                                onAddTask = { onAddTaskFromAction(action) }
                            )
                        }
                    }
                }

                // Show risks if any
                if (result.risks.isNotEmpty()) {
                    Text(
                        "Puntos de Atención",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        result.risks.take(2).forEach { risk ->
                            Text(
                                risk,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
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
                Text(if (isAnalyzing) "Analizando..." else "Analizar Actividad")
            }
        }
    }
}

/**
 * Compact action button for assistant suggestions.
 */
@Composable
private fun ActionButton(
    action: String,
    onAddTask: () -> Unit
) {
    Button(
        onClick = onAddTask,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            action.take(50) + if (action.length > 50) "..." else "",
            style = MaterialTheme.typography.labelSmall
        )
    }
}
