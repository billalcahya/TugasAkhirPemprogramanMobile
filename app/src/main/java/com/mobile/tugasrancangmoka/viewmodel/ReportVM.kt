package com.mobile.tugasrancangmoka.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.tugasrancangmoka.model.ReportResponse
import com.mobile.tugasrancangmoka.repository.ReportRepo
import kotlinx.coroutines.launch

sealed class ReportResult {
    object Loading : ReportResult()
    data class Success(val response: ReportResponse) : ReportResult()
    data class Error(val message: String) : ReportResult()
}

class ReportVM(private val repository: ReportRepo) : ViewModel() {

    private val _reportState = MutableLiveData<ReportResult>()
    val reportState: LiveData<ReportResult> = _reportState

    fun fetchDailyReport(date: String) {
        _reportState.value = ReportResult.Loading
        viewModelScope.launch {
            try {
                val response = repository.getDailyReport(date)
                handleResponse(response)
            } catch (e: Exception) {
                _reportState.value = ReportResult.Error("Kesalahan jaringan: ${e.message}")
            }
        }
    }

    fun fetchMonthlyReport(month: String) {
        _reportState.value = ReportResult.Loading
        viewModelScope.launch {
            try {
                val response = repository.getMonthlyReport(month)
                handleResponse(response)
            } catch (e: Exception) {
                _reportState.value = ReportResult.Error("Kesalahan jaringan: ${e.message}")
            }
        }
    }

    private fun handleResponse(response: retrofit2.Response<ReportResponse>) {
        if (response.isSuccessful && response.body() != null) {
            _reportState.value = ReportResult.Success(response.body()!!)
        } else {
            _reportState.value = ReportResult.Error("Gagal memproses dokumen laporan")
        }
    }
}