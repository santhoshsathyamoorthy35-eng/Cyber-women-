package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.util.AppLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "cyber_women_prefs")

class PreferencesStore(private val context: Context) {

    private object Keys {
        val LANGUAGE = stringPreferencesKey("language_code")
        val SELECTED_CITY = stringPreferencesKey("selected_city")
        val SHAKE_TO_SOS = booleanPreferencesKey("shake_to_sos")
        val CHECKED_SAFETY_ITEMS = stringSetPreferencesKey("safety_checklist_items")
    }

    val languageFlow: Flow<AppLanguage> = context.dataStore.data.map { prefs ->
        if (prefs[Keys.LANGUAGE] == "ta") AppLanguage.TA else AppLanguage.EN
    }

    val selectedCityFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.SELECTED_CITY] ?: "Puducherry"
    }

    val shakeToSosFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.SHAKE_TO_SOS] ?: false
    }

    val checklistItemsFlow: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[Keys.CHECKED_SAFETY_ITEMS] ?: emptySet()
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { it[Keys.LANGUAGE] = language.code }
    }

    suspend fun setSelectedCity(city: String) {
        context.dataStore.edit { it[Keys.SELECTED_CITY] = city }
    }

    suspend fun setShakeToSos(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SHAKE_TO_SOS] = enabled }
    }

    suspend fun toggleChecklistItem(itemId: String) {
        context.dataStore.edit { prefs ->
            val cur = prefs[Keys.CHECKED_SAFETY_ITEMS] ?: emptySet()
            prefs[Keys.CHECKED_SAFETY_ITEMS] = if (itemId in cur) cur - itemId else cur + itemId
        }
    }
}
