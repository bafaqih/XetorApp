// app/src/main/java/id/xetor/app/ui/profile/ProfileViewModel.kt
package id.xetor.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.xetor.app.data.UserRepository
import id.xetor.app.data.local.UserPreferences
import id.xetor.app.data.remote.UserDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// State untuk UI Profile
data class ProfileUiState(
    val isLoading: Boolean = true,
    val isLoadingProfilePhoto: Boolean = true, // Loading state terpisah untuk profile photo
    val isLoadingStatistics: Boolean = true, // Loading state terpisah untuk statistics
    val isLoadingVersion: Boolean = true, // Loading state terpisah untuk version (untuk skeleton)
    val userProfile: UserDto? = null,
    val appVersion: String = "1.0.0",
    val errorMessage: String? = null,
    val totalDeposit: Int = 0, // Total jumlah setoran (deposit)
    val totalTransactions: Int = 0 // Total jumlah transaksi
)

class ProfileViewModel(
    private val userRepository: UserRepository,
    private val token: String,
    private val appVersion: String,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState(appVersion = appVersion, isLoadingVersion = false))
    val uiState = _uiState.asStateFlow()
    
    // Cache untuk profile photo URL - untuk tracking perubahan
    private var cachedProfilePhotoUrl: String? = null
    
    // Photo refresh key untuk cache busting - increment hanya saat foto benar-benar berubah
    private var photoRefreshKey = 0
    val photoRefreshKeyFlow = MutableStateFlow(photoRefreshKey)
    
    init {
        // Cek apakah ada foto yang di-update sejak terakhir kali
        // Jika ya, increment photoRefreshKey untuk bypass cache
        viewModelScope.launch {
            val photoUpdated = userPreferences.isPhotoUpdated()
            if (photoUpdated) {
                photoRefreshKey++
                photoRefreshKeyFlow.value = photoRefreshKey
                // Reset flag setelah increment photoRefreshKey
                // Ini memastikan flag hanya di-reset setelah photoRefreshKey di-update
                userPreferences.resetPhotoUpdated()
            }
        }
    }

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
                val urlChanged = newPhotoUrl != cachedProfilePhotoUrl
                if (urlChanged) {
                    // Jika URL berubah, update cache dan increment refresh key untuk cache busting
                    cachedProfilePhotoUrl = newPhotoUrl
                    photoRefreshKey++
                    photoRefreshKeyFlow.value = photoRefreshKey
                    // Hanya load photo jika belum ada di state atau URL berbeda
                    // Tapi jika foto sudah ada di state dengan URL yang sama, tidak perlu load
                    val currentPhotoInState = _uiState.value.userProfile?.photo
                    if (currentPhotoInState != newPhotoUrl) {
                        loadProfilePhoto(forceReload = false)
                    } else {
                        // Foto sudah ada di state dengan URL yang sama, pastikan loading = false
                        _uiState.update { it.copy(isLoadingProfilePhoto = false) }
                    }
                } else {
                    // URL sama, pastikan loading = false dan tidak perlu reload
                    _uiState.update { it.copy(isLoadingProfilePhoto = false) }
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
     * @param forceReload jika true, akan force reload meskipun URL sama (untuk cache busting)
     */
    fun loadProfilePhoto(forceReload: Boolean = false) {
        viewModelScope.launch {
            val currentPhotoUrl = _uiState.value.userProfile?.photo
            
            // Jika foto sudah ada dan URL tidak berubah, tidak perlu loading (kecuali forceReload)
            if (!forceReload && currentPhotoUrl != null && currentPhotoUrl == cachedProfilePhotoUrl) {
                // Foto sudah ada dan URL sama, tidak perlu loading
                // Pastikan isLoadingProfilePhoto = false agar tidak muncul skeleton
                _uiState.update { it.copy(isLoadingProfilePhoto = false) }
                return@launch
            }
            
            // Jika forceReload, selalu set loading untuk menunjukkan bahwa foto sedang di-refresh
            // Jika tidak forceReload, hanya set loading jika foto belum ada atau URL berubah
            val shouldShowLoading = forceReload || currentPhotoUrl == null || currentPhotoUrl != cachedProfilePhotoUrl
            if (shouldShowLoading) {
                _uiState.update { it.copy(isLoadingProfilePhoto = true) }
            }
            
            val profileResult = userRepository.getUserProfile(token)
            
            if (profileResult.isSuccess) {
                val profile = profileResult.getOrNull()
                val photoUrl = profile?.photo
                
                // Update cache jika URL berubah atau forceReload
                val urlChanged = photoUrl != cachedProfilePhotoUrl
                if (urlChanged || forceReload) {
                    cachedProfilePhotoUrl = photoUrl
                    // Increment refresh key jika URL berubah atau forceReload (untuk cache busting)
                    // photoRefreshKey sudah di-increment di init jika ada flag photo_updated
                    // Jadi kita hanya perlu increment lagi jika URL berubah atau forceReload
                    if (urlChanged || forceReload) {
                        photoRefreshKey++
                        photoRefreshKeyFlow.value = photoRefreshKey
                    }
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
     * Termasuk preload statistics
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
                
                // Cek apakah profile photo URL berubah
                val urlChanged = newPhotoUrl != cachedProfilePhotoUrl
                val previousPhotoUrl = cachedProfilePhotoUrl
                
                if (urlChanged) {
                    // Jika URL berubah, update cache dan increment refresh key untuk cache busting
                    cachedProfilePhotoUrl = newPhotoUrl
                    // Increment refreshKey untuk memastikan foto terbaru di-load (bypass Coil cache)
                    if (newPhotoUrl != null) {
                        photoRefreshKey++
                        photoRefreshKeyFlow.value = photoRefreshKey
                    }
                }
                
                _uiState.update {
                    it.copy(
                        isLoading = false, // Set isLoading = false karena data sudah ada
                        userProfile = profile,
                        errorMessage = null,
                        // Jika foto sudah ada, set isLoadingProfilePhoto = false agar tidak muncul loading
                        isLoadingProfilePhoto = if (newPhotoUrl != null && newPhotoUrl == previousPhotoUrl) {
                            false
                        } else {
                            it.isLoadingProfilePhoto // Tetap state sebelumnya jika URL berubah
                        }
                    )
                }
                
                // Preload profile photo juga
                // Jika URL berubah, loadProfilePhoto() akan fetch foto baru dengan refreshKey yang sudah di-increment
                // Jika URL tidak berubah, tidak perlu load lagi karena foto sudah ada di state
                if (urlChanged && newPhotoUrl != null) {
                    loadProfilePhoto()
                } else if (newPhotoUrl != null) {
                    // URL tidak berubah, pastikan loading = false
                    _uiState.update { it.copy(isLoadingProfilePhoto = false) }
                }
                
                // Preload statistics juga
                loadStatistics()
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

    /**
     * Calculate total deposit count from transaction history
     * Frontend method - tidak perlu ubah backend
     */
    suspend fun calculateTotalDeposit(): Int {
        return try {
            val historyResult = userRepository.getTransactionHistory(token)
            if (historyResult.isSuccess) {
                val transactions = historyResult.getOrNull() ?: emptyList()
                transactions.count { it.type.lowercase() == "deposit" }
            } else {
                0
            }
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Calculate total transactions count from transaction history
     * Frontend method - tidak perlu ubah backend
     */
    suspend fun calculateTotalTransactions(): Int {
        return try {
            val historyResult = userRepository.getTransactionHistory(token)
            if (historyResult.isSuccess) {
                val transactions = historyResult.getOrNull() ?: emptyList()
                transactions.size
            } else {
                0
            }
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Load statistics (total deposit and total transactions)
     */
    fun loadStatistics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingStatistics = true) }
            val depositCount = calculateTotalDeposit()
            val transactionCount = calculateTotalTransactions()
            _uiState.update {
                it.copy(
                    totalDeposit = depositCount,
                    totalTransactions = transactionCount,
                    isLoadingStatistics = false
                )
            }
        }
    }
}

