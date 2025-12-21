// AuthInterceptor.kt
package id.xetor.app.data.remote

import android.util.Log
import id.xetor.app.auth.TokenExpiredManager
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor untuk mendeteksi response 401 Unauthorized
 * dan memberitahu TokenExpiredManager bahwa token telah expired
 */
class AuthInterceptor(
    private val tokenExpiredManager: TokenExpiredManager
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        
        // Deteksi 401 Unauthorized
        if (response.code == 401) {
            Log.w("AuthInterceptor", "Received 401 Unauthorized - Token expired")
            // Notify global state bahwa token expired
            tokenExpiredManager.markTokenExpired(true)
        }
        
        return response
    }
}

