// app/src/main/java/id/xetor/app/ui/profile/ProfileViewModelFactory.kt
package id.xetor.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import id.xetor.app.data.UserRepository
import id.xetor.app.data.local.UserPreferences

class ProfileViewModelFactory(
    private val userRepository: UserRepository,
    private val token: String,
    private val fallbackVersion: String,
    private val userPreferences: UserPreferences
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(userRepository, token, fallbackVersion, userPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

