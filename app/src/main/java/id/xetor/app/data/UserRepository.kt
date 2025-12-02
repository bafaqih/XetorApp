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
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
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

    // Fungsi untuk mengambil profil user
    suspend fun getUserProfile(token: String): Result<id.xetor.app.data.remote.UserDto> {
        return try {
            val response = apiService.getUserProfile("Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = parseErrorResponse(response.errorBody())
                Log.e("UserRepository", "Get profile failed: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Get profile exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Fungsi untuk mengambil wallet user
    suspend fun getUserWallet(token: String): Result<id.xetor.app.data.remote.WalletResponse> {
        return try {
            val response = apiService.getUserWallet("Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = parseErrorResponse(response.errorBody())
                Log.e("UserRepository", "Get wallet failed: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Get wallet exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Fungsi untuk mengambil statistik user
    suspend fun getUserStatistics(token: String): Result<id.xetor.app.data.remote.StatisticsResponse> {
        return try {
            val response = apiService.getUserStatistics("Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = parseErrorResponse(response.errorBody())
                Log.e("UserRepository", "Get statistics failed: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Get statistics exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Fungsi untuk mengambil transaction history user
    suspend fun getTransactionHistory(token: String): Result<List<id.xetor.app.data.remote.TransactionHistoryResponse>> {
        return try {
            val response = apiService.getTransactionHistory("Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = parseErrorResponse(response.errorBody())
                Log.e("UserRepository", "Get transaction history failed: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Get transaction history exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Fungsi untuk submit withdraw
    suspend fun submitWithdraw(
        token: String,
        paymentMethodId: Int,
        accountNumber: String,
        amount: Double,
        accountHolderName: String?
    ): Result<id.xetor.app.data.remote.WithdrawResponse> {
        return try {
            val request = id.xetor.app.data.remote.WithdrawRequest(
                paymentMethodId = paymentMethodId,
                accountNumber = accountNumber,
                amount = amount,
                accountHolderName = accountHolderName
            )
            val response = apiService.submitWithdraw("Bearer $token", request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = parseErrorResponse(response.errorBody())
                Log.e("UserRepository", "Submit withdraw failed: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Submit withdraw exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Fungsi untuk request topup
    suspend fun requestTopup(
        token: String,
        amount: Double
    ): Result<id.xetor.app.data.remote.TopupResponse> {
        return try {
            // Payment method ID 1 = Gopay (hardcoded sesuai requirement)
            val request = id.xetor.app.data.remote.TopupRequest(
                paymentMethodId = 1,
                amount = amount
            )
            val response = apiService.requestTopup("Bearer $token", request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = parseErrorResponse(response.errorBody())
                Log.e("UserRepository", "Request topup failed: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Request topup exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Fungsi untuk mengambil payment methods
    suspend fun getPaymentMethods(): Result<List<id.xetor.app.data.remote.PaymentMethodResponse>> {
        return try {
            val response = apiService.getPaymentMethods()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = parseErrorResponse(response.errorBody())
                Log.e("UserRepository", "Get payment methods failed: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Get payment methods exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getPromotionBanners(): Result<List<id.xetor.app.data.remote.PromotionBannerResponse>> {
        return try {
            val response = apiService.getPromotionBanners()
            Log.d("UserRepository", "Get promotion banners response code: ${response.code()}")
            if (response.isSuccessful && response.body() != null) {
                val banners = response.body()!!
                Log.d("UserRepository", "Get promotion banners success: ${banners.size} banners")
                Result.success(banners)
            } else {
                val errorMsg = parseErrorResponse(response.errorBody())
                Log.e("UserRepository", "Get promotion banners failed: $errorMsg, code: ${response.code()}")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Get promotion banners exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Fungsi untuk submit transfer
    suspend fun submitTransfer(
        token: String,
        amount: Int,
        recipientEmail: String
    ): Result<id.xetor.app.data.remote.TransferResponse> {
        return try {
            val request = id.xetor.app.data.remote.TransferRequest(
                amount = amount,
                recipientEmail = recipientEmail
            )
            val response = apiService.submitTransfer("Bearer $token", request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = parseErrorResponse(response.errorBody())
                Log.e("UserRepository", "Submit transfer failed: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Submit transfer exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Fungsi untuk convert Xp to Rp
    suspend fun convertXpToRp(
        token: String,
        amount: Double
    ): Result<id.xetor.app.data.remote.ConversionResponse> {
        return try {
            val request = id.xetor.app.data.remote.ConversionRequest(amount = amount)
            val response = apiService.convertXpToRp("Bearer $token", request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = parseErrorResponse(response.errorBody())
                Log.e("UserRepository", "Convert Xp to Rp failed: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Convert Xp to Rp exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Fungsi untuk convert Rp to Xp
    suspend fun convertRpToXp(
        token: String,
        amount: Double
    ): Result<id.xetor.app.data.remote.ConversionResponse> {
        return try {
            val request = id.xetor.app.data.remote.ConversionRequest(amount = amount)
            val response = apiService.convertRpToXp("Bearer $token", request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = parseErrorResponse(response.errorBody())
                Log.e("UserRepository", "Convert Rp to Xp failed: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Convert Rp to Xp exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Fungsi untuk menghapus akun
    suspend fun deleteAccount(token: String): Result<Unit> {
        return try {
            val response = apiService.deleteAccount("Bearer $token")
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorMsg = parseErrorResponse(response.errorBody())
                Log.e("UserRepository", "Delete account failed: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Delete account exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Fungsi untuk clear auth token (logout)
    suspend fun clearAuthToken() {
        userPreferences.clearAuthToken()
    }

    // Fungsi untuk update profile
    suspend fun updateProfile(
        token: String,
        fullname: String
    ): Result<Unit> {
        return try {
            // Get current profile to get email and phone (read-only fields)
            val currentProfileResult = getUserProfile(token)
            if (currentProfileResult.isFailure) {
                return Result.failure(
                    Exception(currentProfileResult.exceptionOrNull()?.message ?: "Gagal mengambil data profil")
                )
            }
            val currentProfile = currentProfileResult.getOrNull()
            
            val request = id.xetor.app.data.remote.UpdateProfileRequest(
                fullname = fullname,
                email = currentProfile?.email ?: "",
                phone = currentProfile?.phone ?: ""
            )
            val response = apiService.updateProfile("Bearer $token", request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorMsg = parseErrorResponse(response.errorBody())
                Log.e("UserRepository", "Update profile failed: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Update profile exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Fungsi untuk upload profile photo
    suspend fun uploadProfilePhoto(
        token: String,
        photoFile: java.io.File
    ): Result<String> {
        return try {
            val requestFile = RequestBody.create(
                "image/*".toMediaType(),
                photoFile
            )
            val photoPart = okhttp3.MultipartBody.Part.createFormData("photo", photoFile.name, requestFile)
            val response = apiService.uploadProfilePhoto("Bearer $token", photoPart)
            if (response.isSuccessful && response.body() != null) {
                val photoUrl = response.body()!!.photoUrl
                Result.success(photoUrl)
            } else {
                val errorMsg = parseErrorResponse(response.errorBody())
                Log.e("UserRepository", "Upload profile photo failed: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Upload profile photo exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Fungsi untuk delete profile photo (set to default)
    suspend fun deleteProfilePhoto(token: String): Result<Unit> {
        return try {
            val response = apiService.deleteProfilePhoto("Bearer $token")
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorMsg = parseErrorResponse(response.errorBody())
                Log.e("UserRepository", "Delete profile photo failed: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Delete profile photo exception: ${e.message}", e)
            Result.failure(e)
        }
    }

}