// app/src/main/java/id/xetor/app/ui/withdraw/WithdrawScreen.kt
package id.xetor.app.ui.withdraw

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import id.xetor.app.R
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
    
    // Auto refresh saat screen kembali dari detail (onResume)
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

    // Payment methods list - Gopay only untuk sekarang (besok fix backend validation)
    val paymentMethods = remember {
        listOf(
            PaymentMethod(1, "Gopay", R.drawable.ic_gopay, isAvailable = true),
            PaymentMethod(2, "ShopeePay", R.drawable.ic_spay, isAvailable = false),
            PaymentMethod(3, "Dana", R.drawable.ic_dana, isAvailable = false),
            PaymentMethod(4, "OVO", R.drawable.ic_ovo, isAvailable = false),
            PaymentMethod(5, "LinkAja", R.drawable.ic_linkaja, isAvailable = false),
            PaymentMethod(6, "BCA", R.drawable.ic_bca, isAvailable = false),
            PaymentMethod(7, "BRI", R.drawable.ic_bri, isAvailable = false),
            PaymentMethod(8, "BNI", R.drawable.ic_bni, isAvailable = false),
            PaymentMethod(9, "Mandiri", R.drawable.ic_mandiri, isAvailable = false),
            PaymentMethod(10, "BSI", R.drawable.ic_bsi, isAvailable = false)
        )
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
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
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
                // Success state - Split: Fixed top + Scrollable list
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Fixed Top Section (tidak ikut scroll)
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Saldo Card
                        SaldoCard(
                            saldo = uiState.wallet?.balance ?: "0",
                            onTopUpClick = onTopUpClick
                        )

                        Spacer(modifier = Modifier.height(1.dp))

                        // Payment Methods Grid
                        PaymentMethodsGrid(
                            paymentMethods = paymentMethods,
                            onMethodClick = onPaymentMethodClick
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Riwayat Withdraw Header dengan Filters
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
                            
                            WithdrawFiltersCompact(
                                selectedDateRange = uiState.selectedDateRange,
                                selectedStatus = uiState.selectedStatus,
                                onDateRangeChange = { viewModel.setDateRangeFilter(it) },
                                onStatusChange = { viewModel.setStatusFilter(it) }
                            )
                        }
                    }

                    // Scrollable Transaction List Only
                    if (uiState.filteredTransactions.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
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
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(0.dp)
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
            // Row 1: First 5 methods
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                paymentMethods.take(5).forEach { method ->
                    PaymentMethodItem(
                        method = method,
                        onClick = { onMethodClick(method) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Row 2: Last 5 methods
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                paymentMethods.drop(5).forEach { method ->
                    PaymentMethodItem(
                        method = method,
                        onClick = { onMethodClick(method) },
                        modifier = Modifier.weight(1f)
                    )
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
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF5F5F5)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = method.iconRes),
                contentDescription = method.name,
                modifier = Modifier.size(32.dp),
                tint = Color.Unspecified  // PNG already has colors
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = method.name,
            fontSize = 10.sp,
            color = Color.Black,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithdrawFiltersCompact(
    selectedDateRange: DateRangeFilter,
    selectedStatus: StatusFilter,
    onDateRangeChange: (DateRangeFilter) -> Unit,
    onStatusChange: (StatusFilter) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Date Range Filter - Compact
        var dateExpanded by remember { mutableStateOf(false) }
        
        Box {
            OutlinedButton(
                onClick = { dateExpanded = !dateExpanded },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.Gray
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text(
                    text = selectedDateRange.label,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                // Arrow: up saat close, down saat open
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_back),
                    contentDescription = null,
                    modifier = Modifier
                        .size(14.dp)
                        .graphicsLayer {
                            rotationZ = if (dateExpanded) 90f else -90f  // 90 = down, -90 = up
                        }
                )
            }
            
            DropdownMenu(
                expanded = dateExpanded,
                onDismissRequest = { dateExpanded = false },
                modifier = Modifier.background(Color.White)
            ) {
                DateRangeFilter.values().forEach { filter ->
                    DropdownMenuItem(
                        text = { Text(filter.label, fontSize = 12.sp, color = Color.Black) },
                        onClick = {
                            onDateRangeChange(filter)
                            dateExpanded = false
                        },
                        colors = MenuDefaults.itemColors(
                            textColor = Color.Black
                        )
                    )
                }
            }
        }

        // Status Filter - Compact
        var statusExpanded by remember { mutableStateOf(false) }
        
        Box {
            OutlinedButton(
                onClick = { statusExpanded = !statusExpanded },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.Gray
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text(
                    text = selectedStatus.label,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                // Arrow: up saat close, down saat open
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_back),
                    contentDescription = null,
                    modifier = Modifier
                        .size(14.dp)
                        .graphicsLayer {
                            rotationZ = if (statusExpanded) 90f else -90f  // 90 = down, -90 = up
                        }
                )
            }
            
            DropdownMenu(
                expanded = statusExpanded,
                onDismissRequest = { statusExpanded = false },
                modifier = Modifier.background(Color.White)
            ) {
                StatusFilter.values().forEach { filter ->
                    DropdownMenuItem(
                        text = { Text(filter.label, fontSize = 12.sp, color = Color.Black) },
                        onClick = {
                            onStatusChange(filter)
                            statusExpanded = false
                        },
                        colors = MenuDefaults.itemColors(
                            textColor = Color.Black
                        )
                    )
                }
            }
        }
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
            Column {
                Text(
                    text = methodName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Text(
                    text = formatDateTime(transaction.timestamp),
                    fontSize = 11.sp,
                    color = Color.Gray
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

