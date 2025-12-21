// app/src/main/java/id/xetor/app/ui/profile/PasswordViewModel.kt
package id.xetor.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.xetor.app.data.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// State untuk UI Password
data class PasswordUiState(
    val oldPassword: String = "",
    val newPassword: String = "",
    val confirmNewPassword: String = "",
    val isOldPasswordVisible: Boolean = false,
    val isNewPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val oldPasswordError: String? = null,
    val newPasswordError: String? = null,
    val confirmPasswordError: String? = null,
    val isChanging: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class PasswordViewModel(
    private val userRepository: UserRepository,
    private val token: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(PasswordUiState())
    val uiState = _uiState.asStateFlow()

    fun updateOldPassword(password: String) {
        _uiState.update { 
            it.copy(
                oldPassword = password,
                oldPasswordError = null // Clear error when user types
            ) 
        }
    }

    fun updateNewPassword(password: String) {
        _uiState.update { 
            it.copy(
                newPassword = password,
                newPasswordError = null, // Clear error when user types
                confirmPasswordError = null // Clear confirm error too when new password changes
            ) 
        }
    }

    fun updateConfirmPassword(password: String) {
        _uiState.update { 
            it.copy(
                confirmNewPassword = password,
                confirmPasswordError = null // Clear error when user types
            ) 
        }
    }

    fun toggleOldPasswordVisibility() {
        _uiState.update { it.copy(isOldPasswordVisible = !it.isOldPasswordVisible) }
    }

    fun toggleNewPasswordVisibility() {
        _uiState.update { it.copy(isNewPasswordVisible = !it.isNewPasswordVisible) }
    }

    fun toggleConfirmPasswordVisibility() {
        _uiState.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
    }

    fun changePassword() {
        val state = _uiState.value
        
        // Clear previous errors
        var hasError = false
        
        // Validate old password
        if (state.oldPassword.isEmpty()) {
            _uiState.update { it.copy(oldPasswordError = "Kata sandi saat ini harus diisi") }
            hasError = true
        }
        
        // Validate new password
        if (state.newPassword.isEmpty()) {
            _uiState.update { it.copy(newPasswordError = "Kata sandi baru harus diisi") }
            hasError = true
        } else if (state.newPassword.length < 6) {
            _uiState.update { it.copy(newPasswordError = "Kata sandi baru minimal 6 karakter") }
            hasError = true
        }
        
        // Validate confirm password
        if (state.confirmNewPassword.isEmpty()) {
            _uiState.update { it.copy(confirmPasswordError = "Konfirmasi kata sandi baru harus diisi") }
            hasError = true
        } else if (state.newPassword != state.confirmNewPassword) {
            _uiState.update { it.copy(confirmPasswordError = "Konfirmasi kata sandi baru tidak cocok") }
            hasError = true
        }
        
        if (hasError) {
            return
        }
        
        // Call API
        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    isChanging = true,
                    errorMessage = null,
                    successMessage = null
                ) 
            }
            
            val result = userRepository.changePassword(
                token = token,
                oldPassword = state.oldPassword,
                newPassword = state.newPassword,
                confirmNewPassword = state.confirmNewPassword
            )
            
            if (result.isSuccess) {
                // Success - clear all fields
                _uiState.update { 
                    it.copy(
                        isChanging = false,
                        oldPassword = "",
                        newPassword = "",
                        confirmNewPassword = "",
                        oldPasswordError = null,
                        newPasswordError = null,
                        confirmPasswordError = null,
                        successMessage = "Kata sandi berhasil diubah"
                    ) 
                }
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Gagal mengubah kata sandi"
                
                // Check error message and set appropriate field error
                when {
                    errorMsg.contains("password lama salah", ignoreCase = true) -> {
                        _uiState.update { 
                            it.copy(
                                isChanging = false,
                                oldPasswordError = "Kata sandi saat ini salah"
                            ) 
                        }
                    }
                    errorMsg.contains("konfirmasi password baru tidak cocok", ignoreCase = true) -> {
                        _uiState.update { 
                            it.copy(
                                isChanging = false,
                                confirmPasswordError = "Konfirmasi kata sandi baru tidak cocok"
                            ) 
                        }
                    }
                    errorMsg.contains("password baru minimal 6 karakter", ignoreCase = true) -> {
                        _uiState.update { 
                            it.copy(
                                isChanging = false,
                                newPasswordError = "Kata sandi baru minimal 6 karakter"
                            ) 
                        }
                    }
                    else -> {
                        _uiState.update { 
                            it.copy(
                                isChanging = false,
                                errorMessage = errorMsg
                            ) 
                        }
                    }
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun clearSuccess() {
        _uiState.update { it.copy(successMessage = null) }
    }
}

