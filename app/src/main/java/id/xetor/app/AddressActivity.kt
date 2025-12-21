// app/src/main/java/id/xetor/app/AddressActivity.kt
package id.xetor.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import id.xetor.app.di.AppContainer
import id.xetor.app.ui.profile.AddressScreen
import id.xetor.app.ui.profile.AddressViewModel
import id.xetor.app.ui.profile.AddressViewModelFactory
import id.xetor.app.ui.theme.XetorAppTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class AddressActivity : ComponentActivity() {
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
                val viewModel: AddressViewModel = viewModel(
                    factory = AddressViewModelFactory(
                        userRepository = appContainer.userRepository,
                        token = token
                    )
                )
                
                AddressScreen(
                    viewModel = viewModel,
                    onBackClick = { finish() }
                )
            }
        }
    }
}

