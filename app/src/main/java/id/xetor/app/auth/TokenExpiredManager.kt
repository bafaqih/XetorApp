// TokenExpiredManager.kt
package id.xetor.app.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Global state manager untuk menangani token expired
 * Singleton object yang bisa diakses dari mana saja
 */
object TokenExpiredManager {
    // State untuk menandai apakah token sudah expired
    var isTokenExpired by mutableStateOf(false)
        private set
    
    // State untuk menandai apakah dialog sudah ditampilkan (untuk menghindari multiple dialogs)
    var isDialogShown by mutableStateOf(false)
        private set
    
    /**
     * Mark token as expired or reset expired state
     * @param expired true jika token expired, false untuk reset
     */
    fun markTokenExpired(expired: Boolean) {
        isTokenExpired = expired
        if (expired) {
            isDialogShown = false // Reset dialog state agar bisa ditampilkan lagi
        }
    }
    
    /**
     * Mark dialog sebagai sudah ditampilkan
     */
    fun markDialogShown() {
        isDialogShown = true
    }
    
    /**
     * Reset semua state (setelah logout atau login ulang)
     */
    fun reset() {
        isTokenExpired = false
        isDialogShown = false
    }
}

