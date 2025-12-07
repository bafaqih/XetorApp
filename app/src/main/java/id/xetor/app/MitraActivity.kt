// app/src/main/java/id/xetor/app/MitraActivity.kt
package id.xetor.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import id.xetor.app.di.AppContainer
import id.xetor.app.XetorApplication
import id.xetor.app.ui.mitra.MitraScreen
import id.xetor.app.ui.mitra.MitraViewModelFactory
import id.xetor.app.ui.theme.XetorAppTheme

class MitraActivity : ComponentActivity() {
    private val appContainer: AppContainer by lazy {
        (application as XetorApplication).appContainer
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            XetorAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: id.xetor.app.ui.mitra.MitraViewModel = viewModel(
                        factory = MitraViewModelFactory(
                            userRepository = appContainer.userRepository
                        )
                    )

                    MitraScreen(
                        viewModel = viewModel,
                        onBackClick = { finish() }
                    )
                }
            }
        }
    }
}

