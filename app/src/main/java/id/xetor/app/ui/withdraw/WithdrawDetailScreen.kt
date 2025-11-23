// app/src/main/java/id/xetor/app/ui/withdraw/WithdrawDetailScreen.kt
package id.xetor.app.ui.withdraw

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import id.xetor.app.R
import id.xetor.app.ui.components.CustomSnackbar
import id.xetor.app.ui.components.SkeletonBox
import id.xetor.app.ui.components.SkeletonCircle
import id.xetor.app.ui.components.SkeletonText
import id.xetor.app.ui.theme.GreenPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithdrawDetailScreen(
    viewModel: WithdrawDetailViewModel,
    paymentMethod: PaymentMethod,
    onBackClick: () -> Unit = {},
    onSuccessNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // State untuk TextFieldValue dengan cursor position
    var textFieldValue by remember { mutableStateOf(TextFieldValue("")) }
    
    // Initialize atau sync dengan uiState.amount (hanya jika berbeda)
    LaunchedEffect(uiState.amount) {
        val formatted = formatNumberWithDots(uiState.amount)
        // Hanya update jika text berbeda (untuk avoid infinite loop)
        if (textFieldValue.text.filter { it.isDigit() } != uiState.amount) {
            textFieldValue = TextFieldValue(
                text = formatted,
                selection = TextRange(formatted.length)
            )
        }
    }

    // Show error toast
    LaunchedEffect(uiState.errorMessage) {
        // Error will be shown as Snackbar in UI
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Withdraw", fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
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
        },
        snackbarHost = {
            if (uiState.errorMessage != null) {
                CustomSnackbar(
                    message = uiState.errorMessage ?: "",
                    onDismiss = { viewModel.clearError() }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                // Loading state - Semua skeleton kecuali topBar (back button + title)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Payment Method Logo - skeleton
                    SkeletonCircle(size = 120.dp)

                    // Form Card - skeleton
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Saldo Anda - skeleton
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                SkeletonText(modifier = Modifier.width(80.dp).height(12.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                SkeletonText(modifier = Modifier.width(150.dp).height(20.dp))
                            }

                            Divider(color = Color.LightGray.copy(alpha = 0.3f))

                            // Masukkan Nominal - skeleton
                            Column {
                                SkeletonText(modifier = Modifier.width(120.dp).height(12.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                SkeletonBox(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }

                            // Nomor HP / Rekening - skeleton
                            Column {
                                SkeletonText(modifier = Modifier.width(100.dp).height(12.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                SkeletonBox(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }

                            // Nama Pemilik Rekening - skeleton
                            Column {
                                SkeletonText(modifier = Modifier.width(150.dp).height(12.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                SkeletonBox(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }

                            // Withdraw Button - skeleton
                            SkeletonBox(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(8.dp)
                            )

                            // Info - skeleton
                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                SkeletonText(modifier = Modifier.width(180.dp).height(11.dp))
                                SkeletonText(modifier = Modifier.width(120.dp).height(11.dp))
                            }
                        }
                    }
                }
            } else {
                // Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Payment Method Logo (Full, tanpa circle dan nama)
                    Icon(
                        painter = painterResource(id = paymentMethod.iconRes),
                        contentDescription = paymentMethod.name,
                        modifier = Modifier.size(120.dp),
                        tint = Color.Unspecified
                    )

                    // Form Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Saldo Anda (Read-only, centered)
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Saldo Anda",
                                    fontSize = 12.sp,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Rp ${formatCurrency(uiState.wallet?.balance ?: "0")}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Black
                                )
                            }

                            Divider(color = Color.LightGray.copy(alpha = 0.3f))

                            // Masukkan Nominal
                            Column {
                                Text(
                                    text = "Masukkan Nominal",
                                    fontSize = 12.sp,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = textFieldValue,
                                    onValueChange = { newValue ->
                                        // Simpan cursor position dan text sebelum perubahan
                                        val oldCursorPosition = textFieldValue.selection.start
                                        val oldFormatted = textFieldValue.text
                                        
                                        // Hitung berapa digit yang ada sebelum cursor di old formatted text
                                        val digitsBeforeOldCursor = oldFormatted.take(oldCursorPosition).filter { it.isDigit() }.length
                                        
                                        // Extract digits only (remove dots) dari newValue
                                        val newDigitsOnly = newValue.text.filter { it.isDigit() }
                                        
                                        // Update ViewModel
                                        viewModel.setAmount(newDigitsOnly)
                                        
                                        // Format ulang
                                        val newFormatted = formatNumberWithDots(newDigitsOnly)
                                        
                                        // Hitung cursor position baru
                                        // Jika user mengetik di akhir, cursor di akhir
                                        // Jika user mengetik di tengah, cursor di posisi sesuai digit count
                                        val oldDigitsOnly = oldFormatted.filter { it.isDigit() }
                                        val isTypingAtEnd = oldCursorPosition >= oldFormatted.length - 1
                                        
                                        val newCursorPosition = if (isTypingAtEnd && newDigitsOnly.length > oldDigitsOnly.length) {
                                            // User mengetik di akhir: cursor di akhir
                                            newFormatted.length
                                        } else {
                                            // User mengetik di tengah atau menghapus: hitung posisi berdasarkan digit count
                                            calculateCursorPosition(
                                                formatted = newFormatted,
                                                digitCountBeforeCursor = digitsBeforeOldCursor.coerceIn(0, newDigitsOnly.length)
                                            )
                                        }
                                        
                                        textFieldValue = TextFieldValue(
                                            text = newFormatted,
                                            selection = TextRange(newCursorPosition)
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { Text("0", color = Color.LightGray) },
                                    prefix = { Text("Rp ", color = Color.Black) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = GreenPrimary,
                                        unfocusedBorderColor = Color.LightGray
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }

                            // Nomor HP / Rekening
                            Column {
                                val label = if (isEWallet(paymentMethod)) "Nomor ${paymentMethod.name}" else "Nomor Rekening"
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = uiState.accountNumber,
                                    onValueChange = { viewModel.setAccountNumber(it) },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { Text("08...", color = Color.LightGray) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = GreenPrimary,
                                        unfocusedBorderColor = Color.LightGray
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }

                            // Nama Pemilik Rekening (Bank only)
                            if (!isEWallet(paymentMethod)) {
                                Column {
                                    Text(
                                        text = "Nama Pemilik Rekening",
                                        fontSize = 12.sp,
                                        color = Color.Black,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = uiState.accountHolderName,
                                        onValueChange = { viewModel.setAccountHolderName(it) },
                                        modifier = Modifier.fillMaxWidth(),
                                        placeholder = { Text("Nama sesuai rekening", color = Color.LightGray) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = GreenPrimary,
                                            unfocusedBorderColor = Color.LightGray
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                            }

                            // Withdraw Button
                            Button(
                                onClick = { viewModel.submitWithdraw() },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !uiState.isSubmitting,
                                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(vertical = 14.dp)
                            ) {
                                if (uiState.isSubmitting) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        text = "Withdraw",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            // Info
                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "*Minimum penarikan: Rp10.000",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "*Biaya admin: Rp2.500",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }

            // Success Dialog
            if (uiState.showSuccessDialog) {
                WithdrawSuccessDialog(
                    onDismiss = {
                        viewModel.dismissSuccessDialog()
                        onSuccessNavigateBack()
                    }
                )
            }
        }
    }
}

@Composable
fun WithdrawSuccessDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Check Icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(GreenPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_check),
                        contentDescription = "Success",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }

                // Title
                Text(
                    text = "Withdraw Berhasil",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = GreenPrimary
                )

                // Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GreenPrimary)
                ) {
                    Text(
                        text = "Kembali",
                        color = GreenPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

private fun isEWallet(paymentMethod: PaymentMethod): Boolean {
    return paymentMethod.name.lowercase() in listOf("gopay", "shopeepay", "dana", "ovo", "linkaja")
}

private fun formatCurrency(value: String): String {
    return try {
        val num = value.toDoubleOrNull() ?: 0.0
        val intValue = num.toInt()
        String.format("%,d", intValue).replace(',', '.')
    } catch (e: Exception) {
        "0"
    }
}

// Format angka dengan titik sebagai pemisah ribuan (real-time saat mengetik)
// Contoh: "3000" -> "3.000", "1234567" -> "1.234.567"
private fun formatNumberWithDots(value: String): String {
    if (value.isEmpty()) return ""
    
    // Hapus semua karakter non-digit (jika ada)
    val digitsOnly = value.filter { it.isDigit() }
    if (digitsOnly.isEmpty()) return ""
    
    // Format dengan cara yang lebih sederhana dan aman
    // Reverse string, chunk setiap 3, reverse setiap chunk, join, reverse hasil
    val reversed = digitsOnly.reversed()
    val result = StringBuilder()
    
    for (i in reversed.indices) {
        if (i > 0 && i % 3 == 0) {
            result.append(".")
        }
        result.append(reversed[i])
    }
    
    return result.toString().reversed()
}

// Hitung cursor position yang benar setelah format
// Berdasarkan jumlah digit yang ada sebelum cursor
private fun calculateCursorPosition(
    formatted: String,
    digitCountBeforeCursor: Int
): Int {
    if (digitCountBeforeCursor <= 0) {
        return 0
    }
    
    if (digitCountBeforeCursor >= formatted.filter { it.isDigit() }.length) {
        return formatted.length
    }
    
    // Cari posisi di formatted string yang sesuai dengan digitCountBeforeCursor
    var digitIndex = 0
    for (i in formatted.indices) {
        if (formatted[i].isDigit()) {
            digitIndex++
            if (digitIndex >= digitCountBeforeCursor) {
                return i + 1
            }
        }
    }
    
    // Fallback: return akhir string
    return formatted.length
}

