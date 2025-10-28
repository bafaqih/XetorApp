// app/src/main/java/id/xetor/app/OnBoardingActivity.kt
package id.xetor.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import id.xetor.app.ui.theme.XetorAppTheme
import android.content.Intent

class OnBoardingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            XetorAppTheme {
                OnBoardingScreen(
                    onStartedClick = {
                        val intent = Intent(this, WelcomeActivity::class.java)
                        startActivity(intent)
                        finish() // Tutup onboarding setelah selesai
                    }
                )
            }
        }
    }
}