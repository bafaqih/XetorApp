// app/src/main/java/id/xetor/app/components/AuthUI.kt
package id.xetor.app.components
import id.xetor.app.R

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xetor.app.ui.theme.GreenPrimary
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.draw.shadow
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.BorderStroke

// Cetakan untuk Tombol Utama (Solid)
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp), // Tinggi tombol dikurangi
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
        enabled = enabled && !isLoading
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp
            )
        } else {
            Text(text = text, color = Color.White, fontSize = 16.sp)
        }
    }
}

// Cetakan untuk Tombol Outline
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onBackground)
    ) {
        // Box allows us to align items independently
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center // Default alignment for children is Center
        ) {
            // Align the icon to the start (left) of the Box
            Box(modifier = Modifier.align(Alignment.CenterStart)) {
                if (icon != null) {
                    icon()
                }
            }

            // The text will remain in the center of the Box by default
            Text(text = text)
        }
    }
}

// --- TEXT FIELD BARU SESUAI DESAIN ---
// Menggunakan TextField, bukan OutlinedTextField
@Composable
fun CustomFilledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    labelText: String, // Teks kecil di atas
    placeholderText: String, // Teks di dalam saat kosong
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(labelText) },
        placeholder = { Text(placeholderText) },
        shape = RoundedCornerShape(50.dp), // Sedikit melengkung, tidak bulat penuh
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFF5F5F5), // Warna background abu-abu
            unfocusedContainerColor = Color(0xFFF5F5F5),
            disabledContainerColor = Color(0xFFF5F5F5),
            focusedIndicatorColor = Color.Transparent, // Hilangkan garis bawah
            unfocusedIndicatorColor = Color.Transparent, // Hilangkan garis bawah
            disabledIndicatorColor = Color.Transparent,
        ),
        singleLine = true,
        visualTransformation = visualTransformation,
        trailingIcon = trailingIcon
    )
}

// --- KOMPONEN BARU UNTUK DIVIDER ---
@Composable
fun DividerWithText(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp),
            color = Color.Gray
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
    }
}

@Composable
fun FloatingBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .padding(16.dp)
            .background(color = GreenPrimary, shape = CircleShape)
            .size(50.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_back),
            contentDescription = "Kembali",
            tint = Color.White
        )
    }
}