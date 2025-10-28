// app/src/main/java/id/xetor/app/WelcomeScreen.kt
package id.xetor.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xetor.app.ui.theme.GreenPrimary
import id.xetor.app.ui.theme.TextDark
import id.xetor.app.ui.theme.XetorAppTheme

@Composable
fun WelcomeScreen(
    onSignInClick: () -> Unit,
    onSignUpClick: () -> Unit
) {
    // Box with constraints untuk menata elemen dari atas dan bawah
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 200.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.xetor_vertical_warna),
                contentDescription = "Logo Xetor",
                modifier = Modifier.size(width = 211.dp, height = 202.dp)
            )
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Setor Sampah, Panen Manfaat.",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        // Kolom untuk tombol-tombol di bagian bawah
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter) // Letakkan di bawah
                .padding(horizontal = 32.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Tombol "Masuk"
            Button(
                onClick = onSignInClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) {
                Text(text = "Masuk", color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tombol "Buat Akun"
            OutlinedButton(
                onClick = onSignUpClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextDark)
            ) {
                Text(text = "Buat Akun")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    XetorAppTheme {
        Surface {
            WelcomeScreen(onSignInClick = {}, onSignUpClick = {})
        }
    }
}