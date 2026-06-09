package com.bitacora.pro

import android.content.Intent
import android.net.Uri
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
import com.bitacora.pro.ui.screens.CreateJobScreen
import com.bitacora.pro.ui.screens.HomeScreen
import com.bitacora.pro.ui.screens.JobDetailScreen
import com.bitacora.pro.ui.screens.ShareIntakeScreen
import com.bitacora.pro.ui.screens.SharedContent
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
    private val pendingSharedContent = mutableStateOf<SharedContent?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        storageManager = StorageManager(this)

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

                NavHost(navController = navController, startDestination = NavRoutes.HOME) {
                    composable(NavRoutes.HOME) {
                        HomeScreen(
                            storageManager = storageManager,
                            onCreateJobClick = {
                                sharedContent.value = null
                                navController.navigate(NavRoutes.CREATE_JOB)
                            },
                            onJobClick = { jobId ->
                                navController.navigate(NavRoutes.jobDetailRoute(jobId))
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
                }

                // Navigate to share intake AFTER NavHost is composed
                LaunchedEffect(sharedContent.value) {
                    if (sharedContent.value != null) {
                        if (DEBUG_SHARE_INTENTS) Log.d("MainActivity", "LaunchedEffect: Navigating to SHARE_INTAKE")
                        navController.navigate(NavRoutes.SHARE_INTAKE) {
                            launchSingleTop = true
                            popUpTo(NavRoutes.HOME) { inclusive = false }
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
}
