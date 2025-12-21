// app/src/main/java/id/xetor/app/ui/qrcode/QrCodeViewModel.kt
package id.xetor.app.ui.qrcode

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.xetor.app.data.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class QrCodeUiState(
    val isLoading: Boolean = false,
    val token: String? = null,
    val expiresAt: Date? = null,
    val isExpired: Boolean = false,
    val errorMessage: String? = null
)

class QrCodeViewModel(
    private val userRepository: UserRepository,
    private val token: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(QrCodeUiState())
    val uiState = _uiState.asStateFlow()

    fun generateQrToken() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            val result = userRepository.generateDepositQrToken(token)
            
            if (result.isSuccess) {
                val response = result.getOrNull()
                if (response != null) {
                    val expiresAt = parseExpiresAt(response.expiresAt)
                    if (expiresAt != null) {
                        // Update state dengan expiresAt, timer akan dihitung di UI layer
                        _uiState.value = QrCodeUiState(
                            isLoading = false,
                            token = response.token,
                            expiresAt = expiresAt,
                            isExpired = false,
                            errorMessage = null
                        )
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Gagal memparse waktu kedaluwarsa",
                                token = null,
                                expiresAt = null
                            )
                        }
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Gagal mendapatkan token QR",
                            token = null,
                            expiresAt = null
                        )
                    }
                }
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Gagal membuat QR Code"
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = errorMsg,
                        token = null,
                        expiresAt = null
                    )
                }
            }
        }
    }

    private fun parseExpiresAt(expiresAtString: String): Date? {
        return try {
            var cleaned = expiresAtString.replace(" ", "")
            cleaned = cleaned.replace(Regex("\\.\\d{6}"), { matchResult ->
                "." + matchResult.value.substring(1, 4)
            })
            
            if (cleaned.endsWith("Z")) {
                cleaned = cleaned.replace("Z", "+0000")
            }
            
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault())
            dateFormat.parse(cleaned)
        } catch (e: Exception) {
            try {
                var cleaned = expiresAtString.replace(" ", "")
                cleaned = cleaned.replace(Regex("\\.\\d+"), "")
                
                if (cleaned.endsWith("Z")) {
                    cleaned = cleaned.replace("Z", "+0000")
                }
                
                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault())
                dateFormat.parse(cleaned)
            } catch (e2: Exception) {
                null
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

