// app/src/main/java/id/xetor/app/ConversionActivity.kt
package id.xetor.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import id.xetor.app.ui.components.TokenExpiredDialog
import id.xetor.app.ui.conversion.ConversionScreen
import id.xetor.app.ui.conversion.ConversionViewModel
import id.xetor.app.ui.conversion.ConversionViewModelFactory
import id.xetor.app.ui.theme.XetorAppTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class ConversionActivity : ComponentActivity() {
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
                val conversionViewModel: ConversionViewModel = viewModel(
                    factory = ConversionViewModelFactory(
                        userRepository = appContainer.userRepository,
                        token = token
                    )
                )

                ConversionScreen(
                    viewModel = conversionViewModel,
                    onBackClick = {
                        finish()
                    },
                    onSuccessNavigateBack = {
                        // Trigger refresh home di background
                        (application as XetorApplication).triggerHomeRefresh()
                        // Trigger refresh profile statistics di background
                        (application as XetorApplication).triggerProfileStatisticsRefresh()
                        finish()
                    },
                    onSuccess = {
                        // Trigger refresh home di background saat konversi berhasil
                        (application as XetorApplication).triggerHomeRefresh()
                        // Trigger refresh profile statistics di background
                        (application as XetorApplication).triggerProfileStatisticsRefresh()
                    }
                )
            }
        }
    }
}

