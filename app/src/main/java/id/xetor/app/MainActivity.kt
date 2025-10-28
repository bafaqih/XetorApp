// app/src/main/java/id/xetor/app/MainActivity.kt
package id.xetor.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import id.xetor.app.data.local.UserPreferences
import id.xetor.app.ui.theme.XetorAppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            XetorAppTheme {
                LaunchedEffect(key1 = true) {
                    delay(1500)

                    // Gunakan UserPreferences, bukan SessionManager
                    val userPreferences = UserPreferences(applicationContext)

                    // Cek apakah token ada atau tidak
                    val authToken = userPreferences.authToken.first() // .first() untuk mengambil satu nilai saja

                    if (authToken != null) {
                        // Jika token ada (sudah login), langsung ke Dashboard
                        startActivity(Intent(this@MainActivity, HomeActivity::class.java))
                    } else {
                        // Jika token null (belum login), ke Onboarding
                        startActivity(Intent(this@MainActivity, OnBoardingActivity::class.java))
                    }
                    finish()
                }
                SplashScreen()
            }
        }
    }
}