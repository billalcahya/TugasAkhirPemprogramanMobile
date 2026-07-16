package com.mobile.tugasrancangmoka.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.tugasrancangmoka.model.DashboardResponse
import com.mobile.tugasrancangmoka.repository.DashboardRepo
import kotlinx.coroutines.launch

sealed class DashboardResult {
    object Loading : DashboardResult()
    data class Success(val response: DashboardResponse) : DashboardResult()
    data class Error(val message: String) : DashboardResult()
}
class DashboardVM(private val repository: DashboardRepo) : ViewModel() {
    private val _dashboardState = MutableLiveData<DashboardResult>()
    val dashboardState: LiveData<DashboardResult> = _dashboardState
    fun fetchDashboard() {
        _dashboardState.postValue(DashboardResult.Loading)
        viewModelScope.launch {
            try {
                val response = repository.getDashboardData()
                if (response.isSuccessful && response.body() != null) {
                    _dashboardState.postValue(DashboardResult.Success(response.body()!!))
                } else {
                    _dashboardState.postValue(DashboardResult.Error("Gagal mengambil data ringkasan dashboard"))
                }
            } catch (e: Exception) {
                _dashboardState.postValue(DashboardResult.Error(com.mobile.tugasrancangmoka.utils.ErrorUtils.getFriendlyMessage(e)))
            }
        }
    }
}