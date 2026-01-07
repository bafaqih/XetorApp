// app/src/main/java/id/xetor/app/ui/setor/PickUpViewModel.kt
package id.xetor.app.ui.setor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.xetor.app.data.UserRepository
import id.xetor.app.data.remote.WasteDetailResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

data class PickUpUiState(
    val isLoading: Boolean = true,
    val wasteDetails: List<WasteDetailResponse> = emptyList(),
    val wasteItems: List<WasteItem> = emptyList(),
    val additionalInfo: String = "",
    val userLocation: GeoPoint? = null,
    val showSuccess: Boolean = false,
    val errorMessage: String? = null
)

class PickUpViewModel(
    private val userRepository: UserRepository,
    private val token: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(PickUpUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadWasteDetails()
    }

    fun loadWasteDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            val result = userRepository.getAllWasteDetails(token)
            
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        wasteDetails = result.getOrNull() ?: emptyList(),
                        errorMessage = null
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Gagal memuat data sampah"
                    )
                }
            }
        }
    }

    fun updateUserLocation(location: GeoPoint) {
        _uiState.update { it.copy(userLocation = location) }
    }

    fun addWasteItem(wasteDetail: WasteDetailResponse, weight: Double) {
        val newItem = WasteItem(
            wasteDetail = wasteDetail,
            weight = weight
        )
        _uiState.update {
            it.copy(wasteItems = it.wasteItems + newItem)
        }
    }

    fun updateWasteItem(itemId: String, wasteDetail: WasteDetailResponse, weight: Double) {
        _uiState.update {
            it.copy(
                wasteItems = it.wasteItems.map { item ->
                    if (item.id == itemId) {
                        WasteItem(
                            id = item.id,
                            wasteDetail = wasteDetail,
                            weight = weight
                        )
                    } else {
                        item
                    }
                }
            )
        }
    }

    fun removeWasteItem(itemId: String) {
        _uiState.update {
            it.copy(wasteItems = it.wasteItems.filter { it.id != itemId })
        }
    }

    fun setAdditionalInfo(info: String) {
        _uiState.update { it.copy(additionalInfo = info) }
    }

    fun confirmPickUp() {
        // Dummy confirmation - tidak mengirim ke backend
        _uiState.update { it.copy(showSuccess = true) }
    }

    fun resetSuccess() {
        _uiState.update { 
            it.copy(
                showSuccess = false,
                wasteItems = emptyList(),
                additionalInfo = ""
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

