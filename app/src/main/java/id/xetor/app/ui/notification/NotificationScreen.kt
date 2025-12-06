// app/src/main/java/id/xetor/app/ui/notification/NotificationScreen.kt
package id.xetor.app.ui.notification

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import id.xetor.app.R
import id.xetor.app.ui.components.CustomSnackbar
import id.xetor.app.ui.components.SkeletonBox
import id.xetor.app.ui.components.SkeletonText
import id.xetor.app.ui.theme.GreenPrimary
import id.xetor.app.ui.withdraw.DateRangeFilter
import id.xetor.app.ui.withdraw.StatusFilter
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

// Transaction type filter enum
enum class TransactionTypeFilter(val label: String) {
    ALL("Semua"),
    SETOR("Setor"),
    WITHDRAW("Withdraw"),
    TOPUP("Top Up"),
    TRANSFER("Transfer"),
    CONVERT("Convert")
}

// Notification data model (temporary, will be replaced with real data later)
data class NotificationItem(
    val id: String,
    val title: String,
    val body: String,
    val dateTime: String,
    val type: TransactionTypeFilter,
    val isRead: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    onBackClick: () -> Unit = {},
    isLoading: Boolean = false
) {
    // State untuk filter
    var selectedTransactionType by remember { mutableStateOf(TransactionTypeFilter.ALL) }
    var isFilterOpen by remember { mutableStateOf(false) }
    var selectedDateRange by remember { mutableStateOf(DateRangeFilter.ALL) }
    var selectedStatus by remember { mutableStateOf(StatusFilter.ALL) }
    val scope = rememberCoroutineScope()
    
    // Temporary mock data - akan diganti dengan data dari Firestore nanti
    val mockNotifications = remember {
        listOf<NotificationItem>(
            // Uncomment untuk testing dengan data
            // NotificationItem(
            //     id = "1",
            //     title = "Transfer Berhasil",
            //     body = "Kamu berhasil mentransfer 13 Xpoin ke ipul@xetor.id",
            //     dateTime = "30 Nov 2025",
            //     type = TransactionTypeFilter.TRANSFER,
            //     isRead = false
            // ),
            // NotificationItem(
            //     id = "2",
            //     title = "Konversi Berhasil",
            //     body = "Rp1.000.000 berhasil dikonversi menjadi 200.000 Xpoin",
            //     dateTime = "30 Nov 2025",
            //     type = TransactionTypeFilter.CONVERT,
            //     isRead = false
            // ),
            // NotificationItem(
            //     id = "3",
            //     title = "Penarikan Saldo",
            //     body = "Permintaan penarikan saldo sebesar Rp1.000.000 sedang diproses",
            //     dateTime = "30 Nov 2025",
            //     type = TransactionTypeFilter.WITHDRAW,
            //     isRead = false
            // ),
            // NotificationItem(
            //     id = "4",
            //     title = "Top Up Berhasil",
            //     body = "Top up saldo sebesar Rp200.000 berhasil ditambahkan ke akun Anda.",
            //     dateTime = "30 Nov 2025",
            //     type = TransactionTypeFilter.TOPUP,
            //     isRead = true
            // )
        )
    }
    
    // Filter notifications berdasarkan transaction type
    val filteredNotifications = remember(mockNotifications, selectedTransactionType) {
        if (selectedTransactionType == TransactionTypeFilter.ALL) {
            mockNotifications
        } else {
            mockNotifications.filter { it.type == selectedTransactionType }
        }
    }
    
    // State untuk track read notifications
    var readNotificationIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    
    // Combine mock isRead dengan readNotificationIds
    val notificationsWithReadState = filteredNotifications.map { notification ->
        notification.copy(isRead = notification.isRead || readNotificationIds.contains(notification.id))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Notifikasi", 
                        fontSize = 18.sp, 
                        fontWeight = FontWeight.SemiBold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { isFilterOpen = !isFilterOpen },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter",
                            tint = GreenPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                if (isLoading) {
                    // Skeleton loading state
                    // Filter tabs skeleton
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        repeat(6) {
                            SkeletonBox(
                                modifier = Modifier
                                    .height(36.dp)
                                    .width(80.dp),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                    
                    // Notification cards skeleton
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(5) {
                            NotificationCardSkeleton()
                        }
                    }
                } else {
                    // Horizontal scrollable filter tabs
                    val filterScrollState = rememberScrollState()
                    val density = LocalDensity.current
                    val configuration = LocalConfiguration.current
                    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
                    val filterPositions = remember { mutableMapOf<TransactionTypeFilter, Pair<Float, Float>>() } // left, width
                    var rowLeft by remember { mutableStateOf(0f) }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(filterScrollState)
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .onGloballyPositioned { coordinates ->
                                val positionInWindow = coordinates.localToWindow(Offset.Zero)
                                rowLeft = positionInWindow.x
                            },
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TransactionTypeFilter.values().forEach { filter ->
                            FilterChip(
                                label = filter.label,
                                isSelected = selectedTransactionType == filter,
                                onClick = { 
                                    selectedTransactionType = filter
                                    
                                    // Auto-scroll filter - mentok ke kiri atau kanan sesuai posisi
                                    filterPositions[filter]?.let { (filterLeft, filterWidth) ->
                                        val filterRight = filterLeft + filterWidth
                                        val filterCenter = filterLeft + filterWidth / 2
                                        val currentScroll = filterScrollState.value.toFloat()
                                        val visibleLeft = rowLeft + currentScroll
                                        val visibleRight = visibleLeft + screenWidthPx
                                        val visibleCenter = visibleLeft + screenWidthPx / 2
                                        
                                        scope.launch {
                                            val paddingPx = with(density) { 16.dp.toPx() }
                                            
                                            // Tentukan apakah scroll mentok kiri atau kanan
                                            if (filterCenter < visibleCenter) {
                                                // Filter di bagian kiri, scroll mentok ke kiri
                                                val targetScroll = (filterLeft - rowLeft - paddingPx).coerceAtLeast(0f)
                                                filterScrollState.animateScrollTo(targetScroll.toInt())
                                            } else {
                                                // Filter di bagian kanan, scroll mentok ke kanan
                                                // Gunakan posisi absolut untuk perhitungan yang lebih akurat
                                                val filterRight = filterLeft + filterWidth
                                                
                                                // Kita ingin filterRight berada di: screenWidth - paddingPx
                                                // scrollPosition = filterRight - rowLeft - screenWidth + paddingPx*2
                                                
                                                val maxScroll = filterScrollState.maxValue.toFloat()
                                                val targetScroll = (filterRight - rowLeft - screenWidthPx + paddingPx * 2).coerceIn(0f, maxScroll)
                                                
                                                // Pastikan benar-benar mentok - jika mendekati max, gunakan max
                                                if (targetScroll >= maxScroll - 1) {
                                                    filterScrollState.animateScrollTo(maxScroll.toInt())
                                                } else {
                                                    filterScrollState.animateScrollTo(targetScroll.toInt())
                                                }
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.onGloballyPositioned { coordinates ->
                                    val positionInWindow = coordinates.localToWindow(Offset.Zero)
                                    val size = coordinates.size
                                    filterPositions[filter] = Pair(positionInWindow.x, size.width.toFloat())
                                }
                            )
                        }
                    }
                    
                    // Notification list
                    if (notificationsWithReadState.isEmpty()) {
                        // Empty state
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Yahh.... Belum ada notifikasi terbaru.",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(
                                items = notificationsWithReadState,
                                key = { it.id }
                            ) { notification ->
                                NotificationCard(
                                    notification = notification,
                                    onClick = {
                                        // Mark as read when clicked
                                        readNotificationIds = readNotificationIds + notification.id
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            // Filter Bottom Sheet
            NotificationFilterBottomSheet(
                isOpen = isFilterOpen,
                onDismiss = { isFilterOpen = false },
                selectedDateRange = selectedDateRange,
                selectedStatus = selectedStatus,
                onDateRangeChange = { selectedDateRange = it },
                onStatusChange = { selectedStatus = it }
            )
        }
    }
}

@Composable
fun FilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
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
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = if (isSelected) GreenPrimary else Color.Gray,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
fun NotificationCard(
    notification: NotificationItem,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (notification.isRead) {
                    Color.White
                } else {
                    GreenPrimary.copy(alpha = 0.1f)
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon dengan background hijau
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(GreenPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = getIconForTransactionType(notification.type)),
                    contentDescription = notification.type.label,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = notification.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Text(
                    text = notification.body,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    lineHeight = 16.sp
                )
            }
            
            // Date time
            Text(
                text = notification.dateTime,
                fontSize = 11.sp,
                color = Color.Gray,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
fun getIconForTransactionType(type: TransactionTypeFilter): Int {
    return when (type) {
        TransactionTypeFilter.WITHDRAW -> R.drawable.ic_withdraw
        TransactionTypeFilter.TOPUP -> R.drawable.ic_topup
        TransactionTypeFilter.TRANSFER -> R.drawable.ic_transfer
        TransactionTypeFilter.CONVERT -> R.drawable.ic_convert
        TransactionTypeFilter.SETOR -> R.drawable.ic_shop // Using shop icon as placeholder for setor
        TransactionTypeFilter.ALL -> R.drawable.ic_bell
    }
}

@Composable
fun NotificationCardSkeleton() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon skeleton
            SkeletonBox(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(8.dp)
            )
            
            // Content skeleton
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SkeletonText(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(14.dp)
                )
                SkeletonText(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                )
            }
            
            // Date time skeleton
            SkeletonText(
                modifier = Modifier
                    .width(70.dp)
                    .height(11.dp)
            )
        }
    }
}

// Reuse filter bottom sheet from withdraw page with full drag gesture support
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationFilterBottomSheet(
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
    
    // State untuk track drag offset
    var rawDragOffset by remember { mutableStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var shouldDismiss by remember { mutableStateOf(false) }
    var dismissStartOffset by remember { mutableStateOf(0f) }
    var isDismissingFromClick by remember { mutableStateOf(false) }
    
    // Get screen height untuk swipe threshold
    val configuration = LocalConfiguration.current
    val screenHeight = with(density) { 
        configuration.screenHeightDp.dp.toPx() 
    }
    val dismissThreshold = screenHeight * 0.15f
    
    // Animated drag offset untuk dismiss
    val dismissAnimatable = remember { Animatable(0f) }
    
    // Animate ke screenHeight saat shouldDismiss menjadi true
    LaunchedEffect(shouldDismiss, dismissStartOffset, isDismissingFromClick) {
        if (shouldDismiss) {
            val startOffset = if (isDismissingFromClick) 0f else dismissStartOffset.coerceAtLeast(0f)
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
    
    // Animated drag offset untuk snap back
    val snapBackAnimatedOffset by animateFloatAsState(
        targetValue = 0f,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        label = "snapBackOffset"
    )
    
    val currentOffset = if (isDragging) {
        rawDragOffset.coerceAtLeast(0f)
    } else if (shouldDismiss) {
        if (isDismissingFromClick) {
            dismissAnimatedOffset
        } else {
            dismissAnimatedOffset.coerceAtLeast(dismissStartOffset)
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
    
    // Internal dismiss function
    val handleDismiss = {
        if (!shouldDismiss && isOpen) {
            isDismissingFromClick = true
            dismissStartOffset = 0f
            shouldDismiss = true
        }
    }
    
    // Trigger dismiss setelah animasi ke bawah selesai
    LaunchedEffect(shouldDismiss, dismissAnimatedOffset) {
        if (shouldDismiss && dismissAnimatedOffset >= screenHeight * 0.95f) {
            dismissStartOffset = 0f
            rawDragOffset = 0f
            isDismissingFromClick = false
            onDismiss()
            kotlinx.coroutines.delay(10)
            shouldDismiss = false
        }
    }
    
    // Bottom Sheet Filter dengan animasi dari bawah
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Clickable area di belakang filter box
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
        
        // Tampilkan box dengan offset manual saat shouldDismiss
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
                            text = "Notifikasi",
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
            visible = isOpen && !shouldDismiss,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            ),
            exit = ExitTransition.None,
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
                                    if (rawDragOffset > dismissThreshold) {
                                        dismissStartOffset = rawDragOffset.coerceAtLeast(0f)
                                        shouldDismiss = true
                                    } else {
                                        rawDragOffset = 0f
                                        shouldDismiss = false
                                        dismissStartOffset = 0f
                                    }
                                },
                                onVerticalDrag = { change, dragAmount ->
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
                            text = "Notifikasi",
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

