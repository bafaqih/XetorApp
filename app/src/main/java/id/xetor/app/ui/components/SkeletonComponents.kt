// SkeletonComponents.kt
package id.xetor.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight

/**
 * Base skeleton dengan shimmer effect
 */
@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton_shimmer")
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_offset"
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFE0E0E0),
                        Color(0xFFF5F5F5),
                        Color(0xFFE0E0E0)
                    ),
                    start = Offset(shimmerOffset - 300f, shimmerOffset - 300f),
                    end = Offset(shimmerOffset, shimmerOffset)
                )
            )
    )
}

/**
 * Skeleton untuk text
 */
@Composable
fun SkeletonText(
    modifier: Modifier = Modifier,
    height: Dp = 16.dp
) {
    SkeletonBox(
        modifier = modifier.height(height)
    )
}

/**
 * Skeleton untuk card
 */
@Composable
fun SkeletonCard(
    modifier: Modifier = Modifier,
    height: Dp? = null
) {
    Card(
        modifier = modifier.then(
            height?.let { Modifier.height(it) } ?: Modifier
        ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        SkeletonBox(
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Skeleton untuk circular (avatar, icon)
 */
@Composable
fun SkeletonCircle(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    SkeletonBox(
        modifier = modifier.size(size),
        shape = CircleShape
    )
}

/**
 * Note: HomeScreen skeleton sudah di-inline di HomeScreen.kt
 * Hanya bagian dinamis yang menggunakan skeleton, elemen statis tetap tampil normal
 */

/**
 * Skeleton untuk HomeScreen - Banner
 */
@Composable
fun HomeBannerSkeleton() {
    SkeletonBox(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        shape = RoundedCornerShape(12.dp)
    )
}

/**
 * Skeleton untuk HomeScreen - Statistic Cards (hanya nilai dinamis, label & icon tetap tampil)
 * Note: Label dan icon akan ditampilkan di HomeScreen, skeleton hanya untuk nilai
 */
@Composable
fun HomeStatisticCardSkeleton() {
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
                    // Label dan icon akan ditampilkan di HomeScreen (statis)
                    Spacer(modifier = Modifier.weight(1f))
                    Column {
                        // Nilai dinamis - skeleton
                        SkeletonText(modifier = Modifier.width(60.dp).height(22.dp))
                        // Unit akan ditampilkan di HomeScreen (statis)
                    }
                }
            }
        }
    }
}

/**
 * Skeleton untuk WithdrawScreen - Saldo Card
 */
@Composable
fun WithdrawSaldoCardSkeleton() {
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
                SkeletonText(modifier = Modifier.width(80.dp).height(12.dp))
                Spacer(modifier = Modifier.height(4.dp))
                SkeletonText(modifier = Modifier.width(150.dp).height(24.dp))
            }
            SkeletonCircle(size = 48.dp)
        }
    }
}

/**
 * Skeleton untuk WithdrawScreen - Payment Methods Grid
 */
@Composable
fun WithdrawPaymentMethodsSkeleton() {
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
            repeat(2) { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    repeat(4) {
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            SkeletonBox(
                                modifier = Modifier
                                    .size(56.dp),
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
}

/**
 * Skeleton untuk WithdrawScreen - Transaction History Item
 */
@Composable
fun WithdrawHistoryItemSkeleton() {
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
        SkeletonText(modifier = Modifier.width(100.dp).height(14.dp))
    }
}

