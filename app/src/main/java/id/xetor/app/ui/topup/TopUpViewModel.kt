// app/src/main/java/id/xetor/app/ui/topup/TopUpViewModel.kt
package id.xetor.app.ui.topup

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

// State untuk UI TopUp
data class TopUpUiState(
    val isLoading: Boolean = true,
    val wallet: WalletResponse? = null,
    val allTransactions: List<TransactionHistoryResponse> = emptyList(),
    val filteredTransactions: List<TransactionHistoryResponse> = emptyList(),
    val selectedDateRange: DateRangeFilter = DateRangeFilter.ALL,
    val selectedStatus: StatusFilter = StatusFilter.ALL,
    val errorMessage: String? = null
)

enum class DateRangeFilter(val days: Int?, val label: String) {
    ALL(null, "Semua"),
    SEVEN_DAYS(7, "7 Hari"),
    FOURTEEN_DAYS(14, "14 Hari"),
    ONE_MONTH(30, "30 Hari")
}

enum class StatusFilter(val value: String?, val label: String) {
    ALL(null, "Semua"),
    COMPLETED("Completed", "Berhasil"),
    PENDING("Pending", "Diproses"),
    FAILED("Failed", "Gagal")
}

class TopUpViewModel(
    private val userRepository: UserRepository,
    private val token: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(TopUpUiState())
    val uiState = _uiState.asStateFlow()
    
    // Timestamp untuk track last refresh time
    private var lastRefreshTime: Long = 0
    // Minimum interval untuk refresh (30 detik)
    private val REFRESH_INTERVAL_MS = 30_000L // 30 detik

    init {
        // Load data saat init dengan skeleton
        loadTopUpData(showLoading = true)
    }

    /**
     * Load topup data dengan loading skeleton
     * Digunakan saat initial load atau manual refresh
     */
    fun loadTopUpData(showLoading: Boolean = true) {
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
                
                // Filter only topup transactions
                val topupTransactions = allTransactions.filter { it.type == "topup" }
                
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        wallet = walletResult.getOrNull(),
                        allTransactions = topupTransactions,
                        filteredTransactions = filterTransactions(
                            topupTransactions,
                            it.selectedDateRange,
                            it.selectedStatus
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

    fun setDateRangeFilter(dateRange: DateRangeFilter) {
        _uiState.update {
            it.copy(
                selectedDateRange = dateRange,
                filteredTransactions = filterTransactions(
                    it.allTransactions,
                    dateRange,
                    it.selectedStatus
                )
            )
        }
    }

    fun setStatusFilter(status: StatusFilter) {
        _uiState.update {
            it.copy(
                selectedStatus = status,
                filteredTransactions = filterTransactions(
                    it.allTransactions,
                    it.selectedDateRange,
                    status
                )
            )
        }
    }

    private fun filterTransactions(
        transactions: List<TransactionHistoryResponse>,
        dateRange: DateRangeFilter,
        status: StatusFilter
    ): List<TransactionHistoryResponse> {
        return transactions.filter { transaction ->
            // Filter by date
            val isWithinDateRange = if (dateRange.days == null) {
                true
            } else {
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_YEAR, -dateRange.days)
                val cutoffDate = calendar.time
                val transactionDate = parseTimestamp(transaction.timestamp)
                if (transactionDate != null) {
                    transactionDate.after(cutoffDate)
                } else {
                    false
                }
            }

            // Filter by status
            val matchesStatus = if (status.value == null) {
                true
            } else {
                transaction.status == status.value
            }

            isWithinDateRange && matchesStatus
        }
    }
    
    private fun parseTimestamp(timestamp: String): Date? {
        return try {
            var cleanedTimestamp = timestamp.replace(" ", "")
            cleanedTimestamp = cleanedTimestamp.replace(Regex("\\.\\d{6}"), { matchResult ->
                "." + matchResult.value.substring(1, 4)
            })
            
            if (cleanedTimestamp.endsWith("Z")) {
                cleanedTimestamp = cleanedTimestamp.replace("Z", "+0000")
            }
            
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault())
            dateFormat.parse(cleanedTimestamp)
        } catch (e: Exception) {
            try {
                var cleanedTimestamp = timestamp.replace(" ", "")
                cleanedTimestamp = cleanedTimestamp.replace(Regex("\\.\\d+"), "")
                
                if (cleanedTimestamp.endsWith("Z")) {
                    cleanedTimestamp = cleanedTimestamp.replace("Z", "+0000")
                }
                
                val dateFormatNoMillis = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault())
                dateFormatNoMillis.parse(cleanedTimestamp)
            } catch (e2: Exception) {
                null
            }
        }
    }

    fun setLoading(isLoading: Boolean) {
        _uiState.update { it.copy(isLoading = isLoading) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun refresh() {
        loadTopUpData(showLoading = true)
    }

    fun silentRefresh() {
        val currentTime = System.currentTimeMillis()
        val timeSinceLastRefresh = currentTime - lastRefreshTime
        
        if (lastRefreshTime == 0L || timeSinceLastRefresh >= REFRESH_INTERVAL_MS) {
            android.util.Log.d("TopUpViewModel", "Silent refresh triggered (time since last: ${timeSinceLastRefresh}ms)")
            loadTopUpData(showLoading = false)
        } else {
            android.util.Log.d("TopUpViewModel", "Skip silent refresh (data masih fresh, time since last: ${timeSinceLastRefresh}ms)")
        }
    }

    fun forceRefresh() {
        android.util.Log.d("TopUpViewModel", "Force refresh triggered (ignore interval)")
        loadTopUpData(showLoading = false)
    }
}

