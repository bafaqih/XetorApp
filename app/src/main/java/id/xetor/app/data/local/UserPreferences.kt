// UserPreferences.kt
package id.xetor.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// Membuat instance DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {
    companion object {
        // Kunci untuk menyimpan token
        private val AUTH_TOKEN = stringPreferencesKey("auth_token")
        // Kunci untuk menyimpan email dan status remember me
        private val REMEMBERED_EMAIL = stringPreferencesKey("remembered_email")
        private val REMEMBER_ME = booleanPreferencesKey("remember_me")
        // Kunci untuk flag photo updated (untuk cache busting)
        private val PHOTO_UPDATED = booleanPreferencesKey("photo_updated")
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

    // Fungsi untuk menyimpan email yang diingat
    suspend fun saveRememberedEmail(email: String) {
        context.dataStore.edit { preferences ->
            preferences[REMEMBERED_EMAIL] = email
            preferences[REMEMBER_ME] = true
        }
    }

    // Flow untuk memantau email yang diingat
    val rememberedEmail: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[REMEMBERED_EMAIL]
    }

    // Flow untuk memantau status remember me
    val rememberMe: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[REMEMBER_ME] ?: false
    }

    // Fungsi untuk menghapus email yang diingat
    suspend fun clearRememberedEmail() {
        context.dataStore.edit { preferences ->
            preferences.remove(REMEMBERED_EMAIL)
            preferences[REMEMBER_ME] = false
        }
    }

    // Fungsi untuk set flag photo updated (setelah upload/delete foto)
    suspend fun setPhotoUpdated(updated: Boolean = true) {
        context.dataStore.edit { preferences ->
            preferences[PHOTO_UPDATED] = updated
        }
    }

    // Fungsi untuk cek flag photo updated (tanpa reset)
    suspend fun isPhotoUpdated(): Boolean {
        return context.dataStore.data.map { preferences ->
            preferences[PHOTO_UPDATED] ?: false
        }.first()
    }
    
    // Fungsi untuk reset flag photo updated
    suspend fun resetPhotoUpdated() {
        context.dataStore.edit { preferences ->
            preferences[PHOTO_UPDATED] = false
        }
    }
}