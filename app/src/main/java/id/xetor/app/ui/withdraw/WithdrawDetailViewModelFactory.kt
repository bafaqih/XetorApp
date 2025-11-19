// app/src/main/java/id/xetor/app/ui/withdraw/WithdrawDetailViewModelFactory.kt
package id.xetor.app.ui.withdraw

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import id.xetor.app.data.UserRepository

class WithdrawDetailViewModelFactory(
    private val userRepository: UserRepository,
    private val token: String,
    private val paymentMethod: PaymentMethod
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WithdrawDetailViewModel::class.java)) {
            return WithdrawDetailViewModel(userRepository, token, paymentMethod) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

