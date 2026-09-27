package com.chartyar.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "chartyar_prefs")

class PrefsManager(private val context: Context) {

    companion object {
        private val KEY_DISCLAIMER_ACCEPTED = booleanPreferencesKey("disclaimer_accepted")
    }

    val isDisclaimerAccepted: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[KEY_DISCLAIMER_ACCEPTED] ?: false }

    suspend fun acceptDisclaimer() {
        context.dataStore.edit { prefs ->
            prefs[KEY_DISCLAIMER_ACCEPTED] = true
        }
    }
}