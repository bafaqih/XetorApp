// app/src/main/java/id/xetor/app/TransactionHistoryActivity.kt
package id.xetor.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.ui.platform.LocalContext
import id.xetor.app.ui.components.TokenExpiredDialog
import id.xetor.app.ui.theme.XetorAppTheme
import id.xetor.app.ui.transactionhistory.TransactionHistoryScreen
import id.xetor.app.ui.transactionhistory.TransactionHistoryViewModel
import id.xetor.app.ui.transactionhistory.TransactionHistoryViewModelFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class TransactionHistoryActivity : ComponentActivity() {
    
    // Get dependencies
    private val appContainer by lazy {
        (application as XetorApplication).appContainer
    }
    
    private val token by lazy {
        runBlocking { appContainer.userPreferences.authToken.first() } ?: ""
    }
    
    // Activity-scoped ViewModel
    private val transactionHistoryViewModel: TransactionHistoryViewModel by viewModels {
        TransactionHistoryViewModelFactory(
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

                TransactionHistoryScreen(
                    viewModel = transactionHistoryViewModel,
                    onBackClick = {
                        finish()
                    }
                )
            }
        }
    }
}

