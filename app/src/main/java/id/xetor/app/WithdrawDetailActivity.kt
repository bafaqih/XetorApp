// app/src/main/java/id/xetor/app/WithdrawDetailActivity.kt
package id.xetor.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import id.xetor.app.ui.theme.XetorAppTheme
import id.xetor.app.ui.withdraw.PaymentMethod
import id.xetor.app.ui.withdraw.WithdrawDetailScreen
import id.xetor.app.ui.withdraw.WithdrawDetailViewModel
import id.xetor.app.ui.withdraw.WithdrawDetailViewModelFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class WithdrawDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get payment method from intent
        val methodId = intent.getIntExtra("PAYMENT_METHOD_ID", 1)
        val methodName = intent.getStringExtra("PAYMENT_METHOD_NAME") ?: "Gopay"
        val methodIconRes = intent.getIntExtra("PAYMENT_METHOD_ICON", id.xetor.app.R.drawable.ic_gopay)
        val methodIsAvailable = intent.getBooleanExtra("PAYMENT_METHOD_AVAILABLE", false)

        val paymentMethod = PaymentMethod(
            id = methodId,
            name = methodName,
            iconRes = methodIconRes,
            isAvailable = methodIsAvailable
        )

        // Get dependencies
        val appContainer = (application as XetorApplication).appContainer
        val token = runBlocking { appContainer.userPreferences.authToken.first() } ?: ""

        setContent {
            XetorAppTheme {
                // Initialize ViewModel
                val withdrawDetailViewModel: WithdrawDetailViewModel = viewModel(
                    factory = WithdrawDetailViewModelFactory(
                        userRepository = appContainer.userRepository,
                        token = token,
                        paymentMethod = paymentMethod
                    )
                )

                WithdrawDetailScreen(
                    viewModel = withdrawDetailViewModel,
                    paymentMethod = paymentMethod,
                    onBackClick = {
                        finish()
                    },
                    onSuccessNavigateBack = {
                        // Close this activity and refresh previous activity
                        setResult(RESULT_OK)
                        finish()
                    }
                )
            }
        }
    }
}

