// app/src/main/java/id/xetor/app/ui/home/HomeScreen.kt
package id.xetor.app.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
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
    onSetorClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Auto refresh saat screen kembali (onResume)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            // Loading state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = GreenPrimary)
            }
        } else if (uiState.errorMessage != null) {
            // Error state - User-friendly message
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Gagal memuat data",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Mohon coba lagi dalam beberapa saat",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { viewModel.refresh() },
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                    ) {
                        Text("Coba Lagi")
                    }
                }
            }
        } else {
            // Success state
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header dengan background hijau
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
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
                                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                                ) {
                                    Text(
                                        text = "Setor",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Banner Carousel dengan HorizontalPager (Infinite Loop)
                    Column {
                        val bannerCount = 3
                        val initialPage = 999 // Start di tengah untuk fake infinite (999 % 3 = 0 -> Banner 1)
                        val pagerState = rememberPagerState(
                            initialPage = initialPage,
                            pageCount = { 10000 } // Fake infinite scroll
                        )
                        
                        // Banner Pager mentok tepi dengan preview samping
                        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                            val screenWidth = maxWidth
                            val bannerWidth = screenWidth - 32.dp // Sedikit lebih kecil dari full width
                            val peekWidth = 16.dp // Lebar banner samping yang keliatan
                            
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(horizontal = peekWidth),
                                pageSpacing = 8.dp
                            ) { page ->
                                val actualPage = page % bannerCount // Loop banner
                                val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                                
                                Box(
                                    modifier = Modifier
                                        .width(bannerWidth)
                                        .height(160.dp)
                                        .graphicsLayer {
                                            // Alpha effect: banner aktif lebih terang, banner samping semi transparent
                                            alpha = lerp(
                                                start = 0.5f,
                                                stop = 1f,
                                                fraction = 1f - pageOffset.absoluteValue.coerceIn(0f, 1f)
                                            )
                                        }
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.LightGray),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Banner ${actualPage + 1}",
                                        color = Color.Gray,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Dot Indicator (update sesuai actual page)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val currentActualPage = pagerState.currentPage % bannerCount
                            repeat(bannerCount) { index ->
                                Box(
                                    modifier = Modifier
                                        .size(if (index == currentActualPage) 8.dp else 6.dp)
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

                    Spacer(modifier = Modifier.height(20.dp))

                    // Cards Statistik (2x2 Grid)
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

                    Spacer(modifier = Modifier.height(20.dp)) // Space untuk bottom nav
                }
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
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(GreenPrimary, RoundedCornerShape(12.dp)),
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
    unit: String
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
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
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
                Text(
                    text = value,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
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

// Helper function untuk format number dengan separator
private fun formatNumber(value: Int): String {
    return String.format("%,d", value).replace(',', '.')
}

