package itb.ac.id.purrytify.data.api.interceptors

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStoreFile
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class TokenManager @Inject constructor(private val context: Context) {
    private val dataStore = PreferenceDataStoreFactory.create {
        context.preferencesDataStoreFile("auth_store")
    }
    private val accessToken = stringPreferencesKey("access_token")
    private val refreshToken = stringPreferencesKey("refresh_token")
    private val currentUserID = intPreferencesKey("current_user_id")
//    private val isLoggedIn = booleanPreferencesKey("is_logged_in")

//    suspend fun isLoggedIn(): Boolean {
//        return dataStore.data.first()[isLoggedIn] ?: false
//    }
//    suspend fun setLoggedIn(loggedIn: Boolean) {
//        dataStore.edit { preferences ->
//            preferences[isLoggedIn] = loggedIn
//        }
//    }

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

    suspend fun getCurrentUserID(): Int {
        return dataStore.data.first()[currentUserID] ?: -1
    }

    suspend fun saveCurrentUserID(userID: Int) {
        dataStore.edit { preferences ->
            preferences[currentUserID] = userID
        }
    }

    suspend fun clearToken() {
        dataStore.edit { preferences ->
            preferences.remove(accessToken)
            preferences.remove(refreshToken)
            preferences.remove(currentUserID)
        }
    }
}