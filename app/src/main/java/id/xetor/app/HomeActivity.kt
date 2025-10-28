// app/src/main/java/id/xetor/app/HomeActivity.kt
package id.xetor.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import id.xetor.app.ui.components.MainBottomBar
import id.xetor.app.ui.theme.GreenPrimary
import id.xetor.app.ui.theme.XetorAppTheme

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            XetorAppTheme {
                var currentScreen by remember { mutableStateOf("home") }
                val context = LocalContext.current

                Scaffold(
                    bottomBar = {
                        // Di sini HANYA berisi BottomBar, tidak ada Box
                        MainBottomBar(
                            currentRoute = currentScreen,
                            onItemSelected = { route ->
                                if (route != "scan") {
                                    currentScreen = route
                                }
                            }
                        )
                    },
                    // TOMBOL SCAN SEKARANG DITEMPATKAN DI PARAMETER KHUSUS INI
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = {
                                Toast.makeText(context, "Buka Kamera Scan...", Toast.LENGTH_SHORT).show()
                            },
                            shape = CircleShape,
                            containerColor = GreenPrimary,
                            contentColor = Color.White,
                            modifier = Modifier
                                .size(60.dp)
                                .offset(y = 63.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_scan),
                                contentDescription = "Scan",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    },
                    // Beritahu Scaffold untuk menempatkan FAB di tengah
                    floatingActionButtonPosition = FabPosition.Center
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "Konten untuk: $currentScreen")
                    }
                }
            }
        }
    }
}