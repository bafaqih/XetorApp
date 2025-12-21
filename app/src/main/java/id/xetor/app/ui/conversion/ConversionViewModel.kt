// app/src/main/java/id/xetor/app/ui/conversion/ConversionViewModel.kt
package id.xetor.app.ui.conversion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.xetor.app.data.UserRepository
import id.xetor.app.data.remote.TransactionHistoryResponse
import id.xetor.app.data.remote.WalletResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// State untuk UI Conversion
data class ConversionUiState(
    val isLoading: Boolean = true,
    val wallet: WalletResponse? = null,
    val allTransactions: List<TransactionHistoryResponse> = emptyList(),
    val filteredTransactions: List<TransactionHistoryResponse> = emptyList(),
    val selectedConversionType: ConversionType = ConversionType.XP_TO_RP,
    val selectedDateRange: DateRangeFilter = DateRangeFilter.ALL,
    val selectedTypeFilter: TypeFilter = TypeFilter.XP_TO_RP, // Default sesuai selectedConversionType
    val amount: String = "",
    val isSubmitting: Boolean = false,
    val showSuccessDialog: Boolean = false,
    val errorMessage: String? = null
)

enum class ConversionType {
    XP_TO_RP,
    RP_TO_XP
}

enum class DateRangeFilter(val days: Int?, val label: String) {
    ALL(null, "Semua"),
    SEVEN_DAYS(7, "7 Hari"),
    FOURTEEN_DAYS(14, "14 Hari"),
    ONE_MONTH(30, "30 Hari")
}

enum class TypeFilter(val value: String?, val label: String) {
    ALL(null, "Semua"),
    XP_TO_RP("xp_to_rp", "Xp - Rp"),
    RP_TO_XP("rp_to_xp", "Rp - Xp")
}

class ConversionViewModel(
    private val userRepository: UserRepository,
    private val token: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConversionUiState())
    val uiState = _uiState.asStateFlow()
    
    // Timestamp untuk track last refresh time
    private var lastRefreshTime: Long = 0
    // Minimum interval untuk refresh (30 detik)
    private val REFRESH_INTERVAL_MS = 30_000L // 30 detik

    init {
        // Load data saat init dengan skeleton
        loadConversionData(showLoading = true)
    }

    /**
     * Load conversion data dengan loading skeleton
     * Digunakan saat initial load atau manual refresh
     */
    fun loadConversionData(showLoading: Boolean = true) {
        viewModelScope.launch {
            if (showLoading) {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            }

            // Fetch wallet
            val walletResult = userRepository.getUserWallet(token)
            
            // Fetch transactions
            val transactionsResult = userRepository.getTransactionHistory(token)

            if (walletResult.isSuccess && transactionsResult.isSuccess) {
                val allTransactions = transactionsResult.getOrNull() ?: emptyList()
                
                // Filter only conversion transactions
                val conversionTransactions = allTransactions.filter { it.type == "convert" }
                
                // Get current state untuk mendapatkan selectedConversionType
                val currentState = _uiState.value
                // Set type filter sesuai dengan selectedConversionType
                val typeFilter = when (currentState.selectedConversionType) {
                    ConversionType.XP_TO_RP -> TypeFilter.XP_TO_RP
                    ConversionType.RP_TO_XP -> TypeFilter.RP_TO_XP
                }
                
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        wallet = walletResult.getOrNull(),
                        allTransactions = conversionTransactions,
                        selectedTypeFilter = typeFilter, // Set filter sesuai conversion type
                        filteredTransactions = filterTransactions(
                            conversionTransactions,
                            it.selectedDateRange,
                            typeFilter
                        ),
                        errorMessage = null
                    )
                }
                lastRefreshTime = System.currentTimeMillis()
            } else {
                val errorMsg = walletResult.exceptionOrNull()?.message
                    ?: transactionsResult.exceptionOrNull()?.message
                    ?: "Gagal memuat data"

                _uiState.update {
                    it.copy(
                        isLoading = true,
                        errorMessage = errorMsg
                    )
                }
            }
        }
    }

    /**
     * Silent refresh tanpa skeleton loading
     * Digunakan saat kembali ke halaman (onResume)
     */
    fun silentRefresh() {
        val currentTime = System.currentTimeMillis()
        // Hanya refresh jika sudah lebih dari 30 detik sejak refresh terakhir
        if (currentTime - lastRefreshTime > REFRESH_INTERVAL_MS) {
            loadConversionData(showLoading = false)
        }
    }

    /**
     * Manual refresh dengan skeleton
     */
    fun refresh() {
        loadConversionData(showLoading = true)
    }

    fun setConversionType(type: ConversionType) {
        // Auto-update type filter to match selected conversion type
        val matchingTypeFilter = when (type) {
            ConversionType.XP_TO_RP -> TypeFilter.XP_TO_RP
            ConversionType.RP_TO_XP -> TypeFilter.RP_TO_XP
        }
        
        _uiState.update { 
            it.copy(
                selectedConversionType = type,
                selectedTypeFilter = matchingTypeFilter,
                amount = "", // Reset amount saat ganti type
                filteredTransactions = filterTransactions(
                    it.allTransactions,
                    it.selectedDateRange,
                    matchingTypeFilter
                )
            )
        }
    }

    fun setAmount(amount: String) {
        _uiState.update { it.copy(amount = amount) }
    }

    fun setDateRangeFilter(dateRange: DateRangeFilter) {
        _uiState.update { 
            it.copy(
                selectedDateRange = dateRange,
                filteredTransactions = filterTransactions(
                    it.allTransactions,
                    dateRange,
                    it.selectedTypeFilter
                )
            )
        }
    }

    fun setTypeFilter(typeFilter: TypeFilter) {
        _uiState.update { 
            it.copy(
                selectedTypeFilter = typeFilter,
                filteredTransactions = filterTransactions(
                    it.allTransactions,
                    it.selectedDateRange,
                    typeFilter
                )
            )
        }
    }

    private fun filterTransactions(
        transactions: List<TransactionHistoryResponse>,
        dateRange: DateRangeFilter,
        typeFilter: TypeFilter
    ): List<TransactionHistoryResponse> {
        var filtered = transactions

        // Filter by date range
        if (dateRange.days != null) {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -dateRange.days)
            val cutoffDate = calendar.time

            filtered = filtered.filter { transaction ->
                try {
                    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    val date = inputFormat.parse(transaction.timestamp.substringBefore("."))
                    date != null && date.after(cutoffDate)
                } catch (e: Exception) {
                    false
                }
            }
        }

        // Filter by type
        if (typeFilter.value != null) {
            filtered = filtered.filter { 
                it.conversionType == typeFilter.value
            }
        }

        return filtered
    }

    fun submitConversion() {
        val currentState = _uiState.value
        val amountValue = currentState.amount.filter { it.isDigit() }
        
        // Validasi
        if (amountValue.isEmpty()) {
            _uiState.update { 
                it.copy(errorMessage = "Masukkan nominal terlebih dahulu")
            }
            return
        }

        val amountDouble = amountValue.toDoubleOrNull()
        if (amountDouble == null || amountDouble <= 0) {
            _uiState.update { 
                it.copy(errorMessage = "Nominal harus lebih besar dari 0")
            }
            return
        }

        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }

        viewModelScope.launch {
            val result = when (currentState.selectedConversionType) {
                ConversionType.XP_TO_RP -> {
                    userRepository.convertXpToRp(token, amountDouble)
                }
                ConversionType.RP_TO_XP -> {
                    userRepository.convertRpToXp(token, amountDouble)
                }
            }

            if (result.isSuccess) {
                // Refresh data tanpa skeleton
                loadConversionData(showLoading = false)
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        showSuccessDialog = true,
                        amount = "", // Reset amount setelah berhasil
                        errorMessage = null
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Gagal melakukan konversi"
                    )
                }
            }
        }
    }

    fun dismissSuccessDialog() {
        _uiState.update { it.copy(showSuccessDialog = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun setLoading(loading: Boolean) {
        _uiState.update { it.copy(isLoading = loading) }
    }

    /**
     * Force refresh tanpa loading skeleton, selalu refresh tanpa cek interval
     * Digunakan saat konversi berhasil untuk langsung update data
     */
    fun forceRefresh() {
        loadConversionData(showLoading = false)
    }
}

