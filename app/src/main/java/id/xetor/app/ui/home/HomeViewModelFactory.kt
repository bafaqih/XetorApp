// app/src/main/java/id/xetor/app/ui/home/HomeViewModelFactory.kt
package id.xetor.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import id.xetor.app.data.UserRepository

class HomeViewModelFactory(
    private val userRepository: UserRepository,
    private val token: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(userRepository, token) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

