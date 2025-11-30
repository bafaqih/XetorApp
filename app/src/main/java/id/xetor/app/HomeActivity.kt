// app/src/main/java/id/xetor/app/HomeActivity.kt
package id.xetor.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import id.xetor.app.di.AppContainer
import id.xetor.app.auth.TokenExpiredManager
import id.xetor.app.ui.components.MainBottomBar
import id.xetor.app.ui.components.TokenExpiredDialog
import id.xetor.app.ui.home.HomeScreen
import id.xetor.app.ui.home.HomeViewModel
import id.xetor.app.ui.home.HomeViewModelFactory
import id.xetor.app.ui.theme.GreenPrimary
import id.xetor.app.ui.theme.XetorAppTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Get dependencies
        val appContainer = (application as XetorApplication).appContainer
        val token = runBlocking { appContainer.userPreferences.authToken.first() } ?: ""
        
        // Jika token kosong, langsung redirect ke OnBoardingActivity
        if (token.isEmpty()) {
            val intent = Intent(this, OnBoardingActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }
        
        // Reset TokenExpiredManager state saat HomeActivity dimulai
        // Ini penting untuk menghindari bug dimana state expired dari session sebelumnya
        // masih aktif dan menyebabkan auto logout
        TokenExpiredManager.reset()
        
        setContent {
            XetorAppTheme {
                var currentScreen by remember { mutableStateOf("home") }
                var isFirstLoad by remember { mutableStateOf(true) }
                var scrollToTopKey by remember { mutableStateOf(0) }
                val context = LocalContext.current

                // Initialize ViewModel
                val homeViewModel: HomeViewModel = viewModel(
                    factory = HomeViewModelFactory(
                        userRepository = appContainer.userRepository,
                        token = token
                    )
                )

                // Observe UI state untuk mendapatkan profile photo URL
                val homeUiState by homeViewModel.uiState.collectAsState()
                val profilePhotoUrl = homeUiState.userProfile?.photo

                // Set flag bahwa load pertama sudah selesai setelah delay singkat
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(2000) // Tunggu 2 detik untuk pastikan load pertama selesai
                    isFirstLoad = false
                }

                // Token Expired Dialog - muncul jika token expired saat user sedang menggunakan app
                // Dialog hanya muncul jika bukan first load (untuk menghindari dialog muncul saat app start)
                if (!isFirstLoad) {
                    TokenExpiredDialog(
                        context = context,
                        userPreferences = appContainer.userPreferences
                    )
                }

                Scaffold(
                    bottomBar = {
                        MainBottomBar(
                            currentRoute = currentScreen,
                            onItemSelected = { route ->
                                if (route != "scan") {
                                    // Jika route sama dengan current screen, trigger scroll to top
                                    if (route == currentScreen && route == "home") {
                                        scrollToTopKey++
                                    } else {
                                        currentScreen = route
                                    }
                                }
                            },
                            profilePhotoUrl = profilePhotoUrl
                        )
                    },
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = {
                                Toast.makeText(context, "Buka Kamera Scan...", Toast.LENGTH_SHORT).show()
                            },
                            shape = CircleShape,
                            containerColor = GreenPrimary,
                            contentColor = Color.White,
                            modifier = Modifier
                                .size(60.dp)
                                .offset(y = 63.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_scan),
                                contentDescription = "Scan",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    },
                    floatingActionButtonPosition = FabPosition.Center
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                    ) {
                        when (currentScreen) {
                            "home" -> HomeScreen(
                                viewModel = homeViewModel,
                                onNotificationClick = {
                                    Toast.makeText(context, "Notifikasi (Coming Soon)", Toast.LENGTH_SHORT).show()
                                },
                                onWithdrawClick = {
                                    startActivity(Intent(this@HomeActivity, WithdrawActivity::class.java))
                                },
                                onTopUpClick = {
                                    startActivity(Intent(this@HomeActivity, TopUpActivity::class.java))
                                },
                                onTransferClick = {
                                    startActivity(Intent(this@HomeActivity, TransferActivity::class.java))
                                },
                                onConvertClick = {
                                    Toast.makeText(context, "Convert (Coming Soon)", Toast.LENGTH_SHORT).show()
                                },
                                onXpayClick = {
                                    Toast.makeText(context, "Xpay (Coming Soon)", Toast.LENGTH_SHORT).show()
                                },
                                onSetorClick = {
                                    Toast.makeText(context, "Generate QR untuk Setor (Coming Soon)", Toast.LENGTH_SHORT).show()
                                },
                                scrollToTopKey = scrollToTopKey
                            )
                            "map" -> Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "Peta (Coming Soon)")
                            }
                            "marketplace" -> Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "Marketplace (Coming Soon)")
                            }
                            "profile" -> Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "Profile (Coming Soon)")
                            }
                        }
                    }
                }
            }
        }
    }
}