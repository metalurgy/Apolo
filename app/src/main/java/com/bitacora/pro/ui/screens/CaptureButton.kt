package com.bitacora.pro.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Universal Capture Button with menu for quick actions.
 * v0.8.0: Provides quick access to capture, inbox, agenda, and assistant features
 *
 * Features:
 * - Floating action button with menu
 * - Options: Capture Photo, Capture Audio, Capture Text, Create Job
 * - Accessible from any screen
 * - Clean, minimal design
 */
@Composable
fun UniversalCaptureButton(
    onCapturePhoto: () -> Unit,
    onCaptureAudio: () -> Unit,
    onCaptureText: () -> Unit,
    onCreateJob: () -> Unit
) {
    val showMenu = remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.End
    ) {
        if (showMenu.value) {
            CaptureMenuItems(
                onCapturePhoto = {
                    onCapturePhoto()
                    showMenu.value = false
                },
                onCaptureAudio = {
                    onCaptureAudio()
                    showMenu.value = false
                },
                onCaptureText = {
                    onCaptureText()
                    showMenu.value = false
                },
                onCreateJob = {
                    onCreateJob()
                    showMenu.value = false
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        FloatingActionButton(
            onClick = { showMenu.value = !showMenu.value },
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Capturar contenido")
        }
    }
}

/**
 * Menu items for capture button.
 * v0.8.2: Shows 4 quick action options with clear labels
 * CRITICAL FIX: Separated "Capturar" (evidence) from "Nueva actividad" (job creation)
 */
@Composable
private fun CaptureMenuItems(
    onCapturePhoto: () -> Unit,
    onCaptureAudio: () -> Unit,
    onCaptureText: () -> Unit,
    onCreateJob: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.End
    ) {
        CaptureMenuItem(
            icon = "📸",
            label = "Tomar foto",
            onClick = onCapturePhoto,
            description = "Captura rápida"
        )
        Spacer(modifier = Modifier.height(8.dp))

        CaptureMenuItem(
            icon = "🎙️",
            label = "Grabar audio",
            onClick = onCaptureAudio,
            description = "Captura rápida"
        )
        Spacer(modifier = Modifier.height(8.dp))

        CaptureMenuItem(
            icon = "📝",
            label = "Escribir nota",
            onClick = onCaptureText,
            description = "Captura rápida"
        )
        Spacer(modifier = Modifier.height(8.dp))

        CaptureMenuItem(
            icon = "✨",
            label = "Nueva actividad",
            onClick = onCreateJob,
            description = "Crear actividad"
        )
    }
}

/**
 * Individual capture menu item.
 * v0.8.2: Minimal, clean design with emoji icon, label, and description
 */
@Composable
private fun CaptureMenuItem(
    icon: String,
    label: String,
    onClick: () -> Unit,
    description: String = ""
) {
    androidx.compose.material3.Button(
        onClick = onClick,
        modifier = Modifier.height(40.dp)
    ) {
        Text(icon, fontSize = 14.sp)
        Spacer(modifier = Modifier.width(4.dp))
        Column {
            Text(label, fontSize = 12.sp)
            if (description.isNotEmpty()) {
                Text(description, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
