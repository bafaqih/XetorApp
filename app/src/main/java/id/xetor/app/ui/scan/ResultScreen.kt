// app/src/main/java/id/xetor/app/ui/scan/ResultScreen.kt
package id.xetor.app.ui.scan

import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.ui.res.painterResource
import id.xetor.app.R
import id.xetor.app.SetorActivity
import id.xetor.app.data.remote.WasteDetailResponse
import id.xetor.app.ui.theme.GreenPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    imageBitmap: Bitmap?,
    wasteDetail: WasteDetailResponse?,
    isLoading: Boolean,
    onBackClick: () -> Unit,
    onSetorClick: () -> Unit
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenHeight = with(density) { configuration.screenHeightDp.dp.toPx() }
    
    // Bottom sheet state - mulai dari setengah layar
    var isExpanded by remember { mutableStateOf(false) }
    var rawDragOffset by remember { mutableStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    
    // Scroll state untuk detect posisi scroll
    val scrollState = rememberScrollState()
    // Update canSwipeDown saat scroll state berubah
    var canSwipeDown by remember { mutableStateOf(true) } // Default true (scroll di atas)
    
    LaunchedEffect(scrollState.value) {
        canSwipeDown = scrollState.value == 0
    }
    
    val topBarHeight = with(density) { 80.dp.toPx() } // Height top bar + padding
    val initialOffset = screenHeight * 0.5f // Setengah layar
    val expandedOffset = topBarHeight // Full screen tapi di bawah top bar
    val bottomBarHeight = with(density) { 88.dp.toPx() } // Height bottom bar + padding
    
    // Current offset saat dragging atau setelah snap
    val currentOffset = if (isDragging) {
        val currentBase = if (isExpanded) expandedOffset else initialOffset
        (currentBase + rawDragOffset).coerceIn(expandedOffset, screenHeight)
    } else {
        if (isExpanded) expandedOffset else initialOffset
    }
    
    val animatedOffset by animateFloatAsState(
        targetValue = currentOffset,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        label = "bottomSheetOffset"
    )
    
    // Reset drag offset saat tidak dragging
    LaunchedEffect(isDragging) {
        if (!isDragging) {
            rawDragOffset = 0f
        }
    }
    
    // Nested scroll connection untuk menangani swipe down saat scroll di atas
    val nestedScrollConnection = remember(isExpanded, scrollState.value) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // Hanya intercept jika expanded, scroll di atas, dan swipe down
                if (isExpanded && scrollState.value == 0 && available.y > 0) {
                    isDragging = true
                    val newOffset = rawDragOffset + available.y
                    rawDragOffset = newOffset.coerceAtLeast(0f)
                    // Check threshold untuk auto collapse
                    val threshold = screenHeight * 0.15f
                    if (rawDragOffset > threshold) {
                        isExpanded = false
                        rawDragOffset = 0f
                    }
                    return available // Consume gesture untuk mencegah scroll
                }
                return Offset.Zero // Biarkan scroll handle
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Background Image
        if (imageBitmap != null) {
            Image(
                bitmap = imageBitmap.asImageBitmap(),
                contentDescription = "Scanned Image",
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(0f),
                contentScale = ContentScale.Crop
            )
        }

        // Top Bar - Transparent, hanya button back
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, top = 16.dp, end = 16.dp, bottom = 16.dp) // Padding kiri 4dp seperti TopAppBar
                .zIndex(2f),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(48.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.Transparent
                )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_back),
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Bottom Sheet Content - Selalu muncul di setengah layar
        // Pastikan bottom sheet selalu terlihat dengan menggunakan fillMaxHeight dan offset
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight() // Gunakan fillMaxHeight agar selalu terlihat
                .graphicsLayer {
                    translationY = animatedOffset
                }
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    spotColor = Color.Black.copy(alpha = 0.3f)
                )
                .background(
                    Color.White,
                    RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
                .zIndex(1f)
                .pointerInput(isExpanded, scrollState.value) {
                    detectVerticalDragGestures(
                        onDragStart = {
                            // Hanya mulai drag jika kondisi memenuhi
                            if (!isExpanded || (isExpanded && scrollState.value == 0)) {
                                isDragging = true
                            }
                        },
                        onDragEnd = {
                            if (isDragging) {
                                isDragging = false
                                // Auto snap berdasarkan drag distance
                                val threshold = screenHeight * 0.15f
                                if (isExpanded) {
                                    // Jika expanded, drag ke bawah untuk collapse (hanya jika scroll di atas)
                                    if (scrollState.value == 0 && rawDragOffset > threshold) {
                                        isExpanded = false
                                    }
                                } else {
                                    // Jika collapsed, drag ke atas untuk expand
                                    if (rawDragOffset < -threshold) {
                                        isExpanded = true
                                    }
                                }
                                rawDragOffset = 0f
                            }
                        },
                        onVerticalDrag = { change, dragAmount ->
                            if (isExpanded) {
                                // Saat expanded, hanya bisa swipe down jika scroll di posisi paling atas
                                if (scrollState.value == 0 && dragAmount > 0 && isDragging) {
                                    // Swipe down untuk collapse - consume untuk mencegah scroll
                                    val newOffset = rawDragOffset + dragAmount
                                    rawDragOffset = newOffset.coerceAtLeast(0f)
                                    change.consume()
                                } else {
                                    // Jika scroll tidak di atas atau swipe up, reset isDragging dan biarkan scroll handle
                                    if (scrollState.value != 0 || dragAmount < 0) {
                                        isDragging = false
                                        rawDragOffset = 0f
                                    }
                                }
                            } else {
                                // Saat collapsed (half), swipe up untuk expand
                                // Disable scroll, jadi semua gesture digunakan untuk swipe up
                                if (isDragging) {
                                    val newOffset = rawDragOffset + dragAmount
                                    rawDragOffset = newOffset.coerceAtMost(0f)
                                    change.consume()
                                }
                            }
                        }
                    )
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight() // Gunakan fillMaxHeight agar konten selalu terlihat
                    .padding(bottom = with(density) { bottomBarHeight.toDp() })
            ) {
                // Drag Handle
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0xFFE0E0E0))
                    )
                }

                // Content
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = GreenPrimary)
                    }
                } else if (wasteDetail != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .nestedScroll(nestedScrollConnection) // Tambahkan nested scroll connection
                            .verticalScroll(
                                state = scrollState,
                                enabled = isExpanded // Hanya enable scroll saat expanded
                            )
                            .padding(horizontal = 20.dp)
                            .padding(bottom = if (isExpanded) with(density) { bottomBarHeight.toDp() } else 0.dp) // Padding bottom saat full
                    ) {
                        // Title and Type Badge - Side by side
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = wasteDetail.name,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333),
                                modifier = Modifier.weight(1f)
                            )
                            
                            if (wasteDetail.wasteTypeName.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = GreenPrimary,
                                    modifier = Modifier.padding(start = 8.dp)
                                ) {
                                    Text(
                                        text = wasteDetail.wasteTypeName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Cara Pengelolaan yang Benar
                        Text(
                            text = "Cara Pengelolaan yang Benar",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        // Parse bullet points dari string
                        val disposalMethods = wasteDetail.properDisposalMethod.split(". ")
                            .filter { it.isNotBlank() }
                            .map { it.trim() }
                        
                        disposalMethods.forEach { method ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "• ",
                                    fontSize = 14.sp,
                                    color = Color(0xFF666666),
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(
                                    text = method + if (!method.endsWith(".")) "." else "",
                                    fontSize = 14.sp,
                                    color = Color(0xFF666666),
                                    lineHeight = 20.sp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Dampak Positif
                        Text(
                            text = "Dampak Positif",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        // Parse bullet points dari string
                        val positiveImpacts = wasteDetail.positiveImpact.split(". ")
                            .filter { it.isNotBlank() }
                            .map { it.trim() }
                        
                        positiveImpacts.forEach { impact ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "• ",
                                    fontSize = 14.sp,
                                    color = Color(0xFF666666),
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(
                                    text = impact + if (!impact.endsWith(".")) "." else "",
                                    fontSize = 14.sp,
                                    color = Color(0xFF666666),
                                    lineHeight = 20.sp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Waktu Dekomposisi
                        Text(
                            text = "Waktu Dekomposisi",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        Text(
                            text = wasteDetail.decompositionTime,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GreenPrimary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        if (wasteDetail.decompositionTime.isNotEmpty()) {
                            Text(
                                text = "Jika dibuang sembarangan di alam terbuka, ${wasteDetail.name.lowercase()} membutuhkan waktu sekitar ${wasteDetail.decompositionTime} untuk terurai sepenuhnya. Selama itu, akan pecah menjadi mikroplastik yang berpotensi masuk ke rantai makanan manusia dan hewan.",
                                fontSize = 14.sp,
                                color = Color(0xFF666666),
                                lineHeight = 20.sp,
                                modifier = Modifier.padding(bottom = 32.dp)
                            )
                        }
                    }
                } else {
                    // Error state
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Gagal memuat data",
                            fontSize = 16.sp,
                            color = Color.Red
                        )
                    }
                }
            }
        }

        // Bottom Bar - Fixed at bottom (Xpoin dan Setor)
        if (!isLoading && wasteDetail != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .zIndex(3f),
                color = Color.White,
                shadowElevation = 4.dp,
                tonalElevation = 0.dp
                ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Xpoin Button - No fill dengan stroke hijau, icon + nominal saja (tanpa efek klik)
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, GreenPrimary),
                    color = Color.Transparent
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Icon Xetor
                        Image(
                            painter = painterResource(id = R.drawable.icon_xetor_hijau_png),
                            contentDescription = "X Icon",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        // Nominal saja
                        Text(
                            text = formatNumber(wasteDetail.xpoin),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = GreenPrimary
                        )
                    }
                }

                // Setor Button - Lebih besar
                Button(
                    onClick = {
                        val intent = Intent(context, SetorActivity::class.java)
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .weight(1.5f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenPrimary
                    )
                ) {
                    Text(
                        text = "Setor",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                }
            }
        }
    }
}

// Helper function untuk format number seperti di SetorScreen
private fun formatNumber(value: Int): String {
    return String.format("%,d", value).replace(',', '.')
}
