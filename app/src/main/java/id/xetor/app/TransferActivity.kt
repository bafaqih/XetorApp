// app/src/main/java/id/xetor/app/TransferActivity.kt
package id.xetor.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.ui.platform.LocalContext
import id.xetor.app.ui.components.TokenExpiredDialog
import id.xetor.app.ui.theme.XetorAppTheme
import id.xetor.app.ui.transfer.TransferScreen
import id.xetor.app.ui.transfer.TransferViewModel
import id.xetor.app.ui.transfer.TransferViewModelFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class TransferActivity : ComponentActivity() {
    
    // Get dependencies
    private val appContainer by lazy {
        (application as XetorApplication).appContainer
    }
    
    private val token by lazy {
        runBlocking { appContainer.userPreferences.authToken.first() } ?: ""
    }
    
    // Activity-scoped ViewModel
    private val transferViewModel: TransferViewModel by viewModels {
        TransferViewModelFactory(
            userRepository = appContainer.userRepository,
            token = token
        )
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            XetorAppTheme {
                val context = LocalContext.current
                
                // Token Expired Dialog
                TokenExpiredDialog(
                    context = context,
                    userPreferences = appContainer.userPreferences
                )

                TransferScreen(
                    viewModel = transferViewModel,
                    onBackClick = {
                        finish()
                    }
                )
            }
        }
    }
}

