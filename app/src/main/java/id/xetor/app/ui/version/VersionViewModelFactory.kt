// app/src/main/java/id/xetor/app/ui/version/VersionViewModelFactory.kt
package id.xetor.app.ui.version

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import id.xetor.app.data.UserRepository

class VersionViewModelFactory(
    private val userRepository: UserRepository,
    private val fallbackVersion: String = "1.0.0"
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VersionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VersionViewModel(userRepository, fallbackVersion) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

