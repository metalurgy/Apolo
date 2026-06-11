package com.bitacora.pro

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bitacora.pro.data.models.EvidenceType
import com.bitacora.pro.data.models.SharedFileDescriptor
import com.bitacora.pro.data.storage.StorageManager
import com.bitacora.pro.ui.navigation.NavRoutes
import com.bitacora.pro.data.storage.InboxManager
import com.bitacora.pro.ui.screens.AboutScreen
import com.bitacora.pro.ui.screens.AskScreen
import com.bitacora.pro.ui.screens.AssistantScreen
import com.bitacora.pro.ui.screens.CreateJobScreen
import com.bitacora.pro.ui.screens.DailyAgendaScreen
import com.bitacora.pro.ui.screens.HomeScreen
import com.bitacora.pro.ui.screens.InboxScreen
import com.bitacora.pro.ui.screens.JobDetailScreen
import com.bitacora.pro.ui.screens.ShareIntakeScreen
import com.bitacora.pro.ui.screens.SharedContent
import com.bitacora.pro.ui.screens.WelcomeScreen
import com.bitacora.pro.ui.theme.BitacoraProTheme

/**
 * MainActivity is the single activity for the app.
 * It handles share intents and manages navigation between screens.
 *
 * Share Intent Flow:
 * 1. User shares content from WhatsApp/Gallery
 * 2. MainActivity receives ACTION_SEND or ACTION_SEND_MULTIPLE
 * 3. Extract text and file URIs (do NOT copy files yet)
 * 4. Create SharedContent with pending file descriptors
 * 5. Navigate to ShareIntakeScreen
 * 6. User creates new job or selects existing job
 * 7. Files are copied to the final job folder
 * 8. Evidence metadata is saved
 */
class MainActivity : ComponentActivity() {

    companion object {
        private const val DEBUG_SHARE_INTENTS = false // Gate debug logs for pilot testing
    }

    private lateinit var storageManager: StorageManager
    private lateinit var inboxManager: InboxManager
    private val pendingSharedContent = mutableStateOf<SharedContent?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        storageManager = StorageManager(this)
        inboxManager = InboxManager(this)

        // Create notification channel for agenda reminders
        createNotificationChannel()

        // Extract shared content from intent BEFORE setContent
        val extractedContent = extractSharedContentFromIntent(intent)
        if (DEBUG_SHARE_INTENTS && extractedContent != null) {
            Log.d("MainActivity", "onCreate: Extracted shared content - files: ${extractedContent.sharedFiles.size}, text length: ${extractedContent.textContent.length}")
        }
        pendingSharedContent.value = extractedContent

        setContent {
            BitacoraProTheme {
                val navController = rememberNavController()
                val sharedContent = pendingSharedContent

                NavHost(navController = navController, startDestination = NavRoutes.WELCOME) {
                    composable(NavRoutes.WELCOME) {
                        WelcomeScreen(
                            onContinue = {
                                navController.navigate(NavRoutes.HOME) {
                                    popUpTo(NavRoutes.WELCOME) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(NavRoutes.HOME) {
                        HomeScreen(
                            storageManager = storageManager,
                            onCreateJobClick = {
                                sharedContent.value = null
                                navController.navigate(NavRoutes.CREATE_JOB)
                            },
                            onJobClick = { jobId ->
                                navController.navigate(NavRoutes.jobDetailRoute(jobId))
                            },
                            onCaptureClick = {
                                navController.navigate(NavRoutes.CREATE_JOB)
                            },
                            onInboxClick = {
                                navController.navigate(NavRoutes.INBOX)
                            },
                            onAgendaClick = {
                                navController.navigate(NavRoutes.DAILY_AGENDA)
                            },
                            onAssistantClick = {
                                navController.navigate(NavRoutes.ASSISTANT)
                            },
                            onAboutClick = {
                                navController.navigate(NavRoutes.ABOUT)
                            }
                        )
                    }

                    composable(NavRoutes.SHARE_INTAKE) {
                        ShareIntakeScreen(
                            storageManager = storageManager,
                            sharedContent = sharedContent.value ?: SharedContent(),
                            onCreateNewJob = { content ->
                                sharedContent.value = content
                                navController.navigate(NavRoutes.CREATE_JOB) {
                                    popUpTo(NavRoutes.SHARE_INTAKE) { inclusive = true }
                                }
                            },
                            onAddToExistingJob = { jobId, content ->
                                // Copy all shared files to the selected job
                                copySharedContentToJob(jobId, content)
                                sharedContent.value = null
                                navController.navigate(NavRoutes.jobDetailRoute(jobId)) {
                                    popUpTo(NavRoutes.HOME) { inclusive = false }
                                }
                            },
                            onBack = {
                                sharedContent.value = null
                                navController.popBackStack()
                            }
                        )
                    }

                    composable(NavRoutes.CREATE_JOB) {
                        CreateJobScreen(
                            storageManager = storageManager,
                            sharedContent = sharedContent.value,
                            onJobCreated = { jobId ->
                                sharedContent.value = null
                                navController.navigate(NavRoutes.jobDetailRoute(jobId)) {
                                    popUpTo(NavRoutes.HOME) { inclusive = false }
                                }
                            },
                            onBack = {
                                sharedContent.value = null
                                navController.popBackStack()
                            }
                        )
                    }

                    composable(NavRoutes.JOB_DETAIL) { backStackEntry ->
                        val jobId = backStackEntry.arguments?.getString("jobId") ?: return@composable
                        JobDetailScreen(
                            jobId = jobId,
                            storageManager = storageManager,
                            onBack = {
                                navController.popBackStack()
                            }
                        )
                    }

                    composable(NavRoutes.INBOX) {
                        InboxScreen(
                            inboxManager = inboxManager,
                            onBack = {
                                navController.popBackStack()
                            },
                            onAssignToJob = { inboxItem ->
                                // Navigate to job detail or create job with inbox item
                                navController.navigate(NavRoutes.CREATE_JOB)
                            }
                        )
                    }

                    composable(NavRoutes.DAILY_AGENDA) {
                        DailyAgendaScreen(
                            storageManager = storageManager,
                            onBack = {
                                navController.popBackStack()
                            },
                            onAddAgendaItem = {
                                // Navigate to create job or job detail to add agenda item
                                navController.navigate(NavRoutes.HOME)
                            }
                        )
                    }

                    composable(NavRoutes.ASSISTANT) {
                        AssistantScreen(
                            onBack = {
                                navController.popBackStack()
                            },
                            onReviewMissing = {
                                // Real workflow: Navigate to Pendientes to review overdue/missing items
                                navController.navigate(NavRoutes.DAILY_AGENDA) {
                                    popUpTo(NavRoutes.ASSISTANT) { inclusive = true }
                                }
                            },
                            onCaptureEvidence = {
                                // Real workflow: Navigate to CreateJob for quick evidence capture
                                navController.navigate(NavRoutes.CREATE_JOB) {
                                    popUpTo(NavRoutes.ASSISTANT) { inclusive = true }
                                }
                            },
                            onPrepareReport = {
                                // Real workflow: Navigate to Home to select job for report generation
                                navController.navigate(NavRoutes.HOME) {
                                    popUpTo(NavRoutes.ASSISTANT) { inclusive = true }
                                }
                            },
                            onCloseActivity = {
                                // Real workflow: Navigate to Home to mark jobs as completed
                                navController.navigate(NavRoutes.HOME) {
                                    popUpTo(NavRoutes.ASSISTANT) { inclusive = true }
                                }
                            },
                            onAskQuestion = {
                                navController.navigate(NavRoutes.ASK)
                            }
                        )
                    }

                    composable(NavRoutes.ASK) {
                        AskScreen(
                            onBackClick = {
                                navController.popBackStack()
                            }
                        )
                    }

                    composable(NavRoutes.ABOUT) {
                        AboutScreen(
                            onBackClick = {
                                navController.popBackStack()
                            },
                            storageManager = storageManager
                        )
                    }
                }

                // Navigate to share intake AFTER NavHost is composed
                // If there's shared content, skip welcome and go directly to share intake
                LaunchedEffect(sharedContent.value) {
                    if (sharedContent.value != null) {
                        if (DEBUG_SHARE_INTENTS) Log.d("MainActivity", "LaunchedEffect: Navigating to SHARE_INTAKE")
                        navController.navigate(NavRoutes.SHARE_INTAKE) {
                            launchSingleTop = true
                            popUpTo(NavRoutes.WELCOME) { inclusive = true }
                        }
                    }
                }
            }
        }
    }

    /**
     * Extracts shared content from the incoming intent.
     * Returns SharedContent if ACTION_SEND or ACTION_SEND_MULTIPLE, null otherwise.
     */
    private fun extractSharedContentFromIntent(intent: Intent?): SharedContent? {
        return when (intent?.action) {
            Intent.ACTION_SEND -> {
                if (DEBUG_SHARE_INTENTS) Log.d("MainActivity", "extractSharedContentFromIntent: ACTION_SEND")
                handleShareIntent(intent)
            }
            Intent.ACTION_SEND_MULTIPLE -> {
                if (DEBUG_SHARE_INTENTS) Log.d("MainActivity", "extractSharedContentFromIntent: ACTION_SEND_MULTIPLE")
                handleShareMultipleIntent(intent)
            }
            else -> {
                if (DEBUG_SHARE_INTENTS) Log.d("MainActivity", "extractSharedContentFromIntent: No share action")
                null
            }
        }
    }

    /**
     * Handles ACTION_SEND intent (single file or text).
     * Extracts text and file URI without copying files.
     */
    private fun handleShareIntent(intent: Intent): SharedContent {
        val sharedFiles = mutableListOf<SharedFileDescriptor>()
        var textContent = ""

        // Handle text content
        intent.getStringExtra(Intent.EXTRA_TEXT)?.let { text ->
            textContent = text
        }

        // Handle single file
        intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)?.let { uri ->
            // Resolve MIME type: try ContentResolver first, fall back to intent.type
            val mimeType = contentResolver.getType(uri) ?: intent.type ?: "application/octet-stream"
            sharedFiles.add(SharedFileDescriptor(uri, mimeType))
        }

        return SharedContent(
            textContent = textContent,
            sharedFiles = sharedFiles
        )
    }

    /**
     * Handles ACTION_SEND_MULTIPLE intent (multiple files).
     * Extracts file URIs without copying files.
     */
    private fun handleShareMultipleIntent(intent: Intent): SharedContent {
        val sharedFiles = mutableListOf<SharedFileDescriptor>()

        intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)?.forEach { uri ->
            // Resolve MIME type: try ContentResolver first, fall back to intent.type
            val mimeType = contentResolver.getType(uri) ?: intent.type ?: "application/octet-stream"
            sharedFiles.add(SharedFileDescriptor(uri, mimeType))
        }

        return SharedContent(sharedFiles = sharedFiles)
    }

    /**
     * Copies all shared content (files and text) to the specified job.
     * This is called when user selects an existing job in ShareIntakeScreen.
     */
    private fun copySharedContentToJob(jobId: String, content: SharedContent) {
        // Copy all shared files to the job
        content.sharedFiles.forEach { fileDescriptor ->
            val evidenceType = getEvidenceTypeFromMimeType(fileDescriptor.mimeType)
            val evidence = storageManager.copyEvidenceFromUri(
                jobId = jobId,
                uri = fileDescriptor.uri,
                evidenceType = evidenceType,
                mimeType = fileDescriptor.mimeType
            )
            if (evidence != null) {
                storageManager.addEvidenceToJob(jobId, evidence)
            }
        }

        // Store shared text as TEXT evidence
        if (content.textContent.isNotEmpty()) {
            val textEvidence = storageManager.saveTextEvidence(jobId, content.textContent)
            storageManager.addEvidenceToJob(jobId, textEvidence)
        }
    }

    /**
     * Determines EvidenceType from MIME type.
     */
    private fun getEvidenceTypeFromMimeType(mimeType: String): EvidenceType {
        return when {
            mimeType.startsWith("image/") -> EvidenceType.IMAGE
            mimeType.startsWith("audio/") -> EvidenceType.AUDIO
            mimeType.contains("pdf") -> EvidenceType.PDF
            else -> EvidenceType.TEXT
        }
    }

    /**
     * Handle new intents when app is already running.
     * If a new share intent arrives, update the observable Compose state.
     * This triggers LaunchedEffect to navigate to ShareIntakeScreen.
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val extractedContent = extractSharedContentFromIntent(intent)
        if (DEBUG_SHARE_INTENTS && extractedContent != null) {
            Log.d("MainActivity", "onNewIntent: Updating pendingSharedContent - files: ${extractedContent.sharedFiles.size}, text length: ${extractedContent.textContent.length}")
        }
        pendingSharedContent.value = extractedContent
    }

    /**
     * Creates the notification channel for agenda reminders.
     * Required for Android 8.0 (API 26) and above.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "agenda_reminders"
            val channelName = "Recordatorios de Agenda"
            val channelDescription = "Notificaciones de recordatorio para elementos de agenda"
            val importance = NotificationManager.IMPORTANCE_HIGH

            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
                enableVibration(true)
                enableLights(true)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}
