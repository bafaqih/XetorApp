// UserPreferences.kt
package id.xetor.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Membuat instance DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {
    companion object {
        // Kunci untuk menyimpan token
        private val AUTH_TOKEN = stringPreferencesKey("auth_token")
    }

    // Fungsi untuk menyimpan token setelah login
    suspend fun saveAuthToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[AUTH_TOKEN] = token
        }
    }

    // Flow untuk memantau token (dan status login)
    val authToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[AUTH_TOKEN]
    }

    suspend fun clearAuthToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(AUTH_TOKEN)
        }
    }
}