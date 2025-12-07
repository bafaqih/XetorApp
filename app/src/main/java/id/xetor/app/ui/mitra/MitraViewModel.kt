// app/src/main/java/id/xetor/app/ui/mitra/MitraViewModel.kt
package id.xetor.app.ui.mitra

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.xetor.app.data.UserRepository
import id.xetor.app.data.remote.PublicPartnerResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// State untuk UI Mitra
data class MitraUiState(
    val isLoading: Boolean = true,
    val partners: List<PublicPartnerResponse> = emptyList(),
    val errorMessage: String? = null
)

class MitraViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MitraUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadPartners()
    }

    fun loadPartners() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            val result = userRepository.getApprovedPartners()
            result.fold(
                onSuccess = { partners ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            partners = partners,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Gagal memuat daftar mitra"
                        )
                    }
                }
            )
        }
    }

    fun refresh() {
        loadPartners()
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

