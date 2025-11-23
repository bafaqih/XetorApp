// app/src/main/java/id/xetor/app/ui/withdraw/WithdrawViewModel.kt
package id.xetor.app.ui.withdraw

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

// State untuk UI Withdraw
data class WithdrawUiState(
    val isLoading: Boolean = true,
    val wallet: WalletResponse? = null,
    val paymentMethods: List<PaymentMethod> = emptyList(),
    val allTransactions: List<TransactionHistoryResponse> = emptyList(),
    val filteredTransactions: List<TransactionHistoryResponse> = emptyList(),
    val selectedDateRange: DateRangeFilter = DateRangeFilter.SEVEN_DAYS,
    val selectedStatus: StatusFilter = StatusFilter.ALL,
    val errorMessage: String? = null
)

enum class DateRangeFilter(val days: Int, val label: String) {
    SEVEN_DAYS(7, "7 Hari Terakhir"),
    FOURTEEN_DAYS(14, "14 Hari Terakhir"),
    ONE_MONTH(30, "1 Bulan Terakhir")
}

enum class StatusFilter(val value: String?, val label: String) {
    ALL(null, "Semua"),
    COMPLETED("Completed", "Completed"),
    PENDING("Pending", "Pending"),
    FAILED("Failed", "Failed")
}

class WithdrawViewModel(
    private val userRepository: UserRepository,
    private val token: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(WithdrawUiState())
    val uiState = _uiState.asStateFlow()
    
    // Timestamp untuk track last refresh time
    private var lastRefreshTime: Long = 0
    // Minimum interval untuk refresh (30 detik)
    private val REFRESH_INTERVAL_MS = 30_000L // 30 detik

    init {
        loadWithdrawData()
    }

    /**
     * Load withdraw data dengan loading skeleton
     * Digunakan saat initial load atau manual refresh
     */
    fun loadWithdrawData(showLoading: Boolean = true) {
        viewModelScope.launch {
            if (showLoading) {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            }

            // Fetch wallet
            val walletResult = userRepository.getUserWallet(token)
            
            // Fetch payment methods
            val paymentMethodsResult = userRepository.getPaymentMethods()
            
            // Fetch transactions
            val transactionsResult = userRepository.getTransactionHistory(token)

            if (walletResult.isSuccess && paymentMethodsResult.isSuccess && transactionsResult.isSuccess) {
                val allTransactions = transactionsResult.getOrNull() ?: emptyList()
                
                // Filter only withdraw transactions
                val withdrawTransactions = allTransactions.filter { it.type == "withdraw" }
                
                // Convert API response to PaymentMethod list
                val paymentMethods = paymentMethodsResult.getOrNull()?.map { apiMethod ->
                    PaymentMethod(
                        id = apiMethod.id,
                        name = apiMethod.name,
                        iconRes = mapIconFromMethodName(apiMethod.name),
                        isAvailable = true  // Semua dari backend sudah Active
                    )
                } ?: emptyList()
                
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        wallet = walletResult.getOrNull(),
                        paymentMethods = paymentMethods,
                        allTransactions = withdrawTransactions,
                        filteredTransactions = filterTransactions(
                            withdrawTransactions,
                            it.selectedDateRange,
                            it.selectedStatus
                        ),
                        errorMessage = null
                    )
                }
                lastRefreshTime = System.currentTimeMillis()
            } else {
                val errorMsg = walletResult.exceptionOrNull()?.message
                    ?: paymentMethodsResult.exceptionOrNull()?.message
                    ?: transactionsResult.exceptionOrNull()?.message
                    ?: "Gagal memuat data"

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = errorMsg
                    )
                }
            }
        }
    }
    
    private fun mapIconFromMethodName(name: String): Int {
        return when (name.lowercase().trim()) {
            "gopay" -> id.xetor.app.R.drawable.ic_gopay
            "shopeepay" -> id.xetor.app.R.drawable.ic_spay
            "dana" -> id.xetor.app.R.drawable.ic_dana
            "ovo" -> id.xetor.app.R.drawable.ic_ovo
            "linkaja" -> id.xetor.app.R.drawable.ic_linkaja
            "bca" -> id.xetor.app.R.drawable.ic_bca
            "bri" -> id.xetor.app.R.drawable.ic_bri
            "bni" -> id.xetor.app.R.drawable.ic_bni
            "mandiri" -> id.xetor.app.R.drawable.ic_mandiri
            "bsi" -> id.xetor.app.R.drawable.ic_bsi
            else -> {
                // Log warning untuk unknown method
                android.util.Log.w("WithdrawViewModel", "Unknown payment method: $name, using placeholder")
                id.xetor.app.R.drawable.ic_shop  // Use shop icon as placeholder
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
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -dateRange.days)
        val cutoffDate = calendar.time
        
        return transactions.filter { transaction ->
            // Filter by date
            val transactionDate = parseTimestamp(transaction.timestamp)
            val isWithinDateRange = if (transactionDate != null) {
                transactionDate.after(cutoffDate)
            } else {
                // Jika gagal parse, exclude dari hasil (lebih aman)
                false
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
            // Handle microseconds: .123456 -> .123
            var cleanedTimestamp = timestamp.replace(" ", "")
            cleanedTimestamp = cleanedTimestamp.replace(Regex("\\.\\d{6}"), { matchResult ->
                "." + matchResult.value.substring(1, 4)
            })
            
            // Ganti Z dengan +0000
            if (cleanedTimestamp.endsWith("Z")) {
                cleanedTimestamp = cleanedTimestamp.replace("Z", "+0000")
            }
            
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault())
            dateFormat.parse(cleanedTimestamp)
        } catch (e: Exception) {
            // Fallback: tanpa milidetik
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

    /**
     * Full refresh dengan loading skeleton
     * Digunakan saat initial load atau manual refresh
     */
    fun refresh() {
        loadWithdrawData(showLoading = true)
    }

    /**
     * Silent refresh di background tanpa loading skeleton
     * Hanya refresh jika data sudah cukup lama (lebih dari REFRESH_INTERVAL_MS)
     * Digunakan saat kembali ke withdraw screen
     */
    fun silentRefresh() {
        val currentTime = System.currentTimeMillis()
        val timeSinceLastRefresh = currentTime - lastRefreshTime
        
        // Hanya refresh jika data sudah cukup lama atau belum pernah di-refresh
        if (lastRefreshTime == 0L || timeSinceLastRefresh >= REFRESH_INTERVAL_MS) {
            android.util.Log.d("WithdrawViewModel", "Silent refresh triggered (time since last: ${timeSinceLastRefresh}ms)")
            loadWithdrawData(showLoading = false)
        } else {
            android.util.Log.d("WithdrawViewModel", "Skip silent refresh (data masih fresh, time since last: ${timeSinceLastRefresh}ms)")
        }
    }
}

