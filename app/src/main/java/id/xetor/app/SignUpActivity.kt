// app/src/main/java/id/xetor/app/SignUpActivity.kt
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
import kotlinx.coroutines.launch

class SignUpActivity : ComponentActivity() {

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

    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                lifecycleScope.launch {
                    val idToken = googleAuthClient.getIdTokenFromIntent(result.data!!)
                    if (idToken != null) {
                        viewModel.loginWithGoogle(idToken)
                    } else {
                        Toast.makeText(this@SignUpActivity, "Gagal mendapatkan token Google", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this@SignUpActivity, "Login Google dibatalkan", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            XetorAppTheme {
                var name by remember { mutableStateOf("") }
                var email by remember { mutableStateOf("") }
                var phone by remember { mutableStateOf("") }
                var password by remember { mutableStateOf("") }
                var confirmPassword by remember { mutableStateOf("") }
                var termsAccepted by remember { mutableStateOf(false) }

                val authUiState by viewModel.uiState.collectAsState()
                val isLoading = authUiState is AuthUiState.Loading

                LaunchedEffect(authUiState) {
                    when (val state = authUiState) {
                        is AuthUiState.Success -> {
                            // --- PERUBAHAN DI SINI ---
                            Toast.makeText(this@SignUpActivity, "Akun berhasil dibuat! Silakan login.", Toast.LENGTH_LONG).show()

                            // Arahkan ke halaman SignIn, bukan Dashboard
                            val intent = Intent(this@SignUpActivity, SignInActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }
                        is AuthUiState.Error -> {
                            Toast.makeText(this@SignUpActivity, state.message, Toast.LENGTH_LONG).show()
                            Log.e("SIGNUP_ERROR", "Error: ${state.message}")
                        }
                        else -> Unit
                    }
                }

                SignUpScreen(
                    nameValue = name,
                    emailValue = email,
                    phoneValue = phone,
                    passwordValue = password,
                    confirmPasswordValue = confirmPassword,
                    termsAcceptedValue = termsAccepted,
                    onNameChange = { name = it },
                    onEmailChange = { email = it },
                    onPhoneChange = { phone = it },
                    onPasswordChange = { password = it },
                    onConfirmPasswordChange = { confirmPassword = it },
                    onTermsAcceptedChange = { termsAccepted = it },
                    onSignUpClick = {
                        if (password != confirmPassword) {
                            Toast.makeText(this, "Konfirmasi password tidak cocok", Toast.LENGTH_SHORT).show()
                            return@SignUpScreen
                        }
                        viewModel.signUp(name, email, phone, password)
                    },
                    onSignInClick = {
                        startActivity(Intent(this, SignInActivity::class.java))
                        finish()
                    },
                    onBackClick = { finish() },

                    onGoogleSignInClick = {
                        googleSignInLauncher.launch(googleAuthClient.googleSignInClient.signInIntent)
                    },
                    onTermsClick = {
                        startActivity(Intent(this@SignUpActivity, TermsAndConditionsActivity::class.java))
                    },
                    onPrivacyClick = {
                        startActivity(Intent(this@SignUpActivity, PrivacyPolicyActivity::class.java))
                    },
                    isLoading = isLoading
                )
            }
        }
    }
}