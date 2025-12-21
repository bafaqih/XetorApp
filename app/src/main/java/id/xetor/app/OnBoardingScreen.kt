// app/src/main/java/id/xetor/app/OnBoardingScreen.kt
package id.xetor.app

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
        Box(
            modifier = Modifier.weight(1f) // Mengisi sisa ruang
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                OnBoardingPage(item = onBoardingItems[page])
            }
            
            // Skip button di pojok kanan atas (hanya tampil di halaman 1 dan 2)
            if (pagerState.currentPage < onBoardingItems.size - 1) {
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                Text(
                    text = "Skip",
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .clickable(
                            onClick = onStartedClick,
                            interactionSource = interactionSource,
                            indication = null
                        ),
                    color = if (isPressed) Color.DarkGray else Color.Gray,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Indikator titik dengan spacing yang cukup dari button
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
            onStartClick = onStartedClick
        )
    }
}

// Composable untuk satu halaman onboarding (hanya teks)
@Composable
fun OnBoardingPage(item: OnBoardingItem) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp)
    ) {
        // Grup: Placeholder ilustrasi + Text - diturunkan lebih banyak agar lebih dekat ke dot indicator
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = 60.dp), // Geser ke bawah agar lebih dekat ke dot indicator
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Placeholder ilustrasi - kotak abu-abu dengan rounded corner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.LightGray)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
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
}

// Composable untuk indikator titik
@Composable
fun DotsIndicator(
    totalDots: Int,
    selectedIndex: Int
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 40.dp) // Spacing yang cukup dari button agar tidak tertutup
    ) {
        for (i in 0 until totalDots) {
            val isSelected = i == selectedIndex
            val color = if (isSelected) GreenPrimary else Color.LightGray

            Box(
                modifier = Modifier
                    .size(6.dp) // Ukuran sama untuk active dan inactive (sama dengan banner)
                    .clip(CircleShape)
                    .background(color)
            )
            if (i < totalDots - 1) {
                Spacer(modifier = Modifier.width(6.dp))
            }
        }
    }
}

// Composable untuk tombol-tombol di bagian bawah
@Composable
fun BottomButtons(
    currentPage: Int,
    pageCount: Int,
    onNextClick: () -> Unit,
    onStartClick: () -> Unit
) {
    // Tombol "Yuk Mulai" yang hanya muncul di halaman terakhir
    if (currentPage == pageCount - 1) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Button(
                onClick = onStartClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) {
                Text(text = "Yuk Mulai", fontSize = 16.sp, color = Color.White)
            }
        }
    } else {
        // Tombol Panah "Next" di kanan dengan padding yang sama dengan Yuk Mulai
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            IconButton(
                onClick = onNextClick,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(GreenPrimary)
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