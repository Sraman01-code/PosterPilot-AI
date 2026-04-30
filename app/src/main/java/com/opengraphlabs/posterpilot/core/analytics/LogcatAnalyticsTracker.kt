package com.opengraphlabs.posterpilot.core.analytics

import android.util.Log

class LogcatAnalyticsTracker : AnalyticsTracker {
    override fun track(event: String, params: Map<String, String>) {
        Log.d(TAG, "event=$event params=$params")
    }

    private companion object {
        const val TAG = "PosterPilotAnalytics"
    }
}
