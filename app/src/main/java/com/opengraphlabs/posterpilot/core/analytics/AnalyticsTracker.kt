package com.opengraphlabs.posterpilot.core.analytics

interface AnalyticsTracker {
    fun track(event: String, params: Map<String, String> = emptyMap())
}

object AnalyticsEvents {
    const val BusinessProfileCompleted = "business_profile_completed"
    const val HomeViewed = "home_viewed"
    const val TemplateOpened = "template_opened"
    const val EditorOpened = "editor_opened"
    const val AiCopyRequested = "ai_copy_requested"
    const val AiCopySuccess = "ai_copy_success"
    const val AiCopyFailed = "ai_copy_failed"
    const val ExportStarted = "export_started"
    const val ExportSuccess = "export_success"
    const val ExportFailed = "export_failed"
    const val ShareSheetOpened = "share_sheet_opened"
    const val HistoryOpened = "history_opened"
    const val HistoryShareOpened = "history_share_opened"
}
