package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.GharTakConstants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.ghartakDataStore by preferencesDataStore(name = "ghartak_prefs")

class PreferencesRepository(private val context: Context) {
    private val keySelectedCity = stringPreferencesKey("selected_city")
    private val keyDarkMode = booleanPreferencesKey("dark_mode")
    private val keyNotificationsEnabled = booleanPreferencesKey("notifications_enabled")
    private val keyOnboardingSeen = booleanPreferencesKey("onboarding_seen")

    val selectedCity: Flow<String> = context.ghartakDataStore.data.map { prefs ->
        prefs[keySelectedCity] ?: GharTakConstants.DEFAULT_CITY
    }

    val darkMode: Flow<Boolean> = context.ghartakDataStore.data.map { prefs ->
        prefs[keyDarkMode] ?: false
    }

    val notificationsEnabled: Flow<Boolean> = context.ghartakDataStore.data.map { prefs ->
        prefs[keyNotificationsEnabled] ?: true
    }

    val onboardingSeen: Flow<Boolean> = context.ghartakDataStore.data.map { prefs ->
        prefs[keyOnboardingSeen] ?: false
    }

    suspend fun setSelectedCity(city: String) {
        context.ghartakDataStore.edit { it[keySelectedCity] = city }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.ghartakDataStore.edit { it[keyDarkMode] = enabled }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.ghartakDataStore.edit { it[keyNotificationsEnabled] = enabled }
    }

    suspend fun setOnboardingSeen(seen: Boolean) {
        context.ghartakDataStore.edit { it[keyOnboardingSeen] = seen }
    }
}
