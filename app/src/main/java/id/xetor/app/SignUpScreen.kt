// app/src/main/java/id/xetor/app/SignUpScreen.kt
package id.xetor.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xetor.app.components.*
import id.xetor.app.ui.theme.GreenPrimary
import id.xetor.app.ui.theme.XetorAppTheme

import id.xetor.app.components.SecondaryButton
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun SignUpScreen(
    nameValue: String,
    emailValue: String,
    phoneValue: String,
    passwordValue: String,
    confirmPasswordValue: String,
    termsAcceptedValue: Boolean,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onTermsAcceptedChange: (Boolean) -> Unit,
    onSignUpClick: () -> Unit,
    onSignInClick: () -> Unit,
    onBackClick: () -> Unit,
    onGoogleSignInClick: () -> Unit,
    isLoading: Boolean = false
) {
    // State untuk UI (seperti visibility password) tetap di sini
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Konten utama dengan scroll
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Spacer Atas untuk mendorong konten ke bawah
            Spacer(modifier = Modifier.height(80.dp))

            Image(
                painter = painterResource(id = R.drawable.icon_xetor_hijau_png),
                contentDescription = "Logo",
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Selamat Datang!",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))

            CustomFilledTextField(value = nameValue, onValueChange = onNameChange, labelText = "Nama Lengkap", placeholderText = "Masukkan nama lengkap")
            Spacer(modifier = Modifier.height(16.dp))
            CustomFilledTextField(value = emailValue, onValueChange = onEmailChange, labelText = "Email", placeholderText = "Masukkan email")
            Spacer(modifier = Modifier.height(16.dp))
            CustomFilledTextField(value = phoneValue, onValueChange = onPhoneChange, labelText = "No. Whatsapp", placeholderText = "Masukkan nomor whatsapp")
            Spacer(modifier = Modifier.height(16.dp))
            CustomFilledTextField(
                value = passwordValue,
                onValueChange = onPasswordChange,
                labelText = "Password",
                placeholderText = "Masukkan password",
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = "Toggle Password")
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            CustomFilledTextField(
                value = confirmPasswordValue,
                onValueChange = onConfirmPasswordChange,
                labelText = "Konfirmasi Password",
                placeholderText = "Masukkan kembali password",
                visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                        Icon(imageVector = if (isConfirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = "Toggle Password")
                    }
                }
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(checked = termsAcceptedValue, onCheckedChange = onTermsAcceptedChange)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Setuju dengan Syarat dan Ketentuan serta Kebijakan Privasi", fontSize = 14.sp, color = Color.Gray, lineHeight = 18.sp)
            }
            Spacer(modifier = Modifier.height(24.dp))
            PrimaryButton(
                text = "Buat Akun",
                onClick = onSignUpClick,
                enabled = termsAcceptedValue,
                isLoading = isLoading
            )
            Spacer(modifier = Modifier.height(24.dp))
            DividerWithText(text = "Atau")
            Spacer(modifier = Modifier.height(24.dp))
            SecondaryButton(
                text = "Lanjutkan dengan Google",
                onClick = onGoogleSignInClick,
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_google),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = Color.Unspecified
                    )
                }
            )
            Spacer(modifier = Modifier.height(32.dp))
            Row {
                Text("Sudah memiliki akun? ", color = Color.Gray)
                Text(
                    text = "Masuk",
                    color = GreenPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onSignInClick() }
                )
            }
            // Spacer Bawah
            Spacer(modifier = Modifier.height(32.dp))
        }

        FloatingBackButton(
            onClick = onBackClick,
            modifier = Modifier.align(Alignment.TopStart)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    XetorAppTheme {
        Surface {
            // Preview di-update untuk menyediakan nilai dan fungsi dummy
            SignUpScreen(
                nameValue = "Fadil Bafaqih",
                emailValue = "fadil@example.com",
                phoneValue = "08123456789",
                passwordValue = "password",
                confirmPasswordValue = "password",
                termsAcceptedValue = true,
                onNameChange = {},
                onEmailChange = {},
                onPhoneChange = {},
                onPasswordChange = {},
                onConfirmPasswordChange = {},
                onTermsAcceptedChange = {},
                onSignUpClick = {},
                onSignInClick = {},
                onBackClick = {},
                onGoogleSignInClick = {},
                isLoading = false
            )
        }
    }
}