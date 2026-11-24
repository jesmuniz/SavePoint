
package br.unasp.savepoint.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.authDataStore by preferencesDataStore(
    name = "auth_preferences"
)

class AuthDataStore(
    private val context: Context,
    private val tokenEncryption: TokenEncryption = TokenEncryption()
) {

    companion object {
        private val ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val USER_ID = stringPreferencesKey("user_id")
        private val STEAM_ID = stringPreferencesKey("steam_id")
    }

    suspend fun saveSession(
        accessToken: String,
        userId: String,
        steamId: String
    ) {
        val encryptedToken = tokenEncryption.encrypt(accessToken)

        context.authDataStore.edit { preferences ->
            preferences[ACCESS_TOKEN] = encryptedToken
            preferences[USER_ID] = userId
            preferences[STEAM_ID] = steamId
        }
    }

    suspend fun getAccessToken(): String? {
        val encryptedToken =
            context.authDataStore.data.first()[ACCESS_TOKEN]

        return encryptedToken?.let {
            tokenEncryption.decrypt(it)
        }
    }

    suspend fun getUserId(): String? {
        return context.authDataStore.data.first()[USER_ID]
    }

    suspend fun getSteamId(): String? {
        return context.authDataStore.data.first()[STEAM_ID]
    }

    suspend fun clearSession() {
        context.authDataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN)
            preferences.remove(USER_ID)
            preferences.remove(STEAM_ID)
        }
    }
}
