// app/src/main/java/id/xetor/app/ui/conversion/ConversionViewModelFactory.kt
package id.xetor.app.ui.conversion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import id.xetor.app.data.UserRepository

class ConversionViewModelFactory(
    private val userRepository: UserRepository,
    private val token: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ConversionViewModel::class.java)) {
            return ConversionViewModel(userRepository, token) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

