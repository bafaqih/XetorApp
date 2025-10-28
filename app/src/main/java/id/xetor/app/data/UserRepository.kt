// UserRepository.kt
package id.xetor.app.data

import android.util.Log
import id.xetor.app.data.local.User
import id.xetor.app.data.local.UserDao
import id.xetor.app.data.local.UserPreferences
import id.xetor.app.data.local.mapToEntity
import id.xetor.app.data.remote.ApiService
import id.xetor.app.data.remote.LoginRequest
import id.xetor.app.data.remote.SignUpRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import id.xetor.app.data.remote.GoogleAuthRequest
import okhttp3.ResponseBody
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

// ... import lainnya

class UserRepository(
    private val apiService: ApiService,
    private val userDao: UserDao,
    private val userPreferences: UserPreferences
) {
    private fun parseErrorResponse(errorBody: ResponseBody?): String {
        return try {
            val json = errorBody?.string()
            if (json == null) return "Terjadi kesalahan tidak diketahui"

            // Buat instance Moshi sementara untuk mem-parsing error
            val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
            val adapter = moshi.adapter(Map::class.java)
            val errorMap = adapter.fromJson(json) as? Map<String, Any>

            // Ambil pesan dari key "error"
            (errorMap?.get("error") as? String) ?: "Terjadi kesalahan (format error tidak dikenal)"
        } catch (e: Exception) {
            "Gagal memparsing pesan error"
        }
    }

    // Fungsi untuk login
    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            val request = LoginRequest(email, password)
            val response = apiService.login(request)

            if (response.isSuccessful && response.body() != null) {
                val loginData = response.body()!!
                // Simpan token ke DataStore
                userPreferences.saveAuthToken(loginData.token)
                // Simpan data user ke database Room
                val userEntity = User(
                    id = loginData.user.id,
                    name = loginData.user.fullname,
                    email = loginData.user.email,
                    phone = loginData.user.phone,
                    photo = loginData.user.photo
                )
                userDao.insertUser(userEntity)
                Result.success(Unit)
            } else {
                val errorMsg = parseErrorResponse(response.errorBody()) // <-- Gunakan helper
                Log.e("UserRepository", "Login failed: $errorMsg")
                Result.failure(Exception(errorMsg)) // <-- Kirim pesan error yang sudah diparsing
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUp(name: String, email: String, phone: String, password: String): Result<Unit> {
        return try {
            val request = SignUpRequest(name, email, phone, password)
            val response = apiService.signUp(request)

            if (response.isSuccessful) {
                // Registrasi sukses, langsung kirim hasil sukses tanpa login
                Result.success(Unit) // <-- UBAH DI SINI
            } else {
                val errorMsg = parseErrorResponse(response.errorBody()) // <-- Gunakan helper
                Log.e("UserRepository", "Sign up failed: $errorMsg")
                Result.failure(Exception(errorMsg)) // <-- Kirim pesan error yang sudah diparsing
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginWithGoogle(idToken: String): Result<Unit> {
        return try {
            val request = GoogleAuthRequest(idToken = idToken)
            val response = apiService.loginWithGoogle(request)

            if (response.isSuccessful && response.body() != null) {
                val loginData = response.body()!!
                // Simpan token Xetor kita ke DataStore
                userPreferences.saveAuthToken(loginData.token)
                // Simpan data user ke database Room
                userDao.insertUser(loginData.user.mapToEntity())
                Log.d("UserRepository", "Google Sign-In/Up successful. User saved.")
                Result.success(Unit)
            } else {
                val errorMsg = parseErrorResponse(response.errorBody()) // <-- Gunakan helper
                Log.e("UserRepository", "Google Sign-In/Up failed: $errorMsg")
                Result.failure(Exception(errorMsg)) // <-- Kirim pesan error yang sudah diparsing
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Google Sign-In/Up exception: ${e.message}", e)
            Result.failure(e)
        }
    }


    // Pantau status login dari DataStore
    val isLoggedIn: Flow<Boolean> = userPreferences.authToken.map { it != null }

}