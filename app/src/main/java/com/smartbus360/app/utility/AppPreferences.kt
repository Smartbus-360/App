package com.smartbus360.app.utility

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first


object AppPreferences {
    private val Context.dataStore by preferencesDataStore(name = "smartbus_prefs")
    private val APP_LAUNCH_COUNT = intPreferencesKey("app_launch_count")
    private val REVIEW_SHOWN = booleanPreferencesKey("review_shown")

    suspend fun incrementLaunchCount(context: Context) {
        context.dataStore.edit { prefs ->
            val current = prefs[APP_LAUNCH_COUNT] ?: 0
            prefs[APP_LAUNCH_COUNT] = current + 1
        }
    }

    suspend fun getLaunchCount(context: Context): Int {
        val prefs = context.dataStore.data.first()
        return prefs[APP_LAUNCH_COUNT] ?: 0
    }

    suspend fun hasReviewBeenShown(context: Context): Boolean {
        val prefs = context.dataStore.data.first()
        return prefs[REVIEW_SHOWN] ?: false
    }

    suspend fun setReviewShown(context: Context) {
        context.dataStore.edit { prefs ->
            prefs[REVIEW_SHOWN] = true
        }
    }
}
