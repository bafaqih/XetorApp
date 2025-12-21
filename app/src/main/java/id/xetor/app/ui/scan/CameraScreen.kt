// app/src/main/java/id/xetor/app/ui/scan/CameraScreen.kt
package id.xetor.app.ui.scan

import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.zIndex
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.core.content.ContextCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import id.xetor.app.R
import id.xetor.app.ui.theme.GreenPrimary
import kotlinx.coroutines.guava.await

suspend fun getCameraProvider(context: android.content.Context): ProcessCameraProvider {
    return ProcessCameraProvider.getInstance(context).await()
}

@Composable
fun CameraScreen(
    onBackClick: () -> Unit,
    onFlashToggle: () -> Unit,
    isFlashOn: Boolean,
    onGalleryClick: () -> Unit,
    onImageCaptured: (android.net.Uri, Float, Float) -> Unit // URI, guideWidthRatio, aspectRatio
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var cameraProvider: ProcessCameraProvider? by remember { mutableStateOf(null) }
    var camera: Camera? by remember { mutableStateOf(null) }
    var cameraControl: CameraControl? by remember { mutableStateOf(null) }
    
    // Get output directory
    val outputDirectory = remember {
        File(context.getExternalFilesDir(null), "scan_images").apply {
            if (!exists()) mkdirs()
        }
    }
    
    // Initialize CameraX
    LaunchedEffect(Unit) {
        cameraProvider = getCameraProvider(context)
    }
    
    // Update flash/torch when isFlashOn changes
    LaunchedEffect(isFlashOn) {
        // Update torch for preview
        cameraControl?.let { control ->
            try {
                control.enableTorch(isFlashOn)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        // Also update ImageCapture flash mode for capture
        imageCapture?.flashMode = if (isFlashOn) ImageCapture.FLASH_MODE_ON
        else ImageCapture.FLASH_MODE_OFF
    }
    
    // Also update when cameraControl becomes available
    LaunchedEffect(cameraControl) {
        cameraControl?.let { control ->
            try {
                control.enableTorch(isFlashOn)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    // Function to capture image
    fun captureImage() {
        val imageCapture = imageCapture ?: return
        
        // Create output file
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
                .format(System.currentTimeMillis()) + ".jpg"
        )
        
        // Create output file options
        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        
        // Guide lines info untuk crop (4:5 aspect ratio, 80% of screen width)
        val guideWidthRatio = 0.8f // 80% dari lebar layar
        val aspectRatio = 4f / 5f // 4:5 aspect ratio
        
        // Take picture
        imageCapture.takePicture(
            outputFileOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    // Convert file to URI
                    val photoUri = android.net.Uri.fromFile(photoFile)
                    onImageCaptured(photoUri, guideWidthRatio, aspectRatio)
                }
                
                override fun onError(exception: ImageCaptureException) {
                    exception.printStackTrace()
                }
            }
        )
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Camera Preview dengan CameraX
        cameraProvider?.let { provider ->
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }
                    
                    val preview = Preview.Builder()
                        .build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }
                    
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    
                    imageCapture = ImageCapture.Builder()
                        .setFlashMode(
                            if (isFlashOn) ImageCapture.FLASH_MODE_ON
                            else ImageCapture.FLASH_MODE_OFF
                        )
                        .build()
                    
                    try {
                        provider.unbindAll()
                        val cameraInstance = provider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageCapture
                        )
                        camera = cameraInstance
                        cameraControl = cameraInstance.cameraControl
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
        } ?: run {
            // Loading state while initializing camera
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }

        // Guide Lines - 4:5 aspect ratio dengan corner brackets
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(1f), // Di atas camera preview, di bawah top/bottom bar
            contentAlignment = Alignment.Center
        ) {
            val density = LocalDensity.current
            val configuration = LocalConfiguration.current
            val screenWidth = configuration.screenWidthDp.dp
            val guideWidth = with(density) { screenWidth.toPx() * 0.8f } // 80% dari lebar layar
            val guideHeight = guideWidth * (5f / 4f) // Aspect ratio 4:5 (height = width * 5/4)
            val cornerLength = 40.dp
            val strokeWidth = 3.dp
            
            Canvas(
                modifier = Modifier
                    .size(
                        width = with(density) { guideWidth.toDp() },
                        height = with(density) { guideHeight.toDp() }
                    )
            ) {
                val cornerLengthPx = with(density) { cornerLength.toPx() }
                val strokeWidthPx = with(density) { strokeWidth.toPx() }
                
                // Top-left corner
                drawPath(
                    path = Path().apply {
                        moveTo(0f, cornerLengthPx)
                        lineTo(0f, 0f)
                        lineTo(cornerLengthPx, 0f)
                    },
                    color = Color.White,
                    style = Stroke(width = strokeWidthPx)
                )
                
                // Top-right corner
                drawPath(
                    path = Path().apply {
                        moveTo(size.width - cornerLengthPx, 0f)
                        lineTo(size.width, 0f)
                        lineTo(size.width, cornerLengthPx)
                    },
                    color = Color.White,
                    style = Stroke(width = strokeWidthPx)
                )
                
                // Bottom-left corner
                drawPath(
                    path = Path().apply {
                        moveTo(0f, size.height - cornerLengthPx)
                        lineTo(0f, size.height)
                        lineTo(cornerLengthPx, size.height)
                    },
                    color = Color.White,
                    style = Stroke(width = strokeWidthPx)
                )
                
                // Bottom-right corner
                drawPath(
                    path = Path().apply {
                        moveTo(size.width - cornerLengthPx, size.height)
                        lineTo(size.width, size.height)
                        lineTo(size.width, size.height - cornerLengthPx)
                    },
                    color = Color.White,
                    style = Stroke(width = strokeWidthPx)
                )
            }
        }

        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, top = 16.dp, end = 4.dp, bottom = 16.dp) // Padding kiri dan kanan 4dp seperti TopAppBar
                .zIndex(2f), // Di atas guide lines
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // Back Button - tanpa background, hanya efek klik circle
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_back),
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Flash Toggle Button - tanpa background, hanya efek klik circle
            IconButton(
                onClick = onFlashToggle,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    contentDescription = if (isFlashOn) "Flash On" else "Flash Off",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Bottom Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 32.dp)
                .zIndex(2f), // Di atas guide lines
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Gallery Button
            IconButton(
                onClick = onGalleryClick,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoLibrary,
                    contentDescription = "Gallery",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Shutter Button (Center) - Stroke abu tua, bulat dalamnya abu muda
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .border(3.dp, Color(0xFF424242), CircleShape) // Stroke abu tua
                    .clickable {
                        // Capture image using CameraX
                        captureImage()
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFBDBDBD)) // Abu muda
                )
            }

            // Spacer untuk balance layout
            Spacer(modifier = Modifier.size(56.dp))
        }
    }
}

