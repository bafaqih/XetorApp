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
import id.xetor.app.ui.profile.ProfileScreen
import id.xetor.app.ui.profile.ProfileViewModel
import id.xetor.app.ui.profile.ProfileViewModelFactory
import id.xetor.app.ProfilSayaActivity
import id.xetor.app.ui.shop.ShopScreen
import id.xetor.app.ui.mitra.MitraScreen
import id.xetor.app.ui.mitra.MitraViewModel
import id.xetor.app.ui.mitra.MitraViewModelFactory
import id.xetor.app.ui.theme.GreenPrimary
import id.xetor.app.ui.theme.XetorAppTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import android.content.pm.PackageManager

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
                val photoRefreshKey by homeViewModel.photoRefreshKeyFlow.collectAsState()

                // Preload ProfileViewModel setelah home terload
                // Get app version untuk ProfileViewModel (sebagai fallback jika API gagal)
                val fallbackVersion = remember {
                    try {
                        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                        packageInfo.versionName ?: "1.0.0"
                    } catch (e: PackageManager.NameNotFoundException) {
                        "1.0.0"
                    }
                }
                
                // Buat ProfileViewModel untuk preload (dibuat sekali dan di-reuse)
                val profileViewModel: ProfileViewModel = viewModel(
                    factory = ProfileViewModelFactory(
                        userRepository = appContainer.userRepository,
                        token = token,
                        fallbackVersion = fallbackVersion,
                        userPreferences = appContainer.userPreferences
                    )
                )

                // Buat MitraViewModel untuk halaman mitra
                val mitraViewModel: MitraViewModel = viewModel(
                    factory = MitraViewModelFactory(
                        userRepository = appContainer.userRepository
                    )
                )

                // Set callback untuk refresh home setelah transaksi berhasil
                LaunchedEffect(Unit) {
                    (application as XetorApplication).setHomeRefreshCallback {
                        homeViewModel.forceSilentRefresh()
                    }
                    
                    // Set callback untuk refresh hanya profile photo di Home setelah upload foto
                    (application as XetorApplication).setHomeProfilePhotoRefreshCallback {
                        homeViewModel.refreshProfilePhotoOnly()
                    }
                    
                    // Set callback untuk refresh profile setelah update profil
                    (application as XetorApplication).setProfileRefreshCallback {
                        profileViewModel.loadProfileData(showLoading = false)
                        // Force reload photo untuk memastikan foto terbaru di-load setelah upload
                        // Ini penting karena cachedProfilePhotoUrl mungkin masih menyimpan URL lama
                        profileViewModel.loadProfilePhoto(forceReload = true)
                    }
                    
                    // Set callback untuk refresh profile statistics setelah transaksi berhasil
                    (application as XetorApplication).setProfileStatisticsRefreshCallback {
                        profileViewModel.loadStatistics()
                    }
                }

                // Preload profile data setelah home terload (tanpa loading skeleton)
                LaunchedEffect(homeUiState.isLoading) {
                    if (!homeUiState.isLoading && homeUiState.wallet != null) {
                        // Home sudah terload, preload profile data di background
                        // preloadProfileData() sudah memanggil loadProfilePhoto() jika diperlukan
                        profileViewModel.preloadProfileData()
                    }
                }

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
                            photoRefreshKey = photoRefreshKey,
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
                                    startActivity(Intent(this@HomeActivity, NotificationActivity::class.java))
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
                                    startActivity(Intent(this@HomeActivity, ConversionActivity::class.java))
                                },
                                onXpayClick = {
                                    Toast.makeText(context, "Xpay (Coming Soon)", Toast.LENGTH_SHORT).show()
                                },
                                onSetorClick = {
                                    startActivity(Intent(this@HomeActivity, SetorActivity::class.java))
                                },
                                scrollToTopKey = scrollToTopKey
                            )
                            "map" -> {
                                // MitraScreen ditampilkan langsung seperti ShopScreen
                                MitraScreen(
                                    viewModel = mitraViewModel,
                                    onBackClick = {
                                        // Tidak perlu back karena sudah di HomeActivity
                                    }
                                )
                            }
                            "marketplace" -> ShopScreen()
                            "profile" -> {
                                // ProfileViewModel sudah dibuat dan di-preload di atas
                                // Jika data belum ada (misalnya preload belum selesai atau gagal), load dengan skeleton
                                val profileUiState by profileViewModel.uiState.collectAsState()
                                LaunchedEffect(Unit) {
                                    if (profileUiState.userProfile == null && !profileUiState.isLoading) {
                                        // Data belum ada, load dengan skeleton
                                        profileViewModel.loadProfileData(showLoading = true)
                                    }
                                }
                                
                                ProfileScreen(
                                    viewModel = profileViewModel,
                                    onNotificationClick = {
                                        startActivity(Intent(this@HomeActivity, NotificationActivity::class.java))
                                    },
                                    onProfilSayaClick = {
                                        startActivity(Intent(this@HomeActivity, ProfilSayaActivity::class.java))
                                    },
                                    onKataSandiClick = {
                                        startActivity(Intent(this@HomeActivity, PasswordActivity::class.java))
                                    },
                                    onAlamatSayaClick = {
                                        startActivity(Intent(this@HomeActivity, AddressActivity::class.java))
                                    },
                                    onRiwayatPesananClick = {
                                        startActivity(Intent(this@HomeActivity, OrderActivity::class.java))
                                    },
                                    onRiwayatTransaksiClick = {
                                        startActivity(Intent(this@HomeActivity, TransactionHistoryActivity::class.java))
                                    },
                                    onSyaratKetentuanClick = {
                                        startActivity(Intent(this@HomeActivity, TermsAndConditionsActivity::class.java))
                                    },
                                    onKebijakanPrivasiClick = {
                                        startActivity(Intent(this@HomeActivity, PrivacyPolicyActivity::class.java))
                                    },
                                    onVersiClick = {
                                        startActivity(Intent(this@HomeActivity, VersionActivity::class.java))
                                    },
                                    onFotoProfilClick = {
                                        startActivity(Intent(this@HomeActivity, ProfilSayaActivity::class.java))
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clear callback untuk menghindari memory leak
        (application as XetorApplication).setHomeRefreshCallback(null)
    }
}