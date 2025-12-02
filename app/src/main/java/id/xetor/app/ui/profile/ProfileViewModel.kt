// app/src/main/java/id/xetor/app/ui/profile/ProfileViewModel.kt
package id.xetor.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.xetor.app.data.UserRepository
import id.xetor.app.data.remote.UserDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// State untuk UI Profile
data class ProfileUiState(
    val isLoading: Boolean = true,
    val isLoadingProfilePhoto: Boolean = true, // Loading state terpisah untuk profile photo
    val userProfile: UserDto? = null,
    val appVersion: String = "1.0.0",
    val errorMessage: String? = null
)

class ProfileViewModel(
    private val userRepository: UserRepository,
    private val token: String,
    private val appVersion: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState(appVersion = appVersion))
    val uiState = _uiState.asStateFlow()
    
    // Cache untuk profile photo URL - untuk tracking perubahan
    private var cachedProfilePhotoUrl: String? = null

    init {
        // Jangan load otomatis di init, biarkan dipanggil secara eksplisit
        // Ini memungkinkan preload di background tanpa loading skeleton
        // Initial state: isLoading = true (untuk skeleton jika data belum ada)
        // Jika data sudah ada dari preload, isLoading akan di-set false oleh preloadProfileData
    }

    /**
     * Load profile data (nama, email) dengan loading skeleton
     * Digunakan saat initial load atau manual refresh
     * Profile photo tidak dimuat di sini, dipisah di loadProfilePhoto()
     */
    fun loadProfileData(showLoading: Boolean = true) {
        viewModelScope.launch {
            if (showLoading) {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            }

            val profileResult = userRepository.getUserProfile(token)

            if (profileResult.isSuccess) {
                val profile = profileResult.getOrNull()
                val newPhotoUrl = profile?.photo
                
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        userProfile = profile,
                        errorMessage = null,
                        // Jika foto sudah ada dan URL sama, tidak perlu loading
                        isLoadingProfilePhoto = if (newPhotoUrl != null && newPhotoUrl == cachedProfilePhotoUrl) {
                            false
                        } else {
                            it.isLoadingProfilePhoto // Tetap state sebelumnya jika URL berubah
                        }
                    )
                }
                
                // Cek apakah profile photo URL berubah
                if (newPhotoUrl != cachedProfilePhotoUrl) {
                    // Jika URL berubah, reload photo
                    cachedProfilePhotoUrl = newPhotoUrl
                    loadProfilePhoto()
                }
            } else {
                val errorMsg = profileResult.exceptionOrNull()?.message ?: "Gagal memuat data"
                
                // Jika token expired (401), pertahankan data terakhir
                val isTokenExpired = errorMsg.contains("Unauthorized", ignoreCase = true) ||
                        errorMsg.contains("401", ignoreCase = true) ||
                        errorMsg.contains("token", ignoreCase = true)

                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = if (isTokenExpired) false else true,
                        userProfile = if (isTokenExpired) currentState.userProfile else null,
                        errorMessage = if (isTokenExpired) null else errorMsg
                    )
                }
            }
        }
    }

    /**
     * Load profile photo secara terpisah dari profile data
     * Agar loading profile tidak menghambat tampilan halaman
     * Hanya reload jika URL berubah
     */
    fun loadProfilePhoto() {
        viewModelScope.launch {
            // Jika foto sudah ada dan URL tidak berubah, tidak perlu loading
            val currentPhotoUrl = _uiState.value.userProfile?.photo
            if (currentPhotoUrl != null && currentPhotoUrl == cachedProfilePhotoUrl) {
                // Foto sudah ada dan URL sama, tidak perlu loading
                return@launch
            }
            
            _uiState.update { it.copy(isLoadingProfilePhoto = true) }
            
            val profileResult = userRepository.getUserProfile(token)
            
            if (profileResult.isSuccess) {
                val profile = profileResult.getOrNull()
                val photoUrl = profile?.photo
                
                // Update cache jika URL berubah
                if (photoUrl != cachedProfilePhotoUrl) {
                    cachedProfilePhotoUrl = photoUrl
                }
                
                _uiState.update {
                    it.copy(
                        userProfile = profile,
                        isLoadingProfilePhoto = false
                    )
                }
            } else {
                // Jika error, tetap set loadingProfilePhoto = false agar tidak stuck
                _uiState.update {
                    it.copy(isLoadingProfilePhoto = false)
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
     * Preload profile data di background (tanpa loading skeleton)
     * Digunakan untuk preload setelah home terload
     */
    fun preloadProfileData() {
        // Jika data sudah ada, tidak perlu preload lagi
        if (_uiState.value.userProfile != null && !_uiState.value.isLoading) {
            return
        }
        // Preload tanpa loading skeleton
        viewModelScope.launch {
            val profileResult = userRepository.getUserProfile(token)
            
            if (profileResult.isSuccess) {
                val profile = profileResult.getOrNull()
                val newPhotoUrl = profile?.photo
                
                _uiState.update {
                    it.copy(
                        isLoading = false, // Set isLoading = false karena data sudah ada
                        userProfile = profile,
                        errorMessage = null,
                        // Jika foto sudah ada, set isLoadingProfilePhoto = false agar tidak muncul loading
                        isLoadingProfilePhoto = if (newPhotoUrl != null && newPhotoUrl == cachedProfilePhotoUrl) {
                            false
                        } else {
                            it.isLoadingProfilePhoto // Tetap state sebelumnya jika URL berubah
                        }
                    )
                }
                
                // Preload profile photo juga (selalu dipanggil untuk preload pertama kali)
                cachedProfilePhotoUrl = newPhotoUrl
                loadProfilePhoto()
            }
            // Jika error, biarkan isLoading tetap true agar skeleton muncul saat user buka profile
        }
    }

    /**
     * Full refresh dengan loading skeleton
     * Digunakan saat initial load atau manual refresh
     */
    fun refresh() {
        loadProfileData(showLoading = true)
        loadProfilePhoto() // Refresh profile photo juga
    }

    /**
     * Logout - clear token dan data
     */
    suspend fun logout() {
        userRepository.clearAuthToken()
    }

    /**
     * Verify password - verify password dengan login endpoint
     */
    suspend fun verifyPassword(email: String, password: String): Result<Unit> {
        return try {
            val result = userRepository.login(email, password)
            if (result.isSuccess) {
                Result.success(Unit)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Password salah"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete account - call API dan clear token
     */
    suspend fun deleteAccount(): Result<Unit> {
        return try {
            val result = userRepository.deleteAccount(token)
            if (result.isSuccess) {
                userRepository.clearAuthToken()
                Result.success(Unit)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Gagal menghapus akun"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

