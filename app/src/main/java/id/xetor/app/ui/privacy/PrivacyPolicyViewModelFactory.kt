// app/src/main/java/id/xetor/app/ui/privacy/PrivacyPolicyViewModelFactory.kt
package id.xetor.app.ui.privacy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import id.xetor.app.data.UserRepository

class PrivacyPolicyViewModelFactory(
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PrivacyPolicyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PrivacyPolicyViewModel(userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

