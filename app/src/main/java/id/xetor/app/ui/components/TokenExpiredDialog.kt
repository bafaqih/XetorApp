// TokenExpiredDialog.kt
package id.xetor.app.ui.components

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import id.xetor.app.SignInActivity
import id.xetor.app.auth.TokenExpiredManager
import id.xetor.app.data.local.UserPreferences
import id.xetor.app.ui.theme.GreenPrimary
import kotlinx.coroutines.launch

/**
 * Dialog untuk menampilkan notifikasi token expired
 * Muncul otomatis ketika token expired terdeteksi
 */
@Composable
fun TokenExpiredDialog(
    context: android.content.Context,
    userPreferences: UserPreferences,
    onDismiss: () -> Unit = {}
) {
    // Akses langsung ke state di TokenExpiredManager (reactive karena menggunakan mutableStateOf)
    val isTokenExpired = TokenExpiredManager.isTokenExpired
    val isDialogShown = TokenExpiredManager.isDialogShown
    val scope = rememberCoroutineScope()

    // Tampilkan dialog jika token expired dan belum ditampilkan
    if (isTokenExpired && !isDialogShown) {
        Dialog(onDismissRequest = { /* Tidak bisa dismiss dengan tap outside */ }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Icon atau emoji
                    Text(
                        text = "⏰",
                        fontSize = 48.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Title
                    Text(
                        text = "Sesi Telah Berakhir",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Message
                    Text(
                        text = "Sesi telah berakhir, harap masuk kembali",
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Button Masuk Kembali
                    Button(
                        onClick = {
                            scope.launch {
                                // Clear token
                                userPreferences.clearAuthToken()
                                // Reset state
                                TokenExpiredManager.reset()
                                // Navigate to SignIn
                                val intent = Intent(context, SignInActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                context.startActivity(intent)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GreenPrimary
                        )
                    ) {
                        Text("Masuk Kembali")
                    }
                }
            }
        }

        // Mark dialog as shown
        LaunchedEffect(Unit) {
            TokenExpiredManager.markDialogShown()
        }
    }
}

