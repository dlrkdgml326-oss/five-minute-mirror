package com.fiveminutemirror

import android.content.Context

class ProtectionStateStore(context: Context) {
    private val prefs = context.getSharedPreferences("protection_state", Context.MODE_PRIVATE)

    var enabled: Boolean
        get() = prefs.getBoolean(KEY_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_ENABLED, value).apply()

    var allowedPackage: String?
        get() = prefs.getString(KEY_ALLOWED_PACKAGE, null)
        set(value) = prefs.edit().putString(KEY_ALLOWED_PACKAGE, value).apply()

    fun clearAllowance() {
        prefs.edit().remove(KEY_ALLOWED_PACKAGE).apply()
    }

    companion object {
        private const val KEY_ENABLED = "enabled"
        private const val KEY_ALLOWED_PACKAGE = "allowed_package"
    }
}
