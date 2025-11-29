// app/src/main/java/id/xetor/app/ui/withdraw/WithdrawScreen.kt
package id.xetor.app.ui.withdraw

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.zIndex
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.launch
import id.xetor.app.R
import id.xetor.app.ui.components.*
import id.xetor.app.ui.components.CustomSnackbar
import id.xetor.app.ui.theme.GreenPrimary
import java.text.SimpleDateFormat
import java.util.*

data class PaymentMethod(
    val id: Int,
    val name: String,
    val iconRes: Int,
    val isAvailable: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithdrawScreen(
    viewModel: WithdrawViewModel,
    onTopUpClick: () -> Unit = {},
    onPaymentMethodClick: (PaymentMethod) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val lazyListState = rememberLazyListState()
    var previousTransactionsCount by remember { mutableStateOf(0) }
    
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
    
    // Scroll to top ketika transactions list berubah (setelah kembali dari membuat withdraw baru)
    // Hanya scroll jika jumlah transactions bertambah (bukan karena filter berubah)
    LaunchedEffect(uiState.allTransactions.size) {
        val currentCount = uiState.allTransactions.size
        if (currentCount > previousTransactionsCount && currentCount > 0) {
            // Transactions bertambah, scroll to top
            lazyListState.animateScrollToItem(0)
        }
        previousTransactionsCount = currentCount
    }
    
    // Scroll to top ketika filter berubah (date range atau status)
    LaunchedEffect(uiState.selectedDateRange, uiState.selectedStatus) {
        if (uiState.filteredTransactions.isNotEmpty()) {
            lazyListState.animateScrollToItem(0)
        }
    }
    
    // Cek apakah ini initial load (data masih kosong) atau bukan
    // Jika data sudah ada, berarti ini bukan initial load, jadi jangan tampilkan skeleton
    // Ini perlu dilakukan setelah ViewModel selesai load pertama kali
    LaunchedEffect(uiState.wallet, uiState.paymentMethods, uiState.allTransactions) {
        val hasData = uiState.wallet != null || 
                      uiState.paymentMethods.isNotEmpty() || 
                      uiState.allTransactions.isNotEmpty()
        
        // Jika sudah ada data tapi masih loading, berarti ini bukan initial load
        // Set loading = false untuk menghindari skeleton muncul saat kembali ke halaman
        if (hasData && uiState.isLoading) {
            viewModel.setLoading(false)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Withdraw", fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        },
        snackbarHost = {
            if (uiState.errorMessage != null) {
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
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                // Loading state - Semua skeleton kecuali topBar (back button + title)
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Fixed Top Section - semua skeleton
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Saldo Card - skeleton
                        WithdrawSaldoCardSkeleton()

                        Spacer(modifier = Modifier.height(1.dp))

                        // Payment Methods Grid - skeleton
                        WithdrawPaymentMethodsSkeleton()

                        // Riwayat Withdraw Header - skeleton
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SkeletonText(modifier = Modifier.width(140.dp).height(16.dp))
                            SkeletonBox(
                                modifier = Modifier
                                    .size(40.dp),
                                shape = CircleShape
                            )
                        }
                    }

                    // Transaction history skeleton
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        repeat(5) {
                            WithdrawHistoryItemSkeleton()
                        }
                    }
                }
            } else {
                // Success state - Split: Fixed top + Scrollable list
                var isFilterOpen by remember { mutableStateOf(false) }
                var headerBottomY by remember { mutableStateOf(0.dp) }
                val density = LocalDensity.current
                var boxTopY by remember { mutableStateOf(0f) }
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .onGloballyPositioned { coordinates ->
                            val positionInWindow = coordinates.localToWindow(Offset.Zero)
                            boxTopY = positionInWindow.y
                        }
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Fixed Top Section (tidak ikut scroll) - zIndex tinggi agar di atas overlay
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .zIndex(3f) // Paling atas
                                .padding(horizontal = 16.dp)
                                .padding(top = 16.dp)
                                .padding(bottom = 8.dp) // Kurangi padding bawah header filter
                                .onGloballyPositioned { coordinates ->
                                    // Hitung posisi bottom dari top section relatif terhadap Box parent
                                    val positionInWindow = coordinates.localToWindow(Offset.Zero)
                                    val relativeY = positionInWindow.y - boxTopY + coordinates.size.height
                                    headerBottomY = with(density) { relativeY.toDp() }
                                },
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Saldo Card
                            SaldoCard(
                                saldo = uiState.wallet?.balance ?: "0",
                                onTopUpClick = onTopUpClick
                            )

                            Spacer(modifier = Modifier.height(1.dp))

                            // Payment Methods Grid (dari backend, dinamis)
                            PaymentMethodsGrid(
                                paymentMethods = uiState.paymentMethods,
                                onMethodClick = onPaymentMethodClick
                            )

                            // Riwayat Withdraw Header dengan Filter Button
                            WithdrawFilterHeader(
                                onFilterClick = { isFilterOpen = !isFilterOpen }
                            )
                        }
                        
                        // Scrollable Transaction List (tidak ikut geser saat dropdown dibuka)
                        // zIndex default (0f) - berada di bawah overlay
                        Box(modifier = Modifier
                            .fillMaxSize()
                            .zIndex(0f) // Paling bawah
                        ) {
                            if (uiState.filteredTransactions.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Belum ada riwayat transaksi terbaru",
                                        fontSize = 13.sp,
                                        color = Color.Gray,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else {
                                LazyColumn(
                                    state = lazyListState,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 16.dp),
                                    contentPadding = PaddingValues(bottom = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    items(
                                        items = uiState.filteredTransactions,
                                        key = { it.id }
                                    ) { transaction ->
                                        WithdrawHistoryItem(transaction = transaction)
                                    }
                                }
                            }
                        }
                    }
                    
                    // Bottom Sheet Filter (muncul dari bawah)
                    WithdrawFilterBottomSheet(
                        isOpen = isFilterOpen,
                        onDismiss = { isFilterOpen = false },
                        selectedDateRange = uiState.selectedDateRange,
                        selectedStatus = uiState.selectedStatus,
                        onDateRangeChange = { viewModel.setDateRangeFilter(it) },
                        onStatusChange = { viewModel.setStatusFilter(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun SaldoCard(
    saldo: String,
    onTopUpClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Saldo Anda",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "Rp ",
                        fontSize = 14.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = formatCurrency(saldo),
                        fontSize = 24.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Button Top Up
            IconButton(
                onClick = onTopUpClick,
                modifier = Modifier
                    .size(48.dp)
                    .background(GreenPrimary, CircleShape)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_topup),
                    contentDescription = "Top Up",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun PaymentMethodsGrid(
    paymentMethods: List<PaymentMethod>,
    onMethodClick: (PaymentMethod) -> Unit
) {
    if (paymentMethods.isEmpty()) {
        // Empty state
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Metode pembayaran tidak tersedia",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp) // TODO: Untuk mengurangi jarak antar baris payment method, ubah nilai 16.dp menjadi lebih kecil (misalnya 12.dp atau 8.dp)
            ) {
                // Buat rows dinamis berdasarkan jumlah methods (4 kolom per baris)
                paymentMethods.chunked(4).forEach { rowMethods ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowMethods.forEach { method ->
                            PaymentMethodItem(
                                method = method,
                                onClick = { onMethodClick(method) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // Fill empty space jika kurang dari 4
                        repeat(4 - rowMethods.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentMethodItem(
    method: PaymentMethod,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp)) // Clip ripple effect sesuai rounded shape
                .clickable(onClick = onClick) // Pindahkan clickable ke Box icon saja
                .background(Color(0xFFF5F5F5)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = method.iconRes),
                contentDescription = method.name,
                modifier = Modifier.size(48.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp)) // TODO: Untuk mengurangi jarak antara icon payment method dengan text label di bawahnya, ubah nilai 6.dp menjadi lebih kecil (misalnya 4.dp)
        Text(
            text = method.name,
            fontSize = 11.sp,
            color = Color.Black,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
fun WithdrawFilterHeader(
    onFilterClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Riwayat Withdraw",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )
        
        IconButton(
            onClick = onFilterClick,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = "Filter",
                tint = GreenPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithdrawFilterBottomSheet(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    selectedDateRange: DateRangeFilter,
    selectedStatus: StatusFilter,
    onDateRangeChange: (DateRangeFilter) -> Unit,
    onStatusChange: (StatusFilter) -> Unit
) {
    var tempDateRange by remember { mutableStateOf(selectedDateRange) }
    var tempStatus by remember { mutableStateOf(selectedStatus) }
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    
    // State untuk track drag offset
    var rawDragOffset by remember { mutableStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var shouldDismiss by remember { mutableStateOf(false) }
    var dismissStartOffset by remember { mutableStateOf(0f) } // Simpan posisi awal saat mulai dismiss
    var isDismissingFromClick by remember { mutableStateOf(false) } // Track apakah dismiss dari klik
    
    // Get screen height untuk swipe threshold
    val configuration = LocalConfiguration.current
    val screenHeight = with(density) { 
        configuration.screenHeightDp.dp.toPx() 
    }
    val dismissThreshold = screenHeight * 0.15f // 15% dari screen height (lebih mudah untuk swipe)
    
    // Animated drag offset untuk dismiss - menggunakan Animatable untuk kontrol yang lebih baik
    val dismissAnimatable = remember { Animatable(0f) }
    
    // Animate ke screenHeight saat shouldDismiss menjadi true, mulai dari dismissStartOffset atau 0 (jika dari klik)
    LaunchedEffect(shouldDismiss, dismissStartOffset, isDismissingFromClick) {
        if (shouldDismiss) {
            val startOffset = if (isDismissingFromClick) 0f else dismissStartOffset.coerceAtLeast(0f)
            // Set initial value ke startOffset, lalu animate ke screenHeight
            dismissAnimatable.snapTo(startOffset)
            dismissAnimatable.animateTo(
                targetValue = screenHeight,
                animationSpec = tween(
                    durationMillis = 200,
                    easing = FastOutSlowInEasing
                )
            )
        } else if (!shouldDismiss) {
            dismissAnimatable.snapTo(0f)
        }
    }
    
    val dismissAnimatedOffset = dismissAnimatable.value
    
    // Animated drag offset untuk snap back - hanya animate saat tidak dragging (untuk snap back)
    val snapBackAnimatedOffset by animateFloatAsState(
        targetValue = 0f,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        label = "snapBackOffset"
    )
    
    // Offset yang digunakan: saat dragging langsung ikut jari tanpa animasi, saat tidak dragging gunakan animated untuk snap back
    // Jika shouldDismiss, gunakan dismissAnimatedOffset yang mulai dari dismissStartOffset (jika dari swipe) atau 0 (jika dari klik)
    val currentOffset = if (isDragging) {
        rawDragOffset.coerceAtLeast(0f)
    } else if (shouldDismiss) {
        // Gunakan dismissAnimatedOffset yang sudah mulai dari dismissStartOffset (jika dari swipe) atau 0 (jika dari klik)
        if (isDismissingFromClick) {
            dismissAnimatedOffset // Mulai dari 0
        } else {
            dismissAnimatedOffset.coerceAtLeast(dismissStartOffset) // Mulai dari posisi terakhir
        }
    } else {
        snapBackAnimatedOffset
    }
    
    // Update temporary state ketika selected filter berubah dari luar
    LaunchedEffect(selectedDateRange, selectedStatus) {
        tempDateRange = selectedDateRange
        tempStatus = selectedStatus
    }
    
    // Reset temporary state dan drag offset ketika bottom sheet dibuka
    LaunchedEffect(isOpen) {
        if (isOpen) {
            tempDateRange = selectedDateRange
            tempStatus = selectedStatus
            rawDragOffset = 0f
            isDragging = false
            shouldDismiss = false
            dismissStartOffset = 0f
            isDismissingFromClick = false
        } else {
            rawDragOffset = 0f
            isDragging = false
            shouldDismiss = false
            dismissStartOffset = 0f
            isDismissingFromClick = false
        }
    }
    
    // Internal dismiss function yang akan trigger animasi jika perlu
    val handleDismiss = {
        // Jika belum ada animasi dismiss yang berjalan, trigger animasi terlebih dahulu
        if (!shouldDismiss && isOpen) {
            isDismissingFromClick = true
            dismissStartOffset = 0f // Mulai dari 0 karena dari klik
            shouldDismiss = true
        }
    }
    
    // Trigger dismiss setelah animasi ke bawah selesai
    LaunchedEffect(shouldDismiss, dismissAnimatedOffset) {
        if (shouldDismiss && dismissAnimatedOffset >= screenHeight * 0.95f) {
            // Reset state
            dismissStartOffset = 0f
            rawDragOffset = 0f
            isDismissingFromClick = false
            // Panggil dismiss langsung - akan set isOpen = false
            onDismiss()
            // Set shouldDismiss = false setelah delay kecil untuk memastikan onDismiss() sudah dipanggil
            kotlinx.coroutines.delay(10)
            shouldDismiss = false
        }
    }
    
    // Bottom Sheet Filter dengan animasi dari bawah
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Clickable area di belakang filter box untuk menutup saat klik halaman utama
        if (isOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(1f)
                    .pointerInput(Unit) {
                        detectTapGestures {
                            handleDismiss()
                        }
                    }
            )
        }
        
        // Tampilkan box dengan offset manual saat shouldDismiss (untuk animasi dismiss)
        if (isOpen && shouldDismiss) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .zIndex(2f)
                    .fillMaxWidth()
                    .graphicsLayer {
                        translationY = currentOffset
                    }
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                        spotColor = Color.Black.copy(alpha = 0.2f)
                    )
                    .background(
                        Color.White,
                        RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 12.dp, bottom = 24.dp)
                ) {
                    // Handle bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color.Gray.copy(alpha = 0.4f))
                        )
                    }
                    
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Riwayat Withdraw",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                        
                        IconButton(
                            onClick = handleDismiss,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filter",
                                tint = GreenPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Periode Filter
                    Column {
                        Text(
                            text = "Periode",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DateRangeFilter.values().forEach { filter ->
                                FilterChip(
                                    label = filter.label,
                                    isSelected = tempDateRange == filter,
                                    onClick = { tempDateRange = filter }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Status Filter
                    Column {
                        Text(
                            text = "Status",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatusFilter.values().forEach { filter ->
                                FilterChip(
                                    label = filter.label,
                                    isSelected = tempStatus == filter,
                                    onClick = { tempStatus = filter }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                onDateRangeChange(DateRangeFilter.ALL)
                                onStatusChange(StatusFilter.ALL)
                                handleDismiss()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = GreenPrimary
                            ),
                            border = BorderStroke(1.dp, GreenPrimary)
                        ) {
                            Text("Atur Ulang", fontSize = 14.sp)
                        }
                        
                        Button(
                            onClick = {
                                onDateRangeChange(tempDateRange)
                                onStatusChange(tempStatus)
                                handleDismiss()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                        ) {
                            Text("Pakai", fontSize = 14.sp, color = Color.White)
                        }
                    }
                }
            }
        }
        
        AnimatedVisibility(
            visible = isOpen && !shouldDismiss, // Hide langsung saat shouldDismiss mencapai threshold
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            ),
            exit = ExitTransition.None, // Nonaktifkan exit animation karena kita handle sendiri dengan dismissAnimatedOffset
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .zIndex(2f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        translationY = currentOffset
                    }
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                        spotColor = Color.Black.copy(alpha = 0.2f)
                    )
                    .background(
                        Color.White,
                        RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    )
                    .pointerInput(isOpen) {
                        if (isOpen) {
                            detectVerticalDragGestures(
                                onDragStart = {
                                    isDragging = true
                                },
                                onDragEnd = {
                                    isDragging = false
                                    // Auto-slide assistance: jika drag lebih dari threshold, tutup
                                    // Jika tidak, kembali ke posisi awal
                                    if (rawDragOffset > dismissThreshold) {
                                        // Simpan posisi terakhir sebagai starting point untuk dismiss animation
                                        dismissStartOffset = rawDragOffset.coerceAtLeast(0f)
                                        // Set flag untuk trigger animasi ke bawah, lalu dismiss
                                        shouldDismiss = true
                                        // dismissAnimatedOffset akan otomatis animate dari dismissStartOffset ke screenHeight
                                        // LaunchedEffect akan trigger dismiss setelah animasi selesai
                                    } else {
                                        // Reset drag offset (akan di-animate oleh snapBackAnimatedOffset)
                                        rawDragOffset = 0f
                                        shouldDismiss = false
                                        dismissStartOffset = 0f
                                    }
                                },
                                onVerticalDrag = { change, dragAmount ->
                                    // Allow drag ke atas dan ke bawah, tapi clamp ke 0 minimum
                                    rawDragOffset = (rawDragOffset + dragAmount).coerceAtLeast(0f)
                                    change.consume()
                                }
                            )
                        }
                    }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 12.dp, bottom = 24.dp)
                ) {
                    // Handle bar (garis abu-abu) untuk menandakan bisa di-swipe
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color.Gray.copy(alpha = 0.4f))
                        )
                    }
                    
                    // Header dengan title dan filter button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Riwayat Withdraw",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                        
                        IconButton(
                            onClick = handleDismiss,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filter",
                                tint = GreenPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Periode Filter
                    Column {
                        Text(
                            text = "Periode",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DateRangeFilter.values().forEach { filter ->
                                FilterChip(
                                    label = filter.label,
                                    isSelected = tempDateRange == filter,
                                    onClick = { tempDateRange = filter }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Status Filter
                    Column {
                        Text(
                            text = "Status",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatusFilter.values().forEach { filter ->
                                FilterChip(
                                    label = filter.label,
                                    isSelected = tempStatus == filter,
                                    onClick = { tempStatus = filter }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                onDateRangeChange(DateRangeFilter.ALL)
                                onStatusChange(StatusFilter.ALL)
                                handleDismiss()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = GreenPrimary
                            ),
                            border = BorderStroke(1.dp, GreenPrimary)
                        ) {
                            Text("Atur Ulang", fontSize = 14.sp)
                        }
                        
                        Button(
                            onClick = {
                                onDateRangeChange(tempDateRange)
                                onStatusChange(tempStatus)
                                handleDismiss()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                        ) {
                            Text("Pakai", fontSize = 14.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                color = if (isSelected) GreenPrimary.copy(alpha = 0.1f) else Color.Transparent
            )
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 1.dp,
                        color = GreenPrimary,
                        shape = RoundedCornerShape(8.dp)
                    )
                } else {
                    Modifier.border(
                        width = 1.dp,
                        color = Color.Gray.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = if (isSelected) GreenPrimary else Color.Gray,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun WithdrawHistoryItem(
    transaction: id.xetor.app.data.remote.TransactionHistoryResponse
) {
    // Extract payment method name from description (e.g., "Withdraw ke Gopay")
    val methodName = transaction.description.substringAfter("ke ").trim().takeIf { it.isNotEmpty() } ?: "Gopay"
    
    // Map payment method name to icon
    val iconRes = when (methodName.lowercase()) {
        "gopay" -> R.drawable.ic_gopay
        "shopeepay", "spay" -> R.drawable.ic_spay
        "dana" -> R.drawable.ic_dana
        "ovo" -> R.drawable.ic_ovo
        "linkaja" -> R.drawable.ic_linkaja
        "bca" -> R.drawable.ic_bca
        "bri" -> R.drawable.ic_bri
        "bni" -> R.drawable.ic_bni
        "mandiri" -> R.drawable.ic_mandiri
        "bsi" -> R.drawable.ic_bsi
        else -> R.drawable.ic_gopay
    }

    // Tanpa Card dan tanpa Divider
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // Method Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = methodName,
                    modifier = Modifier.size(28.dp),
                    tint = Color.Unspecified  // PNG already has colors
                )
            }

            // Method Info
            Column(
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Text(
                    text = methodName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = formatDateTime(transaction.timestamp),
                    fontSize = 11.sp,
                    color = Color.Gray,
                    lineHeight = 12.sp
                )
            }
        }

        // Amount
        Text(
            text = "Rp${formatCurrency(transaction.amount?.getAmount() ?: "0")}",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = GreenPrimary
        )
    }
}

// Helper functions
private fun formatCurrency(value: String): String {
    return try {
        val num = value.toDoubleOrNull() ?: 0.0
        val intValue = num.toInt()
        String.format("%,d", intValue).replace(',', '.')
    } catch (e: Exception) {
        "0"
    }
}

private fun formatDateTime(timestamp: String): String {
    return try {
        // Backend bisa kirim berbagai format:
        // 1. "2025-11-19T18:23:46.406839Z" - ISO 8601 UTC dengan microseconds
        // 2. "2025-10-23T23:12:38.908+0700" - dengan timezone offset
        
        // Potong microseconds jadi 3 digit (milliseconds) untuk SimpleDateFormat
        var cleanedTimestamp = timestamp.replace(" ", "")
        
        // Regex untuk potong microseconds: .123456 -> .123
        cleanedTimestamp = cleanedTimestamp.replace(Regex("\\.\\d{6}"), { matchResult ->
            "." + matchResult.value.substring(1, 4)
        })
        
        // Ganti Z dengan +0000 untuk format timezone
        if (cleanedTimestamp.endsWith("Z")) {
            cleanedTimestamp = cleanedTimestamp.replace("Z", "+0000")
        }
        
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault())
        val outputFormat = SimpleDateFormat("d MMM yyyy 'at' HH:mm", Locale("id", "ID"))
        
        val date = inputFormat.parse(cleanedTimestamp)
        date?.let { outputFormat.format(it) } ?: timestamp
    } catch (e: Exception) {
        // Fallback: coba parse format sederhana tanpa milidetik
        try {
            var cleanedTimestamp = timestamp.replace(" ", "")
            
            // Hilangkan microseconds/milliseconds
            cleanedTimestamp = cleanedTimestamp.replace(Regex("\\.\\d+"), "")
            
            if (cleanedTimestamp.endsWith("Z")) {
                cleanedTimestamp = cleanedTimestamp.replace("Z", "+0000")
            }
            
            val inputFormatNoMillis = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault())
            val outputFormat = SimpleDateFormat("d MMM yyyy 'at' HH:mm", Locale("id", "ID"))
            val date = inputFormatNoMillis.parse(cleanedTimestamp)
            date?.let { outputFormat.format(it) } ?: timestamp
        } catch (e2: Exception) {
            // Last resort: return as is
            timestamp
        }
    }
}

