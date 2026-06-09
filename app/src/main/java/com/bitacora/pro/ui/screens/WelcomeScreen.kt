package com.bitacora.pro.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Professional welcome/splash screen for Bitacora Pro.
 * Displays app branding and key features before navigating to home.
 */
@Composable
fun WelcomeScreen(
    onContinue: () -> Unit
) {
    val showContent = remember { mutableStateOf(false) }
    val showButton = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(300)
        showContent.value = true
        delay(800)
        showButton.value = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = Color(0xFF00897B)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo/Title
            AnimatedVisibility(
                visible = showContent.value,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it })
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Bitacora Pro",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Gestión Profesional de Trabajos",
                        fontSize = 18.sp,
                        color = Color(0xFFB2EBE7),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Features
            AnimatedVisibility(
                visible = showContent.value,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it })
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    FeatureItem("📋", "Organiza tus trabajos", "Crea y gestiona proyectos con facilidad")
                    Spacer(modifier = Modifier.height(20.dp))
                    FeatureItem("📸", "Captura evidencia", "Fotos, audio, texto y documentos")
                    Spacer(modifier = Modifier.height(20.dp))
                    FeatureItem("📅", "Agenda tareas", "Seguimiento de pendientes y vencimientos")
                    Spacer(modifier = Modifier.height(20.dp))
                    FeatureItem("📄", "Genera reportes", "Reportes PDF profesionales")
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Continue Button
            AnimatedVisibility(
                visible = showButton.value,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it })
            ) {
                Button(
                    onClick = onContinue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        "Comenzar",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Version info
            Text(
                "v0.6",
                fontSize = 12.sp,
                color = Color(0xFFB2EBE7),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Individual feature item with icon, title, and description.
 */
@Composable
private fun FeatureItem(
    icon: String,
    title: String,
    description: String
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            "$icon $title",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            description,
            fontSize = 14.sp,
            color = Color(0xFFB2EBE7)
        )
    }
}
