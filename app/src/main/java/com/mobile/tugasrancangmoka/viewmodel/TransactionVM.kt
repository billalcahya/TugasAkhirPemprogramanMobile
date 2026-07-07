package com.mobile.tugasrancangmoka.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.tugasrancangmoka.model.TransactionRecord
import com.mobile.tugasrancangmoka.repository.TransactionRepo
import kotlinx.coroutines.launch

sealed class HistoryResult {
    object Loading : HistoryResult()
    data class Success(val list: List<TransactionRecord>) : HistoryResult()
    data class Error(val message: String) : HistoryResult()
}

sealed class DetailResult {
    object Loading : DetailResult()
    data class Success(val record: TransactionRecord) : DetailResult()
    data class Error(val message: String) : DetailResult()
}

sealed class VoidResult {
    object Loading : VoidResult()
    object Success : VoidResult()
    data class Error(val message: String) : VoidResult()
}

class TransactionVM(private val repository: TransactionRepo) : ViewModel() {

    private val _historyState = MutableLiveData<HistoryResult>()
    val historyState: LiveData<HistoryResult> = _historyState

    private val _detailState = MutableLiveData<DetailResult>()
    val detailState: LiveData<DetailResult> = _detailState

    private val _voidState = MutableLiveData<VoidResult>()
    val voidState: LiveData<VoidResult> = _voidState

    fun fetchTransactionHistory(status: String? = null) {
        _historyState.value = HistoryResult.Loading
        viewModelScope.launch {
            try {
                val response = repository.getTransactions(status)
                if (response.isSuccessful && response.body() != null) {
                    _historyState.value = HistoryResult.Success(response.body()?.data.orEmpty())
                } else {
                    _historyState.value = HistoryResult.Error("Gagal memuat riwayat transaksi")
                }
            } catch (e: Exception) {
                _historyState.value = HistoryResult.Error("Koneksi bermasalah: ${e.message}")
            }
        }
    }

    fun fetchTransactionDetail(id: Int) {
        _detailState.value = DetailResult.Loading
        viewModelScope.launch {
            try {
                val response = repository.getTransactionDetail(id)
                if (response.isSuccessful && response.body()?.data != null) {
                    _detailState.value = DetailResult.Success(response.body()!!.data!!)
                } else {
                    _detailState.value = DetailResult.Error("Gagal memuat detail transaksi")
                }
            } catch (e: Exception) {
                _detailState.value = DetailResult.Error("Terjadi kesalahan jaringan")
            }
        }
    }

    fun voidTransaction(id: Int, reason: String) {
        _voidState.value = VoidResult.Loading
        viewModelScope.launch {
            try {
                val response = repository.voidTransaction(id, reason)
                if (response.isSuccessful) {
                    _voidState.value = VoidResult.Success
                    // Refresh data setelah berhasil di-void
                    fetchTransactionDetail(id)
                } else {
                    _voidState.value = VoidResult.Error("Gagal membatalkan transaksi")
                }
            } catch (e: Exception) {
                _voidState.value = VoidResult.Error("Koneksi gagal: ${e.message}")
            }
        }
    }
}