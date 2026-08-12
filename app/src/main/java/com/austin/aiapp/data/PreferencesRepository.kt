package com.austin.aiapp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesRepository(private val context: Context) {

    private val SERVER_URL = stringPreferencesKey("server_url")
    private val MODEL_NAME = stringPreferencesKey("model_name")

    val serverUrl: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[SERVER_URL] ?: "http://100.x.x.x:11434/v1"
    }

    val modelName: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[MODEL_NAME] ?: "gemma-4-abliterated"
    }

    suspend fun setServerUrl(url: String) {
        context.dataStore.edit { it[SERVER_URL] = url }
    }

    suspend fun setModelName(name: String) {
        context.dataStore.edit { it[MODEL_NAME] = name }
    }
}
