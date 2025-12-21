// app/src/main/java/id/xetor/app/PasswordActivity.kt
package id.xetor.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import id.xetor.app.di.AppContainer
import id.xetor.app.ui.profile.PasswordScreen
import id.xetor.app.ui.profile.PasswordViewModel
import id.xetor.app.ui.profile.PasswordViewModelFactory
import id.xetor.app.ui.theme.XetorAppTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class PasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val appContainer = (application as XetorApplication).appContainer
        val token = runBlocking { appContainer.userPreferences.authToken.first() } ?: ""
        
        if (token.isEmpty()) {
            finish()
            return
        }
        
        setContent {
            XetorAppTheme {
                val viewModel: PasswordViewModel = viewModel(
                    factory = PasswordViewModelFactory(
                        userRepository = appContainer.userRepository,
                        token = token
                    )
                )
                
                PasswordScreen(
                    viewModel = viewModel,
                    onBackClick = { finish() }
                )
            }
        }
    }
}

