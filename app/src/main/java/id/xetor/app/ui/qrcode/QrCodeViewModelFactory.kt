// app/src/main/java/id/xetor/app/ui/qrcode/QrCodeViewModelFactory.kt
package id.xetor.app.ui.qrcode

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import id.xetor.app.data.UserRepository

class QrCodeViewModelFactory(
    private val userRepository: UserRepository,
    private val token: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QrCodeViewModel::class.java)) {
            return QrCodeViewModel(userRepository, token) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

