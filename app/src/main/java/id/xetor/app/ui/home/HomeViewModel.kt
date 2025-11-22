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

// State untuk UI Home
data class HomeUiState(
    val isLoading: Boolean = true,
    val userProfile: UserDto? = null,
    val wallet: WalletResponse? = null,
    val statistics: StatisticsResponse? = null,
    val banners: List<PromotionBannerResponse> = emptyList(),
    val isLoadingBanners: Boolean = true,
    val errorMessage: String? = null
)

class HomeViewModel(
    private val userRepository: UserRepository,
    private val token: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadHomeData()
        loadBanners()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // Fetch profile
            val profileResult = userRepository.getUserProfile(token)
            // Fetch wallet
            val walletResult = userRepository.getUserWallet(token)
            // Fetch statistics
            val statsResult = userRepository.getUserStatistics(token)

            // Update state berdasarkan hasil
            if (profileResult.isSuccess && walletResult.isSuccess && statsResult.isSuccess) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        userProfile = profileResult.getOrNull(),
                        wallet = walletResult.getOrNull(),
                        statistics = statsResult.getOrNull(),
                        errorMessage = null
                    )
                }
            } else {
                // Ada error - cek apakah error karena 401 (token expired)
                val errorMsg = profileResult.exceptionOrNull()?.message
                    ?: walletResult.exceptionOrNull()?.message
                    ?: statsResult.exceptionOrNull()?.message
                    ?: "Gagal memuat data"
                
                // Jika token expired (401), pertahankan data terakhir dan jangan tampilkan error
                // Dialog token expired akan muncul otomatis dari TokenExpiredDialog
                val isTokenExpired = errorMsg.contains("Unauthorized", ignoreCase = true) ||
                        errorMsg.contains("401", ignoreCase = true) ||
                        errorMsg.contains("token", ignoreCase = true)

                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        // Jika token expired, pertahankan data terakhir (jangan reset)
                        // Jika error lain, tetap tampilkan error tapi pertahankan data jika ada
                        userProfile = if (isTokenExpired) currentState.userProfile else profileResult.getOrNull() ?: currentState.userProfile,
                        wallet = if (isTokenExpired) currentState.wallet else walletResult.getOrNull() ?: currentState.wallet,
                        statistics = if (isTokenExpired) currentState.statistics else statsResult.getOrNull() ?: currentState.statistics,
                        // Jangan tampilkan error message jika token expired (dialog sudah handle)
                        errorMessage = if (isTokenExpired) null else errorMsg
                    )
                }
            }
        }
    }

    private fun loadBanners() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingBanners = true) }
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
                        banners = emptyList(),
                        isLoadingBanners = false
                    )
                }
            }
        }
    }

    fun refresh() {
        loadHomeData()
        loadBanners()
    }
}

