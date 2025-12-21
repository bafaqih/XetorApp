// app/src/main/java/id/xetor/app/SplashScreen.kt
package id.xetor.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.xetor.app.ui.theme.GreenPrimary
import id.xetor.app.ui.theme.XetorAppTheme

@Composable
fun SplashScreen() {
    // Surface adalah container dasar di Material Design.
    // Kita gunakan untuk memberi warna background.
    Surface(
        color = GreenPrimary, // Mengambil warna dari Color.kt
        modifier = Modifier.fillMaxSize()
    ) {
        // Box digunakan untuk menumpuk elemen.
        // Sangat mudah untuk menempatkan item di tengah dengan Box.
        Box(
            contentAlignment = Alignment.Center, // Menempatkan konten di tengah
            modifier = Modifier.fillMaxSize()
        ) {
            Image(
                painter = painterResource(id = R.drawable.icon_xetor_putih),
                contentDescription = "Logo Xetor",
                modifier = Modifier.size(150.dp) // Atur ukuran logo
            )
        }
    }
}

// @Preview digunakan agar kita bisa melihat tampilan UI
// di panel Design Android Studio tanpa harus menjalankan aplikasi.
@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    XetorAppTheme {
        SplashScreen()
    }
}