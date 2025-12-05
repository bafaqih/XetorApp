// app/src/main/java/id/xetor/app/ui/profile/AddressViewModel.kt
package id.xetor.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.xetor.app.data.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// State untuk UI Address
data class AddressUiState(
    val isLoading: Boolean = true,
    val fullname: String = "",
    val phone: String = "",
    val address: String = "",
    val cityRegency: String = "",
    val province: String = "",
    val postalCode: String = "",
    val fullnameError: String? = null,
    val phoneError: String? = null,
    val addressError: String? = null,
    val cityRegencyError: String? = null,
    val provinceError: String? = null,
    val postalCodeError: String? = null,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val addressId: Int? = null // ID alamat jika sudah ada (untuk update)
)

class AddressViewModel(
    private val userRepository: UserRepository,
    private val token: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddressUiState())
    val uiState = _uiState.asStateFlow()
    
    // Simpan nilai original untuk perbandingan
    private var originalFullname: String = ""
    private var originalPhone: String = ""
    private var originalAddress: String = ""
    private var originalCityRegency: String = ""
    private var originalProvince: String = ""
    private var originalPostalCode: String = ""

    init {
        loadAddress()
    }
    
    // Cek apakah ada perubahan dari nilai original
    fun hasChanges(): Boolean {
        val state = _uiState.value
        return state.fullname != originalFullname ||
                state.phone != originalPhone ||
                state.address != originalAddress ||
                state.cityRegency != originalCityRegency ||
                state.province != originalProvince ||
                state.postalCode != originalPostalCode
    }
    
    // Cek apakah semua field terisi
    fun allFieldsFilled(): Boolean {
        val state = _uiState.value
        return state.fullname.isNotEmpty() &&
                state.phone.isNotEmpty() &&
                state.address.isNotEmpty() &&
                state.cityRegency.isNotEmpty() &&
                state.province.isNotEmpty() &&
                state.postalCode.isNotEmpty()
    }
    
    // Cek apakah button simpan harus ditampilkan
    fun shouldShowSaveButton(): Boolean {
        val state = _uiState.value
        // Jika belum punya alamat: semua field harus terisi
        if (state.addressId == null) {
            return allFieldsFilled()
        }
        // Jika sudah punya alamat: harus ada perubahan
        return hasChanges()
    }

    fun loadAddress() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            val result = userRepository.getUserAddress(token)
            
            if (result.isSuccess) {
                val address = result.getOrNull()
                val fullname = address?.fullname ?: ""
                val phone = address?.phone ?: ""
                val addressValue = address?.address ?: ""
                val cityRegency = address?.cityRegency ?: ""
                val province = address?.province ?: ""
                val postalCode = address?.getPostalCode() ?: ""
                
                // Simpan nilai original
                originalFullname = fullname
                originalPhone = phone
                originalAddress = addressValue
                originalCityRegency = cityRegency
                originalProvince = province
                originalPostalCode = postalCode
                
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        fullname = fullname,
                        phone = phone,
                        address = addressValue,
                        cityRegency = cityRegency,
                        province = province,
                        postalCode = postalCode,
                        addressId = address?.id
                    )
                }
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Gagal memuat alamat"
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = errorMsg
                    )
                }
            }
        }
    }

    fun updateFullname(fullname: String) {
        _uiState.update { 
            it.copy(
                fullname = fullname,
                fullnameError = null // Clear error when user types
            ) 
        }
    }

    fun updatePhone(phone: String) {
        _uiState.update { 
            it.copy(
                phone = phone,
                phoneError = null // Clear error when user types
            ) 
        }
    }

    fun updateAddress(address: String) {
        _uiState.update { 
            it.copy(
                address = address,
                addressError = null // Clear error when user types
            ) 
        }
    }

    fun updateCityRegency(cityRegency: String) {
        _uiState.update { 
            it.copy(
                cityRegency = cityRegency,
                cityRegencyError = null // Clear error when user types
            ) 
        }
    }

    fun updateProvince(province: String) {
        _uiState.update { 
            it.copy(
                province = province,
                provinceError = null // Clear error when user types
            ) 
        }
    }

    fun updatePostalCode(postalCode: String) {
        _uiState.update { 
            it.copy(
                postalCode = postalCode,
                postalCodeError = null // Clear error when user types
            ) 
        }
    }

    fun saveAddress() {
        val state = _uiState.value
        
        // Clear previous errors
        var hasError = false
        
        // Validate all fields (all required)
        if (state.fullname.isEmpty()) {
            _uiState.update { it.copy(fullnameError = "Nama lengkap harus diisi") }
            hasError = true
        }
        
        if (state.phone.isEmpty()) {
            _uiState.update { it.copy(phoneError = "No. WhatsApp harus diisi") }
            hasError = true
        }
        
        if (state.address.isEmpty()) {
            _uiState.update { it.copy(addressError = "Alamat harus diisi") }
            hasError = true
        }
        
        if (state.cityRegency.isEmpty()) {
            _uiState.update { it.copy(cityRegencyError = "Kota/Kabupaten harus diisi") }
            hasError = true
        }
        
        if (state.province.isEmpty()) {
            _uiState.update { it.copy(provinceError = "Provinsi harus diisi") }
            hasError = true
        }
        
        if (state.postalCode.isEmpty()) {
            _uiState.update { it.copy(postalCodeError = "Kode pos harus diisi") }
            hasError = true
        }
        
        if (hasError) {
            return
        }
        
        // Call API
        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    isSaving = true,
                    errorMessage = null,
                    successMessage = null
                ) 
            }
            
            val result = userRepository.saveUserAddress(
                token = token,
                addressId = state.addressId,
                fullname = state.fullname,
                phone = state.phone,
                address = state.address,
                cityRegency = state.cityRegency,
                province = state.province,
                postalCode = state.postalCode
            )
            
            if (result.isSuccess) {
                // Success - reload address to get updated data
                val successMsg = if (state.addressId != null) {
                    "Alamat berhasil diperbarui"
                } else {
                    "Alamat berhasil disimpan"
                }
                
                // Reload untuk update original values
                viewModelScope.launch {
                    val reloadResult = userRepository.getUserAddress(token)
                    if (reloadResult.isSuccess) {
                        val address = reloadResult.getOrNull()
                        val fullname = address?.fullname ?: ""
                        val phone = address?.phone ?: ""
                        val addressValue = address?.address ?: ""
                        val cityRegency = address?.cityRegency ?: ""
                        val province = address?.province ?: ""
                        val postalCode = address?.getPostalCode() ?: ""
                        
                        // Update original values
                        originalFullname = fullname
                        originalPhone = phone
                        originalAddress = addressValue
                        originalCityRegency = cityRegency
                        originalProvince = province
                        originalPostalCode = postalCode
                        
                        _uiState.update { 
                            it.copy(
                                isSaving = false,
                                fullname = fullname,
                                phone = phone,
                                address = addressValue,
                                cityRegency = cityRegency,
                                province = province,
                                postalCode = postalCode,
                                addressId = address?.id,
                                successMessage = successMsg
                            ) 
                        }
                    } else {
                        // Jika reload gagal, tetap set success message
                        _uiState.update { 
                            it.copy(
                                isSaving = false,
                                successMessage = successMsg
                            ) 
                        }
                    }
                }
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Gagal menyimpan alamat"
                _uiState.update { 
                    it.copy(
                        isSaving = false,
                        errorMessage = errorMsg
                    ) 
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun clearSuccess() {
        _uiState.update { it.copy(successMessage = null) }
    }
}

