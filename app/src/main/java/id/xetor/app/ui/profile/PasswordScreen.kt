// app/src/main/java/id/xetor/app/ui/profile/PasswordScreen.kt
package id.xetor.app.ui.profile

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xetor.app.R
import id.xetor.app.ui.components.CustomSnackbar
import id.xetor.app.ui.theme.GreenPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordScreen(
    viewModel: PasswordViewModel,
    onBackClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Kata Sandi",
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

                    // Kata Sandi Saat Ini Field
                    Column {
                        Text(
                            text = "Kata Sandi Saat Ini",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = uiState.oldPassword,
                            onValueChange = { viewModel.updateOldPassword(it) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Masukkan kata sandi saat ini") },
                            visualTransformation = if (uiState.isOldPasswordVisible) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            trailingIcon = {
                                IconButton(onClick = { viewModel.toggleOldPasswordVisibility() }) {
                                    Icon(
                                        imageVector = if (uiState.isOldPasswordVisible) {
                                            Icons.Default.Visibility
                                        } else {
                                            Icons.Default.VisibilityOff
                                        },
                                        contentDescription = if (uiState.isOldPasswordVisible) {
                                            "Hide password"
                                        } else {
                                            "Show password"
                                        }
                                    )
                                }
                            },
                            isError = uiState.oldPasswordError != null,
                            supportingText = if (uiState.oldPasswordError != null) {
                                { Text(text = uiState.oldPasswordError ?: "") }
                            } else null,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (uiState.oldPasswordError != null) {
                                    Color(0xFFFF5252)
                                } else {
                                    GreenPrimary
                                },
                                unfocusedBorderColor = if (uiState.oldPasswordError != null) {
                                    Color(0xFFFF5252)
                                } else {
                                    Color.LightGray
                                },
                                errorBorderColor = Color(0xFFFF5252),
                                errorSupportingTextColor = Color(0xFFFF5252)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            enabled = !uiState.isChanging
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Kata Sandi Baru Field
                    Column {
                        Text(
                            text = "Kata Sandi Baru",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = uiState.newPassword,
                            onValueChange = { viewModel.updateNewPassword(it) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Masukkan kata sandi baru") },
                            visualTransformation = if (uiState.isNewPasswordVisible) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            trailingIcon = {
                                IconButton(onClick = { viewModel.toggleNewPasswordVisibility() }) {
                                    Icon(
                                        imageVector = if (uiState.isNewPasswordVisible) {
                                            Icons.Default.Visibility
                                        } else {
                                            Icons.Default.VisibilityOff
                                        },
                                        contentDescription = if (uiState.isNewPasswordVisible) {
                                            "Hide password"
                                        } else {
                                            "Show password"
                                        }
                                    )
                                }
                            },
                            isError = uiState.newPasswordError != null,
                            supportingText = if (uiState.newPasswordError != null) {
                                { Text(text = uiState.newPasswordError ?: "") }
                            } else null,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (uiState.newPasswordError != null) {
                                    Color(0xFFFF5252)
                                } else {
                                    GreenPrimary
                                },
                                unfocusedBorderColor = if (uiState.newPasswordError != null) {
                                    Color(0xFFFF5252)
                                } else {
                                    Color.LightGray
                                },
                                errorBorderColor = Color(0xFFFF5252),
                                errorSupportingTextColor = Color(0xFFFF5252)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            enabled = !uiState.isChanging
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Konfirmasi Kata Sandi Baru Field
                    Column {
                        Text(
                            text = "Konfirmasi Kata Sandi Baru",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = uiState.confirmNewPassword,
                            onValueChange = { viewModel.updateConfirmPassword(it) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Konfirmasi kata sandi baru") },
                            visualTransformation = if (uiState.isConfirmPasswordVisible) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            trailingIcon = {
                                IconButton(onClick = { viewModel.toggleConfirmPasswordVisibility() }) {
                                    Icon(
                                        imageVector = if (uiState.isConfirmPasswordVisible) {
                                            Icons.Default.Visibility
                                        } else {
                                            Icons.Default.VisibilityOff
                                        },
                                        contentDescription = if (uiState.isConfirmPasswordVisible) {
                                            "Hide password"
                                        } else {
                                            "Show password"
                                        }
                                    )
                                }
                            },
                            isError = uiState.confirmPasswordError != null,
                            supportingText = if (uiState.confirmPasswordError != null) {
                                { Text(text = uiState.confirmPasswordError ?: "") }
                            } else null,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (uiState.confirmPasswordError != null) {
                                    Color(0xFFFF5252)
                                } else {
                                    GreenPrimary
                                },
                                unfocusedBorderColor = if (uiState.confirmPasswordError != null) {
                                    Color(0xFFFF5252)
                                } else {
                                    Color.LightGray
                                },
                                errorBorderColor = Color(0xFFFF5252),
                                errorSupportingTextColor = Color(0xFFFF5252)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            enabled = !uiState.isChanging
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Lupa Kata Sandi Link
                    Text(
                        text = "Lupa Kata Sandi?",
                        fontSize = 14.sp,
                        color = GreenPrimary,
                        modifier = Modifier
                            .align(Alignment.End)
                            .clickable(
                                onClick = {
                                    Toast.makeText(
                                        context,
                                        "Fitur ini belum tersedia",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            )
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                }

                // Save Button - hanya muncul jika semua field terisi
                val allFieldsFilled = uiState.oldPassword.isNotEmpty() &&
                        uiState.newPassword.isNotEmpty() &&
                        uiState.confirmNewPassword.isNotEmpty()
                
                if (allFieldsFilled) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 16.dp)
                    ) {
                        Button(
                            onClick = {
                                focusManager.clearFocus() // Hilangkan focus saat klik simpan
                                viewModel.changePassword()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                            shape = RoundedCornerShape(8.dp),
                            enabled = !uiState.isChanging
                        ) {
                            if (uiState.isChanging) {
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

