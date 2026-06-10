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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * AssistantScreen displays smart assistant actions for job management.
 * v0.8.1: Daily Copilot with real, actionable workflows
 *
 * Real Workflows:
 * - Revisar faltantes → Navigate to Pendientes to review overdue/missing items
 * - Capturar evidencia → Navigate to CreateJob for quick capture
 * - Preparar reporte → Navigate to Home to select job for report generation
 * - Cerrar actividad → Navigate to Home to mark jobs as completed
 *
 * Hard rule: Every action performs real work, not placeholder navigation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistantScreen(
    onBack: () -> Unit,
    onReviewMissing: () -> Unit,
    onCaptureEvidence: () -> Unit,
    onPrepareReport: () -> Unit,
    onCloseActivity: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Daily Copilot", fontSize = 12.sp, fontWeight = FontWeight.Light)
                        Text("🤖 Asistente", fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
            // Assistant intro section
            AssistantIntroSection()
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    AssistantActionCard(
                        icon = "🔍",
                        title = "Revisar Faltantes",
                        description = "Revisa tareas pendientes, vencidas y sin fecha",
                        actionLabel = "Ir a Pendientes",
                        onClick = onReviewMissing
                    )
                }

                item {
                    AssistantActionCard(
                        icon = "📸",
                        title = "Capturar Evidencia",
                        description = "Captura rápida de fotos, documentos o notas",
                        actionLabel = "Capturar ahora",
                        onClick = onCaptureEvidence
                    )
                }

                item {
                    AssistantActionCard(
                        icon = "📊",
                        title = "Preparar Reporte",
                        description = "Genera reportes PDF de tus actividades",
                        actionLabel = "Seleccionar actividad",
                        onClick = onPrepareReport
                    )
                }

                item {
                    AssistantActionCard(
                        icon = "✅",
                        title = "Cerrar Actividad",
                        description = "Marca actividades como completadas",
                        actionLabel = "Ir a actividades",
                        onClick = onCloseActivity
                    )
                }
            }
        }
    }
}

/**
 * Assistant intro section explaining the purpose
 */
@Composable
private fun AssistantIntroSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            "Tu asistente diario",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Acciones rápidas para gestionar tus actividades",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Individual assistant action card with real action label.
 * v0.8.1: Each card has a specific, meaningful action button
 */
@Composable
private fun AssistantActionCard(
    icon: String,
    title: String,
    description: String,
    actionLabel: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(icon, fontSize = 32.sp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(actionLabel)
            }
        }
    }
}
