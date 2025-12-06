// app/src/main/java/id/xetor/app/ui/version/VersionViewModel.kt
package id.xetor.app.ui.version

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.xetor.app.data.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// State untuk UI Version
data class VersionUiState(
    val isLoading: Boolean = true,
    val version: String? = null,
    val errorMessage: String? = null
)

class VersionViewModel(
    private val userRepository: UserRepository,
    private val fallbackVersion: String = "1.0.0"
) : ViewModel() {

    private val _uiState = MutableStateFlow(VersionUiState(version = fallbackVersion))
    val uiState = _uiState.asStateFlow()

    init {
        loadVersion()
    }

    /**
     * Load version dari API, dengan fallback ke package version jika gagal
     */
    fun loadVersion() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val versionResult = userRepository.getAppVersion()

            if (versionResult.isSuccess) {
                val version = versionResult.getOrNull()
                // Jika berhasil dapat dari API, gunakan itu
                // Jika null atau kosong, gunakan fallback
                val finalVersion = if (version.isNullOrEmpty()) fallbackVersion else version
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        version = finalVersion,
                        errorMessage = null
                    )
                }
            } else {
                // Jika API gagal, gunakan fallback version (dari package info)
                // Tidak perlu tampilkan error karena sudah ada fallback
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        version = fallbackVersion,
                        errorMessage = null // Tidak tampilkan error, langsung pakai fallback
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

