package com.fiveminutemirror

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context

class ForegroundAppDetector(context: Context) {
    private val usageStats =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    fun mostRecentForegroundPackage(lookBackMillis: Long = 5_000L): String? {
        val end = System.currentTimeMillis()
        val events = usageStats.queryEvents(end - lookBackMillis, end)
        val event = UsageEvents.Event()
        var latestPackage: String? = null
        var latestTime = Long.MIN_VALUE

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            val isForeground =
                event.eventType == UsageEvents.Event.ACTIVITY_RESUMED ||
                    event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND

            if (isForeground && event.timeStamp > latestTime) {
                latestTime = event.timeStamp
                latestPackage = event.packageName
            }
        }
        return latestPackage
    }
}
