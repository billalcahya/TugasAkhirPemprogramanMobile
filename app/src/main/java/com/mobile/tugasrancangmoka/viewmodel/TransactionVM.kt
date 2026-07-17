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

    private val _detailState = MutableLiveData<DetailResult?>()
    val detailState: LiveData<DetailResult?> = _detailState

    private val _voidState = MutableLiveData<VoidResult?>()
    val voidState: LiveData<VoidResult?> = _voidState

    fun fetchTransactionHistory(status: String? = null) {
        _historyState.postValue(HistoryResult.Loading)
        viewModelScope.launch {
            try {
                val response = repository.getTransactions(status)
                if (response.isSuccessful && response.body() != null) {
                    _historyState.postValue(HistoryResult.Success(response.body()?.data.orEmpty()))
                } else {
                    _historyState.postValue(HistoryResult.Error("Gagal memuat riwayat transaksi"))
                }
            } catch (e: Exception) {
                _historyState.postValue(HistoryResult.Error(com.mobile.tugasrancangmoka.utils.ErrorUtils.getFriendlyMessage(e)))
            }
        }
    }

    fun fetchTransactionDetail(id: Int) {
        _detailState.postValue(DetailResult.Loading)
        viewModelScope.launch {
            try {
                val response = repository.getTransactionDetail(id)
                val body = response.body()

                if (response.isSuccessful && body != null && !body.data.isNullOrEmpty()) {
                    val transactionRecord = body.data.first()
                    _detailState.postValue(DetailResult.Success(transactionRecord))
                } else {
                    _detailState.postValue(DetailResult.Error("Gagal memuat detail transaksi"))
                }
            } catch (e: Exception) {
                _detailState.postValue(DetailResult.Error(com.mobile.tugasrancangmoka.utils.ErrorUtils.getFriendlyMessage(e)))
            }
        }
    }

    fun voidTransaction(id: Int, reason: String) {
        _voidState.postValue(VoidResult.Loading)
        viewModelScope.launch {
            try {
                val response = repository.voidTransaction(id, reason)
                if (response.isSuccessful) {
                    _voidState.postValue(VoidResult.Success)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = com.mobile.tugasrancangmoka.utils.ErrorUtils.parseApiError(errorBody)
                    _voidState.postValue(VoidResult.Error(errorMsg))
                }
            } catch (e: Exception) {
                _voidState.postValue(VoidResult.Error(com.mobile.tugasrancangmoka.utils.ErrorUtils.getFriendlyMessage(e)))
            }
        }
    }

    fun clearDetailState() {
        _detailState.postValue(null)
    }

    fun clearVoidState() {
        _voidState.postValue(null)
    }
}