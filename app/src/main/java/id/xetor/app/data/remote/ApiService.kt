// ApiService.kt
package id.xetor.app.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/register")
    suspend fun signUp(@Body request: SignUpRequest): Response<SignUpResponse>

    @POST("auth/google")
    suspend fun loginWithGoogle(@Body request: GoogleAuthRequest): Response<LoginResponse>
}