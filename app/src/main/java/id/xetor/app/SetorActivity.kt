// app/src/main/java/id/xetor/app/SetorActivity.kt
package id.xetor.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import id.xetor.app.ui.components.TokenExpiredDialog
import id.xetor.app.ui.setor.SetorScreen
import id.xetor.app.ui.setor.SetorViewModel
import id.xetor.app.ui.setor.SetorViewModelFactory
import id.xetor.app.ui.theme.XetorAppTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class SetorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get dependencies
        val appContainer = (application as XetorApplication).appContainer
        val token = runBlocking { appContainer.userPreferences.authToken.first() } ?: ""

        setContent {
            XetorAppTheme {
                val context = LocalContext.current
                
                // Token Expired Dialog
                TokenExpiredDialog(
                    context = context,
                    userPreferences = appContainer.userPreferences
                )
                
                // Initialize ViewModel
                val setorViewModel: SetorViewModel = viewModel(
                    factory = SetorViewModelFactory(
                        userRepository = appContainer.userRepository,
                        token = token
                    )
                )

                SetorScreen(
                    viewModel = setorViewModel,
                    onBackClick = {
                        finish()
                    },
                    onSuccess = {
                        // Trigger refresh home di background saat deposit berhasil
                        (application as XetorApplication).triggerHomeRefresh()
                        // Trigger refresh profile statistics di background
                        (application as XetorApplication).triggerProfileStatisticsRefresh()
                    }
                )
            }
        }
    }
}

