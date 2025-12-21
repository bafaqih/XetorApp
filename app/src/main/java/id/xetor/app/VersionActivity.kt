// app/src/main/java/id/xetor/app/VersionActivity.kt
package id.xetor.app

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import id.xetor.app.di.AppContainer
import id.xetor.app.ui.version.VersionScreen
import id.xetor.app.ui.version.VersionViewModel
import id.xetor.app.ui.version.VersionViewModelFactory
import id.xetor.app.ui.theme.XetorAppTheme

class VersionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val appContainer = (application as XetorApplication).appContainer
        
        // Get app version dari package info sebagai fallback
        val appVersion = try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            packageInfo.versionName ?: "1.0.0"
        } catch (e: PackageManager.NameNotFoundException) {
            "1.0.0"
        }
        
        setContent {
            XetorAppTheme {
                val viewModel: VersionViewModel = viewModel(
                    factory = VersionViewModelFactory(
                        userRepository = appContainer.userRepository,
                        fallbackVersion = appVersion
                    )
                )
                
                VersionScreen(
                    viewModel = viewModel,
                    onBackClick = { finish() }
                )
            }
        }
    }
}

