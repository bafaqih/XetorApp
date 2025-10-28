// app/src/main/java/id/xetor/app/OnBoardingScreen.kt
package id.xetor.app

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xetor.app.ui.theme.GreenPrimary
import id.xetor.app.ui.theme.XetorAppTheme
import kotlinx.coroutines.launch

// Data class untuk setiap item onboarding
data class OnBoardingItem(val title: String, val description: String)

// Daftar item yang akan ditampilkan
private val onBoardingItems = listOf(
    OnBoardingItem(
        "Kenali Sampah dengan Cerdas, Cepat, & Tepat",
        "Gunakan teknologi AI untuk memindai sampah, mengetahui jenisnya, nilai poinnya, dan cara pengolahannya."
    ),
    OnBoardingItem(
        "Setor Sampah, Dapatkan Poin & Tukarkan Saldo",
        "Setiap sampah yang kamu setor berubah jadi poin digital, yang bisa ditukar jadi saldo, belanja di marketplace, atau bayar transportasi umum."
    ),
    OnBoardingItem(
        "Bersama Ciptakan Lingkungan Lebih Baik",
        "Gabung komunitas hijau, dukung UMKM ramah lingkungan, dan ikut serta dalam gerakan daur ulang untuk bumi yang lebih bersih."
    )
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnBoardingScreen(onStartedClick: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { onBoardingItems.size })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Pager untuk menampilkan halaman-halaman onboarding
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f) // Mengisi sisa ruang
        ) { page ->
            OnBoardingPage(item = onBoardingItems[page])
        }

        // Indikator titik
        DotsIndicator(
            totalDots = onBoardingItems.size,
            selectedIndex = pagerState.currentPage
        )

        // Tombol navigasi di bagian bawah
        BottomButtons(
            currentPage = pagerState.currentPage,
            pageCount = onBoardingItems.size,
            onNextClick = {
                scope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                }
            },
            onStartClick = onStartedClick,
            onSkipClick = onStartedClick
        )
    }
}

// Composable untuk satu halaman onboarding (hanya teks)
@Composable
fun OnBoardingPage(item: OnBoardingItem) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = item.title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = item.description,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = Color.Gray
        )
    }
}

// Composable untuk indikator titik
@Composable
fun DotsIndicator(totalDots: Int, selectedIndex: Int) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp) // Jarak dari teks dan tombol
    ) {
        for (i in 0 until totalDots) {
            val isSelected = i == selectedIndex
            val color = if (isSelected) GreenPrimary else Color.LightGray
            val width = if (isSelected) 26.dp else 6.dp
            val height = 6.dp

            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(width = width, height = height)
                    .clip(RoundedCornerShape(50)) // Membuat sudut melengkung
                    .background(color)
            )
        }
    }
}

// Composable untuk tombol-tombol di bagian bawah
@Composable
fun BottomButtons(
    currentPage: Int,
    pageCount: Int,
    onNextClick: () -> Unit,
    onStartClick: () -> Unit,
    onSkipClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        // Tombol "Yuk Mulai" yang hanya muncul di halaman terakhir
        if (currentPage == pageCount - 1) {
            Button(
                onClick = onStartClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) {
                Text(text = "Yuk Mulai", fontSize = 16.sp, color = Color.White)
            }
        } else {
            // Tombol "Skip" di kiri
            Text(
                text = "Skip",
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 16.dp)
                    .clickable { onSkipClick() },
                color = Color.Gray,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            // Tombol Panah "Next" di kanan
            IconButton(
                onClick = onNextClick,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(GreenPrimary)
                    .align(Alignment.CenterEnd)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_forward), // Kita akan buat drawable ini
                    contentDescription = "Next",
                    tint = Color.White
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OnBoardingScreenPreview() {
    XetorAppTheme {
        Surface {
            OnBoardingScreen(onStartedClick = {})
        }
    }
}