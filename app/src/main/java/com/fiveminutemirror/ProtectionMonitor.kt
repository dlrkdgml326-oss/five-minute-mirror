package com.fiveminutemirror

import android.content.Context

class ProtectionMonitor(context: Context) {
    private val appContext = context.applicationContext
    private val detector = ForegroundAppDetector(appContext)
    private val protectedApps = ProtectedAppsStore(appContext)

    fun protectedForegroundApp(): String? {
        val current = detector.mostRecentForegroundPackage() ?: return null
        if (current == appContext.packageName) return null
        return current.takeIf { it in protectedApps.load() }
    }
}
