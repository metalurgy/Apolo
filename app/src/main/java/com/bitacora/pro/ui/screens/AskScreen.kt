package com.bitacora.pro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bitacora.pro.R
import com.bitacora.pro.assistant.LocalAssistantProvider
import kotlinx.coroutines.launch

/**
 * AskScreen provides a chat-like interface for asking the local AI assistant questions.
 * v0.9.0: Local-first assistant with offline capability
 *
 * Features:
 * - Ask questions about how to use Bitacora Pro
 * - Get instant answers from LocalAssistantProvider
 * - No internet required
 * - Conversation history in current session
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AskScreen(onBackClick: () -> Unit) {
    val messages = remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    val inputText = remember { mutableStateOf("") }
    val isLoading = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top bar
        TopAppBar(
            title = { Text(stringResource(R.string.assistant_ask_title)) },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
            )
        )

        // Messages list
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            reverseLayout = false
        ) {
            items(messages.value) { message ->
                ChatMessageBubble(message)
            }
        }

        // Input area
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Offline mode indicator
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    stringResource(R.string.assistant_offline_mode),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(8.dp)
                )
            }

            // Input field with send button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                TextField(
                    value = inputText.value,
                    onValueChange = { inputText.value = it },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp, max = 120.dp),
                    placeholder = { Text(stringResource(R.string.assistant_ask_hint)) },
                    enabled = !isLoading.value,
                    singleLine = false,
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
                IconButton(
                    onClick = {
                        if (inputText.value.isNotBlank()) {
                            val question = inputText.value
                            inputText.value = ""
                            
                            // Add user message
                            messages.value = messages.value + ChatMessage(
                                text = question,
                                isUser = true
                            )
                            
                            // Get assistant response
                            scope.launch {
                                isLoading.value = true
                                val answer = LocalAssistantProvider.answerQuestion(question)
                                    ?: "No tengo una respuesta para esa pregunta. Intenta preguntar sobre cómo usar Bitacora Pro."
                                messages.value = messages.value + ChatMessage(
                                    text = answer,
                                    isUser = false
                                )
                                isLoading.value = false
                            }
                        }
                    },
                    enabled = !isLoading.value && inputText.value.isNotBlank()
                ) {
                    Icon(Icons.Filled.Send, contentDescription = "Enviar")
                }
            }
        }
    }
}

/**
 * Chat message bubble for displaying user and assistant messages
 */
@Composable
private fun ChatMessageBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .padding(4.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isUser)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodySmall,
                color = if (message.isUser)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

/**
 * Data class for chat messages
 */
data class ChatMessage(
    val text: String,
    val isUser: Boolean
)
