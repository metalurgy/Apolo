package com.bitacora.pro.ui.navigation

/**
 * Navigation routes for the app.
 * v0.8.0: Added inbox, agenda, and assistant routes
 */
object NavRoutes {
    const val WELCOME = "welcome"
    const val HOME = "home"
    const val SHARE_INTAKE = "share_intake"
    const val CREATE_JOB = "create_job"
    const val JOB_DETAIL = "job_detail/{jobId}"
    const val JOB_DETAIL_ROUTE = "job_detail"
    const val INBOX = "inbox"
    const val DAILY_AGENDA = "daily_agenda"
    const val ASSISTANT = "assistant"

    fun jobDetailRoute(jobId: String) = "job_detail/$jobId"
}
