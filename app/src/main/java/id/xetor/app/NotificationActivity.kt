// app/src/main/java/id/xetor/app/NotificationActivity.kt
package id.xetor.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import id.xetor.app.ui.components.TokenExpiredDialog
import id.xetor.app.ui.notification.NotificationScreen
import id.xetor.app.ui.theme.XetorAppTheme
import kotlinx.coroutines.delay

class NotificationActivity : ComponentActivity() {
    
    // Get dependencies
    private val appContainer by lazy {
        (application as XetorApplication).appContainer
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            XetorAppTheme {
                val context = LocalContext.current
                var isLoading by remember { mutableStateOf(true) }
                
                // Delay skeleton loading 1 detik untuk testing
                LaunchedEffect(Unit) {
                    delay(1000)
                    isLoading = false
                }
                
                // Token Expired Dialog
                TokenExpiredDialog(
                    context = context,
                    userPreferences = appContainer.userPreferences
                )

                NotificationScreen(
                    onBackClick = {
                        finish()
                    },
                    isLoading = isLoading
                )
            }
        }
    }
}
