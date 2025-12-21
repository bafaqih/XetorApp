// app/src/main/java/id/xetor/app/SignInScreen.kt
package id.xetor.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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

@Composable
fun SignInScreen(
    emailValue: String,
    passwordValue: String,
    rememberMeValue: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRememberMeChange: (Boolean) -> Unit,
    onSignInClick: () -> Unit,
    onSignUpClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onBackClick: () -> Unit,
    onGoogleSignInClick: () -> Unit,
    isLoading: Boolean = false
) {
    // State untuk UI (seperti visibility password) tetap di sini
    var isPasswordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.icon_xetor_hijau_png),
                contentDescription = "Logo",
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Selamat Datang Kembali!",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))

            CustomFilledTextField(
                value = emailValue,
                onValueChange = onEmailChange,
                labelText = "Email",
                placeholderText = "Masukkan email"
            )
            Spacer(modifier = Modifier.height(16.dp))
            CustomFilledTextField(
                value = passwordValue,
                onValueChange = onPasswordChange,
                labelText = "Password",
                placeholderText = "Masukkan password",
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle Password Visibility"
                        )
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = rememberMeValue, onCheckedChange = onRememberMeChange)
                    Text("Ingat Saya", color = Color.Gray)
                }
                Text(
                    text = "Lupa Kata Sandi?",
                    color = GreenPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onForgotPasswordClick() }
                )
            }
            Spacer(modifier = Modifier.height(32.dp))

            PrimaryButton(text = "Masuk", onClick = onSignInClick, isLoading = isLoading)
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
                Text("Belum memiliki akun? ", color = Color.Gray)
                Text(
                    text = "Daftar",
                    color = GreenPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onSignUpClick() }
                )
            }
        }

        FloatingBackButton(
            onClick = onBackClick,
            modifier = Modifier.align(Alignment.TopStart)
        )
    }
}


@Preview(showBackground = true)
@Composable
fun SignInScreenPreview() {
    XetorAppTheme {
        Surface {
            // Preview di-update untuk menyediakan nilai dan fungsi dummy
            SignInScreen(
                emailValue = "test@example.com",
                passwordValue = "password",
                rememberMeValue = true,
                onEmailChange = {},
                onPasswordChange = {},
                onRememberMeChange = {},
                onSignInClick = {},
                onSignUpClick = {},
                onForgotPasswordClick = {},
                onBackClick = {},
                onGoogleSignInClick = {},
                isLoading = false
            )
        }
    }
}