// app/src/main/java/id/xetor/app/ui/topup/TopUpScreen.kt
package id.xetor.app.ui.topup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.zIndex
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import id.xetor.app.R
import id.xetor.app.ui.components.*
import id.xetor.app.ui.components.CustomSnackbar
import id.xetor.app.ui.theme.GreenPrimary
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopUpScreen(
    viewModel: TopUpViewModel,
    onBackClick: () -> Unit = {},
    onProceedToPayment: (Double, () -> Unit, (String) -> Unit, () -> Unit) -> Unit = { _, _, _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsState()
    val lazyListState = rememberLazyListState()
    var previousTransactionsCount by remember { mutableStateOf(0) }
    
    // State untuk input nominal
    var textFieldValue by remember { mutableStateOf(TextFieldValue("")) }
    var amount by remember { mutableStateOf("") }
    
    // State untuk loading dan error
    var isLoadingTopup by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    
    // Smart refresh saat screen kembali (onResume)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.silentRefresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    // Scroll to top ketika transactions list berubah
    LaunchedEffect(uiState.allTransactions.size) {
        val currentCount = uiState.allTransactions.size
        if (currentCount > previousTransactionsCount && currentCount > 0) {
            lazyListState.animateScrollToItem(0)
        }
        previousTransactionsCount = currentCount
    }
    
    // Scroll to top ketika filter berubah
    LaunchedEffect(uiState.selectedDateRange, uiState.selectedStatus) {
        if (uiState.filteredTransactions.isNotEmpty()) {
            lazyListState.animateScrollToItem(0)
        }
    }
    
    // Cek apakah ini initial load
    LaunchedEffect(uiState.wallet, uiState.allTransactions) {
        val hasData = uiState.wallet != null || uiState.allTransactions.isNotEmpty()
        if (hasData && uiState.isLoading) {
            viewModel.setLoading(false)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Top Up", fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
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
                        viewModel.clearError()
                        viewModel.refresh()
                    },
                    buttonText = "Coba Lagi"
                )
            }
            if (errorMessage != null) {
                CustomSnackbar(
                    message = errorMessage ?: "",
                    onDismiss = { 
                        errorMessage = null
                    }
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
                        // Top Up Input Card - skeleton
                        TopUpInputCardSkeleton()
                        
                        // Riwayat Top Up Header - skeleton
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SkeletonText(modifier = Modifier.width(140.dp).height(16.dp))
                            SkeletonBox(
                                modifier = Modifier.size(40.dp),
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
                            TopUpHistoryItemSkeleton()
                        }
                    }
                }
            } else {
                // Success state
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
                        // Fixed Top Section
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .zIndex(3f)
                                .padding(horizontal = 16.dp)
                                .padding(top = 16.dp)
                                .padding(bottom = 8.dp)
                                .onGloballyPositioned { coordinates ->
                                    val positionInWindow = coordinates.localToWindow(Offset.Zero)
                                    val relativeY = positionInWindow.y - boxTopY + coordinates.size.height
                                    headerBottomY = with(density) { relativeY.toDp() }
                                },
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Top Up Input Card
                            TopUpInputCard(
                                amount = amount,
                                textFieldValue = textFieldValue,
                                isLoading = isLoadingTopup,
                                onAmountChange = { newValue ->
                                    val oldCursorPosition = textFieldValue.selection.start
                                    val oldFormatted = textFieldValue.text
                                    val digitsBeforeOldCursor = oldFormatted.take(oldCursorPosition).filter { it.isDigit() }.length
                                    val newDigitsOnly = newValue.text.filter { it.isDigit() }
                                    amount = newDigitsOnly
                                    val newFormatted = formatNumberWithDots(newDigitsOnly)
                                    val oldDigitsOnly = oldFormatted.filter { it.isDigit() }
                                    val isTypingAtEnd = oldCursorPosition >= oldFormatted.length - 1
                                    val newCursorPosition = if (isTypingAtEnd && newDigitsOnly.length > oldDigitsOnly.length) {
                                        newFormatted.length
                                    } else {
                                        calculateCursorPosition(newFormatted, digitsBeforeOldCursor.coerceIn(0, newDigitsOnly.length))
                                    }
                                    textFieldValue = TextFieldValue(
                                        text = newFormatted,
                                        selection = TextRange(newCursorPosition)
                                    )
                                    // Clear error saat user mengetik
                                    errorMessage = null
                                },
                                onProceedClick = {
                                    // Validasi
                                    if (amount.isEmpty() || amount.toDoubleOrNull() == null || amount.toDoubleOrNull() == 0.0) {
                                        errorMessage = "Masukkan nominal top up"
                                        return@TopUpInputCard
                                    }
                                    
                                    val amountValue = amount.toDoubleOrNull() ?: 0.0
                                    if (amountValue < 10000) {
                                        errorMessage = "Minimum Top Up: Rp 10.000"
                                        return@TopUpInputCard
                                    }
                                    
                                    // Clear error dan proceed
                                    errorMessage = null
                                    isLoadingTopup = true
                                    
                                    onProceedToPayment(
                                        amountValue,
                                        {
                                            // Topup berhasil
                                            isLoadingTopup = false
                                            // Kosongkan field
                                            textFieldValue = TextFieldValue("")
                                            amount = ""
                                            // Tampilkan success dialog
                                            showSuccessDialog = true
                                            // Refresh data
                                            viewModel.forceRefresh()
                                        },
                                        { errorMsg ->
                                            // Topup gagal
                                            isLoadingTopup = false
                                            errorMessage = errorMsg
                                        },
                                        {
                                            // User cancel/tutup modal sebelum pilih payment method
                                            isLoadingTopup = false
                                            // Tidak perlu clear field atau show error
                                        }
                                    )
                                }
                            )
                            
                            // Riwayat Top Up Header dengan Filter Button
                            TopUpFilterHeader(
                                onFilterClick = { isFilterOpen = !isFilterOpen }
                            )
                        }
                        
                        // Scrollable Transaction List
                        Box(modifier = Modifier
                            .fillMaxSize()
                            .zIndex(0f)
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
                                        TopUpHistoryItem(transaction = transaction)
                                    }
                                }
                            }
                        }
                    }
                    
                    // Bottom Sheet Filter
                    TopUpFilterBottomSheet(
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
        
        // Success Dialog
        if (showSuccessDialog) {
            TopUpSuccessDialog(
                onDismiss = {
                    showSuccessDialog = false
                }
            )
        }
    }
}

@Composable
fun TopUpInputCard(
    amount: String,
    textFieldValue: TextFieldValue,
    isLoading: Boolean = false,
    onAmountChange: (TextFieldValue) -> Unit,
    onProceedClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Masukkan Nominal",
                fontSize = 12.sp,
                color = Color.Black,
                fontWeight = FontWeight.Medium
            )
            
            OutlinedTextField(
                value = textFieldValue,
                onValueChange = onAmountChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("0", color = Color.LightGray) },
                prefix = { Text("Rp ", color = Color.Black) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GreenPrimary,
                    unfocusedBorderColor = Color.LightGray,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )
            
            Button(
                onClick = onProceedClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Lanjut ke Pembayaran",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
            
            Text(
                text = "*Minimum Top Up: Rp 10.000",
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun TopUpFilterHeader(
    onFilterClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Riwayat Top Up",
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

@Composable
fun TopUpHistoryItem(
    transaction: id.xetor.app.data.remote.TransactionHistoryResponse
) {
    // Extract payment method name from description (e.g., "Top Up via Gopay")
    val methodName = transaction.description.substringAfter("via ").trim().takeIf { it.isNotEmpty() } 
        ?: transaction.description.substringAfter("ke ").trim().takeIf { it.isNotEmpty() }
        ?: "Gopay"
    
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
                    tint = Color.Unspecified
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

// Skeleton Components
@Composable
fun TopUpInputCardSkeleton() {
    // Card putih tetap tampil, hanya isinya yang skeleton
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Label skeleton
            SkeletonText(modifier = Modifier.width(120.dp).height(12.dp))
            
            // Input field skeleton
            SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(8.dp)
            )
            
            // Button skeleton
            SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp)
            )
            
            // Helper text skeleton
            SkeletonText(modifier = Modifier.width(180.dp).height(11.dp))
        }
    }
}

@Composable
fun TopUpHistoryItemSkeleton() {
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
            // Icon payment skeleton - kotak dengan rounded corners (mengikuti bentuk asli)
            SkeletonBox(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(8.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                SkeletonText(modifier = Modifier.width(80.dp).height(14.dp))
                SkeletonText(modifier = Modifier.width(120.dp).height(11.dp))
            }
        }
        SkeletonText(modifier = Modifier.width(80.dp).height(14.dp))
    }
}

// Filter Bottom Sheet (mirip WithdrawScreen)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopUpFilterBottomSheet(
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
    var rawDragOffset by remember { mutableStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var shouldDismiss by remember { mutableStateOf(false) }
    var dismissStartOffset by remember { mutableStateOf(0f) }
    var isDismissingFromClick by remember { mutableStateOf(false) }
    
    val configuration = LocalConfiguration.current
    val screenHeight = with(density) { 
        configuration.screenHeightDp.dp.toPx() 
    }
    val dismissThreshold = screenHeight * 0.15f
    
    val dismissAnimatable = remember { Animatable(0f) }
    
    LaunchedEffect(shouldDismiss, dismissStartOffset, isDismissingFromClick) {
        if (shouldDismiss) {
            val startOffset = if (isDismissingFromClick) 0f else dismissStartOffset.coerceAtLeast(0f)
            dismissAnimatable.snapTo(startOffset)
            dismissAnimatable.animateTo(
                targetValue = screenHeight,
                animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
            )
        } else if (!shouldDismiss) {
            dismissAnimatable.snapTo(0f)
        }
    }
    
    val dismissAnimatedOffset = dismissAnimatable.value
    
    val snapBackAnimatedOffset by animateFloatAsState(
        targetValue = 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
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
    
    LaunchedEffect(selectedDateRange, selectedStatus) {
        tempDateRange = selectedDateRange
        tempStatus = selectedStatus
    }
    
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
    
    val handleDismiss = {
        if (!shouldDismiss && isOpen) {
            isDismissingFromClick = true
            dismissStartOffset = 0f
            shouldDismiss = true
        }
    }
    
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
    
    Box(modifier = Modifier.fillMaxSize()) {
        if (isOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(1f)
                    .pointerInput(isOpen) {
                        if (isOpen) {
                            detectTapGestures {
                                handleDismiss()
                            }
                        }
                    }
            )
        }
        
        if (isOpen && shouldDismiss) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .zIndex(2f)
                    .fillMaxWidth()
                    .graphicsLayer {
                        this.translationY = currentOffset
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
                TopUpFilterContent(
                    tempDateRange = tempDateRange,
                    tempStatus = tempStatus,
                    onDateRangeChange = { tempDateRange = it },
                    onStatusChange = { tempStatus = it },
                    onReset = {
                        onDateRangeChange(DateRangeFilter.ALL)
                        onStatusChange(StatusFilter.ALL)
                        handleDismiss()
                    },
                    onApply = {
                        onDateRangeChange(tempDateRange)
                        onStatusChange(tempStatus)
                        handleDismiss()
                    }
                )
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
                        this.translationY = currentOffset
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
                TopUpFilterContent(
                    tempDateRange = tempDateRange,
                    tempStatus = tempStatus,
                    onDateRangeChange = { tempDateRange = it },
                    onStatusChange = { tempStatus = it },
                    onReset = {
                        onDateRangeChange(DateRangeFilter.ALL)
                        onStatusChange(StatusFilter.ALL)
                        handleDismiss()
                    },
                    onApply = {
                        onDateRangeChange(tempDateRange)
                        onStatusChange(tempStatus)
                        handleDismiss()
                    }
                )
            }
        }
    }
}

@Composable
fun TopUpFilterContent(
    tempDateRange: DateRangeFilter,
    tempStatus: StatusFilter,
    onDateRangeChange: (DateRangeFilter) -> Unit,
    onStatusChange: (StatusFilter) -> Unit,
    onReset: () -> Unit,
    onApply: () -> Unit
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
                text = "Riwayat Top Up",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
            
            IconButton(
                onClick = onReset,
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
                        onClick = { onDateRangeChange(filter) }
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
                        onClick = { onStatusChange(filter) }
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
                onClick = onReset,
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
                onClick = onApply,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) {
                Text("Pakai", fontSize = 14.sp, color = Color.White)
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
        var cleanedTimestamp = timestamp.replace(" ", "")
        cleanedTimestamp = cleanedTimestamp.replace(Regex("\\.\\d{6}"), { matchResult ->
            "." + matchResult.value.substring(1, 4)
        })
        
        if (cleanedTimestamp.endsWith("Z")) {
            cleanedTimestamp = cleanedTimestamp.replace("Z", "+0000")
        }
        
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault())
        val outputFormat = SimpleDateFormat("d MMM yyyy 'at' HH:mm", Locale("id", "ID"))
        
        val date = inputFormat.parse(cleanedTimestamp)
        date?.let { outputFormat.format(it) } ?: timestamp
    } catch (e: Exception) {
        try {
            var cleanedTimestamp = timestamp.replace(" ", "")
            cleanedTimestamp = cleanedTimestamp.replace(Regex("\\.\\d+"), "")
            
            if (cleanedTimestamp.endsWith("Z")) {
                cleanedTimestamp = cleanedTimestamp.replace("Z", "+0000")
            }
            
            val inputFormatNoMillis = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault())
            val outputFormat = SimpleDateFormat("d MMM yyyy 'at' HH:mm", Locale("id", "ID"))
            val date = inputFormatNoMillis.parse(cleanedTimestamp)
            date?.let { outputFormat.format(it) } ?: timestamp
        } catch (e2: Exception) {
            timestamp
        }
    }
}

private fun formatNumberWithDots(value: String): String {
    if (value.isEmpty()) return ""
    val digitsOnly = value.filter { it.isDigit() }
    if (digitsOnly.isEmpty()) return ""
    val reversed = digitsOnly.reversed()
    val result = StringBuilder()
    for (i in reversed.indices) {
        if (i > 0 && i % 3 == 0) {
            result.append(".")
        }
        result.append(reversed[i])
    }
    return result.toString().reversed()
}

private fun calculateCursorPosition(
    formatted: String,
    digitCountBeforeCursor: Int
): Int {
    if (digitCountBeforeCursor <= 0) {
        return 0
    }
    if (digitCountBeforeCursor >= formatted.filter { it.isDigit() }.length) {
        return formatted.length
    }
    var digitIndex = 0
    for (i in formatted.indices) {
        if (formatted[i].isDigit()) {
            digitIndex++
            if (digitIndex >= digitCountBeforeCursor) {
                return i + 1
            }
        }
    }
    return formatted.length
}

@Composable
fun TopUpSuccessDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
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
                // Check Icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(GreenPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_check),
                        contentDescription = "Success",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }

                // Title
                Text(
                    text = "Top Up Berhasil",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = GreenPrimary
                )

                // Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GreenPrimary)
                ) {
                    Text(
                        text = "Tutup",
                        color = GreenPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

