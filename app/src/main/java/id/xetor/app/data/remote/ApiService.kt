// ApiService.kt
package id.xetor.app.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

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
}