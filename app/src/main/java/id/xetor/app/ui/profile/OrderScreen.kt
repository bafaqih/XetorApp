// app/src/main/java/id/xetor/app/ui/profile/OrderScreen.kt
package id.xetor.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xetor.app.R
import id.xetor.app.ui.components.SkeletonBox
import id.xetor.app.ui.components.SkeletonText
import id.xetor.app.ui.theme.GreenPrimary

// Order status filter enum
enum class OrderStatusFilter(val label: String) {
    BELUM_BAYAR("Belum Bayar"),
    DIKEMAS("Dikemas"),
    DIKIRIM("Dikirim"),
    SELESAI("Selesai"),
    PENGEMBALIAN("Pengembalian"),
    DIBATALKAN("Dibatalkan")
}

// Order data model (dummy data)
data class OrderItem(
    val id: String,
    val orderNumber: String,
    val productName: String,
    val status: OrderStatusFilter,
    val dateTime: String,
    val totalPrice: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderScreen(
    onBackClick: () -> Unit = {},
    isLoading: Boolean = false
) {
    // State untuk filter
    var selectedStatus by remember { mutableStateOf<OrderStatusFilter?>(null) }
    
    // Temporary mock data - dummy data seperti notifikasi
    val mockOrders = remember {
        listOf<OrderItem>(
            // Empty list untuk testing empty state
        )
    }
    
    // Filter orders berdasarkan status
    val filteredOrders = remember(mockOrders, selectedStatus) {
        if (selectedStatus == null) {
            mockOrders
        } else {
            mockOrders.filter { it.status == selectedStatus }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Pesanan Saya", 
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
                    )                     {
                        repeat(6) {
                            SkeletonBox(
                                modifier = Modifier
                                    .height(36.dp)
                                    .width(80.dp),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                    
                    // Order cards skeleton
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(5) {
                            OrderCardSkeleton()
                        }
                    }
                } else {
                    // Horizontal scrollable filter tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OrderStatusFilter.values().forEach { filter ->
                            OrderFilterChip(
                                label = filter.label,
                                isSelected = selectedStatus == filter,
                                onClick = { 
                                    selectedStatus = if (selectedStatus == filter) null else filter
                                }
                            )
                        }
                    }
                    
                    // Order list
                    if (filteredOrders.isEmpty()) {
                        // Empty state
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Yahh.... Belum ada pesanan terbaru.",
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
                                items = filteredOrders,
                                key = { it.id }
                            ) { order ->
                                OrderCard(order = order)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrderFilterChip(
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
fun OrderCard(
    order: OrderItem
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Order number and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.orderNumber,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Text(
                    text = order.status.label,
                    fontSize = 12.sp,
                    color = GreenPrimary,
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Product name
            Text(
                text = order.productName,
                fontSize = 14.sp,
                color = Color.Black
            )
            
            // Date and total price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.dateTime,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = order.totalPrice,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
fun OrderCardSkeleton() {
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

