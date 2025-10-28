package id.xetor.app.data.remote

import com.squareup.moshi.Json

// Data yang dikirim ke server saat login
data class LoginRequest(
    val email: String,
    val password: String
)

// Data yang diterima dari server setelah login berhasil
data class LoginResponse(
    @Json(name = "token") val token: String,
    @Json(name = "user") val user: UserDto
)

// Struktur data user dari API
data class UserDto(
    @Json(name = "id") val id: String,
    @Json(name = "fullname") val fullname: String,
    @Json(name = "email") val email: String,
    @Json(name = "phone") val phone: String?,
    @Json(name = "photo") val photo: String?
)

// Data yang dikirim ke server saat registrasi
data class SignUpRequest(
    val fullname: String,
    val email: String,
    val phone: String,
    val password: String
)

// Data yang diterima dari server setelah registrasi berhasil
data class SignUpResponse(
    @Json(name = "message") val message: String
)

// Data yang dikirim ke server saat login dengan Google
data class GoogleAuthRequest(
    @Json(name = "id_token") val idToken: String
)