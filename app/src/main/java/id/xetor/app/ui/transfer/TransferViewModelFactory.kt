// app/src/main/java/id/xetor/app/ui/transfer/TransferViewModelFactory.kt
package id.xetor.app.ui.transfer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import id.xetor.app.data.UserRepository

class TransferViewModelFactory(
    private val userRepository: UserRepository,
    private val token: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransferViewModel::class.java)) {
            return TransferViewModel(userRepository, token) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

