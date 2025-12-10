// app/src/main/java/id/xetor/app/QrCodeActivity.kt
package id.xetor.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import id.xetor.app.ui.components.TokenExpiredDialog
import id.xetor.app.ui.qrcode.QrCodeScreen
import id.xetor.app.ui.qrcode.QrCodeViewModel
import id.xetor.app.ui.qrcode.QrCodeViewModelFactory
import id.xetor.app.ui.theme.XetorAppTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class QrCodeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appContainer = (application as XetorApplication).appContainer
        val token = runBlocking { appContainer.userPreferences.authToken.first() } ?: ""

        setContent {
            XetorAppTheme {
                val context = LocalContext.current
                
                TokenExpiredDialog(
                    context = context,
                    userPreferences = appContainer.userPreferences
                )
                
                // Buat ViewModel baru setiap kali Activity dibuat
                val qrCodeViewModel: QrCodeViewModel = viewModel(
                    factory = QrCodeViewModelFactory(
                        userRepository = appContainer.userRepository,
                        token = token
                    )
                )

                // Selalu generate QR baru saat masuk halaman
                LaunchedEffect(Unit) {
                    qrCodeViewModel.generateQrToken()
                }

                QrCodeScreen(
                    viewModel = qrCodeViewModel,
                    onBackClick = {
                        finish()
                    }
                )
            }
        }
    }
}
