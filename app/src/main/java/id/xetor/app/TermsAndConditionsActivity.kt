// app/src/main/java/id/xetor/app/TermsAndConditionsActivity.kt
package id.xetor.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import id.xetor.app.di.AppContainer
import id.xetor.app.ui.terms.TermsAndConditionsScreen
import id.xetor.app.ui.terms.TermsAndConditionsViewModel
import id.xetor.app.ui.terms.TermsAndConditionsViewModelFactory
import id.xetor.app.ui.theme.XetorAppTheme

class TermsAndConditionsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val appContainer = (application as XetorApplication).appContainer
        
        setContent {
            XetorAppTheme {
                val viewModel: TermsAndConditionsViewModel = viewModel(
                    factory = TermsAndConditionsViewModelFactory(
                        userRepository = appContainer.userRepository
                    )
                )
                
                TermsAndConditionsScreen(
                    viewModel = viewModel,
                    onBackClick = { finish() }
                )
            }
        }
    }
}

