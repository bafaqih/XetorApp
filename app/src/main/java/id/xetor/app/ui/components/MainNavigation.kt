// app/src/main/java/id/xetor/app/ui/components/MainNavigation.kt
package id.xetor.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import id.xetor.app.R // Pastikan R di-import dari package aplikasimu
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
            horizontalArrangement = Arrangement.SpaceEvenly, // Jarak antar ikon lebih rapat
            verticalAlignment = Alignment.CenterVertically
        ) {
            StandardBottomNavItem(navItems[0], currentRoute == navItems[0].route) { onItemSelected(navItems[0].route) }
            StandardBottomNavItem(navItems[1], currentRoute == navItems[1].route) { onItemSelected(navItems[1].route) }
            Spacer(modifier = Modifier.width(68.dp)) // Beri ruang untuk FAB yang lebih besar
            StandardBottomNavItem(navItems[2], currentRoute == navItems[2].route) { onItemSelected(navItems[2].route) }
            StandardBottomNavItem(navItems[3], currentRoute == navItems[3].route) { onItemSelected(navItems[3].route) }
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
            .fillMaxHeight()
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .height(40.dp) // Beri tinggi yang tetap
                .clip(RoundedCornerShape(50))
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