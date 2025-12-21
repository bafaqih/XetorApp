// app/src/main/java/id/xetor/app/ui/conversion/ConversionScreen.kt
package id.xetor.app.ui.conversion

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.launch
import id.xetor.app.R
import id.xetor.app.data.remote.TransactionHistoryResponse
import id.xetor.app.ui.components.*
import id.xetor.app.ui.components.CustomSnackbar
import id.xetor.app.ui.theme.GreenPrimary
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversionScreen(
    viewModel: ConversionViewModel,
    onBackClick: () -> Unit = {},
    onSuccessNavigateBack: () -> Unit = {},
    onSuccess: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val lazyListState = rememberLazyListState()
    var previousTransactionsCount by remember { mutableStateOf(0) }
    
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
    
    // Scroll to top ketika transactions list berubah (setelah konversi berhasil)
    // Hanya scroll jika jumlah transactions bertambah (bukan karena filter berubah)
    LaunchedEffect(uiState.allTransactions.size) {
        val currentCount = uiState.allTransactions.size
        if (currentCount > previousTransactionsCount && currentCount > 0) {
            // Transactions bertambah, scroll to top
            lazyListState.animateScrollToItem(0)
        }
        previousTransactionsCount = currentCount
    }
    
    // Scroll to top ketika filter berubah (date range atau type)
    LaunchedEffect(uiState.selectedDateRange, uiState.selectedTypeFilter) {
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
                title = { Text("Konversi", fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
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
                    onDismiss = { viewModel.clearError() }
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
                // Loading state - Skeleton
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Fixed Top Section - skeleton
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Conversion Type Toggle - skeleton
                        SkeletonBox(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(8.dp)
                        )

                        // Conversion Card - skeleton
                        ConversionCardSkeleton()
                    }

                    // History Header - skeleton
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SkeletonText(modifier = Modifier.width(140.dp).height(16.dp))
                        SkeletonBox(
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape
                        )
                    }

                    // Transaction history skeleton
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        repeat(5) {
                            ConversionHistoryItemSkeleton()
                        }
                    }
                }
            } else {
                // Success state
                var isFilterOpen by remember { mutableStateOf(false) }
                var headerBottomY by remember { mutableStateOf(0.dp) }
                val density = LocalDensity.current
                var boxTopY by remember { mutableStateOf(0f) }
                
                // State untuk swipe gesture
                var dragOffset by remember { mutableStateOf(0f) }
                var verticalDragOffset by remember { mutableStateOf(0f) }
                var isDragging by remember { mutableStateOf(false) }
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .onGloballyPositioned { coordinates ->
                            val positionInWindow = coordinates.localToWindow(Offset.Zero)
                            boxTopY = positionInWindow.y
                        }
                        .pointerInput(uiState.selectedConversionType) {
                            detectHorizontalDragGestures(
                                onDragStart = {
                                    isDragging = true
                                    dragOffset = 0f
                                    verticalDragOffset = 0f
                                },
                                onDragEnd = {
                                    isDragging = false
                                    // Hanya trigger swipe jika gerakan horizontal lebih dominan
                                    val horizontalAbs = kotlin.math.abs(dragOffset)
                                    val verticalAbs = kotlin.math.abs(verticalDragOffset)
                                    
                                    if (horizontalAbs > verticalAbs) {
                                        // Threshold untuk swipe: 30% dari lebar layar
                                        val threshold = size.width * 0.3f
                                        if (dragOffset > threshold) {
                                            // Swipe ke kanan: Xp - Rp -> Rp - Xp
                                            if (uiState.selectedConversionType == ConversionType.XP_TO_RP) {
                                                viewModel.setConversionType(ConversionType.RP_TO_XP)
                                            }
                                        } else if (dragOffset < -threshold) {
                                            // Swipe ke kiri: Rp - Xp -> Xp - Rp
                                            if (uiState.selectedConversionType == ConversionType.RP_TO_XP) {
                                                viewModel.setConversionType(ConversionType.XP_TO_RP)
                                            }
                                        }
                                    }
                                    dragOffset = 0f
                                    verticalDragOffset = 0f
                                },
                                onHorizontalDrag = { change, dragAmount ->
                                    // Track vertical movement juga untuk menentukan apakah horizontal lebih dominan
                                    val verticalChange = change.position.y - (change.previousPosition?.y ?: change.position.y)
                                    verticalDragOffset += verticalChange
                                    
                                    dragOffset += dragAmount
                                    // Hanya consume jika horizontal lebih dominan
                                    if (kotlin.math.abs(dragAmount) > kotlin.math.abs(verticalChange)) {
                                        change.consume()
                                    }
                                }
                            )
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
                            // Conversion Type Toggle
                            ConversionTypeToggle(
                                selectedType = uiState.selectedConversionType,
                                onTypeSelected = { viewModel.setConversionType(it) }
                            )

                            // Conversion Card
                            ConversionCard(
                                conversionType = uiState.selectedConversionType,
                                wallet = uiState.wallet,
                                amount = uiState.amount,
                                onAmountChange = { viewModel.setAmount(it) },
                                isSubmitting = uiState.isSubmitting,
                                onSubmit = { viewModel.submitConversion() }
                            )
                        }
                        
                        // Scrollable History List
                        Box(modifier = Modifier
                            .fillMaxSize()
                            .zIndex(0f)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp)
                            ) {
                                // History Header
                                ConversionFilterHeader(
                                    onFilterClick = { isFilterOpen = !isFilterOpen }
                                )

                                if (uiState.filteredTransactions.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(vertical = 16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Belum ada riwayat konversi",
                                            fontSize = 13.sp,
                                            color = Color.Gray,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                } else {
                                    LazyColumn(
                                        state = lazyListState,
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(bottom = 16.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(
                                            items = uiState.filteredTransactions,
                                            key = { it.id }
                                        ) { transaction ->
                                            ConversionHistoryItem(transaction = transaction)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    // Bottom Sheet Filter
                    ConversionFilterBottomSheet(
                        isOpen = isFilterOpen,
                        onDismiss = { isFilterOpen = false },
                        selectedDateRange = uiState.selectedDateRange,
                        selectedTypeFilter = uiState.selectedTypeFilter,
                        onDateRangeChange = { viewModel.setDateRangeFilter(it) },
                        onTypeFilterChange = { viewModel.setTypeFilter(it) }
                    )
                }
            }

            // Success Dialog
            LaunchedEffect(uiState.showSuccessDialog) {
                if (uiState.showSuccessDialog) {
                    // Trigger refresh home saat konversi berhasil
                    onSuccess()
                }
            }
            
            if (uiState.showSuccessDialog) {
                ConversionSuccessDialog(
                    onDismiss = {
                        viewModel.dismissSuccessDialog()
                    }
                )
            }
        }
    }
}

@Composable
fun ConversionTypeToggle(
    selectedType: ConversionType,
    onTypeSelected: (ConversionType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF5F5F5))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Xp - Rp Option
        Box(
            modifier = Modifier
                .weight(1f)
                .height(40.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(
                    if (selectedType == ConversionType.XP_TO_RP) GreenPrimary else Color.Transparent
                )
                .clickable { onTypeSelected(ConversionType.XP_TO_RP) },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Xp - Rp",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (selectedType == ConversionType.XP_TO_RP) Color.White else Color.Gray
            )
        }

        // Rp - Xp Option
        Box(
            modifier = Modifier
                .weight(1f)
                .height(40.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(
                    if (selectedType == ConversionType.RP_TO_XP) GreenPrimary else Color.Transparent
                )
                .clickable { onTypeSelected(ConversionType.RP_TO_XP) },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Rp - Xp",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (selectedType == ConversionType.RP_TO_XP) Color.White else Color.Gray
            )
        }
    }
}

@Composable
fun ConversionCard(
    conversionType: ConversionType,
    wallet: id.xetor.app.data.remote.WalletResponse?,
    amount: String,
    onAmountChange: (String) -> Unit,
    isSubmitting: Boolean,
    onSubmit: () -> Unit
) {
    // State untuk TextFieldValue dengan cursor position
    var textFieldValue by remember { mutableStateOf(TextFieldValue("")) }
    
    // Initialize atau sync dengan amount
    LaunchedEffect(amount) {
        val formatted = when (conversionType) {
            ConversionType.XP_TO_RP -> formatNumberWithDots(amount)
            ConversionType.RP_TO_XP -> formatCurrencyWithDots(amount)
        }
        if (textFieldValue.text.filter { it.isDigit() } != amount) {
            textFieldValue = TextFieldValue(
                text = formatted,
                selection = TextRange(formatted.length)
            )
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Balance Display
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = when (conversionType) {
                        ConversionType.XP_TO_RP -> "Xpoin Anda"
                        ConversionType.RP_TO_XP -> "Saldo Anda"
                    },
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when (conversionType) {
                        ConversionType.XP_TO_RP -> "${formatNumber(wallet?.xpoin ?: 0)} Xp"
                        ConversionType.RP_TO_XP -> "Rp ${formatCurrency(wallet?.balance ?: "0")}"
                    },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
            }

            Divider(color = Color.LightGray.copy(alpha = 0.3f))

            // Input Amount
            Column {
                Text(
                    text = "Masukkan Nominal",
                    fontSize = 12.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = textFieldValue,
                    onValueChange = { newValue ->
                        val oldCursorPosition = textFieldValue.selection.start
                        val oldFormatted = textFieldValue.text
                        val digitsBeforeOldCursor = oldFormatted.take(oldCursorPosition).filter { it.isDigit() }.length
                        val newDigitsOnly = newValue.text.filter { it.isDigit() }
                        
                        onAmountChange(newDigitsOnly)
                        
                        val newFormatted = when (conversionType) {
                            ConversionType.XP_TO_RP -> formatNumberWithDots(newDigitsOnly)
                            ConversionType.RP_TO_XP -> formatCurrencyWithDots(newDigitsOnly)
                        }
                        
                        val oldDigitsOnly = oldFormatted.filter { it.isDigit() }
                        val isTypingAtEnd = oldCursorPosition >= oldFormatted.length - 1
                        
                        val newCursorPosition = if (isTypingAtEnd && newDigitsOnly.length > oldDigitsOnly.length) {
                            newFormatted.length
                        } else {
                            calculateCursorPosition(
                                formatted = newFormatted,
                                digitCountBeforeCursor = digitsBeforeOldCursor.coerceIn(0, newDigitsOnly.length)
                            )
                        }
                        
                        textFieldValue = TextFieldValue(
                            text = newFormatted,
                            selection = TextRange(newCursorPosition)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { 
                        Text(
                            "0",
                            color = Color.LightGray
                        )
                    },
                    prefix = if (conversionType == ConversionType.RP_TO_XP) {
                        { Text("Rp ", color = Color.Black) }
                    } else null,
                    suffix = if (conversionType == ConversionType.XP_TO_RP) {
                        { Text(" Xp", color = Color.Black) }
                    } else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenPrimary,
                        unfocusedBorderColor = Color.LightGray
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            // Amount Received
            Column {
                Text(
                    text = "Jumlah yang Diterima",
                    fontSize = 12.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = calculateReceivedAmount(conversionType, amount),
                    onValueChange = { },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = Color.LightGray,
                        disabledTextColor = Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            // Convert Button
            Button(
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Konversi",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun ConversionFilterHeader(
    onFilterClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Riwayat Konversi",
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
fun ConversionFilterBottomSheet(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    selectedDateRange: DateRangeFilter,
    selectedTypeFilter: TypeFilter,
    onDateRangeChange: (DateRangeFilter) -> Unit,
    onTypeFilterChange: (TypeFilter) -> Unit
) {
    var tempDateRange by remember { mutableStateOf(selectedDateRange) }
    var tempTypeFilter by remember { mutableStateOf(selectedTypeFilter) }
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    
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
    
    LaunchedEffect(selectedDateRange, selectedTypeFilter) {
        tempDateRange = selectedDateRange
        tempTypeFilter = selectedTypeFilter
    }
    
    LaunchedEffect(isOpen) {
        if (isOpen) {
            tempDateRange = selectedDateRange
            tempTypeFilter = selectedTypeFilter
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
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
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
                FilterBottomSheetContent(
                    tempDateRange = tempDateRange,
                    tempTypeFilter = tempTypeFilter,
                    onDateRangeChange = { tempDateRange = it },
                    onTypeFilterChange = { tempTypeFilter = it },
                    onReset = {
                        onDateRangeChange(DateRangeFilter.ALL)
                        onTypeFilterChange(TypeFilter.ALL)
                        handleDismiss()
                    },
                    onApply = {
                        onDateRangeChange(tempDateRange)
                        onTypeFilterChange(tempTypeFilter)
                        handleDismiss()
                    },
                    onDismiss = handleDismiss
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
                FilterBottomSheetContent(
                    tempDateRange = tempDateRange,
                    tempTypeFilter = tempTypeFilter,
                    onDateRangeChange = { tempDateRange = it },
                    onTypeFilterChange = { tempTypeFilter = it },
                    onReset = {
                        onDateRangeChange(DateRangeFilter.ALL)
                        onTypeFilterChange(TypeFilter.ALL)
                        handleDismiss()
                    },
                    onApply = {
                        onDateRangeChange(tempDateRange)
                        onTypeFilterChange(tempTypeFilter)
                        handleDismiss()
                    },
                    onDismiss = handleDismiss
                )
            }
        }
    }
}

@Composable
fun FilterBottomSheetContent(
    tempDateRange: DateRangeFilter,
    tempTypeFilter: TypeFilter,
    onDateRangeChange: (DateRangeFilter) -> Unit,
    onTypeFilterChange: (TypeFilter) -> Unit,
    onReset: () -> Unit,
    onApply: () -> Unit,
    onDismiss: () -> Unit
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
                text = "Riwayat Konversi",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
            
            IconButton(
                onClick = onDismiss,
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
        
        // Type Filter
        Column {
            Text(
                text = "Jenis",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TypeFilter.values().forEach { filter ->
                    FilterChip(
                        label = filter.label,
                        isSelected = tempTypeFilter == filter,
                        onClick = { onTypeFilterChange(filter) }
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

@Composable
fun ConversionHistoryItem(
    transaction: TransactionHistoryResponse
) {
    val conversionTypeLabel = when (transaction.conversionType) {
        "xp_to_rp" -> "Xp to Rp"
        "rp_to_xp" -> "Rp to Xp"
        else -> "Konversi"
    }
    
    val amountText = when (transaction.conversionType) {
        "xp_to_rp" -> "Rp${formatCurrency(transaction.amount?.getAmount() ?: "0")}"
        "rp_to_xp" -> "${formatNumber(transaction.points?.getPoints() ?: 0)} Xp"
        else -> transaction.description
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
            // Convert Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_convert),
                    contentDescription = "Convert",
                    modifier = Modifier.size(28.dp),
                    tint = GreenPrimary
                )
            }

            // Conversion Info
            Column(
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Text(
                    text = conversionTypeLabel,
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

        Text(
            text = amountText,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = GreenPrimary
        )
    }
}

@Composable
fun ConversionSuccessDialog(
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

                Text(
                    text = "Konversi Berhasil",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = GreenPrimary
                )

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, GreenPrimary)
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

// Skeleton Components
@Composable
fun ConversionCardSkeleton() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SkeletonText(modifier = Modifier.width(80.dp).height(12.dp))
                Spacer(modifier = Modifier.height(4.dp))
                SkeletonText(modifier = Modifier.width(120.dp).height(20.dp))
            }
            SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
            )
            Column {
                SkeletonText(modifier = Modifier.width(120.dp).height(12.dp))
                Spacer(modifier = Modifier.height(8.dp))
                SkeletonBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(8.dp)
                )
            }
            Column {
                SkeletonText(modifier = Modifier.width(140.dp).height(12.dp))
                Spacer(modifier = Modifier.height(8.dp))
                SkeletonBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(8.dp)
                )
            }
            SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp)
            )
        }
    }
}

@Composable
fun ConversionHistoryItemSkeleton() {
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
            // Icon skeleton
            SkeletonBox(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(8.dp)
            )
            
            // Text skeleton
            Column(
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                SkeletonText(modifier = Modifier.width(100.dp).height(14.dp))
                SkeletonText(modifier = Modifier.width(150.dp).height(11.dp))
            }
        }
        SkeletonText(modifier = Modifier.width(80.dp).height(14.dp))
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

private fun formatNumber(value: Int): String {
    return String.format("%,d", value).replace(',', '.')
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

private fun formatCurrencyWithDots(value: String): String {
    return formatNumberWithDots(value)
}

private fun calculateReceivedAmount(conversionType: ConversionType, amount: String): String {
    val amountValue = amount.filter { it.isDigit() }.toDoubleOrNull() ?: 0.0
    return when (conversionType) {
        ConversionType.XP_TO_RP -> {
            // 1 Xp = 5 Rp
            val rp = amountValue * 5.0
            // Pastikan tidak ada desimal, langsung convert ke int
            val rpInt = rp.toInt()
            "Rp ${formatCurrency(rpInt.toString())}"
        }
        ConversionType.RP_TO_XP -> {
            // 1 Rp = 0.2 Xp (5 Rp = 1 Xp)
            val xp = (amountValue / 5.0).toInt()
            "${formatNumber(xp)} Xp"
        }
    }
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

