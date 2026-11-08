package com.tarkeez.app

/**
 * Cloud integration is deliberately OFF until a secure backend exists.
 * No API keys or privileged database credentials belong in the APK.
 */
object CloudConfig {
    const val AI_ENABLED = false
    const val CONTENT_SYNC_ENABLED = false
    const val MONTHLY_AI_REQUEST_LIMIT = 10
    const val PILOT_MONTHLY_BUDGET_SAR = 50
    const val API_BASE_URL = "" // HTTPS backend endpoint after deployment
    const val OFFLINE_FIRST = true
}
