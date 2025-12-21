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

// Data wallet user dari API
data class WalletResponse(
    @Json(name = "id") val id: Int,
    @Json(name = "user_id") val userId: Int,
    @Json(name = "balance") val balance: String,
    @Json(name = "xpoin") val xpoin: Int,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "updated_at") val updatedAt: String
)

// Data statistik user dari API
data class StatisticsResponse(
    @Json(name = "id") val id: Int,
    @Json(name = "user_id") val userId: Int,
    @Json(name = "waste") val waste: String,
    @Json(name = "energy") val energy: String,
    @Json(name = "co2") val co2: String,
    @Json(name = "water") val water: String,
    @Json(name = "tree") val tree: Int,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "updated_at") val updatedAt: String
)

// Data partner info untuk deposit history (optional)
data class PartnerInfo(
    @Json(name = "id") val id: NullableInt32?,
    @Json(name = "name") val name: NullableString?,
    @Json(name = "photo") val photo: NullableString?
)

// Data transaction history dari API
data class TransactionHistoryResponse(
    @Json(name = "id") val id: String,
    @Json(name = "type") val type: String,  // deposit, withdraw, topup, transfer, convert
    @Json(name = "amount") val amount: NullableAmount?,
    @Json(name = "points") val points: NullablePoints?,
    @Json(name = "status") val status: String,
    @Json(name = "timestamp") val timestamp: String,
    @Json(name = "description") val description: String,
    @Json(name = "conversion_type") val conversionType: String? = null,  // xp_to_rp atau rp_to_xp (hanya untuk type="convert")
    @Json(name = "partner") val partner: PartnerInfo? = null  // Informasi partner (hanya untuk type="deposit")
)

// Helper untuk handle sql.NullString dari backend
data class NullableAmount(
    @Json(name = "String") val value: String?,
    @Json(name = "Valid") val valid: Boolean
) {
    fun getAmount(): String {
        return if (valid && value != null) value else "0"
    }
}

// Helper untuk handle sql.NullInt32 dari backend
data class NullablePoints(
    @Json(name = "Int32") val value: Int?,
    @Json(name = "Valid") val valid: Boolean
) {
    fun getPoints(): Int {
        return if (valid && value != null) value else 0
    }
}

// Helper untuk handle sql.NullInt32 dari backend (untuk partner ID)
data class NullableInt32(
    @Json(name = "Int32") val value: Int?,
    @Json(name = "Valid") val valid: Boolean
)

// Data waste detail dari API untuk scan result
data class WasteDetailResponse(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "waste_type_id") val wasteTypeId: Int,
    @Json(name = "waste_type_name") val wasteTypeName: String,
    @Json(name = "proper_disposal_method") val properDisposalMethod: String,
    @Json(name = "positive_impact") val positiveImpact: String,
    @Json(name = "decomposition_time") val decompositionTime: String,
    @Json(name = "xpoin") val xpoin: Int
)

// Helper untuk handle sql.NullString dari backend
data class NullableString(
    @Json(name = "String") val value: String?,
    @Json(name = "Valid") val valid: Boolean
) {
    fun getString(): String? {
        return if (valid && value != null) value else null
    }
}

// Request withdraw
data class WithdrawRequest(
    @Json(name = "payment_method_id") val paymentMethodId: Int,
    @Json(name = "account_number") val accountNumber: String,
    @Json(name = "amount") val amount: Double,
    @Json(name = "account_holder_name") val accountHolderName: String? = null
)

// Response withdraw
data class WithdrawResponse(
    @Json(name = "message") val message: String,
    @Json(name = "order_id") val orderId: String
)

// Request topup
data class TopupRequest(
    @Json(name = "payment_method_id") val paymentMethodId: Int,
    @Json(name = "amount") val amount: Double
)

// Response topup
data class TopupResponse(
    @Json(name = "message") val message: String,
    @Json(name = "order_id") val orderId: String,
    @Json(name = "snap_token") val snapToken: String,
    @Json(name = "redirect_url") val redirectUrl: String
)

// Payment method dari API
data class PaymentMethodResponse(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "type") val type: String,
    @Json(name = "status") val status: String
)

// Promotion banner dari API
data class PromotionBannerResponse(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "image") val image: String,
    @Json(name = "link") val link: String?,
    @Json(name = "status") val status: String,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "updated_at") val updatedAt: String
)

// Request transfer
data class TransferRequest(
    @Json(name = "amount") val amount: Int,
    @Json(name = "recipient_email") val recipientEmail: String
)

// Response transfer
data class TransferResponse(
    @Json(name = "message") val message: String,
    @Json(name = "order_id") val orderId: String
)

// Request conversion
data class ConversionRequest(
    @Json(name = "amount") val amount: Double
)

// Response conversion
data class ConversionResponse(
    @Json(name = "message") val message: String,
    @Json(name = "wallet") val wallet: WalletResponse
)

// Request update profile
data class UpdateProfileRequest(
    @Json(name = "fullname") val fullname: String,
    @Json(name = "email") val email: String,
    @Json(name = "phone") val phone: String
)

// Response upload photo
data class UploadPhotoResponse(
    @Json(name = "message") val message: String,
    @Json(name = "photo_url") val photoUrl: String
)

// Request change password
data class ChangePasswordRequest(
    @Json(name = "old_password") val oldPassword: String,
    @Json(name = "new_password") val newPassword: String,
    @Json(name = "confirm_new_password") val confirmNewPassword: String
)

// User Address Response
data class UserAddressResponse(
    @Json(name = "id") val id: Int,
    @Json(name = "user_id") val userId: Int,
    @Json(name = "fullname") val fullname: String,
    @Json(name = "phone") val phone: String,
    @Json(name = "address") val address: String,
    @Json(name = "city_regency") val cityRegency: String,
    @Json(name = "province") val province: String,
    @Json(name = "postal_code") val postalCode: NullableString?,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "updated_at") val updatedAt: String
) {
    fun getPostalCode(): String? {
        return postalCode?.getString()
    }
}

// Request create user address
data class CreateUserAddressRequest(
    @Json(name = "fullname") val fullname: String,
    @Json(name = "phone") val phone: String,
    @Json(name = "address") val address: String,
    @Json(name = "city_regency") val cityRegency: String,
    @Json(name = "province") val province: String,
    @Json(name = "postal_code") val postalCode: String
)

// Request update user address
data class UpdateUserAddressRequest(
    @Json(name = "fullname") val fullname: String,
    @Json(name = "phone") val phone: String,
    @Json(name = "address") val address: String,
    @Json(name = "city_regency") val cityRegency: String,
    @Json(name = "province") val province: String,
    @Json(name = "postal_code") val postalCode: String
)

// About Xetor Response (for version, terms, privacy policy, etc.)
data class AboutXetorResponse(
    @Json(name = "id") val id: Int,
    @Json(name = "title") val title: String,
    @Json(name = "content") val content: String,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "updated_at") val updatedAt: String
)

// Public Partner Response (for /public/partners endpoint)
data class PublicPartnerResponse(
    @Json(name = "id") val id: Int,
    @Json(name = "business_name") val businessName: String,
    @Json(name = "photo") val photo: NullableString?,
    @Json(name = "address") val address: NullableString?,
    @Json(name = "city_regency") val cityRegency: NullableString?,
    @Json(name = "province") val province: NullableString?,
    @Json(name = "postal_code") val postalCode: NullableString?,
    @Json(name = "latitude") val latitude: NullableDouble?,
    @Json(name = "longitude") val longitude: NullableDouble?
) {
    fun getPhoto(): String? = photo?.getString()
    fun getAddress(): String? = address?.getString()
    fun getCityRegency(): String? = cityRegency?.getString()
    fun getProvince(): String? = province?.getString()
    fun getPostalCode(): String? = postalCode?.getString()
    fun getLatitude(): Double? = latitude?.getDouble()
    fun getLongitude(): Double? = longitude?.getDouble()
    
    fun getFullAddress(): String {
        val parts = mutableListOf<String>()
        getAddress()?.let { parts.add(it) }
        getCityRegency()?.let { parts.add(it) }
        getProvince()?.let { parts.add(it) }
        getPostalCode()?.let { parts.add(it) }
        return parts.joinToString(", ")
    }
}

// Helper untuk handle sql.NullFloat64 dari backend
data class NullableDouble(
    @Json(name = "Float64") val value: Double?,
    @Json(name = "Valid") val valid: Boolean
) {
    fun getDouble(): Double? {
        return if (valid && value != null) value else null
    }
}

// Response generate QR token
data class GenerateQrTokenResponse(
    @Json(name = "token") val token: String,
    @Json(name = "expires_at") val expiresAt: String
)