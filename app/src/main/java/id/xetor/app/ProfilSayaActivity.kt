// app/src/main/java/id/xetor/app/ProfilSayaActivity.kt
package id.xetor.app

import android.content.Intent
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
                            }
                        }
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
                                // Launch camera
                                val photoFile = File(getExternalFilesDir(null), "profile_photo.jpg")
                                val photoUri = FileProvider.getUriForFile(
                                    context,
                                    "${packageName}.fileprovider",
                                    photoFile
                                )
                                cameraLauncher.launch(photoUri)
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
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            if (bitmap == null) return null
            
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

