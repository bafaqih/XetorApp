package id.xetor.app

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import id.xetor.app.di.AppContainer
import okhttp3.OkHttpClient
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class XetorApplication : Application(), ImageLoaderFactory {

    lateinit var appContainer: AppContainer
        private set

    // Callback untuk refresh home setelah transaksi berhasil
    var onHomeRefreshRequested: (() -> Unit)? = null
        private set

    fun setHomeRefreshCallback(callback: (() -> Unit)?) {
        onHomeRefreshRequested = callback
    }

    fun triggerHomeRefresh() {
        onHomeRefreshRequested?.invoke()
    }

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
    }

    override fun newImageLoader(): ImageLoader {
        // WARNING: Konfigurasi ini hanya untuk development.
        // Ini membuat ImageLoader menerima SEMUA sertifikat HTTPS (termasuk yang tidak valid).
        // Untuk production, sebaiknya dihapus dan pakai konfigurasi default yang aman.

        // Trust manager yang menerima semua sertifikat
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })

        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustAllCerts, SecureRandom())
        val trustManager = trustAllCerts[0] as X509TrustManager

        val okHttpClient = OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustManager)
            .hostnameVerifier { _, _ -> true } // Terima semua hostname
            .build()

        return ImageLoader.Builder(this)
            .okHttpClient(okHttpClient)
            .build()
    }
}