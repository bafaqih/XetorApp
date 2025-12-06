// app/src/main/java/id/xetor/app/ui/transactionhistory/TransactionHistoryScreen.kt
package id.xetor.app.ui.transactionhistory

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import coil.request.CachePolicy
import id.xetor.app.R
import id.xetor.app.data.remote.ApiConfig
import id.xetor.app.data.remote.TransactionHistoryResponse
import id.xetor.app.ui.components.*
import id.xetor.app.ui.components.CustomSnackbar
import id.xetor.app.ui.theme.GreenPrimary
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen(
    viewModel: TransactionHistoryViewModel,
    onBackClick: () -> Unit = {}
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
    
    // Scroll to top ketika transactions list berubah
    LaunchedEffect(uiState.allTransactions.size) {
        val currentCount = uiState.allTransactions.size
        if (currentCount > previousTransactionsCount && currentCount > 0) {
            lazyListState.animateScrollToItem(0)
        }
        previousTransactionsCount = currentCount
    }
    
    // Scroll to top ketika filter berubah
    LaunchedEffect(uiState.selectedTransactionType, uiState.selectedDateRange, uiState.selectedStatus) {
        if (uiState.filteredTransactions.isNotEmpty()) {
            lazyListState.animateScrollToItem(0)
        }
    }
    
    // Cek apakah ini initial load
    LaunchedEffect(uiState.allTransactions) {
        val hasData = uiState.allTransactions.isNotEmpty()
        if (hasData && uiState.isLoading) {
            viewModel.setLoading(false)
        }
    }

    var isFilterOpen by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Riwayat Transaksi", fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
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
                    // Filter tabs skeleton - dipisah-pisah seperti di NotificationScreen
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

                    // Transaction history skeleton
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        repeat(5) {
                            TransactionHistoryItemSkeleton()
                        }
                    }
                }
            } else {
                // Success state
                var headerBottomY by remember { mutableStateOf(0.dp) }
                val density = LocalDensity.current
                var boxTopY by remember { mutableStateOf(0f) }
                val scope = rememberCoroutineScope()
                
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
                        // Fixed Top Section - hanya Transaction Type Filter
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
                                }
                        ) {
                            // Transaction Type Filter Buttons
                            TransactionTypeFilterButtons(
                                selectedType = uiState.selectedTransactionType,
                                onTypeSelected = { type ->
                                    // Jika klik filter yang sama dengan yang aktif, scroll to top
                                    if (type == uiState.selectedTransactionType) {
                                        // Scroll to top
                                        scope.launch {
                                            lazyListState.animateScrollToItem(0)
                                        }
                                    } else {
                                        // Jika berbeda, ubah filter
                                        viewModel.setTransactionTypeFilter(type)
                                    }
                                }
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
                                        .padding(horizontal = 16.dp)
                                        .padding(top = 8.dp),
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
                                    contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(
                                        items = uiState.filteredTransactions,
                                        key = { it.id }
                                    ) { transaction ->
                                        TransactionHistoryItem(transaction = transaction)
                                    }
                                }
                            }
                        }
                    }
                    
                    // Bottom Sheet Filter
                    TransactionHistoryFilterBottomSheet(
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
fun TransactionTypeFilterButtons(
    selectedType: TransactionTypeFilter,
    onTypeSelected: (TransactionTypeFilter) -> Unit
) {
    // Horizontal scrollable row of filter buttons
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TransactionTypeFilter.values().forEach { type ->
            FilterChip(
                label = type.label,
                isSelected = selectedType == type,
                onClick = { onTypeSelected(type) }
            )
        }
    }
}

@Composable
fun TransactionHistoryItem(
    transaction: TransactionHistoryResponse
) {
    when (transaction.type) {
        "withdraw" -> WithdrawHistoryItem(transaction)
        "topup" -> TopUpHistoryItem(transaction)
        "transfer" -> TransferHistoryItem(transaction)
        "convert" -> ConversionHistoryItem(transaction)
        "deposit" -> SetorHistoryItem(transaction)
        else -> DefaultHistoryItem(transaction)
    }
}

@Composable
fun WithdrawHistoryItem(
    transaction: TransactionHistoryResponse
) {
    val methodName = transaction.description.substringAfter("ke ").trim().takeIf { it.isNotEmpty() } ?: "Gopay"
    
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

        Text(
            text = "Rp${formatCurrency(transaction.amount?.getAmount() ?: "0")}",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = GreenPrimary
        )
    }
}

@Composable
fun TopUpHistoryItem(
    transaction: TransactionHistoryResponse
) {
    val methodName = transaction.description.substringAfter("via ").trim().takeIf { it.isNotEmpty() } 
        ?: transaction.description.substringAfter("ke ").trim().takeIf { it.isNotEmpty() }
        ?: "Gopay"
    
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

        Text(
            text = "Rp${formatCurrency(transaction.amount?.getAmount() ?: "0")}",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = GreenPrimary
        )
    }
}

@Composable
fun TransferHistoryItem(
    transaction: TransactionHistoryResponse
) {
    val recipientEmail = transaction.description.substringAfter("ke ").trim().takeIf { it.isNotEmpty() } ?: ""
    val recipientName = if (recipientEmail.isNotEmpty()) {
        val namePart = recipientEmail.substringBefore("@")
        namePart.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    } else {
        "Unknown"
    }
    
    val amountValue = transaction.amount?.getAmount() ?: "0"
    val xpoinAmount = amountValue.toDoubleOrNull()?.toInt() ?: 0
    val xpoinAmountText = "$xpoinAmount Xp"

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
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(ApiConfig.DEFAULT_PHOTO_URL)
                        .memoryCachePolicy(CachePolicy.DISABLED)
                        .diskCachePolicy(CachePolicy.DISABLED)
                        .build(),
                    contentDescription = recipientName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                ) {
                    when (painter.state) {
                        is AsyncImagePainter.State.Loading -> {
                            SkeletonBox(
                                modifier = Modifier.fillMaxSize(),
                                shape = CircleShape
                            )
                        }
                        is AsyncImagePainter.State.Error -> {
                            Image(
                                painter = painterResource(id = R.drawable.ic_profile),
                                contentDescription = recipientName,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        else -> {
                            SubcomposeAsyncImageContent()
                        }
                    }
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Text(
                    text = recipientName,
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
            text = xpoinAmountText,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = GreenPrimary
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
fun SetorHistoryItem(
    transaction: TransactionHistoryResponse
) {
    val partner = transaction.partner
    val partnerName = partner?.name?.getString() 
        ?: transaction.description.substringAfter("ke ").takeIf { it.isNotEmpty() && it != transaction.description }
        ?: "Mitra"
    
    val partnerPhotoUrl = partner?.photo?.getString()
    val photoUrl = if (partnerPhotoUrl != null && partnerPhotoUrl.isNotEmpty()) {
        if (partnerPhotoUrl.startsWith("http")) {
            partnerPhotoUrl
        } else {
            "${ApiConfig.BASE_URL}$partnerPhotoUrl"
        }
    } else {
        ApiConfig.DEFAULT_PHOTO_URL
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
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(photoUrl)
                        .memoryCachePolicy(CachePolicy.DISABLED)
                        .diskCachePolicy(CachePolicy.DISABLED)
                        .build(),
                    contentDescription = partnerName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                ) {
                    when (painter.state) {
                        is AsyncImagePainter.State.Loading -> {
                            SkeletonBox(
                                modifier = Modifier.fillMaxSize(),
                                shape = CircleShape
                            )
                        }
                        is AsyncImagePainter.State.Error -> {
                            Image(
                                painter = painterResource(id = R.drawable.ic_profile),
                                contentDescription = partnerName,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        else -> {
                            SubcomposeAsyncImageContent()
                        }
                    }
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Text(
                    text = partnerName,
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
            text = "${formatNumber(transaction.points?.getPoints() ?: 0)} Xp",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = GreenPrimary
        )
    }
}

@Composable
fun DefaultHistoryItem(
    transaction: TransactionHistoryResponse
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = transaction.description,
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

        Text(
            text = transaction.amount?.getAmount() ?: "0",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = GreenPrimary
        )
    }
}

@Composable
fun TransactionHistoryItemSkeleton() {
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
            SkeletonBox(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(8.dp)
            )
            Column {
                SkeletonText(modifier = Modifier.width(80.dp).height(14.dp))
                Spacer(modifier = Modifier.height(4.dp))
                SkeletonText(modifier = Modifier.width(120.dp).height(11.dp))
            }
        }
        SkeletonText(modifier = Modifier.width(80.dp).height(14.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryFilterBottomSheet(
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
    tempStatus: StatusFilter,
    onDateRangeChange: (DateRangeFilter) -> Unit,
    onStatusChange: (StatusFilter) -> Unit,
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
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Riwayat Transaksi",
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

private fun formatNumber(value: Int): String {
    return String.format("%,d", value).replace(',', '.')
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

