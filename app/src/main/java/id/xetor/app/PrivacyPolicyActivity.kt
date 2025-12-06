// app/src/main/java/id/xetor/app/PrivacyPolicyActivity.kt
package id.xetor.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import id.xetor.app.di.AppContainer
import id.xetor.app.ui.privacy.PrivacyPolicyScreen
import id.xetor.app.ui.privacy.PrivacyPolicyViewModel
import id.xetor.app.ui.privacy.PrivacyPolicyViewModelFactory
import id.xetor.app.ui.theme.XetorAppTheme

class PrivacyPolicyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val appContainer = (application as XetorApplication).appContainer
        
        setContent {
            XetorAppTheme {
                val viewModel: PrivacyPolicyViewModel = viewModel(
                    factory = PrivacyPolicyViewModelFactory(
                        userRepository = appContainer.userRepository
                    )
                )
                
                PrivacyPolicyScreen(
                    viewModel = viewModel,
                    onBackClick = { finish() }
                )
            }
        }
    }
}

