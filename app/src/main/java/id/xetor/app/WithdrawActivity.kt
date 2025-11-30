// app/src/main/java/id/xetor/app/WithdrawActivity.kt
package id.xetor.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
    
    // Get dependencies
    private val appContainer by lazy {
        (application as XetorApplication).appContainer
    }
    
    private val token by lazy {
        runBlocking { appContainer.userPreferences.authToken.first() } ?: ""
    }
    
    // Activity-scoped ViewModel untuk bisa diakses dari onActivityResult
    private val withdrawViewModel: WithdrawViewModel by viewModels {
        WithdrawViewModelFactory(
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

                WithdrawScreen(
                    viewModel = withdrawViewModel,
                    onTopUpClick = {
                        startActivity(Intent(this@WithdrawActivity, TopUpActivity::class.java))
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
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == REQUEST_WITHDRAW && resultCode == RESULT_OK) {
            // Withdraw berhasil, force refresh data tanpa menunggu interval
            withdrawViewModel.forceRefresh()
            // Trigger refresh home di background
            (application as XetorApplication).triggerHomeRefresh()
        }
    }
}

