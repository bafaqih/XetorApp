// app/src/main/java/id/xetor/app/ui/components/MainNavigation.kt
package id.xetor.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import id.xetor.app.R // Pastikan R di-import dari package aplikasimu
import id.xetor.app.data.remote.ApiConfig
import id.xetor.app.ui.theme.GreenPrimary

// Data class kembali menggunakan Int untuk ID resource drawable
data class BottomNavItem(
    val route: String,
    val icon: Int,
    val contentDescription: String
)

@Composable
fun MainBottomBar(
    currentRoute: String,
    onItemSelected: (String) -> Unit,
    profilePhotoUrl: String? = null,
    photoRefreshKey: Int = 0
) {
    // Daftar item menggunakan ikon kustom dari drawable
    val navItems = listOf(
        BottomNavItem("home", R.drawable.ic_home, "Home"),
        BottomNavItem("map", R.drawable.ic_map, "Map"),
        BottomNavItem("marketplace", R.drawable.ic_shop, "Marketplace"),
        BottomNavItem("profile", R.drawable.ic_profile, "Profile")
    )

    BottomAppBar(
        containerColor = Color.White,
        tonalElevation = 0.dp,
        modifier = Modifier
            .height(60.dp)
            .statusBarsPadding() // Tambahkan ini agar tidak tumpang tindih dengan status bar sistem
            .navigationBarsPadding()
            .shadow(elevation = 8.dp) // Kita gunakan shadow manual agar tetap ada efek bayangan
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StandardBottomNavItem(navItems[0], currentRoute == navItems[0].route) { onItemSelected(navItems[0].route) }
            StandardBottomNavItem(navItems[1], currentRoute == navItems[1].route) { onItemSelected(navItems[1].route) }
            // Spacer simetris untuk FAB - width sama di kedua sisi
            Spacer(modifier = Modifier.width(34.dp)) // Spacing kiri FAB
            Spacer(modifier = Modifier.width(34.dp)) // Spacing kanan FAB (sama dengan kiri)
            StandardBottomNavItem(navItems[2], currentRoute == navItems[2].route) { onItemSelected(navItems[2].route) }
            ProfileBottomNavItem(
                isSelected = currentRoute == navItems[3].route,
                profilePhotoUrl = profilePhotoUrl,
                photoRefreshKey = photoRefreshKey,
                onClick = { onItemSelected(navItems[3].route) }
            )
        }
    }
}

@Composable
private fun RowScope.StandardBottomNavItem(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) Color(0xFFF6F6F6) else Color.Transparent
    val contentColor = if (isSelected) GreenPrimary else Color.Gray.copy(alpha = 0.7f)

    Box(
        modifier = Modifier
            .fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .height(40.dp) // Beri tinggi yang tetap
                .clip(RoundedCornerShape(50)) // Clip ripple effect sesuai rounded shape
                .clickable(onClick = onClick) // Pindahkan clickable ke Row yang sudah di-clip
                .background(backgroundColor)
                .padding(horizontal = 20.dp), // Hanya padding horizontal
            verticalAlignment = Alignment.CenterVertically // Ikon akan otomatis di tengah
        ) {
            Icon(
                painter = painterResource(id = item.icon),
                contentDescription = item.contentDescription,
                tint = contentColor,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

@Composable
private fun RowScope.ProfileBottomNavItem(
    isSelected: Boolean,
    profilePhotoUrl: String?,
    photoRefreshKey: Int = 0,
    onClick: () -> Unit
) {
    val borderWidth = if (isSelected) 1.5.dp else 0.dp
    val borderColor = if (isSelected) GreenPrimary else Color.Transparent
    val photoSize = 26.dp // Foto diperkecil
    // Ukuran container tetap sama (termasuk border) agar tidak menggeser menu lain
    val containerSize = 32.dp // Ukuran tetap termasuk border

    Box(
        modifier = Modifier
            .fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {
        // Box luar untuk padding (tanpa background)
        // Hapus efek klik (ripple) dengan indication = null
        Box(
            modifier = Modifier
                .height(40.dp) // Tinggi sama dengan menu lain
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null, // Hapus ripple effect
                    onClick = onClick
                )
                .padding(horizontal = 20.dp), // Padding horizontal seperti menu lain
            contentAlignment = Alignment.Center
        ) {
            // Box untuk border (outline di luar)
            Box(
                modifier = Modifier
                    .size(containerSize)
                    .clip(CircleShape)
                    .border(
                        width = borderWidth,
                        color = borderColor,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Box dalam untuk foto profil
                Box(
                    modifier = Modifier
                        .size(photoSize)
                        .clip(CircleShape)
                ) {
                    // Jika profilePhotoUrl null, tampilkan loading
                    // Jika ada URL, load image dengan SubcomposeAsyncImage
                    if (profilePhotoUrl == null) {
                        // Loading state: circular progress indicator hijau
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = GreenPrimary,
                                strokeWidth = 2.dp
                            )
                        }
                    } else {
                        // Build full URL with cache busting
                        val fullUrl = if (profilePhotoUrl.startsWith("http")) {
                            profilePhotoUrl
                        } else {
                            "${ApiConfig.BASE_URL}$profilePhotoUrl"
                        }
                        
                        // Smart cache busting: only add parameter when photoRefreshKey changes (new upload)
                        // Remember parameter based on photoRefreshKey, so same photoRefreshKey uses same URL (for Coil memory cache)
                        val urlWithCacheBust = remember(profilePhotoUrl, photoRefreshKey) {
                            if (photoRefreshKey > 0) {
                                // Use photoRefreshKey as part of URL to ensure consistency
                                // Same photoRefreshKey = same URL = Coil memory cache works
                                "$fullUrl?refresh=$photoRefreshKey"
                            } else {
                                // Use normal URL when no refresh needed
                                fullUrl
                            }
                        }
                        
                        SubcomposeAsyncImage(
                            model = urlWithCacheBust,
                            contentDescription = "Profile",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        ) {
                            when (painter.state) {
                                is AsyncImagePainter.State.Loading -> {
                                    // Loading state: circular progress indicator hijau
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            color = GreenPrimary,
                                            strokeWidth = 2.dp
                                        )
                                    }
                                }
                                is AsyncImagePainter.State.Error -> {
                                    // Error state: fallback ke default icon (hanya jika error)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.LightGray),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_profile),
                                            contentDescription = "Profile",
                                            tint = Color.Gray,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                else -> {
                                    // Success state: tampilkan foto profil
                                    SubcomposeAsyncImageContent()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}