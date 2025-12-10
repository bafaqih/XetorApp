// app/src/main/java/id/xetor/app/ui/qrcode/QrCodeScreen.kt
package id.xetor.app.ui.qrcode

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import id.xetor.app.R
import id.xetor.app.ui.components.CustomSnackbar
import id.xetor.app.ui.theme.GreenPrimary
import kotlinx.coroutines.delay
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrCodeScreen(
    viewModel: QrCodeViewModel,
    onBackClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    val isLoading = uiState.isLoading
    val token = uiState.token
    val expiresAt = uiState.expiresAt
    
    // Hitung timer LANGSUNG - TIDAK PAKAI REMEMBER, langsung hitung setiap recompose
    // Gunakan state counter untuk trigger recomposition setiap detik
    var tickCounter by remember { mutableStateOf(0L) }
    
    // Set tickCounter LANGSUNG saat expiresAt berubah - lebih cepat dari LaunchedEffect
    // Ini memastikan recomposition terjadi segera tanpa menunggu LaunchedEffect
    DisposableEffect(expiresAt) {
        if (expiresAt != null) {
            tickCounter = System.currentTimeMillis()
        } else {
            tickCounter = 0L
        }
        onDispose { }
    }
    
    // Hitung remainingSeconds LANGSUNG dari System.currentTimeMillis()
    // tickCounter adalah state yang akan trigger recomposition setiap detik
    // Ini akan langsung terhitung setiap kali composable di-recompose, TANPA DELAY
    // tickCounter dibaca untuk memastikan recomposition terjadi saat tickCounter berubah
    val remainingSeconds = if (expiresAt != null) {
        // tickCounter dibaca untuk dependency, tapi tidak digunakan dalam perhitungan
        val tickValue = tickCounter
        val now = System.currentTimeMillis()
        val diff = expiresAt.time - now
        (diff / 1000).coerceAtLeast(0).coerceAtMost(300L)
    } else {
        0L
    }
    
    // Trigger recomposition setiap detik - mulai LANGSUNG TANPA DELAY
    LaunchedEffect(expiresAt) {
        if (expiresAt == null) {
            return@LaunchedEffect
        }
        
        // Update pertama sudah dilakukan di DisposableEffect, jadi langsung lanjut ke loop
        // Update setiap detik - delay SETELAH update pertama
        while (true) {
            delay(1000)
            tickCounter = System.currentTimeMillis() // Trigger recomposition setiap detik
        }
    }
    
    val isActuallyExpired = remainingSeconds <= 0 || token == null
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Button reload di header - disabled jika timer habis
                    IconButton(
                        onClick = { viewModel.generateQrToken() },
                        enabled = !isActuallyExpired
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh QR Code",
                            tint = if (isActuallyExpired) Color.Gray else Color.Black
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
                    onDismiss = { viewModel.clearError() },
                    buttonText = "OK"
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Title
                Text(
                    text = "QR Code mu sudah siap",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Instruction
                Text(
                    text = "Tunjukkan QR Code ini kepada mitra terkait.",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 40.dp)
                )

                // QR Code or Refresh Button
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(280.dp),
                        color = GreenPrimary
                    )
                } else if (isActuallyExpired) {
                    // Expired state - Show Refresh Button (compact design)
                    // Box dengan tinggi sama seperti QR code (280.dp) untuk konsistensi layout
                    Box(
                        modifier = Modifier.size(280.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = { viewModel.generateQrToken() },
                            enabled = !isLoading,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE0E0E0),
                                contentColor = Color.Black
                            ),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh",
                                    modifier = Modifier.size(20.dp),
                                    tint = Color.Black
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Refresh",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                } else {
                    // Active QR Code
                    val qrBitmap = remember(token) {
                        generateQrCodeBitmap(token ?: "")
                    }
                    
                    if (qrBitmap != null) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "QR Code",
                            modifier = Modifier.size(280.dp)
                        )
                    } else {
                        Text(
                            text = "Gagal membuat QR Code",
                            color = Color.Red,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Expiry text
                Text(
                    text = "QR Code ini akan kadaluwarsa dalam",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Countdown timer - dihitung langsung di UI layer
                val hours = (remainingSeconds / 3600).toInt()
                val minutes = ((remainingSeconds % 3600) / 60).toInt()
                val seconds = (remainingSeconds % 60).toInt()
                val timerText = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)

                Text(
                    text = timerText,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = GreenPrimary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// Non-composable function to generate QR code bitmap
fun generateQrCodeBitmap(text: String): Bitmap? {
    return try {
        val hints = hashMapOf<EncodeHintType, Any>().apply {
            put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H)
            put(EncodeHintType.CHARACTER_SET, "UTF-8")
            put(EncodeHintType.MARGIN, 1)
        }

        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 512, 512, hints)

        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }

        bitmap
    } catch (e: Exception) {
        null
    }
}
