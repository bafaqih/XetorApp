// app/src/main/java/id/xetor/app/ui/withdraw/WithdrawDetailViewModel.kt
package id.xetor.app.ui.withdraw

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.xetor.app.data.UserRepository
import id.xetor.app.data.remote.WalletResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// State untuk UI Withdraw Detail
data class WithdrawDetailUiState(
    val isLoading: Boolean = true,
    val wallet: WalletResponse? = null,
    val amount: String = "",
    val accountNumber: String = "",
    val accountHolderName: String = "",
    val isSubmitting: Boolean = false,
    val showSuccessDialog: Boolean = false,
    val errorMessage: String? = null
)

class WithdrawDetailViewModel(
    private val userRepository: UserRepository,
    private val token: String,
    private val paymentMethod: PaymentMethod
) : ViewModel() {

    private val _uiState = MutableStateFlow(WithdrawDetailUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadWallet()
    }

    private fun loadWallet() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val walletResult = userRepository.getUserWallet(token)

            if (walletResult.isSuccess) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        wallet = walletResult.getOrNull(),
                        errorMessage = null
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Gagal memuat data saldo"
                    )
                }
            }
        }
    }

    fun setAmount(value: String) {
        // Only allow digits
        val filtered = value.filter { it.isDigit() }
        _uiState.update { it.copy(amount = filtered) }
    }

    fun setAccountNumber(value: String) {
        // Only allow digits
        val filtered = value.filter { it.isDigit() }
        _uiState.update { it.copy(accountNumber = filtered) }
    }

    fun setAccountHolderName(value: String) {
        _uiState.update { it.copy(accountHolderName = value) }
    }

    fun submitWithdraw() {
        val currentState = _uiState.value

        // Guard: Prevent double submission
        if (currentState.isSubmitting) {
            return
        }

        // Set submitting flag immediately
        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }

        // Validation
        val errors = mutableListOf<String>()
        
        if (currentState.amount.isEmpty()) {
            errors.add("Nominal withdraw harus diisi")
        }
        
        val amountValue = currentState.amount.toIntOrNull() ?: 0
        if (amountValue < 10000) {
            errors.add("Minimum penarikan Rp10.000")
        }
        
        if (currentState.accountNumber.isEmpty()) {
            val label = if (isEWallet()) "Nomor HP" else "Nomor Rekening"
            errors.add("$label harus diisi")
        }
        
        if (!isEWallet() && currentState.accountHolderName.isEmpty()) {
            errors.add("Nama Pemilik Rekening harus diisi")
        }

        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(isSubmitting = false, errorMessage = errors.first()) }
            return
        }

        // Submit
        viewModelScope.launch {

            val result = userRepository.submitWithdraw(
                token = token,
                paymentMethodId = paymentMethod.id,
                accountNumber = currentState.accountNumber,
                amount = amountValue.toDouble(),
                accountHolderName = if (isEWallet()) null else currentState.accountHolderName
            )

            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        showSuccessDialog = true,
                        errorMessage = null
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Gagal melakukan withdraw"
                    )
                }
            }
        }
    }

    fun dismissSuccessDialog() {
        _uiState.update { it.copy(showSuccessDialog = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun isEWallet(): Boolean {
        return paymentMethod.name.lowercase() in listOf("gopay", "shopeepay", "dana", "ovo", "linkaja")
    }
}

