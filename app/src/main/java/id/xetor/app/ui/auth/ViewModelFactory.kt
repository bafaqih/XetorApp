// app/src/main/java/id/xetor/app/ui/auth/ViewModelFactory.kt
package id.xetor.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import id.xetor.app.data.UserRepository
import java.lang.IllegalArgumentException

class ViewModelFactory(private val userRepository: UserRepository) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(userRepository) as T
        }
        // TODO: Tambahkan logic untuk membuat ViewModel lain di sini nanti

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}