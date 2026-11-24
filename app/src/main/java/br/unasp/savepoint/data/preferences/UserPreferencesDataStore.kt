
package br.unasp.savepoint.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

import androidx.datastore.preferences.core.booleanPreferencesKey
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore(
    name = "user_preferences"
)

class UserPreferencesDataStore(
    private val context: Context
) {

    companion object {
        private val USER_NAME = stringPreferencesKey("user_name")
        private val USER_TAG = stringPreferencesKey("user_tag")
        private val AVATAR_URI = stringPreferencesKey("avatar_uri")

        private val STEAM_LIBRARY_IMPORTED =
            booleanPreferencesKey("steam_library_imported")
    }

    val userName: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[USER_NAME] ?: "Kaluak"
    }

    val userTag: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[USER_TAG] ?: "@kaluak"
    }

    val avatarUri: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[AVATAR_URI]
    }

    suspend fun saveProfile(
        name: String,
        tag: String
    ) {
        context.dataStore.edit { preferences ->
            preferences[USER_NAME] = name
            preferences[USER_TAG] = tag
        }
    }

    suspend fun saveAvatar(uri: String?) {
        context.dataStore.edit { preferences ->
            if (uri == null) {
                preferences.remove(AVATAR_URI)
            } else {
                preferences[AVATAR_URI] = uri
            }
        }
    }

    suspend fun hasImportedSteamLibrary(): Boolean {
        return context.dataStore.data.first()[STEAM_LIBRARY_IMPORTED] ?: false
    }

    suspend fun markSteamLibraryAsImported() {
        context.dataStore.edit { preferences ->
            preferences[STEAM_LIBRARY_IMPORTED] = true
        }
    }
}
