// app/src/main/java/id/xetor/app/ui/profile/ProfilSayaScreen.kt
package id.xetor.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import id.xetor.app.R
import id.xetor.app.data.remote.ApiConfig
import id.xetor.app.ui.components.CustomSnackbar
import id.xetor.app.ui.components.SkeletonCircle
import id.xetor.app.ui.components.SkeletonText
import id.xetor.app.ui.components.SkeletonBox
import id.xetor.app.ui.theme.GreenPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilSayaScreen(
    viewModel: ProfilSayaViewModel,
    onBackClick: () -> Unit = {},
    onPhotoClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current
    val nameTextFieldInteractionSource = remember { MutableInteractionSource() }
    val isNameFocused by nameTextFieldInteractionSource.collectIsFocusedAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profil Saya",
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
            if (uiState.isLoading && uiState.fullname.isEmpty()) {
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

                        // Profile Photo
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .size(120.dp)
                        ) {
                            val photoRefreshKey by viewModel.photoRefreshKeyFlow.collectAsState()
                            // Jika foto masih loading, tampilkan skeleton, jika tidak tampilkan foto
                            if (uiState.isLoadingPhoto) {
                                SkeletonCircle(size = 120.dp)
                            } else {
                                ProfilePhotoDisplay(
                                    photoUrl = uiState.photoUrl,
                                    refreshKey = photoRefreshKey,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .clickable(onClick = onPhotoClick),
                                    onLoadComplete = {
                                        // Set loading photo = false setelah foto load
                                        viewModel.setLoadingPhoto(false)
                                    }
                                )
                            }
                            
                            // Camera icon overlay
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(GreenPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Edit Photo",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Full Name Field
                        Column {
                            Text(
                                text = "Full Name",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            OutlinedTextField(
                                value = uiState.fullname,
                                onValueChange = { viewModel.updateFullname(it) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { focusManager.clearFocus() },
                                placeholder = { Text("Masukkan nama lengkap") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GreenPrimary,
                                    unfocusedBorderColor = Color.LightGray,
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true,
                                interactionSource = nameTextFieldInteractionSource
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Email Field (Read-only)
                        Column {
                            Text(
                                text = "Email",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            OutlinedTextField(
                                value = uiState.email,
                                onValueChange = { },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Email") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.LightGray,
                                    unfocusedBorderColor = Color.LightGray,
                                    focusedContainerColor = Color(0xFFF5F5F5),
                                    unfocusedContainerColor = Color(0xFFF5F5F5),
                                    disabledContainerColor = Color(0xFFF5F5F5),
                                    disabledTextColor = Color.Black,
                                    disabledBorderColor = Color.LightGray
                                ),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true,
                                enabled = false
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Phone Field (Read-only)
                        Column {
                            Text(
                                text = "Phone",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            OutlinedTextField(
                                value = uiState.phone.ifEmpty { "Belum diisi" },
                                onValueChange = { },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("No. WhatsApp") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.LightGray,
                                    unfocusedBorderColor = Color.LightGray,
                                    focusedContainerColor = Color(0xFFF5F5F5),
                                    unfocusedContainerColor = Color(0xFFF5F5F5),
                                    disabledContainerColor = Color(0xFFF5F5F5),
                                    disabledTextColor = Color.Black,
                                    disabledBorderColor = Color.LightGray
                                ),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true,
                                enabled = false
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                
                    // Save Button (only visible when there are changes) - dipindahkan ke bawah
                    if (uiState.hasChanges) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .padding(bottom = 16.dp)
                        ) {
                            Button(
                                onClick = { 
                                    focusManager.clearFocus() // Hilangkan focus saat klik simpan
                                    viewModel.saveProfile() 
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

@Composable
fun ProfilePhotoDisplay(
    photoUrl: String?,
    refreshKey: Int = 0,
    modifier: Modifier = Modifier,
    onLoadComplete: (() -> Unit)? = null // Callback untuk notifikasi setelah foto load
) {
    if (photoUrl != null && photoUrl.isNotEmpty()) {
        val fullUrl = if (photoUrl.startsWith("http")) {
            photoUrl
        } else {
            "${ApiConfig.BASE_URL}$photoUrl"
        }
        
        // Smart cache busting: only add parameter when refreshKey changes (new upload)
        // Remember parameter based on refreshKey, so same refreshKey uses same URL (for Coil memory cache)
        val urlWithCacheBust = remember(photoUrl, refreshKey) {
            if (refreshKey > 0) {
                // Use refreshKey as part of URL to ensure consistency
                // Same refreshKey = same URL = Coil memory cache works
                "$fullUrl?refresh=$refreshKey"
            } else {
                // Use normal URL when no refresh needed
                fullUrl
            }
        }
        
        SubcomposeAsyncImage(
            model = urlWithCacheBust,
            contentDescription = "Profile Photo",
            modifier = modifier.clip(CircleShape),
            contentScale = ContentScale.Crop
        ) {
            val state = painter.state
            if (state is AsyncImagePainter.State.Loading) {
                SkeletonCircle(modifier = modifier)
            } else if (state is AsyncImagePainter.State.Error) {
                // Jika error, tampilkan placeholder
                Box(
                    modifier = modifier
                        .clip(CircleShape)
                        .background(Color.Gray.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile Photo",
                        tint = Color.Gray,
                        modifier = Modifier.size(60.dp)
                    )
                }
                // Notifikasi bahwa loading selesai (meskipun error)
                LaunchedEffect(Unit) {
                    onLoadComplete?.invoke()
                }
            } else {
                SubcomposeAsyncImageContent()
                // Notifikasi bahwa loading selesai
                LaunchedEffect(Unit) {
                    onLoadComplete?.invoke()
                }
            }
        }
    } else {
        // Default placeholder
        Box(
            modifier = modifier
                .clip(CircleShape)
                .background(Color.Gray.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile Photo",
                tint = Color.Gray,
                modifier = Modifier.size(60.dp)
            )
        }
        // Notifikasi bahwa loading selesai (tidak ada foto)
        LaunchedEffect(Unit) {
            onLoadComplete?.invoke()
        }
    }
}

@Composable
fun ProfilSayaSkeletonContent(
    scrollState: androidx.compose.foundation.ScrollState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Profile Photo Skeleton
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(120.dp)
        ) {
            SkeletonCircle(size = 120.dp)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Fields Skeleton
        repeat(3) {
            Column {
                SkeletonText(
                    modifier = Modifier
                        .width(80.dp)
                        .height(14.dp)
                        .padding(bottom = 8.dp)
                )
                SkeletonBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(8.dp)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
