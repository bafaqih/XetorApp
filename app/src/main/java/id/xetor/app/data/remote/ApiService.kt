// ApiService.kt
package id.xetor.app.data.remote

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/register")
    suspend fun signUp(@Body request: SignUpRequest): Response<SignUpResponse>

    @POST("auth/google")
    suspend fun loginWithGoogle(@Body request: GoogleAuthRequest): Response<LoginResponse>

    @GET("user/profile")
    suspend fun getUserProfile(@Header("Authorization") token: String): Response<UserDto>

    @GET("user/wallet")
    suspend fun getUserWallet(@Header("Authorization") token: String): Response<WalletResponse>

    @GET("user/statistics")
    suspend fun getUserStatistics(@Header("Authorization") token: String): Response<StatisticsResponse>

    @GET("user/transactions")
    suspend fun getTransactionHistory(@Header("Authorization") token: String): Response<List<TransactionHistoryResponse>>

    @POST("user/withdraw")
    suspend fun submitWithdraw(
        @Header("Authorization") token: String,
        @Body request: WithdrawRequest
    ): Response<WithdrawResponse>

    @POST("user/topup")
    suspend fun requestTopup(
        @Header("Authorization") token: String,
        @Body request: TopupRequest
    ): Response<TopupResponse>

    @POST("user/transfer")
    suspend fun submitTransfer(
        @Header("Authorization") token: String,
        @Body request: TransferRequest
    ): Response<TransferResponse>

    @POST("user/convert/xp-to-rp")
    suspend fun convertXpToRp(
        @Header("Authorization") token: String,
        @Body request: ConversionRequest
    ): Response<ConversionResponse>

    @POST("user/convert/rp-to-xp")
    suspend fun convertRpToXp(
        @Header("Authorization") token: String,
        @Body request: ConversionRequest
    ): Response<ConversionResponse>

    @GET("public/payment-methods")
    suspend fun getPaymentMethods(): Response<List<PaymentMethodResponse>>

    @GET("public/promotion-banners")
    suspend fun getPromotionBanners(): Response<List<PromotionBannerResponse>>

    @DELETE("user/account")
    suspend fun deleteAccount(@Header("Authorization") token: String): Response<ResponseBody>

    @PUT("user/profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: UpdateProfileRequest
    ): Response<ResponseBody>

    @Multipart
    @POST("user/profile/photo")
    suspend fun uploadProfilePhoto(
        @Header("Authorization") token: String,
        @Part photo: MultipartBody.Part
    ): Response<UploadPhotoResponse>

    @DELETE("user/profile/photo")
    suspend fun deleteProfilePhoto(@Header("Authorization") token: String): Response<ResponseBody>
}