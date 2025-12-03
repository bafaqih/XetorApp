// app/src/main/java/id/xetor/app/ui/profile/ProfilSayaViewModel.kt
package id.xetor.app.ui.profile

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.xetor.app.XetorApplication
import id.xetor.app.data.UserRepository
import id.xetor.app.data.local.UserPreferences
import id.xetor.app.data.remote.UserDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

// State untuk UI ProfilSaya
data class ProfilSayaUiState(
    val isLoading: Boolean = true,
    val userProfile: UserDto? = null,
    val fullname: String = "",
    val email: String = "",
    val phone: String = "",
    val photoUrl: String? = null,
    val hasChanges: Boolean = false,
    val isSaving: Boolean = false,
    val isUploadingPhoto: Boolean = false,
    val isDeletingPhoto: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val photoSuccessMessage: String? = null // Success message untuk upload/delete photo
)

class ProfilSayaViewModel(
    private val userRepository: UserRepository,
    private val token: String,
    private val userPreferences: UserPreferences,
    private val application: Application? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfilSayaUiState())
    val uiState = _uiState.asStateFlow()
    
    private var originalFullname: String = ""
    private var originalPhotoUrl: String? = null
    private var isInitialLoad = true
    
    // Photo refresh key untuk cache busting
    // Gunakan timestamp sebagai base, lalu increment jika ada flag photo_updated
    // Ini memastikan setiap ViewModel instance punya refreshKey unik sejak awal
    private var photoRefreshKey = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
    val photoRefreshKeyFlow = MutableStateFlow(photoRefreshKey)
    
    // Cache untuk melacak URL terakhir yang sudah di-increment refreshKey-nya
    private var lastIncrementedPhotoUrl: String? = null

    init {
        // Cek apakah ada foto yang di-update sejak terakhir kali
        // Jika ya, increment photoRefreshKey lagi untuk bypass cache
        // photoRefreshKey sudah di-set dengan timestamp unik di deklarasi
        viewModelScope.launch {
            val photoUpdated = userPreferences.isPhotoUpdated()
            if (photoUpdated) {
                // Increment lagi untuk memastikan cache di-bypass setelah upload
                photoRefreshKey++
                photoRefreshKeyFlow.value = photoRefreshKey
                // Reset flag setelah increment photoRefreshKey
                userPreferences.resetPhotoUpdated()
            }
        }
        
        // Panggil loadProfile() - photoRefreshKey sudah di-set dengan timestamp unik
        loadProfile()
    }

    /**
     * Load profile data
     * @param showLoading true untuk menampilkan skeleton (initial load), false untuk silent refresh
     */
    fun loadProfile(showLoading: Boolean = true) {
        viewModelScope.launch {
            // Hanya tampilkan skeleton saat initial load
            val shouldShowLoading = showLoading && isInitialLoad
            if (shouldShowLoading) {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            }

            val profileResult = userRepository.getUserProfile(token)

            if (profileResult.isSuccess) {
                val profile = profileResult.getOrNull()
                val fullname = profile?.fullname ?: ""
                val email = profile?.email ?: ""
                val phone = profile?.phone ?: ""
                val photoUrl = profile?.photo

                // Cek apakah photo URL berubah
                val currentPhotoUrl = _uiState.value.photoUrl
                val photoUrlChanged = photoUrl != currentPhotoUrl
                
                // Update photoRefreshKey hanya jika URL berubah
                // photoRefreshKey sudah di-increment di init jika ada flag photo_updated
                if (photoUrl != null) {
                    // Jika URL berubah (foto baru di-upload), increment photoRefreshKey
                    if (photoUrlChanged) {
                        photoRefreshKey++
                        photoRefreshKeyFlow.value = photoRefreshKey
                    }
                    lastIncrementedPhotoUrl = photoUrl
                } else {
                    // Jika foto dihapus, reset lastIncrementedPhotoUrl
                    lastIncrementedPhotoUrl = null
                }

                originalFullname = fullname
                originalPhotoUrl = photoUrl
                isInitialLoad = false // Setelah load pertama, bukan initial load lagi

                // Gunakan photoUrl dari API, atau pertahankan yang ada di state jika sama
                // Ini memastikan bahwa jika photoUrl sudah di-update sebelumnya (setelah upload),
                // kita tetap menggunakan yang terbaru dari API
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        userProfile = profile,
                        fullname = fullname,
                        email = email,
                        phone = phone,
                        photoUrl = photoUrl, // Gunakan dari API (yang terbaru)
                        hasChanges = false,
                        errorMessage = null
                    )
                }
            } else {
                val errorMsg = profileResult.exceptionOrNull()?.message ?: "Gagal memuat data"
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = errorMsg
                    )
                }
                isInitialLoad = false
            }
        }
    }

    /**
     * Update fullname
     */
    fun updateFullname(newFullname: String) {
        _uiState.update {
            val hasChanges = newFullname != originalFullname || it.photoUrl != originalPhotoUrl
            it.copy(
                fullname = newFullname,
                hasChanges = hasChanges
            )
        }
    }

    /**
     * Update photo URL (after upload)
     */
    fun updatePhotoUrl(newPhotoUrl: String?) {
        _uiState.update {
            val hasChanges = it.fullname != originalFullname || newPhotoUrl != originalPhotoUrl
            it.copy(
                photoUrl = newPhotoUrl,
                hasChanges = hasChanges
            )
        }
    }

    /**
     * Save profile changes
     */
    fun saveProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null, successMessage = null) }

            val result = userRepository.updateProfile(
                token = token,
                fullname = _uiState.value.fullname
            )

            if (result.isSuccess) {
                // Reload profile to get updated data (tanpa skeleton karena ini update, bukan initial load)
                loadProfile(showLoading = false)
                _uiState.update { 
                    it.copy(
                        isSaving = false, 
                        hasChanges = false,
                        successMessage = "Nama lengkap berhasil diperbarui"
                    ) 
                }
                
                // Trigger refresh di ProfileViewModel dan HomeViewModel
                (application as? XetorApplication)?.triggerProfileRefresh()
                (application as? XetorApplication)?.triggerHomeRefresh()
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Gagal menyimpan profil"
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = errorMsg
                    )
                }
            }
        }
    }

    /**
     * Upload profile photo
     */
    fun uploadProfilePhoto(photoFile: File) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingPhoto = true, errorMessage = null, photoSuccessMessage = null) }

            val result = userRepository.uploadProfilePhoto(token, photoFile)

            if (result.isSuccess) {
                val photoUrl = result.getOrNull()
                
                // Set flag photo_updated di UserPreferences untuk notifikasi ViewModel lain
                userPreferences.setPhotoUpdated(true)
                
                // Update photo URL langsung di state SEBELUM increment refreshKey
                updatePhotoUrl(photoUrl)
                
                // Increment refresh key untuk cache busting
                photoRefreshKey++
                photoRefreshKeyFlow.value = photoRefreshKey
                
                // Update lastIncrementedPhotoUrl untuk tracking
                lastIncrementedPhotoUrl = photoUrl
                
                _uiState.update { 
                    it.copy(
                        isUploadingPhoto = false,
                        photoUrl = photoUrl, // Update langsung di state dengan URL baru
                        photoSuccessMessage = "Foto profil berhasil diperbarui" // Set success message, akan ditampilkan setelah foto load
                    ) 
                }
                
                // Reload profile to get updated data (tanpa skeleton karena ini update, bukan initial load)
                // loadProfile akan mendeteksi bahwa photoUrl sudah sama dengan yang di state, jadi tidak akan overwrite
                loadProfile(showLoading = false)
                
                // Trigger refresh di ProfileViewModel (untuk refresh profile screen)
                (application as? XetorApplication)?.triggerProfileRefresh()
                // Trigger refresh hanya profile photo di HomeViewModel (bukan full refresh)
                (application as? XetorApplication)?.triggerHomeProfilePhotoRefresh()
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Gagal mengunggah foto"
                _uiState.update {
                    it.copy(
                        isUploadingPhoto = false,
                        errorMessage = errorMsg
                    )
                }
            }
        }
    }

    /**
     * Delete profile photo (set to default)
     */
    fun deleteProfilePhoto() {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeletingPhoto = true, errorMessage = null, photoSuccessMessage = null) }

            val result = userRepository.deleteProfilePhoto(token)

            if (result.isSuccess) {
                // Set flag photo_updated di UserPreferences untuk notifikasi ViewModel lain
                userPreferences.setPhotoUpdated(true)
                
                updatePhotoUrl(null)
                // Reload profile to get updated data (tanpa skeleton karena ini update, bukan initial load)
                loadProfile(showLoading = false)
                _uiState.update { 
                    it.copy(
                        isDeletingPhoto = false,
                        photoSuccessMessage = "Foto profil berhasil dihapus" // Set success message, akan ditampilkan setelah foto load
                    ) 
                }
                
                // Trigger refresh di ProfileViewModel dan HomeViewModel
                (application as? XetorApplication)?.triggerProfileRefresh()
                (application as? XetorApplication)?.triggerHomeRefresh()
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Gagal menghapus foto"
                _uiState.update {
                    it.copy(
                        isDeletingPhoto = false,
                        errorMessage = errorMsg
                    )
                }
            }
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * Clear success message
     */
    fun clearSuccess() {
        _uiState.update { it.copy(successMessage = null) }
    }

    /**
     * Clear photo success message
     */
    fun clearPhotoSuccess() {
        _uiState.update { it.copy(photoSuccessMessage = null) }
    }

    /**
     * Show error message (helper method for external errors)
     */
    fun showError(message: String) {
        _uiState.update { it.copy(errorMessage = message) }
    }
}

