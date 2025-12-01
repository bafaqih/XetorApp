// app/src/main/java/id/xetor/app/ui/shop/ShopScreen.kt
package id.xetor.app.ui.shop

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.border
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import id.xetor.app.R
import id.xetor.app.ui.theme.GreenPrimary

@Composable
fun ShopScreen(
    onCartClick: () -> Unit = {}
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header dengan background hijau (sama persis dengan HomeScreen)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                .background(GreenPrimary)
                .padding(bottom = 60.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                // Row untuk logo dan keranjang
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Logo Xetor dengan teks Eco Shop
                    Column {
                        Image(
                            painter = painterResource(id = R.drawable.text_xetor_putih),
                            contentDescription = "Xetor Logo",
                            modifier = Modifier.height(28.dp),
                            contentScale = ContentScale.Fit
                        )
                        Text(
                            text = "Eco Shop",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    // Button keranjang
                    IconButton(
                        onClick = {
                            Toast.makeText(context, "Keranjang belum tersedia", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_cart),
                            contentDescription = "Cart",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
        
        // Search bar putih dengan filter icon (di luar Box hijau, dengan offset)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-60).dp)
                .padding(horizontal = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, Color.White, RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color(0xFF181818),
                            modifier = Modifier
                                .size(22.dp)
                                .clickable {
                                    if (searchQuery.isNotBlank()) {
                                        Toast.makeText(context, "Produk tidak ditemukan", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Cari produk",
                                    color = Color.Gray,
                                    fontSize = 14.sp,
                                    modifier = Modifier.align(Alignment.CenterStart)
                                )
                            }
                            CompositionLocalProvider(
                                androidx.compose.foundation.text.selection.LocalTextSelectionColors provides TextSelectionColors(
                                    handleColor = GreenPrimary,
                                    backgroundColor = GreenPrimary.copy(alpha = 0.4f)
                                )
                            ) {
                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = { newValue: String -> searchQuery = newValue },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.CenterStart),
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        fontSize = 14.sp,
                                        color = Color.Black
                                    ),
                                    keyboardOptions = KeyboardOptions(
                                        imeAction = ImeAction.Search
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onSearch = {
                                            if (searchQuery.isNotBlank()) {
                                                Toast.makeText(context, "Produk tidak ditemukan", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    )
                                )
                            }
                        }
                    }
                }
                
                // Filter icon di sebelah kanan search bar (tinggi sama dengan search field)
                Box(
                    modifier = Modifier
                        .height(44.dp)
                        .width(44.dp)
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .clickable {
                            Toast.makeText(context, "Filter belum tersedia", Toast.LENGTH_SHORT).show()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter",
                        tint = Color(0xFF181818),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        
        // Content area - pesan kosong (dengan padding top untuk search bar)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 20.dp)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Yahh.... Belum ada produk yang dijual.",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}

