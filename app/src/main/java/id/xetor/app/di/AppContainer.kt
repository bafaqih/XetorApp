package id.xetor.app.di

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import id.xetor.app.auth.TokenExpiredManager
import id.xetor.app.data.UserRepository
import id.xetor.app.data.local.AppDatabase
import id.xetor.app.data.local.UserPreferences
import id.xetor.app.data.remote.ApiConfig
import id.xetor.app.data.remote.ApiService
import id.xetor.app.data.remote.AuthInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

// Ini adalah kelas yang membuat dan menyimpan semua dependensi
class AppContainer(private val context: Context) {

    // Konfigurasi Moshi (JSON Converter)
    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    // Konfigurasi OkHttpClient dengan AuthInterceptor
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(TokenExpiredManager))
            .build()
    }

    // Konfigurasi Retrofit (Network Client)
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    // ApiService (Singleton)
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    // Database Lokal (Room)
    private val database: AppDatabase by lazy {
        AppDatabase.getDatabase(context)
    }

    // Preferensi Lokal (DataStore)
    val userPreferences: UserPreferences by lazy {
        UserPreferences(context)
    }

    val userRepository: UserRepository by lazy {
        UserRepository(apiService, database.userDao(), userPreferences)
    }

    // TODO: Tambahkan partnerRepository di sini nanti
}