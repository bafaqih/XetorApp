// app/src/main/java/id/xetor/app/ui/profile/ProfileScreen.kt
package id.xetor.app.ui.profile

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import android.widget.Toast
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import id.xetor.app.R
import id.xetor.app.WelcomeActivity
import id.xetor.app.data.remote.ApiConfig
import id.xetor.app.ui.components.SkeletonBox
import id.xetor.app.ui.components.SkeletonCircle
import id.xetor.app.ui.components.SkeletonText
import id.xetor.app.ui.components.CustomSnackbar
import id.xetor.app.ui.theme.GreenPrimary
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNotificationClick: () -> Unit = {},
    onProfilSayaClick: () -> Unit = {},
    onKataSandiClick: () -> Unit = {},
    onAlamatSayaClick: () -> Unit = {},
    onRiwayatPesananClick: () -> Unit = {},
    onRiwayatTransaksiClick: () -> Unit = {},
    onSyaratKetentuanClick: () -> Unit = {},
    onKebijakanPrivasiClick: () -> Unit = {},
    onVersiClick: () -> Unit = {}, // Navigate to version page
    onFotoProfilClick: () -> Unit = {} // Navigate to profile photo update page
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    
    // Load statistics and version when screen loads (jika belum di-preload)
    LaunchedEffect(Unit) {
        if (uiState.userProfile == null) {
            // Jika data belum ada, load statistics dan version juga
            viewModel.loadStatistics()
            viewModel.loadVersion()
        } else if (uiState.isLoadingVersion) {
            // Jika profile sudah ada tapi version masih loading, load version
            viewModel.loadVersion()
        }
    }
    
    // State untuk dialog konfirmasi
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showDeleteAccountPasswordDialog by remember { mutableStateOf(false) }
    var showDeleteAccountFinalDialog by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Jika data belum ada, tampilkan skeleton penuh
        // Jika data sudah ada tapi masih loading foto/statistik/versi, tampilkan data dengan skeleton terpisah
        if (uiState.isLoading && uiState.userProfile == null) {
            // Loading state - Skeleton penuh
            ProfileSkeletonContent(
                scrollState = scrollState,
                onNotificationClick = onNotificationClick
            )
        } else {
            // Success state
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState, enabled = true)
            ) {
                // Header dengan background hijau (sama persis dengan home)
                // Di home: padding vertical 24.dp + logo Column (28.dp + 2.dp + ~13.dp = ~43.dp) = total tinggi content ~67.dp
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                        .background(GreenPrimary)
                        .padding(bottom = 60.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 24.dp)
                    ) {
                        // Row untuk settings, spacer, dan notif
                        // Button setting dan notif harus simetris
                        // Posisi yang benar adalah button notif, jadi button setting disesuaikan
                        // Gunakan Box wrapper: placeholder Row di background untuk natural height, button setting di-overlay
                        Box(modifier = Modifier.fillMaxWidth()) {
                            // Row dengan placeholder untuk mempertahankan natural height (sama seperti home)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .zIndex(0f),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Placeholder Column dengan struktur SAMA PERSIS seperti logo Column di home
                                // Button setting akan di-overlay di posisi ini
                                Column {
                                    Box(modifier = Modifier.height(28.dp))
                                    Text(
                                        text = " ",
                                        color = Color.Transparent,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.weight(1f))
                                
                                // Bell icon (kanan) - langsung di Row seperti di home, posisi yang benar
                                IconButton(
                                    onClick = onNotificationClick,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_bell),
                                        contentDescription = "Notifications",
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            
                            // Overlay Settings button - gunakan Row yang sama seperti background dengan struktur identik
                            // Button setting langsung di Row seperti button notif untuk alignment yang sama
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .zIndex(1f),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Settings button (kiri) - langsung di Row seperti button notif untuk alignment yang sama
                                // Tambahkan offset untuk menurunkan posisi button setting
                                IconButton(
                                    onClick = {
                                        Toast.makeText(context, "Pengaturan belum tersedia", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .offset(y = 7 .dp) // Offset untuk menurunkan button setting
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Settings",
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.weight(1f))
                                
                                // Placeholder invisible untuk match struktur background Row (button notif ada di background)
                                Box(modifier = Modifier.size(40.dp))
                            }
                        }
                    }
                }

                // Card 1 (atas) - Profile card dengan foto floating, nama, email, dan statistik
                // Posisi sama persis seperti card saldo di home
                // Foto diletakkan di luar Card agar tidak terpotong
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-60).dp)
                        .padding(horizontal = 20.dp)
                ) {
                    // Profile photo (di luar Card agar tidak terpotong)
                    val photoUrl = uiState.userProfile?.photo
                    val photoRefreshKey by viewModel.photoRefreshKeyFlow.collectAsState()
                    
                    // Profile photo floating di atas Card
                    // Foto dinaikkan agar 50:50 di card dan di luar (55.dp di atas, 55.dp di bawah)
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = (-55).dp) // Negative offset untuk 50:50 split (55.dp di atas card)
                            .size(110.dp) // Ukuran box foto
                            .zIndex(1f) // Pastikan foto di atas card
                            .clip(CircleShape)
                            .clickable(onClick = onFotoProfilClick)
                    ) {
                        if (uiState.isLoadingProfilePhoto) {
                            SkeletonCircle(size = 110.dp)
                        } else if (photoUrl != null) {
                            ProfilePhoto(
                                photoUrl = photoUrl,
                                refreshKey = photoRefreshKey,
                                modifier = Modifier.size(110.dp)
                            )
                        } else {
                            // Placeholder jika tidak ada foto
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(Color.Gray.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile Photo",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(55.dp)
                                )
                            }
                        }
                    }
                    
                    // Card dengan konten (nama, email, statistik)
                    // Card dimulai pada posisi yang sama dengan card saldo di home (tanpa padding top)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 55.dp) // Padding top untuk space foto floating (50:50 split)
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Spacer untuk space foto - dikurangi agar jarak foto ke nama lebih dekat
                            Spacer(modifier = Modifier.height(2.dp)) // Dikurangi dari 55.dp ke 40.dp
                            
                            // Nama lengkap (bold, besar)
                            Text(
                                text = uiState.userProfile?.fullname ?: "User",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            
                            Spacer(modifier = Modifier.height(1.dp)) // Jarak nama dan email didekatkan lagi
                            
                            // Email (kecil, warna hijau)
                            Text(
                                text = uiState.userProfile?.email ?: "",
                                fontSize = 14.sp,
                                color = GreenPrimary
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Statistik: Setoran, Transaksi, Pesanan
                            // Transaksi (tengah) wajib tetap di tengah, tidak terpengaruh posisi statistik kanan kiri
                            // Statistik kanan kiri tidak terlalu ke pinggir
                            // Gunakan Box untuk memastikan Transaksi benar-benar di tengah
                            Box(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Transaksi (tengah) - di tengah dengan align center
                                Column(
                                    modifier = Modifier.align(Alignment.Center),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Transaksi",
                                        fontSize = 14.sp,
                                        color = Color.Gray
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    if (uiState.isLoadingStatistics) {
                                        SkeletonText(
                                            modifier = Modifier
                                                .width(40.dp)
                                                .height(22.dp)
                                        )
                                    } else {
                                        Text(
                                            text = "${uiState.totalTransactions}",
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.Black
                                        )
                                    }
                                }
                                
                                // Row untuk Setoran dan Pesanan di kiri dan kanan
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Setoran (kiri)
                                    Column(
                                        modifier = Modifier.padding(start = 32.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "Setoran",
                                            fontSize = 14.sp,
                                            color = Color.Gray
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        if (uiState.isLoadingStatistics) {
                                            SkeletonText(
                                                modifier = Modifier
                                                    .width(40.dp)
                                                    .height(22.dp)
                                            )
                                        } else {
                                            Text(
                                                text = "${uiState.totalDeposit}",
                                                fontSize = 22.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color.Black
                                            )
                                        }
                                    }
                                    
                                    // Pesanan (kanan)
                                    Column(
                                        modifier = Modifier.padding(end = 32.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "Pesanan",
                                            fontSize = 14.sp,
                                            color = Color.Gray
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "0",
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.Black
                                        )
                                    }
                                }
                            }
                            
                            // Spacer untuk padding bawah statistik - ubah nilai height untuk mengatur space bawah
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }

                // Card 2 (bawah) - Menu profil yang sudah ada
                // Jarak sama seperti antar card di home (offset untuk overlap card sudah di-compensate)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-40).dp) // Offset untuk overlap dengan card 1 (sama seperti home)
                        .padding(horizontal = 20.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            // Section: Akun Anda
                            ProfileSection(
                                title = "Akun Anda",
                                items = listOf(
                                    ProfileMenuItem(
                                        icon = Icons.Default.Person,
                                        title = "Profil Saya",
                                        onClick = onProfilSayaClick,
                                        showArrow = false
                                    ),
                                    ProfileMenuItem(
                                        icon = Icons.Default.Lock,
                                        title = "Kata Sandi",
                                        onClick = onKataSandiClick,
                                        showArrow = false
                                    ),
                                    ProfileMenuItem(
                                        icon = Icons.Default.LocationOn,
                                        title = "Alamat Saya",
                                        onClick = onAlamatSayaClick,
                                        showArrow = false
                                    ),
                                    ProfileMenuItem(
                                        icon = Icons.Default.ShoppingBag,
                                        title = "Pesanan Saya",
                                        onClick = onRiwayatPesananClick,
                                        showArrow = false
                                    ),
                                    ProfileMenuItem(
                                        icon = Icons.Default.Receipt,
                                        title = "Riwayat Transaksi",
                                        onClick = onRiwayatTransaksiClick,
                                        showArrow = false
                                    )
                                )
                            )

                            Spacer(modifier = Modifier.height(18.dp)) // Adjusted spacing

                            // Section: Tentang Xetor
                            ProfileSection(
                                title = "Tentang Xetor",
                                items = listOf(
                                    ProfileMenuItem(
                                        icon = Icons.Default.Description,
                                        title = "Syarat & Ketentuan",
                                        onClick = onSyaratKetentuanClick,
                                        showArrow = false
                                    ),
                                    ProfileMenuItem(
                                        icon = Icons.Default.Security,
                                        title = "Kebijakan Privasi",
                                        onClick = onKebijakanPrivasiClick,
                                        showArrow = false
                                    ),
                                    ProfileMenuItem(
                                        icon = Icons.Default.Info,
                                        title = "Versi",
                                        value = if (uiState.isLoadingVersion) "..." else uiState.appVersion,
                                        onClick = onVersiClick,
                                        showArrow = false // Versi tidak perlu arrow, tapi tetap tampilkan nomor versi
                                    )
                                )
                            )

                            Spacer(modifier = Modifier.height(18.dp)) // Adjusted spacing

                            // Section: Lainnya
                            ProfileSection(
                                title = "Lainnya",
                                items = listOf(
                                    ProfileMenuItem(
                                        icon = Icons.Default.Logout,
                                        title = "Keluar",
                                        onClick = { showLogoutDialog = true },
                                        isDestructive = true,
                                        showArrow = false
                                    ),
                                    ProfileMenuItem(
                                        icon = Icons.Default.Delete,
                                        title = "Hapus Akun",
                                        onClick = { showDeleteAccountDialog = true },
                                        isDestructive = true,
                                        showArrow = false
                                    )
                                )
                            )
                        }
                    }
                }

                // Content area (dengan offset untuk menyesuaikan overlap card - sama seperti home)
                // Spacing minimal, jika konten muat tidak perlu scroll, jika tidak muat bisa scroll
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Snackbar untuk error message
        if (uiState.errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                CustomSnackbar(
                    message = uiState.errorMessage ?: "",
                    onDismiss = {
                        viewModel.clearError()
                        viewModel.refresh()
                    },
                    buttonText = "Coba Lagi"
                )
            }
        }

        // Dialog konfirmasi logout
        if (showLogoutDialog) {
            LogoutConfirmationDialog(
            onConfirm = {
                showLogoutDialog = false
                isProcessing = true
                scope.launch {
                    viewModel.logout()
                    isProcessing = false
                    // Redirect ke WelcomeActivity
                    val intent = Intent(context, WelcomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    context.startActivity(intent)
                }
            },
            onDismiss = { showLogoutDialog = false },
            isProcessing = isProcessing
        )
    }

    // Dialog konfirmasi hapus akun (modal pertama)
    if (showDeleteAccountDialog) {
        DeleteAccountConfirmationDialog(
            onConfirm = {
                showDeleteAccountDialog = false
                showDeleteAccountPasswordDialog = true
            },
            onDismiss = { showDeleteAccountDialog = false },
            isProcessing = false
            )
        }

        // Dialog konfirmasi password hapus akun (modal kedua)
        if (showDeleteAccountPasswordDialog) {
        var passwordError by remember { mutableStateOf<String?>(null) }
        
        DeleteAccountPasswordDialog(
            userEmail = uiState.userProfile?.email ?: "",
            passwordError = passwordError,
            onBack = {
                showDeleteAccountPasswordDialog = false
                showDeleteAccountDialog = true
                passwordError = null
            },
            onConfirm = { password ->
                isProcessing = true
                passwordError = null
                scope.launch {
                    // Verify password dulu
                    val verifyResult = viewModel.verifyPassword(
                        uiState.userProfile?.email ?: "",
                        password
                    )
                    
                    if (verifyResult.isSuccess) {
                        // Jika password benar, buka modal final confirmation
                        isProcessing = false
                        showDeleteAccountPasswordDialog = false
                        showDeleteAccountFinalDialog = true
                    } else {
                        isProcessing = false
                        passwordError = "Password anda salah"
                    }
                }
            },
            onDismiss = { 
                showDeleteAccountPasswordDialog = false
                passwordError = null
            },
            isProcessing = isProcessing
            )
        }

        // Dialog final confirmation hapus akun (modal ketiga)
        if (showDeleteAccountFinalDialog) {
        DeleteAccountFinalConfirmationDialog(
            onDeletePermanent = {
                isProcessing = true
                scope.launch {
                    val deleteResult = viewModel.deleteAccount()
                    isProcessing = false
                    if (deleteResult.isSuccess) {
                        // Redirect ke WelcomeActivity
                        val intent = Intent(context, WelcomeActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        context.startActivity(intent)
                    } else {
                        // Show error
                        // Error akan ditangani oleh snackbar jika ada
                    }
                }
            },
            onCancel = {
                showDeleteAccountFinalDialog = false
            },
            isProcessing = isProcessing
            )
        }
    }
}

@Composable
fun ProfileSkeletonContent(
    scrollState: androidx.compose.foundation.ScrollState,
    onNotificationClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // Header skeleton (sama persis dengan home - settings, spacer, notif)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                .background(GreenPrimary)
                .padding(bottom = 60.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                // Row untuk settings, spacer, dan notif
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Settings button skeleton
                    SkeletonBox(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    // Bell icon (tetap tampil - posisi sama seperti home, center vertically)
                    IconButton(
                        onClick = onNotificationClick,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_bell),
                            contentDescription = "Notifications",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        // Card 1 skeleton (Profile card dengan foto floating)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-60).dp)
                .padding(horizontal = 20.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .padding(top = 30.dp), // Space for floating photo
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile photo skeleton (floating)
                    SkeletonCircle(size = 72.dp)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Nama skeleton
                    SkeletonText(modifier = Modifier.width(150.dp).height(20.dp))
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Email skeleton
                    SkeletonText(modifier = Modifier.width(200.dp).height(14.dp))
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Statistik skeleton
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        repeat(3) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                SkeletonText(modifier = Modifier.width(60.dp).height(12.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                SkeletonText(modifier = Modifier.width(40.dp).height(16.dp))
                            }
                        }
                    }
                }
            }
        }

        // Card 2 skeleton (Menu profil)
        Spacer(modifier = Modifier.height(8.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Section skeleton
                    repeat(3) {
                        SkeletonText(modifier = Modifier.width(100.dp).height(16.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        repeat(3) {
                            SkeletonBox(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(8.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        Spacer(modifier = Modifier.height(18.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun ProfilePhoto(
    photoUrl: String?,
    refreshKey: Int = 0,
    modifier: Modifier = Modifier
) {
    if (photoUrl != null && photoUrl.isNotEmpty()) {
        val fullUrl = if (photoUrl.startsWith("http")) {
            photoUrl
        } else {
            "${ApiConfig.BASE_URL}$photoUrl"
        }
        
        // Smart cache busting: only add timestamp when refreshKey changes (new upload)
        // Remember timestamp based on refreshKey, so same refreshKey uses same URL (for Coil memory cache)
        val urlWithCacheBust = remember(photoUrl, refreshKey) {
            if (refreshKey > 0) {
                // Use refreshKey as part of URL to ensure consistency
                // Same refreshKey = same URL = Coil memory cache works
                "$fullUrl?refresh=$refreshKey"
            } else {
                // Use normal URL when no refresh needed
                fullUrl
            }
        }
        
        SubcomposeAsyncImage(
            model = urlWithCacheBust,
            contentDescription = "Profile Photo",
            modifier = modifier.clip(CircleShape),
            contentScale = ContentScale.Crop
        ) {
            val state = painter.state
            if (state is AsyncImagePainter.State.Loading || state is AsyncImagePainter.State.Error) {
                // Use modifier directly for SkeletonCircle
                SkeletonCircle(modifier = modifier)
            } else {
                SubcomposeAsyncImageContent()
            }
        }
    } else {
        // Placeholder jika tidak ada foto
        Box(
            modifier = modifier
                .clip(CircleShape)
                .background(Color.Gray.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile Photo",
                tint = Color.Gray,
                modifier = Modifier.size((modifier as? androidx.compose.ui.unit.DpSize)?.width?.times(0.6f) ?: 24.dp)
            )
        }
    }
}

@Composable
fun ProfileSection(
    title: String,
    items: List<ProfileMenuItem>
) {
    Column {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = Color.Gray, // Abu tua bukan hitam
            modifier = Modifier.padding(bottom = 6.dp)
        )
        
        items.forEach { item ->
            ProfileMenuItem(item = item)
            // Tidak ada gap antar menu item
        }
    }
}

data class ProfileMenuItem(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val title: String,
    val onClick: () -> Unit = {},
    val value: String? = null,
    val showArrow: Boolean = true,
    val isDestructive: Boolean = false
)

@Composable
fun ProfileMenuItem(
    item: ProfileMenuItem
) {
    val density = LocalDensity.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .layout { measurable, constraints ->
                // Perluas constraints untuk area klik (tambah 16.dp di kiri dan kanan untuk mengisi sampai batas card)
                val extraWidth = with(density) { 32.dp.roundToPx() } // 16.dp kiri + 16.dp kanan
                val expandedConstraints = constraints.copy(
                    minWidth = constraints.minWidth + extraWidth,
                    maxWidth = constraints.maxWidth + extraWidth
                )
                val placeable = measurable.measure(expandedConstraints)
                layout(placeable.width, placeable.height) {
                    // Place konten di posisi semula (tidak di-offset, karena Row sudah punya padding)
                    placeable.placeRelative(x = 0, y = 0)
                }
            }
            .clickable(onClick = item.onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = if (item.isDestructive) Color.Red else GreenPrimary,
                modifier = Modifier.size(24.dp)
            )
            
            Text(
                text = item.title,
                fontSize = 16.sp,
                color = if (item.isDestructive) Color.Red else Color.Black,
                modifier = Modifier.weight(1f)
            )
            
            if (item.value != null) {
                Text(
                    text = item.value,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            
            if (item.showArrow) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun LogoutConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isProcessing: Boolean = false
) {
    Dialog(onDismissRequest = { if (!isProcessing) onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Logout Icon (warna merah, tanpa circle)
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = "Logout",
                    tint = Color(0xFFFF5252), // Merah
                    modifier = Modifier.size(80.dp)
                )

                // Message (text hitam, tidak bold)
                Text(
                    text = "Anda yakin ingin keluar?",
                    fontSize = 16.sp,
                    color = Color.Black,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Button Batal
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isProcessing
                    ) {
                        Text(
                            text = "Batal",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    // Button Ya (outline hijau dengan teks hijau)
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GreenPrimary),
                        enabled = !isProcessing
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = GreenPrimary
                            )
                        } else {
                            Text(
                                text = "Ya",
                                color = GreenPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeleteAccountConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isProcessing: Boolean = false
) {
    Dialog(onDismissRequest = { if (!isProcessing) onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Delete Icon (warna merah, tanpa circle)
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Account",
                    tint = Color(0xFFFF5252), // Merah
                    modifier = Modifier.size(80.dp)
                )

                // Message (text hitam, tidak bold)
                Text(
                    text = "Anda yakin ingin menghapus akun ini?",
                    fontSize = 16.sp,
                    color = Color.Black,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Button Batal
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isProcessing
                    ) {
                        Text(
                            text = "Batal",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    // Button Ya (outline hijau dengan teks hijau)
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GreenPrimary),
                        enabled = !isProcessing
                    ) {
                        Text(
                            text = "Ya",
                            color = GreenPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DeleteAccountPasswordDialog(
    userEmail: String,
    passwordError: String?,
    onBack: () -> Unit,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    isProcessing: Boolean = false
) {
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = { if (!isProcessing) onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Icon panah kembali dengan teks "Kembali" di pojok kiri atas (tanpa circle, tanpa efek klik, seperti di withdraw)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_back),
                        contentDescription = "Back",
                        tint = Color.Black,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable(onClick = onBack, enabled = !isProcessing, indication = null, interactionSource = remember { MutableInteractionSource() })
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Kembali",
                        fontSize = 16.sp,
                        color = Color.Black,
                        modifier = Modifier.clickable(onClick = onBack, enabled = !isProcessing, indication = null, interactionSource = remember { MutableInteractionSource() })
                    )
                }

                // Message (text hitam, dipersingkat)
                Text(
                    text = "Masukkan password anda",
                    fontSize = 16.sp,
                    color = Color.Black,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                // Password Field
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Password") },
                    placeholder = { Text("Masukkan password") },
                    visualTransformation = if (isPasswordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (isPasswordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    isError = passwordError != null,
                    supportingText = if (passwordError != null) {
                        { Text(text = passwordError ?: "") }
                    } else null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (passwordError != null) Color(0xFFFF5252) else GreenPrimary,
                        unfocusedBorderColor = if (passwordError != null) Color(0xFFFF5252) else Color.LightGray,
                        errorBorderColor = Color(0xFFFF5252),
                        errorSupportingTextColor = Color(0xFFFF5252)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isProcessing
                )

                // Button Konfirmasi (full hijau dengan teks putih)
                Button(
                    onClick = {
                        if (password.isEmpty()) {
                            // Error akan ditangani di parent
                        } else {
                            onConfirm(password)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isProcessing && password.isNotEmpty()
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
                        Text(
                            text = "Konfirmasi",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DeleteAccountFinalConfirmationDialog(
    onDeletePermanent: () -> Unit,
    onCancel: () -> Unit,
    isProcessing: Boolean = false
) {
    Dialog(onDismissRequest = { if (!isProcessing) onCancel() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title (font lebih besar dan bold hitam)
                Text(
                    text = "Hapus Akun",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp)) // Didekatkan sekali

                // Message (text hitam)
                Text(
                    text = "Tindakan ini tidak dapat dibatalkan, anda yakin ingin menghapus akun ini secara permanen?",
                    fontSize = 16.sp,
                    color = Color.Black,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp)) // Spacing untuk button

                // Buttons (dempetkan lagi, spacing dikurangi)
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp) // Dikurangi dari 16.dp menjadi 8.dp
                ) {
                    // Button Hapus Permanen (fill hijau dengan teks putih bold)
                    Button(
                        onClick = onDeletePermanent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary), // Hijau
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isProcessing
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Text(
                                text = "Hapus Permanen",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Button Batalkan (outline hijau dengan teks hijau, no fill)
                    Button(
                        onClick = onCancel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GreenPrimary),
                        enabled = !isProcessing
                    ) {
                        Text(
                            text = "Batalkan",
                            color = GreenPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
