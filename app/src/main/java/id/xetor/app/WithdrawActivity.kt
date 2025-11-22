// app/src/main/java/id/xetor/app/WithdrawActivity.kt
package id.xetor.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import id.xetor.app.ui.components.TokenExpiredDialog
import id.xetor.app.ui.theme.XetorAppTheme
import id.xetor.app.ui.withdraw.WithdrawScreen
import id.xetor.app.ui.withdraw.WithdrawViewModel
import id.xetor.app.ui.withdraw.WithdrawViewModelFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class WithdrawActivity : ComponentActivity() {
    
    companion object {
        private const val REQUEST_WITHDRAW = 1001
    }
    
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
                val withdrawViewModel: WithdrawViewModel = viewModel(
                    factory = WithdrawViewModelFactory(
                        userRepository = appContainer.userRepository,
                        token = token
                    )
                )

                WithdrawScreen(
                    viewModel = withdrawViewModel,
                    onTopUpClick = {
                        Toast.makeText(this, "Top Up (Coming Soon)", Toast.LENGTH_SHORT).show()
                    },
                    onPaymentMethodClick = { method ->
                        if (method.isAvailable) {
                            // Navigate to withdraw detail
                            val intent = Intent(this, WithdrawDetailActivity::class.java).apply {
                                putExtra("PAYMENT_METHOD_ID", method.id)
                                putExtra("PAYMENT_METHOD_NAME", method.name)
                                putExtra("PAYMENT_METHOD_ICON", method.iconRes)
                                putExtra("PAYMENT_METHOD_AVAILABLE", method.isAvailable)
                            }
                            startActivityForResult(intent, REQUEST_WITHDRAW)
                        } else {
                            Toast.makeText(
                                this,
                                "${method.name} - Coming Soon",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    onBackClick = {
                        finish()
                    }
                )
            }
        }
    }
    
}

