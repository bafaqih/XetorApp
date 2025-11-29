// app/src/main/java/id/xetor/app/TopUpActivity.kt
package id.xetor.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.ui.platform.LocalContext
import id.xetor.app.ui.components.TokenExpiredDialog
import id.xetor.app.ui.theme.XetorAppTheme
import id.xetor.app.ui.topup.TopUpScreen
import id.xetor.app.ui.topup.TopUpViewModel
import id.xetor.app.ui.topup.TopUpViewModelFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.content.Intent
import android.net.Uri

class TopUpActivity : ComponentActivity() {
    
    // Get dependencies
    private val appContainer by lazy {
        (application as XetorApplication).appContainer
    }
    
    private val token by lazy {
        runBlocking { appContainer.userPreferences.authToken.first() } ?: ""
    }
    
    // Activity-scoped ViewModel
    private val topUpViewModel: TopUpViewModel by viewModels {
        TopUpViewModelFactory(
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

                TopUpScreen(
                    viewModel = topUpViewModel,
                    onBackClick = {
                        finish()
                    },
                    onProceedToPayment = { amount, onSuccess, onError, onCancel ->
                        handleTopUpRequest(amount, onSuccess, onError, onCancel)
                    }
                )
            }
        }
    }
    
    private fun handleTopUpRequest(
        amount: Double,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onCancel: () -> Unit
    ) {
        // Request topup ke backend
        CoroutineScope(Dispatchers.Main).launch {
            val result = appContainer.userRepository.requestTopup(token, amount)
            
            result.onSuccess { topupResponse ->
                // Buka payment modal menggunakan WebView
                openMidtransPaymentModal(
                    snapToken = topupResponse.snapToken,
                    redirectUrl = topupResponse.redirectUrl,
                    onSuccess = onSuccess,
                    onError = onError,
                    onCancel = onCancel
                )
            }.onFailure { error ->
                onError(error.message ?: "Gagal memproses top up")
            }
        }
    }
    
    private fun openMidtransPaymentModal(
        snapToken: String,
        redirectUrl: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onCancel: () -> Unit
    ) {
        try {
            // Buka WebView dialog untuk payment
            val dialog = MidtransPaymentDialog(
                context = this,
                redirectUrl = redirectUrl,
                onSuccess = {
                    // Payment berhasil, tutup dialog dan panggil onSuccess
                    onSuccess()
                },
                onError = { errorMsg ->
                    // Payment gagal atau dibatalkan
                    onError(errorMsg)
                },
                onDismiss = {
                    // User tutup dialog tanpa selesai payment (klik overlay atau back)
                    // Stop loading dan tidak create record di DB
                    onCancel()
                }
            )
            dialog.show()
            
        } catch (e: Exception) {
            onError("Gagal membuka halaman pembayaran: ${e.message}")
        }
    }
}

