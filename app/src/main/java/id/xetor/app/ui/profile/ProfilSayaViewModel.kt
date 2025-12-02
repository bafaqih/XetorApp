// app/src/main/java/id/xetor/app/ui/profile/ProfilSayaViewModel.kt
package id.xetor.app.ui.profile

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.xetor.app.XetorApplication
import id.xetor.app.data.UserRepository
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
    private val application: Application? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfilSayaUiState())
    val uiState = _uiState.asStateFlow()
    
    private var originalFullname: String = ""
    private var originalPhotoUrl: String? = null
    private var isInitialLoad = true

    init {
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

                originalFullname = fullname
                originalPhotoUrl = photoUrl
                isInitialLoad = false // Setelah load pertama, bukan initial load lagi

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        userProfile = profile,
                        fullname = fullname,
                        email = email,
                        phone = phone,
                        photoUrl = photoUrl,
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
                updatePhotoUrl(photoUrl)
                // Reload profile to get updated data (tanpa skeleton karena ini update, bukan initial load)
                loadProfile(showLoading = false)
                _uiState.update { 
                    it.copy(
                        isUploadingPhoto = false,
                        photoSuccessMessage = "Foto profil berhasil diperbarui" // Set success message, akan ditampilkan setelah foto load
                    ) 
                }
                
                // Trigger refresh di ProfileViewModel dan HomeViewModel
                (application as? XetorApplication)?.triggerProfileRefresh()
                (application as? XetorApplication)?.triggerHomeRefresh()
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
}

