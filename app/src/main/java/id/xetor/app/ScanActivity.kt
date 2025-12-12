// app/src/main/java/id/xetor/app/ScanActivity.kt
package id.xetor.app

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import id.xetor.app.data.remote.WasteDetailResponse
import id.xetor.app.di.AppContainer
import id.xetor.app.ui.scan.CameraScreen
import id.xetor.app.ui.scan.PreviewScreen
import id.xetor.app.ui.scan.ResultScreen
import id.xetor.app.ui.theme.XetorAppTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import java.io.File
import java.io.FileOutputStream

class ScanActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val appContainer = (application as XetorApplication).appContainer
        
        setContent {
            XetorAppTheme {
                var currentScreen by remember { mutableStateOf<String?>(null) } // null = checking permission
                var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
                var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
                var isFlashOn by remember { mutableStateOf(false) }
                var wasteDetail by remember { mutableStateOf<WasteDetailResponse?>(null) }
                var isLoadingWasteDetail by remember { mutableStateOf(false) }
                val context = LocalContext.current
                
                // Check permission status
                val hasCameraPermission = remember {
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                }
                
                // Permission launcher untuk kamera
                val cameraPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (isGranted) {
                        currentScreen = "camera"
                    } else {
                        // Permission denied, close activity
                        finish()
                    }
                }
                
                // Check permission on first launch
                LaunchedEffect(Unit) {
                    if (hasCameraPermission) {
                        currentScreen = "camera"
                    } else {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }
                
                // Handle image captured from CameraX
                val handleImageCaptured = { uri: Uri, guideWidthRatio: Float, aspectRatio: Float ->
                    capturedImageUri = uri
                    val bitmap = loadBitmapFromUri(uri)
                    if (bitmap != null) {
                        // Crop bitmap sesuai dengan guide lines area (4:5 aspect ratio, center crop)
                        val croppedBitmap = cropToGuideLines(bitmap, aspectRatio)
                        previewBitmap = croppedBitmap
                        currentScreen = "preview"
                    }
                }
                
                // Gallery launcher
                val galleryLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent()
                ) { uri: Uri? ->
                    uri?.let {
                        val bitmap = loadBitmapFromUri(it)
                        if (bitmap != null) {
                            previewBitmap = bitmap
                            capturedImageUri = it
                            currentScreen = "preview"
                        }
                    }
                }
                
                // Handle back button
                BackHandler(enabled = currentScreen == "preview") {
                    currentScreen = "camera"
                    previewBitmap = null
                    capturedImageUri = null
                }
                
                BackHandler(enabled = currentScreen == "result") {
                    currentScreen = "preview"
                    wasteDetail = null
                    isLoadingWasteDetail = false
                }
                
                // Function to fetch waste detail
                val fetchWasteDetail = {
                    lifecycleScope.launch {
                        try {
                            isLoadingWasteDetail = true
                            val token = appContainer.userPreferences.authToken.first()
                            if (token != null) {
                                val response = appContainer.apiService.getWasteDetailById("Bearer $token", 2)
                                if (response.isSuccessful && response.body() != null) {
                                    wasteDetail = response.body()
                                    currentScreen = "result"
                                } else {
                                    Toast.makeText(context, "Gagal memuat data sampah", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(context, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Toast.makeText(context, "Terjadi kesalahan: ${e.message}", Toast.LENGTH_SHORT).show()
                        } finally {
                            isLoadingWasteDetail = false
                        }
                    }
                }
                
                when (currentScreen) {
                    null -> {
                        // Loading state - checking permission
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = Color.White
                            )
                        }
                    }
                    "camera" -> {
                        CameraScreen(
                            onBackClick = { finish() },
                            onFlashToggle = { isFlashOn = !isFlashOn },
                            isFlashOn = isFlashOn,
                            onGalleryClick = {
                                galleryLauncher.launch("image/*")
                            },
                            onImageCaptured = handleImageCaptured
                        )
                    }
                    "preview" -> {
                        PreviewScreen(
                            imageBitmap = previewBitmap,
                            onBackClick = {
                                currentScreen = "camera"
                                previewBitmap = null
                                capturedImageUri = null
                            },
                            onRetakeClick = {
                                currentScreen = "camera"
                                previewBitmap = null
                                capturedImageUri = null
                            },
                            onConfirmClick = {
                                // Save image locally and fetch waste detail
                                if (capturedImageUri != null) {
                                    saveImageLocally(capturedImageUri!!)
                                }
                                fetchWasteDetail()
                            }
                        )
                    }
                    "result" -> {
                        ResultScreen(
                            imageBitmap = previewBitmap,
                            wasteDetail = wasteDetail,
                            isLoading = isLoadingWasteDetail,
                            onBackClick = {
                                currentScreen = "preview"
                                wasteDetail = null
                                isLoadingWasteDetail = false
                            },
                            onSetorClick = {
                                // Navigate to SetorActivity
                                val intent = Intent(context, SetorActivity::class.java)
                                startActivity(intent)
                            }
                        )
                    }
                }
            }
        }
    }
    
    private fun loadBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = false
            }
            var bitmap = BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()
            
            // Handle EXIF orientation
            if (bitmap != null) {
                try {
                    // Try to get file path from URI
                    val filePath = when {
                        uri.scheme == "file" -> uri.path
                        uri.scheme == "content" -> {
                            // For content URI, try to get file path
                            val file = File(uri.path ?: "")
                            if (file.exists()) file.absolutePath else null
                        }
                        else -> uri.path
                    }
                    
                    if (filePath != null) {
                        val exif = android.media.ExifInterface(filePath)
                        val orientation = exif.getAttributeInt(
                            android.media.ExifInterface.TAG_ORIENTATION,
                            android.media.ExifInterface.ORIENTATION_NORMAL
                        )
                        
                        bitmap = when (orientation) {
                            android.media.ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
                            android.media.ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
                            android.media.ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
                            android.media.ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> flipBitmap(bitmap, horizontal = true)
                            android.media.ExifInterface.ORIENTATION_FLIP_VERTICAL -> flipBitmap(bitmap, horizontal = false)
                            else -> bitmap
                        }
                    }
                } catch (e: Exception) {
                    // If EXIF reading fails, just return original bitmap
                    e.printStackTrace()
                }
            }
            
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = android.graphics.Matrix().apply {
            postRotate(degrees)
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
    
    private fun flipBitmap(bitmap: Bitmap, horizontal: Boolean): Bitmap {
        val matrix = android.graphics.Matrix().apply {
            if (horizontal) {
                postScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f)
            } else {
                postScale(1f, -1f, bitmap.width / 2f, bitmap.height / 2f)
            }
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
    
    // Crop bitmap sesuai dengan guide lines area (4:5 aspect ratio, center crop)
    private fun cropToGuideLines(bitmap: Bitmap, targetAspectRatio: Float): Bitmap {
        val bitmapWidth = bitmap.width.toFloat()
        val bitmapHeight = bitmap.height.toFloat()
        val bitmapAspectRatio = bitmapWidth / bitmapHeight
        
        val cropWidth: Int
        val cropHeight: Int
        val cropX: Int
        val cropY: Int
        
        if (bitmapAspectRatio > targetAspectRatio) {
            // Bitmap lebih lebar, crop dari kiri-kanan
            cropHeight = bitmap.height
            cropWidth = (cropHeight * targetAspectRatio).toInt()
            cropX = ((bitmapWidth - cropWidth) / 2).toInt()
            cropY = 0
        } else {
            // Bitmap lebih tinggi, crop dari atas-bawah
            cropWidth = bitmap.width
            cropHeight = (cropWidth / targetAspectRatio).toInt()
            cropX = 0
            cropY = ((bitmapHeight - cropHeight) / 2).toInt()
        }
        
        return try {
            Bitmap.createBitmap(bitmap, cropX, cropY, cropWidth, cropHeight)
        } catch (e: Exception) {
            e.printStackTrace()
            bitmap // Return original if crop fails
        }
    }
    
    
    private fun saveImageLocally(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val externalFilesDir = getExternalFilesDir(null)
            val scanDir = File(externalFilesDir, "scan_images")
            if (!scanDir.exists()) {
                scanDir.mkdirs()
            }
            
            val timestamp = System.currentTimeMillis()
            val outputFile = File(scanDir, "scan_$timestamp.jpg")
            val outputStream = FileOutputStream(outputFile)
            
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

