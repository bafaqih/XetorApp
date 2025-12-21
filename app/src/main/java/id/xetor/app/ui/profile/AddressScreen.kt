// app/src/main/java/id/xetor/app/ui/profile/AddressScreen.kt
package id.xetor.app.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xetor.app.R
import id.xetor.app.ui.components.CustomSnackbar
import id.xetor.app.ui.theme.GreenPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressScreen(
    viewModel: AddressViewModel,
    onBackClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Alamat Saya",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
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
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
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
            if (uiState.successMessage != null) {
                CustomSnackbar(
                    message = uiState.successMessage ?: "",
                    onDismiss = { viewModel.clearSuccess() },
                    buttonText = "OK"
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (uiState.isLoading) {
                // Loading state - Circular loading di tengah
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        strokeWidth = 4.dp,
                        color = GreenPrimary
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(scrollState)
                            .padding(horizontal = 20.dp)
                            .clickable(
                                onClick = { focusManager.clearFocus() },
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) // Hilangkan focus saat klik di luar field tanpa efek klik
                    ) {
                        Spacer(modifier = Modifier.height(24.dp))

                        // Nama Lengkap Field
                        Column {
                            Text(
                                text = "Nama Lengkap",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            OutlinedTextField(
                                value = uiState.fullname,
                                onValueChange = { viewModel.updateFullname(it) },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Belum diisi") },
                                isError = uiState.fullnameError != null,
                                supportingText = if (uiState.fullnameError != null) {
                                    { Text(text = uiState.fullnameError ?: "") }
                                } else null,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = if (uiState.fullnameError != null) {
                                        Color(0xFFFF5252)
                                    } else {
                                        GreenPrimary
                                    },
                                    unfocusedBorderColor = if (uiState.fullnameError != null) {
                                        Color(0xFFFF5252)
                                    } else {
                                        Color.LightGray
                                    },
                                    errorBorderColor = Color(0xFFFF5252),
                                    errorSupportingTextColor = Color(0xFFFF5252)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true,
                                enabled = !uiState.isSaving
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // No. WhatsApp Field
                        Column {
                            Text(
                                text = "No. Whatsapp",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            OutlinedTextField(
                                value = uiState.phone,
                                onValueChange = { viewModel.updatePhone(it) },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Belum Diisi") },
                                isError = uiState.phoneError != null,
                                supportingText = if (uiState.phoneError != null) {
                                    { Text(text = uiState.phoneError ?: "") }
                                } else null,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = if (uiState.phoneError != null) {
                                        Color(0xFFFF5252)
                                    } else {
                                        GreenPrimary
                                    },
                                    unfocusedBorderColor = if (uiState.phoneError != null) {
                                        Color(0xFFFF5252)
                                    } else {
                                        Color.LightGray
                                    },
                                    errorBorderColor = Color(0xFFFF5252),
                                    errorSupportingTextColor = Color(0xFFFF5252)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true,
                                enabled = !uiState.isSaving
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Alamat Field
                        Column {
                            Text(
                                text = "Alamat",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            OutlinedTextField(
                                value = uiState.address,
                                onValueChange = { viewModel.updateAddress(it) },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Belum Diisi") },
                                isError = uiState.addressError != null,
                                supportingText = if (uiState.addressError != null) {
                                    { Text(text = uiState.addressError ?: "") }
                                } else null,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = if (uiState.addressError != null) {
                                        Color(0xFFFF5252)
                                    } else {
                                        GreenPrimary
                                    },
                                    unfocusedBorderColor = if (uiState.addressError != null) {
                                        Color(0xFFFF5252)
                                    } else {
                                        Color.LightGray
                                    },
                                    errorBorderColor = Color(0xFFFF5252),
                                    errorSupportingTextColor = Color(0xFFFF5252)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                maxLines = 3,
                                enabled = !uiState.isSaving
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Kota/Kabupaten Field
                        Column {
                            Text(
                                text = "Kota/Kabupaten",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            OutlinedTextField(
                                value = uiState.cityRegency,
                                onValueChange = { viewModel.updateCityRegency(it) },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Belum Diisi") },
                                isError = uiState.cityRegencyError != null,
                                supportingText = if (uiState.cityRegencyError != null) {
                                    { Text(text = uiState.cityRegencyError ?: "") }
                                } else null,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = if (uiState.cityRegencyError != null) {
                                        Color(0xFFFF5252)
                                    } else {
                                        GreenPrimary
                                    },
                                    unfocusedBorderColor = if (uiState.cityRegencyError != null) {
                                        Color(0xFFFF5252)
                                    } else {
                                        Color.LightGray
                                    },
                                    errorBorderColor = Color(0xFFFF5252),
                                    errorSupportingTextColor = Color(0xFFFF5252)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true,
                                enabled = !uiState.isSaving
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Provinsi Field
                        Column {
                            Text(
                                text = "Provinsi",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            OutlinedTextField(
                                value = uiState.province,
                                onValueChange = { viewModel.updateProvince(it) },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Belum Diisi") },
                                isError = uiState.provinceError != null,
                                supportingText = if (uiState.provinceError != null) {
                                    { Text(text = uiState.provinceError ?: "") }
                                } else null,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = if (uiState.provinceError != null) {
                                        Color(0xFFFF5252)
                                    } else {
                                        GreenPrimary
                                    },
                                    unfocusedBorderColor = if (uiState.provinceError != null) {
                                        Color(0xFFFF5252)
                                    } else {
                                        Color.LightGray
                                    },
                                    errorBorderColor = Color(0xFFFF5252),
                                    errorSupportingTextColor = Color(0xFFFF5252)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true,
                                enabled = !uiState.isSaving
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Kode Pos Field
                        Column {
                            Text(
                                text = "Kode Pos",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            OutlinedTextField(
                                value = uiState.postalCode,
                                onValueChange = { viewModel.updatePostalCode(it) },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Belum Diisi") },
                                isError = uiState.postalCodeError != null,
                                supportingText = if (uiState.postalCodeError != null) {
                                    { Text(text = uiState.postalCodeError ?: "") }
                                } else null,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = if (uiState.postalCodeError != null) {
                                        Color(0xFFFF5252)
                                    } else {
                                        GreenPrimary
                                    },
                                    unfocusedBorderColor = if (uiState.postalCodeError != null) {
                                        Color(0xFFFF5252)
                                    } else {
                                        Color.LightGray
                                    },
                                    errorBorderColor = Color(0xFFFF5252),
                                    errorSupportingTextColor = Color(0xFFFF5252)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true,
                                enabled = !uiState.isSaving
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                    }

                    // Save Button - muncul berdasarkan kondisi:
                    // - Jika belum punya alamat: semua field harus terisi
                    // - Jika sudah punya alamat: harus ada perubahan
                    if (viewModel.shouldShowSaveButton()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .padding(bottom = 16.dp)
                        ) {
                            Button(
                                onClick = {
                                    focusManager.clearFocus() // Hilangkan focus saat klik simpan
                                    viewModel.saveAddress()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                                shape = RoundedCornerShape(8.dp),
                                enabled = !uiState.isSaving
                            ) {
                                if (uiState.isSaving) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = Color.White
                                    )
                                } else {
                                    Text(
                                        text = "Simpan",
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

