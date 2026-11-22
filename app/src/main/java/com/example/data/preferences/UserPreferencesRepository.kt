package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.model.AiEngineType
import com.example.domain.model.AiMode
import com.example.domain.model.ThemePreference
import com.example.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.veyroxDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "veyrox_settings"
)

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val AI_ENGINE = stringPreferencesKey("ai_engine")
        val STREAMING_ENABLED = booleanPreferencesKey("streaming_enabled")
        val DEFAULT_AI_MODE = stringPreferencesKey("default_ai_mode")
        val CUSTOM_BACKEND_URL = stringPreferencesKey("custom_backend_url")
    }

    val preferences: Flow<UserPreferences> = context.veyroxDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { prefs ->
            UserPreferences(
                themePreference = ThemePreference.fromName(prefs[Keys.THEME_MODE]),
                aiEngineType = AiEngineType.fromName(prefs[Keys.AI_ENGINE]),
                streamingEnabled = prefs[Keys.STREAMING_ENABLED] ?: true,
                defaultAiMode = AiMode.fromName(prefs[Keys.DEFAULT_AI_MODE]),
                customBackendUrl = prefs[Keys.CUSTOM_BACKEND_URL].orEmpty()
            )
        }

    suspend fun setThemePreference(theme: ThemePreference) {
        context.veyroxDataStore.edit { it[Keys.THEME_MODE] = theme.name }
    }

    suspend fun setAiEngineType(engine: AiEngineType) {
        context.veyroxDataStore.edit { it[Keys.AI_ENGINE] = engine.name }
    }

    suspend fun setStreamingEnabled(enabled: Boolean) {
        context.veyroxDataStore.edit { it[Keys.STREAMING_ENABLED] = enabled }
    }

    suspend fun setDefaultAiMode(mode: AiMode) {
        context.veyroxDataStore.edit { it[Keys.DEFAULT_AI_MODE] = mode.name }
    }

    suspend fun setCustomBackendUrl(url: String) {
        context.veyroxDataStore.edit { it[Keys.CUSTOM_BACKEND_URL] = url.trim() }
    }
}
