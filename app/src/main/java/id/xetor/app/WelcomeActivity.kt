// app/src/main/java/id/xetor/app/WelcomeActivity.kt
package id.xetor.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import id.xetor.app.ui.theme.XetorAppTheme
import android.content.Intent

class WelcomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            XetorAppTheme {
                WelcomeScreen(
                    onSignInClick = {
                        startActivity(Intent(this, SignInActivity::class.java))
                    },
                    onSignUpClick = {
                        startActivity(Intent(this, SignUpActivity::class.java))
                    }   
                )
            }
        }
    }
}