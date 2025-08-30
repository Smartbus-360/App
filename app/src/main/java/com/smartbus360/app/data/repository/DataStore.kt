package com.smartbus360.app.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Initialize DataStore
val Context.dataStore by preferencesDataStore("settings")

// Define the key for storing alert status
private val ALERT_STATUS_KEY = stringPreferencesKey("alert_status_key")

// Gson instance for JSON serialization
val gson = Gson()

suspend fun saveAlertStatus(context: Context, alertStatus: List<Boolean>) {
    val json = gson.toJson(alertStatus)
    context.dataStore.edit { preferences ->
        preferences[ALERT_STATUS_KEY] = json
    }
}

fun getAlertStatus(context: Context, defaultSize: Int): Flow<List<Boolean>> {
    return context.dataStore.data.map { preferences ->
        val json = preferences[ALERT_STATUS_KEY] ?: "[]"
        val type = object : TypeToken<List<Boolean>>() {}.type
        val list = gson.fromJson<List<Boolean>>(json, type)
        list ?: List(defaultSize) { false }
    }
}