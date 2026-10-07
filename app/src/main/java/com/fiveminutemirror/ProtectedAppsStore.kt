package com.fiveminutemirror

import android.content.Context

class ProtectedAppsStore(context: Context) {
    private val prefs = context.getSharedPreferences("protected_apps", Context.MODE_PRIVATE)

    fun load(): Set<String> =
        prefs.getStringSet(KEY, emptySet())?.toSet() ?: emptySet()

    fun save(packages: Set<String>) {
        prefs.edit().putStringSet(KEY, packages).apply()
    }

    companion object {
        private const val KEY = "packages"
    }
}
