// app/src/main/java/id/xetor/app/ui/scan/PreviewScreen.kt
package id.xetor.app.ui.scan

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import id.xetor.app.R
import id.xetor.app.ui.theme.GreenPrimary

@Composable
fun PreviewScreen(
    imageBitmap: Bitmap?,
    onBackClick: () -> Unit,
    onRetakeClick: () -> Unit,
    onConfirmClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, top = 16.dp, end = 16.dp, bottom = 16.dp), // Padding kiri 4dp seperti TopAppBar
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_back),
                        contentDescription = "Back",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Spacer setelah top bar
            Spacer(modifier = Modifier.weight(1f))

            // Title - Center
            Text(
                text = "Scanning Result",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            // Preview Image - Aspect ratio 4:5 (tanpa border)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp) // Tambah padding horizontal agar tidak melebihi
                    .aspectRatio(4f / 5f) // 4:5 aspect ratio
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF5F5F5))
            ) {
                if (imageBitmap != null) {
                    Image(
                        bitmap = imageBitmap.asImageBitmap(),
                        contentDescription = "Preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = GreenPrimary
                        )
                    }
                }
            }

            // Spacer untuk menyeimbangkan space atas dan bawah
            Spacer(modifier = Modifier.weight(1f))

            // Bottom Buttons - Refresh di tengah, Check di kanan
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Retake Text - Dekat dengan button retake
                Text(
                    text = "Ambil ulang gambar",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Buttons Row - Retake di tengah, Check di kanan
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Retake Button (Refresh Icon) - Di tengah
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(72.dp)
                            .clip(CircleShape)
                            .border(3.dp, Color(0xFF424242), CircleShape) // Stroke abu tua seperti shutter
                            .clickable(onClick = onRetakeClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Retake",
                            tint = Color(0xFF424242), // Abu tua
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Confirm Button (Green Check) - Di kanan, geser kiri sedikit
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 8.dp) // Geser kiri sedikit
                    ) {
                        Button(
                            onClick = {
                                isLoading = true
                                // Delay 2 detik untuk loading indicator
                                scope.launch {
                                    delay(2000)
                                    isLoading = false
                                    onConfirmClick()
                                }
                            },
                            modifier = Modifier.size(56.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GreenPrimary
                            ),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Confirm",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }

            // Spacer setelah bottom buttons
            Spacer(modifier = Modifier.weight(1f))
        }
        
        // Loading Overlay - Hitam dengan circular progress hijau
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .zIndex(10f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(64.dp),
                    color = GreenPrimary,
                    strokeWidth = 4.dp
                )
            }
        }
    }
}
