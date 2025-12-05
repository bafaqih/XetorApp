// app/src/main/java/id/xetor/app/ui/profile/AddressViewModelFactory.kt
package id.xetor.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import id.xetor.app.data.UserRepository

class AddressViewModelFactory(
    private val userRepository: UserRepository,
    private val token: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddressViewModel::class.java)) {
            return AddressViewModel(userRepository, token) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

