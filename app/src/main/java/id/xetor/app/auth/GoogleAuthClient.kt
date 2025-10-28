// app/src/main/java/id/xetor/app/auth/GoogleAuthClient.kt
package id.xetor.app.auth

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import id.xetor.app.R // Pastikan R diimpor dengan benar

class GoogleAuthClient(
    private val context: Context,
) {

    // Konfigurasi untuk meminta idToken dari Google
    private val gso: GoogleSignInOptions by lazy {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.web_client_id))
            .requestEmail()
            .build()
    }

    // Klien Google Sign-In
    val googleSignInClient: GoogleSignInClient by lazy {
        GoogleSignIn.getClient(context, gso)
    }

    suspend fun getIdTokenFromIntent(intent: Intent): String? {
        val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
        return try {
            val account = task.getResult(ApiException::class.java)!!
            account.idToken
        } catch (e: ApiException) {
            null
        }
    }
}