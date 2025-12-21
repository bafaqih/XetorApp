// app/src/main/java/id/xetor/app/ui/home/HomeViewModel.kt
package id.xetor.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.xetor.app.data.UserRepository
import id.xetor.app.data.remote.PromotionBannerResponse
import id.xetor.app.data.remote.StatisticsResponse
import id.xetor.app.data.remote.UserDto
import id.xetor.app.data.remote.WalletResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

// State untuk UI Home
data class HomeUiState(
    val isLoading: Boolean = true,
    val userProfile: UserDto? = null,
    val wallet: WalletResponse? = null,
    val statistics: StatisticsResponse? = null,
    val banners: List<PromotionBannerResponse> = emptyList(),
    val isLoadingBanners: Boolean = true,
    val isLoadingProfile: Boolean = true, // Loading state terpisah untuk profile photo
    val errorMessage: String? = null
)

class HomeViewModel(
    private val userRepository: UserRepository,
    private val token: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()
    
    // Timestamp untuk track last refresh time
    private var lastRefreshTime: Long = 0
    // Minimum interval untuk refresh (30 detik)
    private val REFRESH_INTERVAL_MS = 30_000L // 30 detik
    
    // Photo refresh key untuk cache busting - increment setiap kali photo di-refresh
    private var photoRefreshKey = 0
    val photoRefreshKeyFlow = MutableStateFlow(photoRefreshKey)

    init {
        loadHomeData()
        loadBanners()
        // Load profile photo secara terpisah agar tidak menghambat loading home
        loadProfilePhoto()
    }

    /**
     * Load home data dengan loading skeleton
     * Digunakan saat initial load atau manual refresh
     * Profile photo tidak dimuat di sini, dipisah di loadProfilePhoto()
     */
    fun loadHomeData(showLoading: Boolean = true) {
        viewModelScope.launch {
            if (showLoading) {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            }

            // Fetch wallet dan statistics saja (tanpa profile)
            val walletResult = userRepository.getUserWallet(token)
            val statsResult = userRepository.getUserStatistics(token)

            // Update state berdasarkan hasil
            if (walletResult.isSuccess && statsResult.isSuccess) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        wallet = walletResult.getOrNull(),
                        statistics = statsResult.getOrNull(),
                        errorMessage = null
                    )
                }
                lastRefreshTime = System.currentTimeMillis()
            } else {
                // Ada error - cek apakah error karena 401 (token expired)
                val errorMsg = walletResult.exceptionOrNull()?.message
                    ?: statsResult.exceptionOrNull()?.message
                    ?: "Gagal memuat data"
                
                // Jika token expired (401), pertahankan data terakhir dan jangan tampilkan error
                // Dialog token expired akan muncul otomatis dari TokenExpiredDialog
                val isTokenExpired = errorMsg.contains("Unauthorized", ignoreCase = true) ||
                        errorMsg.contains("401", ignoreCase = true) ||
                        errorMsg.contains("token", ignoreCase = true)

                _uiState.update { currentState ->
                    currentState.copy(
                        // Tetap loading (skeleton tetap berjalan) saat error, kecuali token expired
                        isLoading = if (isTokenExpired) false else true,
                        // Jika token expired, pertahankan data terakhir (jangan reset)
                        // Jika error lain, tetap tampilkan error tapi pertahankan data jika ada
                        wallet = if (isTokenExpired) currentState.wallet else walletResult.getOrNull() ?: currentState.wallet,
                        statistics = if (isTokenExpired) currentState.statistics else statsResult.getOrNull() ?: currentState.statistics,
                        // Jangan tampilkan error message jika token expired (dialog sudah handle)
                        errorMessage = if (isTokenExpired) null else errorMsg
                    )
                }
            }
        }
    }

    /**
     * Load profile photo secara terpisah dari home data
     * Agar loading profile tidak menghambat tampilan home
     */
    fun loadProfilePhoto() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingProfile = true) }
            
            val profileResult = userRepository.getUserProfile(token)
            
            if (profileResult.isSuccess) {
                val newPhotoUrl = profileResult.getOrNull()?.photo
                val oldPhotoUrl = _uiState.value.userProfile?.photo
                
                // Increment refresh key jika photo URL berubah atau untuk force refresh
                if (newPhotoUrl != oldPhotoUrl) {
                    photoRefreshKey++
                    photoRefreshKeyFlow.value = photoRefreshKey
                }
                
                _uiState.update {
                    it.copy(
                        userProfile = profileResult.getOrNull(),
                        isLoadingProfile = false
                    )
                }
            } else {
                // Jika error, tetap set loadingProfile = false agar tidak stuck di loading
                // Profile photo akan menampilkan default icon jika error
                _uiState.update {
                    it.copy(isLoadingProfile = false)
                }
            }
        }
    }

    private fun loadBanners(showLoading: Boolean = true) {
        viewModelScope.launch {
            if (showLoading) {
                _uiState.update { it.copy(isLoadingBanners = true) }
            }
            val result = userRepository.getPromotionBanners()
            result.onSuccess { banners ->
                android.util.Log.d("HomeViewModel", "Banners loaded: ${banners.size}")
                _uiState.update {
                    it.copy(
                        banners = banners,
                        isLoadingBanners = false
                    )
                }
            }.onFailure { exception ->
                android.util.Log.e("HomeViewModel", "Failed to load banners: ${exception.message}")
                _uiState.update {
                    it.copy(
                        banners = it.banners, // Pertahankan banners lama jika error
                        isLoadingBanners = false
                    )
                }
            }
        }
    }

    /**
     * Clear error message
     * Digunakan saat user klik "Coba Lagi" di snackbar
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * Full refresh dengan loading skeleton
     * Digunakan saat initial load atau manual refresh
     */
    fun refresh() {
        loadHomeData(showLoading = true)
        loadBanners(showLoading = true)
        loadProfilePhoto() // Refresh profile photo juga
    }

    /**
     * Silent refresh di background tanpa loading skeleton
     * Hanya refresh jika data sudah cukup lama (lebih dari REFRESH_INTERVAL_MS)
     * Digunakan saat kembali ke home screen
     */
    fun silentRefresh() {
        val currentTime = System.currentTimeMillis()
        val timeSinceLastRefresh = currentTime - lastRefreshTime
        
        // Hanya refresh jika data sudah cukup lama atau belum pernah di-refresh
        if (lastRefreshTime == 0L || timeSinceLastRefresh >= REFRESH_INTERVAL_MS) {
            android.util.Log.d("HomeViewModel", "Silent refresh triggered (time since last: ${timeSinceLastRefresh}ms)")
            loadHomeData(showLoading = false)
            loadBanners(showLoading = false)
            loadProfilePhoto() // Refresh profile photo juga (terpisah, tidak menghambat)
        } else {
            android.util.Log.d("HomeViewModel", "Skip silent refresh (data masih fresh, time since last: ${timeSinceLastRefresh}ms)")
        }
    }

    /**
     * Force silent refresh di background tanpa loading skeleton dan tanpa cek interval
     * Digunakan setelah transaksi berhasil (withdraw, topup, transfer)
     */
    fun forceSilentRefresh() {
        android.util.Log.d("HomeViewModel", "Force silent refresh triggered (after transaction success)")
        loadHomeData(showLoading = false)
        loadBanners(showLoading = false)
        loadProfilePhoto() // Refresh profile photo juga
    }

    /**
     * Refresh hanya profile photo tanpa reload home data dan banners
     * Digunakan setelah upload/update profile photo
     */
    fun refreshProfilePhotoOnly() {
        android.util.Log.d("HomeViewModel", "Refresh profile photo only triggered")
        // Increment refresh key untuk force cache busting
        photoRefreshKey++
        photoRefreshKeyFlow.value = photoRefreshKey
        loadProfilePhoto()
    }
}

