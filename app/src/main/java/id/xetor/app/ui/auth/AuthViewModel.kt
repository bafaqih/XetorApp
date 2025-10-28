// app/src/main/java/id/xetor/app/ui/auth/AuthViewModel.kt
package id.xetor.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import id.xetor.app.data.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Sealed interface untuk merepresentasikan state UI: Loading, Success, atau Error
sealed interface AuthUiState {
    object Idle : AuthUiState // Kondisi awal
    object Loading : AuthUiState
    object Success : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class AuthViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState = _uiState.asStateFlow() // Versi read-only untuk diakses dari UI

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { AuthUiState.Loading } // 1. Set state menjadi Loading
            val result = userRepository.login(email, password)
            result.onSuccess {
                _uiState.update { AuthUiState.Success } // 2. Jika sukses, set state menjadi Success
            }.onFailure { exception -> // Beri nama 'exception' agar lebih jelas
                _uiState.update { AuthUiState.Error(exception.message ?: "Terjadi kesalahan") }
            }
        }
    }

    fun signUp(name: String, email: String, phone: String, password: String) {
        viewModelScope.launch {
            _uiState.update { AuthUiState.Loading } // Set state jadi Loading
            val result = userRepository.signUp(name, email, phone, password)
            result.onSuccess {
                // Beritahu UI bahwa prosesnya sudah sukses
                _uiState.update { AuthUiState.Success }
            }.onFailure { exception ->
                _uiState.update { AuthUiState.Error(exception.message ?: "Registrasi gagal") }
            }
        }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.update { AuthUiState.Loading } // Set state jadi Loading
            val result = userRepository.loginWithGoogle(idToken)
            result.onSuccess {
                _uiState.update { AuthUiState.Success } // Jika sukses
            }.onFailure { exception -> // <-- Beri nama variabel, misal: 'exception'
                _uiState.update { AuthUiState.Error(exception.message ?: "Login Google Gagal") }
            }
        }
    }
}