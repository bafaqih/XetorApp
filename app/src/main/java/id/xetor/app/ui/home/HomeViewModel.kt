// app/src/main/java/id/xetor/app/ui/home/HomeViewModel.kt
package id.xetor.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.xetor.app.data.UserRepository
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
                // Ada error
                val errorMsg = profileResult.exceptionOrNull()?.message
                    ?: walletResult.exceptionOrNull()?.message
                    ?: statsResult.exceptionOrNull()?.message
                    ?: "Gagal memuat data"

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = errorMsg
                    )
                }
            }
        }
    }

    fun refresh() {
        loadHomeData()
    }
}

