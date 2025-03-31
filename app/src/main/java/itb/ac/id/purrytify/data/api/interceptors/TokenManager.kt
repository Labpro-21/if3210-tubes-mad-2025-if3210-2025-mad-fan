package itb.ac.id.purrytify.data.api.interceptors

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import kotlinx.coroutines.flow.first

class TokenManager(context: Context) {
    private val dataStore = PreferenceDataStoreFactory.create {
        context.preferencesDataStoreFile("auth_store")
    }
    private val accessToken = stringPreferencesKey("access_token")
    private val refreshToken = stringPreferencesKey("refresh_token")

    suspend fun getAccessToken(): String {
        return dataStore.data.first()[accessToken] ?: ""
    }

    suspend fun saveAccessToken(token: String) {
        dataStore.edit { preferences ->
            preferences[accessToken] = token
        }
    }

    suspend fun getRefreshToken(): String {
        return dataStore.data.first()[refreshToken] ?: ""
    }

    suspend fun saveRefreshToken(token: String) {
        dataStore.edit { preferences ->
            preferences[refreshToken] = token
        }
    }

    suspend fun clearToken() {
        dataStore.edit { preferences ->
            preferences.remove(accessToken)
            preferences.remove(refreshToken)
        }
    }
}