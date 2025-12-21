// app/src/main/java/id/xetor/app/SignInActivity.kt
package id.xetor.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.*
import id.xetor.app.ui.auth.AuthUiState
import id.xetor.app.ui.auth.AuthViewModel
import id.xetor.app.ui.auth.ViewModelFactory
import id.xetor.app.ui.theme.XetorAppTheme

import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import id.xetor.app.auth.GoogleAuthClient
import id.xetor.app.ui.theme.XetorAppTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SignInActivity : ComponentActivity() {

    // Ambil AppContainer dari Application
    private val appContainer by lazy {
        (application as XetorApplication).appContainer
    }
    private val factory by lazy {
        ViewModelFactory(appContainer.userRepository)
    }
    private val viewModel: AuthViewModel by viewModels { factory }

    private val googleAuthClient by lazy {
        GoogleAuthClient(this)
    }

    // Launcher untuk menerima hasil dari pop-up Google
    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                lifecycleScope.launch {
                    val idToken = googleAuthClient.getIdTokenFromIntent(result.data!!)
                    if (idToken != null) {
                        viewModel.loginWithGoogle(idToken)
                    } else {
                        Toast.makeText(this@SignInActivity, "Gagal mendapatkan token Google", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this@SignInActivity, "Login Google dibatalkan", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            XetorAppTheme {
                // State "diangkat" dan dipegang oleh Activity
                var email by remember { mutableStateOf("") }
                var password by remember { mutableStateOf("") }
                var rememberMe by remember { mutableStateOf(false) }

                val authUiState by viewModel.uiState.collectAsState()
                val isLoading = authUiState is AuthUiState.Loading

                // Memuat email yang tersimpan saat activity dibuka
                LaunchedEffect(Unit) {
                    val rememberedEmail = appContainer.userPreferences.rememberedEmail.first()
                    val isRememberMe = appContainer.userPreferences.rememberMe.first()
                    
                    if (rememberedEmail != null && isRememberMe) {
                        email = rememberedEmail
                        rememberMe = true
                    }
                }

                LaunchedEffect(authUiState) {
                    when (val state = authUiState) {
                        is AuthUiState.Success -> {
                            // Simpan email jika checkbox "Ingat Saya" dicentang
                            lifecycleScope.launch {
                                if (rememberMe) {
                                    appContainer.userPreferences.saveRememberedEmail(email)
                                } else {
                                    appContainer.userPreferences.clearRememberedEmail()
                                }
                            }
                            
                            Toast.makeText(this@SignInActivity, "Login berhasil!", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@SignInActivity, HomeActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }
                        is AuthUiState.Error -> {
                            Toast.makeText(this@SignInActivity, state.message, Toast.LENGTH_LONG).show()
                            Log.e("LOGIN_ERROR", "Error: ${state.message}")
                        }
                        else -> Unit
                    }
                }

                // Hapus email yang tersimpan jika checkbox tidak dicentang
                LaunchedEffect(rememberMe) {
                    if (!rememberMe) {
                        lifecycleScope.launch {
                            appContainer.userPreferences.clearRememberedEmail()
                        }
                    }
                }

                SignInScreen(
                    emailValue = email,
                    passwordValue = password,
                    rememberMeValue = rememberMe,
                    onEmailChange = { email = it },
                    onPasswordChange = { password = it },
                    onRememberMeChange = { rememberMe = it },
                    onSignInClick = {
                        viewModel.login(email, password)
                    },
                    onSignUpClick = { startActivity(Intent(this, SignUpActivity::class.java)) },
                    onForgotPasswordClick = {
                        Toast.makeText(this, "Fitur belum tersedia", Toast.LENGTH_SHORT).show()
                    },
                    onBackClick = { finish() },

                    onGoogleSignInClick = {
                        googleSignInLauncher.launch(googleAuthClient.googleSignInClient.signInIntent)
                    },
                    isLoading = isLoading
                )
            }
        }
    }
}