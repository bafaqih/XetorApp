// app/src/main/java/id/xetor/app/ui/privacy/PrivacyPolicyViewModel.kt
package id.xetor.app.ui.privacy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.xetor.app.data.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// State untuk UI Privacy Policy
data class PrivacyUiState(
    val isLoading: Boolean = true,
    val content: String? = null,
    val errorMessage: String? = null
)

class PrivacyPolicyViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PrivacyUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadPrivacyPolicy()
    }

    /**
     * Load privacy policy dari API
     */
    fun loadPrivacyPolicy() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = userRepository.getPrivacyPolicy()

            if (result.isSuccess) {
                val aboutXetor = result.getOrNull()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        content = aboutXetor?.content,
                        errorMessage = null
                    )
                }
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Gagal memuat Kebijakan Privasi"
                _uiState.update {
                    it.copy(
                        isLoading = false,
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
}

