package com.chartyar.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "chartyar_prefs")

class PrefsManager(private val context: Context) {

    companion object {
        private val KEY_DISCLAIMER_ACCEPTED = booleanPreferencesKey("disclaimer_accepted")
        private val KEY_AUTO_SAVE_HISTORY = booleanPreferencesKey("auto_save_history")
        private val KEY_SHOW_WARNINGS = booleanPreferencesKey("show_warnings")
        private val KEY_DEFAULT_TIMEFRAME = stringPreferencesKey("default_timeframe")
        private val KEY_LANGUAGE = stringPreferencesKey("language")
        private val KEY_THEME = stringPreferencesKey("theme")
        private val KEY_TUTORIAL_SEEN = booleanPreferencesKey("tutorial_seen")
    }

    val isDisclaimerAccepted: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[KEY_DISCLAIMER_ACCEPTED] ?: false }

    val autoSaveHistory: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[KEY_AUTO_SAVE_HISTORY] ?: true }

    val showWarnings: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[KEY_SHOW_WARNINGS] ?: true }

    val defaultTimeframe: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[KEY_DEFAULT_TIMEFRAME] ?: "4H" }

    val language: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[KEY_LANGUAGE] ?: "fa" }

    val theme: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[KEY_THEME] ?: "dark" }

    val isTutorialSeen: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[KEY_TUTORIAL_SEEN] ?: false }

    suspend fun acceptDisclaimer() {
        context.dataStore.edit { prefs ->
            prefs[KEY_DISCLAIMER_ACCEPTED] = true
        }
    }

    suspend fun setAutoSaveHistory(value: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_AUTO_SAVE_HISTORY] = value
        }
    }

    suspend fun setShowWarnings(value: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SHOW_WARNINGS] = value
        }
    }

    suspend fun setDefaultTimeframe(value: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DEFAULT_TIMEFRAME] = value
        }
    }

    suspend fun setLanguage(value: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LANGUAGE] = value
        }
    }

    suspend fun setTheme(value: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_THEME] = value
        }
    }

    suspend fun setTutorialSeen(value: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_TUTORIAL_SEEN] = value
        }
    }

    suspend fun resetDisclaimer() {
        context.dataStore.edit { prefs ->
            prefs[KEY_DISCLAIMER_ACCEPTED] = false
        }
    }

    suspend fun resetTutorial() {
        context.dataStore.edit { prefs ->
            prefs[KEY_TUTORIAL_SEEN] = false
        }
    }
}