// app/src/main/java/id/xetor/app/ProfilSayaActivity.kt
package id.xetor.app

import android.Manifest
import android.content.Intent
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
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import id.xetor.app.di.AppContainer
import id.xetor.app.ui.profile.FotoProfilScreen
import id.xetor.app.ui.profile.ProfilSayaScreen
import id.xetor.app.ui.profile.ProfilSayaViewModel
import id.xetor.app.ui.profile.ProfilSayaViewModelFactory
import id.xetor.app.ui.theme.XetorAppTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileOutputStream

class ProfilSayaActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val appContainer = (application as XetorApplication).appContainer
        val token = runBlocking { appContainer.userPreferences.authToken.first() } ?: ""
        
        if (token.isEmpty()) {
            finish()
            return
        }
        
        setContent {
            XetorAppTheme {
                var currentScreen by remember { mutableStateOf("profil_saya") }
                val context = LocalContext.current
                
                val viewModel: ProfilSayaViewModel = viewModel(
                    factory = ProfilSayaViewModelFactory(
                        userRepository = appContainer.userRepository,
                        token = token,
                        userPreferences = appContainer.userPreferences,
                        application = this@ProfilSayaActivity.application
                    )
                )
                
                // Camera launcher
                val cameraLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.TakePicture()
                ) { success ->
                    if (success) {
                        // Get the photo file
                        val photoFile = File(getExternalFilesDir(null), "profile_photo.jpg")
                        if (photoFile.exists()) {
                            // Crop to 1:1
                            val croppedFile = cropImageToSquare(photoFile)
                            if (croppedFile != null) {
                                viewModel.uploadProfilePhoto(croppedFile)
                                // Navigate back to foto profil screen to show updated photo
                                currentScreen = "foto_profil"
                            } else {
                                viewModel.showError("Gagal memproses foto")
                            }
                        } else {
                            viewModel.showError("File foto tidak ditemukan")
                        }
                    } else {
                        // Camera was cancelled or failed
                        // Don't show error if user just cancelled
                        android.util.Log.d("ProfilSayaActivity", "Camera cancelled or failed")
                    }
                }
                
                // Function to launch camera with proper setup
                val launchCamera: (android.content.Context) -> Unit = { ctx ->
                    try {
                        // Ensure directory exists
                        val externalFilesDir = getExternalFilesDir(null)
                        if (externalFilesDir == null) {
                            viewModel.showError("Gagal mengakses penyimpanan")
                        } else {
                            // Ensure directory exists
                            if (!externalFilesDir.exists()) {
                                externalFilesDir.mkdirs()
                            }
                            
                            // Create photo file
                            val photoFile = File(externalFilesDir, "profile_photo.jpg")
                            
                            // Create parent directories if they don't exist
                            photoFile.parentFile?.mkdirs()
                            
                            // Create empty file if it doesn't exist
                            if (!photoFile.exists()) {
                                photoFile.createNewFile()
                            }
                            
                            // Get URI from FileProvider
                            val authorities = "${ctx.packageName}.fileprovider"
                            val photoUri = try {
                                FileProvider.getUriForFile(ctx, authorities, photoFile)
                            } catch (e: IllegalArgumentException) {
                                // FileProvider configuration error
                                e.printStackTrace()
                                viewModel.showError("Konfigurasi FileProvider tidak valid. Pastikan file_paths.xml sudah dikonfigurasi dengan benar.")
                                null
                            } catch (e: Exception) {
                                e.printStackTrace()
                                viewModel.showError("Gagal mendapatkan URI: ${e.message ?: "Terjadi kesalahan"}")
                                null
                            }
                            
                            // Launch camera if URI is valid
                            photoUri?.let {
                                cameraLauncher.launch(it)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        // Handle error - show message to user
                        viewModel.showError("Gagal membuka kamera: ${e.message ?: "Terjadi kesalahan"}")
                    }
                }
                
                // Permission launcher untuk kamera
                val cameraPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (isGranted) {
                        // Permission granted, launch camera
                        launchCamera(context)
                    }
                }
                
                // Gallery launcher
                val galleryLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent()
                ) { uri: Uri? ->
                    uri?.let {
                        // Save URI to file and crop to 1:1
                        val photoFile = saveUriToFile(it)
                        if (photoFile != null) {
                            val croppedFile = cropImageToSquare(photoFile)
                            if (croppedFile != null) {
                                viewModel.uploadProfilePhoto(croppedFile)
                                // Navigate back to foto profil screen to show updated photo
                                currentScreen = "foto_profil"
                            }
                        }
                    }
                }
                
                // Handle back button dari sistem
                BackHandler(enabled = currentScreen == "foto_profil") {
                    currentScreen = "profil_saya"
                }
                
                when (currentScreen) {
                    "profil_saya" -> {
                        ProfilSayaScreen(
                            viewModel = viewModel,
                            onBackClick = { finish() },
                            onPhotoClick = {
                                currentScreen = "foto_profil"
                            }
                        )
                    }
                    "foto_profil" -> {
                        FotoProfilScreen(
                            viewModel = viewModel,
                            onBackClick = {
                                currentScreen = "profil_saya"
                            },
                            onCameraClick = {
                                // Check camera permission first
                                when {
                                    ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.CAMERA
                                    ) == PackageManager.PERMISSION_GRANTED -> {
                                        // Permission already granted, launch camera
                                        launchCamera(context)
                                    }
                                    else -> {
                                        // Request permission
                                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                }
                            },
                            onGalleryClick = {
                                // Launch gallery
                                galleryLauncher.launch("image/*")
                            }
                        )
                    }
                }
            }
        }
    }
    
    private fun cropImageToSquare(file: File): File? {
        return try {
            // Read EXIF orientation
            val exif = android.media.ExifInterface(file.absolutePath)
            val orientation = exif.getAttributeInt(
                android.media.ExifInterface.TAG_ORIENTATION,
                android.media.ExifInterface.ORIENTATION_NORMAL
            )
            
            // Decode bitmap with options to handle orientation
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = false
            }
            var bitmap = BitmapFactory.decodeFile(file.absolutePath, options)
            if (bitmap == null) return null
            
            // Apply rotation based on EXIF orientation
            bitmap = when (orientation) {
                android.media.ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
                android.media.ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
                android.media.ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
                android.media.ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> flipBitmap(bitmap, horizontal = true)
                android.media.ExifInterface.ORIENTATION_FLIP_VERTICAL -> flipBitmap(bitmap, horizontal = false)
                else -> bitmap
            }
            
            val size = minOf(bitmap.width, bitmap.height)
            val x = (bitmap.width - size) / 2
            val y = (bitmap.height - size) / 2
            
            val croppedBitmap = Bitmap.createBitmap(bitmap, x, y, size, size)
            
            val outputFile = File(getExternalFilesDir(null), "profile_photo_cropped.jpg")
            val outputStream = FileOutputStream(outputFile)
            croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.flush()
            outputStream.close()
            
            // Recycle bitmaps to free memory
            bitmap.recycle()
            croppedBitmap.recycle()
            
            outputFile
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
    
    private fun saveUriToFile(uri: Uri): File? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val outputFile = File(getExternalFilesDir(null), "profile_photo_temp.jpg")
            val outputStream = FileOutputStream(outputFile)
            
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            
            outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

