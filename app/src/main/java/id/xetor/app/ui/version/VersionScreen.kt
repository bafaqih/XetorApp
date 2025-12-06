// app/src/main/java/id/xetor/app/ui/version/VersionScreen.kt
package id.xetor.app.ui.version

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xetor.app.R
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VersionScreen(
    viewModel: VersionViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Versi Aplikasi",
                        color = Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.isLoading) {
                // Loading state dengan circular progress
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = id.xetor.app.ui.theme.GreenPrimary
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Logo Xetor (lebih besar)
                    Image(
                        painter = painterResource(id = R.drawable.xetor_vertical_warna),
                        contentDescription = "Xetor Logo",
                        modifier = Modifier
                            .size(280.dp)
                            .padding(bottom = 32.dp)
                    )

                    // Teks "Xetor App" (bold)
                    Text(
                        text = "Xetor App",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Teks "Version ..."
                    Text(
                        text = "Version ${uiState.version ?: "1.0.0"}",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    // Copyright dengan tahun dinamis
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "© $currentYear Xetor Sampah Indonesia.",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "All Rights Reserved.",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Error message tidak perlu ditampilkan karena sudah ada fallback ke package version
        }
    }
}

