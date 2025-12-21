// app/src/main/java/id/xetor/app/ui/terms/TermsAndConditionsViewModelFactory.kt
package id.xetor.app.ui.terms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import id.xetor.app.data.UserRepository

class TermsAndConditionsViewModelFactory(
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TermsAndConditionsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TermsAndConditionsViewModel(userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

