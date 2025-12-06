// app/src/main/java/id/xetor/app/ui/home/HomeScreen.kt
package id.xetor.app.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.animation.core.tween
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import id.xetor.app.R
import id.xetor.app.data.remote.ApiConfig
import id.xetor.app.ui.components.*
import id.xetor.app.ui.components.SkeletonText
import id.xetor.app.ui.components.CustomSnackbar
import id.xetor.app.ui.theme.GreenPrimary
import kotlin.math.absoluteValue

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNotificationClick: () -> Unit = {},
    onWithdrawClick: () -> Unit = {},
    onTopUpClick: () -> Unit = {},
    onTransferClick: () -> Unit = {},
    onConvertClick: () -> Unit = {},
    onXpayClick: () -> Unit = {},
    onSetorClick: () -> Unit = {},
    scrollToTopKey: Int = 0
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    
    // Handle scroll to top dari parent - trigger ketika scrollToTopKey berubah
    LaunchedEffect(scrollToTopKey) {
        if (scrollToTopKey > 0) {
            // Scroll to top dengan smooth animation
            scrollState.animateScrollTo(0)
        }
    }
    
    // Smart refresh saat screen kembali (onResume)
    // Menggunakan silent refresh untuk menghindari loading skeleton setiap kali kembali
    // Hanya refresh jika data sudah cukup lama (30 detik)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // Gunakan silent refresh untuk refresh di background tanpa loading skeleton
                // Data lama tetap tampil sambil refresh di background
                viewModel.silentRefresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            // Loading state - Semua skeleton kecuali logo Xetor, teks Lets, dan button notif
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                // Header - hanya logo, teks Lets, dan button notif yang tampil
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
                        // Row untuk logo dan notif (tetap tampil)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Logo Xetor (tetap tampil)
                            Column {
                                Image(
                                    painter = painterResource(id = R.drawable.text_xetor_putih),
                                    contentDescription = "Xetor Logo",
                                    modifier = Modifier.height(28.dp),
                                    contentScale = ContentScale.Fit
                                )
                                Text(
                                    text = "Let's contribution to our earth",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }

                            // Bell icon (tetap tampil)
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

                // Saldo card - Card tetap ada, semua isinya skeleton
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
                            modifier = Modifier.padding(16.dp)
                        ) {
                            // Saldo & Xpoin - semua skeleton
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    SkeletonText(modifier = Modifier.width(80.dp).height(12.dp))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    SkeletonText(modifier = Modifier.width(120.dp).height(20.dp))
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    SkeletonText(modifier = Modifier.width(60.dp).height(12.dp))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    SkeletonText(modifier = Modifier.width(80.dp).height(20.dp))
                                }
                            }
                            // Button shortcuts - semua skeleton
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                repeat(5) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        SkeletonBox(
                                            modifier = Modifier.size(48.dp),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        SkeletonText(modifier = Modifier.width(50.dp).height(11.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                // Content area - Card tetap ada, semua isinya skeleton
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-40).dp)
                        .padding(horizontal = 20.dp)
                ) {
                    Spacer(modifier = Modifier.height(20.dp))
                    // Greeting Card - Card tetap ada, semua isinya skeleton
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            // Greeting text - skeleton
                            SkeletonText(modifier = Modifier.width(150.dp).height(18.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            // Subtitle - skeleton
                            SkeletonText(modifier = Modifier.width(200.dp).height(13.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    // Label - skeleton
                                    SkeletonText(modifier = Modifier.width(100.dp).height(12.dp))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    // Nilai - skeleton
                                    SkeletonText(modifier = Modifier.width(80.dp).height(28.dp))
                                }
                                // Button Setor - skeleton
                                SkeletonBox(
                                    modifier = Modifier
                                        .width(100.dp)
                                        .height(44.dp),
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    // Banner - skeleton
                    HomeBannerSkeleton()
                    Spacer(modifier = Modifier.height(20.dp))
                    // Statistic Cards - Card tetap ada, semua isinya skeleton
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        repeat(2) {
                            Card(
                                modifier = Modifier.weight(1f).height(120.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        // Label - skeleton
                                        SkeletonText(modifier = Modifier.width(80.dp).height(11.dp))
                                        // Icon - skeleton
                                        SkeletonBox(
                                            modifier = Modifier.size(28.dp),
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                    }
                                    Column {
                                        // Nilai - skeleton
                                        SkeletonText(modifier = Modifier.width(60.dp).height(22.dp))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        // Unit - skeleton
                                        SkeletonText(modifier = Modifier.width(40.dp).height(11.dp))
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        repeat(2) {
                            Card(
                                modifier = Modifier.weight(1f).height(120.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        // Label - skeleton
                                        SkeletonText(modifier = Modifier.width(80.dp).height(11.dp))
                                        // Icon - skeleton
                                        SkeletonBox(
                                            modifier = Modifier.size(28.dp),
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                    }
                                    Column {
                                        // Nilai - skeleton
                                        SkeletonText(modifier = Modifier.width(60.dp).height(22.dp))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        // Unit - skeleton
                                        SkeletonText(modifier = Modifier.width(40.dp).height(11.dp))
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        } else {
            // Success state
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                // Header dengan background hijau
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
                        // Row untuk logo dan notif
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Logo Xetor
                            Column {
                                Image(
                                    painter = painterResource(id = R.drawable.text_xetor_putih),
                                    contentDescription = "Xetor Logo",
                                    modifier = Modifier.height(28.dp),
                                    contentScale = ContentScale.Fit
                                )
                                Text(
                                    text = "Let's contribution to our earth",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }

                            // Bell icon (no background, only ripple effect)
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

                // Card Saldo (overlapping dengan header)
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
                            modifier = Modifier.padding(16.dp)
                        ) {
                            // Saldo Anda dan Xpoin
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Saldo Anda
                                Column {
                                    Text(
                                        text = "Saldo Anda",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                    Row(
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        Text(
                                            text = "Rp ",
                                            fontSize = 13.sp,
                                            color = Color.Black,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = formatCurrency(uiState.wallet?.balance ?: "0"),
                                            fontSize = 20.sp,
                                            color = Color.Black,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                // Xpoin
                                Column(
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(
                                        text = "Xpoin",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                    Row(
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        Text(
                                            text = formatNumber(uiState.wallet?.xpoin ?: 0),
                                            fontSize = 20.sp,
                                            color = Color.Black,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = " Xp",
                                            fontSize = 13.sp,
                                            color = Color.Black,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // 5 Shortcut Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                ShortcutButton(
                                    iconRes = R.drawable.ic_withdraw,
                                    label = "Withdraw",
                                    onClick = onWithdrawClick
                                )
                                ShortcutButton(
                                    iconRes = R.drawable.ic_topup,
                                    label = "Top Up",
                                    onClick = onTopUpClick
                                )
                                ShortcutButton(
                                    iconRes = R.drawable.ic_transfer,
                                    label = "Transfer",
                                    onClick = onTransferClick
                                )
                                ShortcutButton(
                                    iconRes = R.drawable.ic_convert,
                                    label = "Convert",
                                    onClick = onConvertClick
                                )
                                ShortcutButton(
                                    iconRes = R.drawable.ic_xpay,
                                    label = "Xpay",
                                    onClick = onXpayClick
                                )
                            }
                        }
                    }
                }

                // Content area (dengan offset untuk menyesuaikan overlap card)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-40).dp)
                        .padding(horizontal = 20.dp)
                ) {
                    // Greeting dan Sampah Terkumpul
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Halo, ${uiState.userProfile?.fullname?.split(" ")?.firstOrNull() ?: "User"}!",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Text(
                                text = "Sudahkah kamu menyetor sampah hari ini?",
                                fontSize = 13.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 4.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Sampah Terkumpul",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                    Row(
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        Text(
                                            text = uiState.statistics?.waste ?: "0,0",
                                            fontSize = 28.sp,
                                            color = Color.Black,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = " Kg",
                                            fontSize = 16.sp,
                                            color = Color.Black,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(bottom = 2.dp)
                                        )
                                    }
                                }

                                // Tombol Setor
                                Button(
                                    onClick = onSetorClick,
                                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 22.dp, vertical = 12.dp)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.icon_xetor_putih),
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "Setor",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
                
                // Banner Carousel di luar Column dengan padding untuk full screen width
                val configuration = LocalConfiguration.current
                val density = LocalDensity.current
                val screenWidthDp = configuration.screenWidthDp.dp
                
                val banners = uiState.banners
                val bannerCount = if (banners.isEmpty()) 1 else banners.size
                val initialPage = if (bannerCount > 0) 999 else 0 // Start di tengah untuk fake infinite
                val pagerState = rememberPagerState(
                    initialPage = initialPage,
                    pageCount = { if (bannerCount > 0) 10000 else 1 } // Fake infinite scroll
                )
                
                // State untuk track apakah user sedang hold banner
                var isHolding by remember { mutableStateOf(false) }
                
                // Auto-slide setiap 3 detik (loop terus menerus, berhenti jika di-hold)
                if (bannerCount > 0 && !uiState.isLoadingBanners) {
                    LaunchedEffect(bannerCount, isHolding) {
                        while (true) {
                            delay(3000) // Delay 3 detik
                            // Hanya auto-slide jika tidak sedang di-hold
                            if (!isHolding) {
                                val currentPage = pagerState.currentPage
                                val nextPage = currentPage + 1
                                // Animasi lebih smooth dengan durasi 800ms
                                pagerState.animateScrollToPage(
                                    page = nextPage,
                                    animationSpec = tween(durationMillis = 800)
                                )
                            }
                        }
                    }
                }
                
                // Banner container full screen width
                // Gunakan layout modifier untuk membuat Box tidak mengambil space di layout flow
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-40).dp) // Offset sama dengan Column content
                        .layout { measurable, constraints ->
                            // Ukur banner dengan constraint yang diberikan
                            val placeable = measurable.measure(constraints)
                            // Layout dengan tinggi 0 agar tidak mengambil space di layout flow
                            layout(placeable.width, 0) {
                                // Place banner di posisi yang diinginkan
                                placeable.placeRelative(0, 0)
                            }
                        }
                ) {
                    // Banner Pager dengan preview samping yang simetris
                    val peekWidth = 24.dp // Lebar banner samping yang keliatan (simetris kiri-kanan)
                    val bannerWidth = screenWidthDp - (peekWidth * 2) // Lebar banner aktif (simetris di tengah)
                    
                    if (uiState.isLoadingBanners) {
                        // Loading state: skeleton untuk banner
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            SkeletonBox(
                                modifier = Modifier
                                    .width(bannerWidth)
                                    .height(160.dp),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    } else if (banners.isEmpty()) {
                        // Empty state
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(bannerWidth)
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.LightGray),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Tidak ada banner",
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    } else {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier
                                .fillMaxWidth() // Gunakan lebar full screen
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onPress = {
                                            // User mulai hold
                                            isHolding = true
                                            tryAwaitRelease()
                                            // User release
                                            isHolding = false
                                        }
                                    )
                                },
                            contentPadding = PaddingValues(horizontal = peekWidth),
                            pageSpacing = 8.dp
                        ) { page ->
                            val actualPage = page % bannerCount // Loop banner
                            val banner = banners[actualPage]
                            val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                            
                            BannerItem(
                                imageUrl = banner.image,
                                modifier = Modifier
                                    .width(bannerWidth)
                                    .height(160.dp)
                                    .graphicsLayer {
                                        // Alpha effect: semua banner 100% opacity (aktif dan inactive)
                                        alpha = lerp(
                                            start = 1f, // Banner inactive: 100% opacity
                                            stop = 1f,  // Banner aktif: 100% opacity
                                            fraction = 1f - pageOffset.absoluteValue.coerceIn(0f, 1f)
                                        )
                                    }
                            )
                        }
                    }
                }
                
                // Spacer untuk mengkompensasi offset banner dan memberikan spacing yang tepat
                // Banner: offset(-40.dp) + height(160.dp) = visual end di 120.dp dari content sebelumnya
                // Tambahkan 4.dp spacing untuk dot indicator
                Spacer(modifier = Modifier.height(132.dp)) // 120.dp (banner end) + 4.dp (spacing)
                
                // Dot Indicator di bawah banner (hanya tampil jika ada banners)
                if (!uiState.isLoadingBanners && banners.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp), // Padding untuk dot indicator
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val currentActualPage = pagerState.currentPage % bannerCount
                            repeat(bannerCount) { index ->
                                Box(
                                    modifier = Modifier
                                        .size(6.dp) // Ukuran sama untuk active dan inactive
                                        .clip(CircleShape)
                                        .background(
                                            if (index == currentActualPage) GreenPrimary 
                                            else Color.LightGray
                                        )
                                )
                                if (index < bannerCount - 1) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                            }
                        }
                    }
                }
                
                // Spacing antara dot indicator dan cards statistik (6.dp tinggi dot + 8.dp spacing)
                Spacer(modifier = Modifier.height(18.dp))
                
                // Cards Statistik (2x2 Grid) - kembali ke Column dengan padding
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatisticCard(
                            modifier = Modifier.weight(1f),
                            iconRes = R.drawable.ic_energy,
                            title = "Energi Dihemat",
                            value = uiState.statistics?.energy ?: "0,0",
                            unit = "kWh"
                        )
                        StatisticCard(
                            modifier = Modifier.weight(1f),
                            iconRes = R.drawable.ic_co2,
                            title = "Pengurangan CO2",
                            value = uiState.statistics?.co2 ?: "0,0",
                            unit = "kg CO2"
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatisticCard(
                            modifier = Modifier.weight(1f),
                            iconRes = R.drawable.ic_water,
                            title = "Air Dihemat",
                            value = uiState.statistics?.water ?: "0,0",
                            unit = "L"
                        )
                        StatisticCard(
                            modifier = Modifier.weight(1f),
                            iconRes = R.drawable.ic_tree,
                            title = "Pohon Terselamatkan",
                            value = "${uiState.statistics?.tree ?: 0}",
                            unit = "pohon"
                        )
                    }

                    Spacer(modifier = Modifier.height(26.dp)) // Spacing untuk bottom navigation bar
                }
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
                        // Saat klik "Coba Lagi", clear error dan refresh
                        viewModel.clearError()
                        viewModel.refresh()
                    },
                    buttonText = "Coba Lagi"
                )
            }
        }
    }
}

@Composable
fun ShortcutButton(
    iconRes: Int,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp)) // Clip ripple effect sesuai rounded shape
                .clickable(onClick = onClick) // Pindahkan clickable ke Box icon saja
                .background(GreenPrimary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color.Black,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun StatisticCard(
    modifier: Modifier = Modifier,
    iconRes: Int,
    title: String,
    value: String,
    unit: String,
    isLoading: Boolean = false
) {
    Card(
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp), // TODO: Untuk mengurangi padding bawah card statistik, ubah nilai ini atau tambahkan paddingBottom
            verticalArrangement = Arrangement.SpaceBetween // TODO: Atau ubah ini menjadi Arrangement.Top dan tambahkan Spacer dengan height yang lebih kecil
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    lineHeight = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = title,
                    tint = GreenPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column {
                if (isLoading) {
                    // Skeleton untuk nilai dinamis
                    SkeletonText(modifier = Modifier.width(60.dp).height(22.dp))
                } else {
                    Text(
                        text = value,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                }
                Text(
                    text = unit,
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

// Helper function untuk format currency (hilangkan .00, tambah separator)
private fun formatCurrency(value: String): String {
    return try {
        val num = value.toDoubleOrNull() ?: 0.0
        val intValue = num.toInt()
        String.format("%,d", intValue).replace(',', '.')
    } catch (e: Exception) {
        "0"
    }
}

@Composable
fun BannerItem(
    imageUrl: String,
    modifier: Modifier = Modifier
) {
    // Log URL untuk debug
    android.util.Log.d("BannerItem", "Loading banner image: $imageUrl")
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.LightGray),
        contentAlignment = Alignment.Center
    ) {
        SubcomposeAsyncImage(
            model = imageUrl,
            contentDescription = "Banner",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        ) {
            when (painter.state) {
                is coil.compose.AsyncImagePainter.State.Loading -> {
                    // Loading state: skeleton untuk gambar banner
                    SkeletonBox(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                is coil.compose.AsyncImagePainter.State.Error -> {
                    // Error state: abu-abu dengan text error
                    val errorState = painter.state as coil.compose.AsyncImagePainter.State.Error
                    android.util.Log.e("BannerItem", "Failed to load image: $imageUrl, error: ${errorState.result.throwable?.message}")
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Gagal memuat gambar",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
                else -> {
                    // Success state: tampilkan gambar
                    android.util.Log.d("BannerItem", "Image loaded successfully: $imageUrl")
                    SubcomposeAsyncImageContent()
                }
            }
        }
    }
}

// Helper function untuk format number dengan separator
private fun formatNumber(value: Int): String {
    return String.format("%,d", value).replace(',', '.')
}
