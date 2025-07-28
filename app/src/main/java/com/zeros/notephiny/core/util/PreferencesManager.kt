package com.zeros.notephiny.core.util


import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject

private val Context.dataStore by preferencesDataStore(name = "settings")

class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val WELCOME_NOTE_ADDED = booleanPreferencesKey("welcome_note_added")

    suspend fun hasAddedWelcomeNote(): Boolean {
        val prefs = context.dataStore.data.first()
        return prefs[WELCOME_NOTE_ADDED] ?: false
    }

    suspend fun setWelcomeNoteAdded() {
        context.dataStore.edit { settings ->
            settings[WELCOME_NOTE_ADDED] = true
        }
    }
}
