// app/src/main/java/id/xetor/app/ui/profile/ProfilSayaViewModelFactory.kt
package id.xetor.app.ui.profile

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import id.xetor.app.data.UserRepository
import id.xetor.app.data.local.UserPreferences

class ProfilSayaViewModelFactory(
    private val userRepository: UserRepository,
    private val token: String,
    private val userPreferences: UserPreferences,
    private val application: Application? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfilSayaViewModel::class.java)) {
            return ProfilSayaViewModel(userRepository, token, userPreferences, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

