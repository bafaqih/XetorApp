// app/src/main/java/id/xetor/app/ui/transactionhistory/TransactionHistoryViewModelFactory.kt
package id.xetor.app.ui.transactionhistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import id.xetor.app.data.UserRepository

class TransactionHistoryViewModelFactory(
    private val userRepository: UserRepository,
    private val token: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransactionHistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TransactionHistoryViewModel(userRepository, token) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

