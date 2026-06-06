package com.bitacora.pro.ui.navigation

/**
 * Navigation routes for the app.
 */
object NavRoutes {
    const val HOME = "home"
    const val SHARE_INTAKE = "share_intake"
    const val CREATE_JOB = "create_job"
    const val JOB_DETAIL = "job_detail/{jobId}"
    const val JOB_DETAIL_ROUTE = "job_detail"

    fun jobDetailRoute(jobId: String) = "job_detail/$jobId"
}
