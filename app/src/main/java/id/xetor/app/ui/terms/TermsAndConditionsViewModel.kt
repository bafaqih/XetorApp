// app/src/main/java/id/xetor/app/ui/terms/TermsAndConditionsViewModel.kt
package id.xetor.app.ui.terms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.xetor.app.data.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// State untuk UI Terms and Conditions
data class TermsUiState(
    val isLoading: Boolean = true,
    val content: String? = null,
    val errorMessage: String? = null
)

class TermsAndConditionsViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TermsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadTermsAndConditions()
    }

    /**
     * Load terms and conditions dari API
     */
    fun loadTermsAndConditions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = userRepository.getTermsAndConditions()

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
                val errorMsg = result.exceptionOrNull()?.message ?: "Gagal memuat Syarat dan Ketentuan"
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

