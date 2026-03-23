package com.forestmood.app.data.local

import android.content.Context

class AppPreferences(context: Context) {
    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun hasAcceptedDisclaimer(): Boolean = preferences.getBoolean(KEY_DISCLAIMER_ACCEPTED, false)

    fun setDisclaimerAccepted() {
        preferences.edit().putBoolean(KEY_DISCLAIMER_ACCEPTED, true).apply()
    }

    companion object {
        private const val PREFS_NAME = "forest_mood_prefs"
        private const val KEY_DISCLAIMER_ACCEPTED = "disclaimer_accepted"
    }
}
