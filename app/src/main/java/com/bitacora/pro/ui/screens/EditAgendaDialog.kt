package com.bitacora.pro.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bitacora.pro.data.models.AgendaItem
import com.bitacora.pro.data.models.AgendaStatus

/**
 * Dialog for editing an existing agenda item.
 * Allows editing title, description, due date/text, and reminder settings.
 * Preserves sourceEvidenceId and createdAt.
 * Handles validation and reminder rescheduling.
 *
 * @param item The agenda item to edit
 * @param onSave Callback when user saves changes (receives updated AgendaItem)
 * @param onDismiss Callback when dialog is dismissed without saving
 */
@Composable
fun EditAgendaDialog(
    item: AgendaItem,
    onSave: (AgendaItem) -> Unit,
    onDismiss: () -> Unit
) {
    // State for form fields
    val editTitle = remember { mutableStateOf(item.title) }
    val editDescription = remember { mutableStateOf(item.description) }
    val editDueText = remember { mutableStateOf(item.dueText) }
    val editDueAt = remember { mutableStateOf(item.dueAt) }
    val editReminderEnabled = remember { mutableStateOf(item.reminderEnabled) }
    val editReminderOffsetDays = remember { mutableStateOf(item.reminderOffsetDays) }
    val showDatePicker = remember { mutableStateOf(false) }
    val validationError = remember { mutableStateOf("") }

    // Show date picker if needed
    if (showDatePicker.value) {
        DatePickerDialog(
            onDateSelected = { timestamp, dateText ->
                editDueAt.value = timestamp
                editDueText.value = dateText
                showDatePicker.value = false
            },
            onDismiss = { showDatePicker.value = false }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Editar Elemento de Agenda",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Show validation error if present
                if (validationError.value.isNotEmpty()) {
                    Text(
                        validationError.value,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                // Title field
                TextField(
                    value = editTitle.value,
                    onValueChange = {
                        editTitle.value = it
                        validationError.value = "" // Clear error on change
                    },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Description field
                TextField(
                    value = editDescription.value,
                    onValueChange = { editDescription.value = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                // Due date section
                Text(
                    "Fecha de Vencimiento",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = editDueText.value,
                        onValueChange = { editDueText.value = it },
                        label = { Text("Texto de fecha") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Button(
                        onClick = { showDatePicker.value = true },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("📅")
                    }
                }

                // Reminder section (only for PENDING items)
                if (item.status == AgendaStatus.PENDING) {
                    Spacer(modifier = Modifier.height(8.dp))
                    ReminderDropdown(
                        reminderEnabled = editReminderEnabled.value,
                        reminderOffsetDays = editReminderOffsetDays.value,
                        onReminderEnabledChange = { editReminderEnabled.value = it },
                        onReminderOffsetChange = { editReminderOffsetDays.value = it }
                    )
                } else if (item.status == AgendaStatus.DONE) {
                    // For DONE items, show reminder info but don't allow editing
                    Text(
                        "Nota: Los recordatorios no se programan para tareas completadas",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else if (item.status == AgendaStatus.ARCHIVED) {
                    // For ARCHIVED items, show read-only message
                    Text(
                        "Nota: Las tareas archivadas no pueden ser editadas",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Status display
                Text(
                    "Estado: ${item.status.name}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Validate title
                    if (editTitle.value.isBlank()) {
                        validationError.value = "El título no puede estar vacío"
                        return@Button
                    }

                    // Validate reminder: if enabled, must have dueAt
                    if (editReminderEnabled.value && editDueAt.value == null) {
                        validationError.value = "Se requiere una fecha de vencimiento para programar recordatorio"
                        return@Button
                    }

                    // Create updated item
                    val updatedItem = item.copy(
                        title = editTitle.value,
                        description = editDescription.value,
                        dueText = editDueText.value,
                        dueAt = editDueAt.value,
                        reminderEnabled = editReminderEnabled.value,
                        reminderOffsetDays = editReminderOffsetDays.value
                    )

                    onSave(updatedItem)
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
