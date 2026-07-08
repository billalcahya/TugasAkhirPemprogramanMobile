package com.mobile.tugasrancangmoka.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mobile.tugasrancangmoka.api.DashboardData
import com.mobile.tugasrancangmoka.repository.DashboardRepository
import kotlinx.coroutines.launch

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DashboardRepository(application)

    private val _dashboard = MutableLiveData<DashboardData>()
    val dashboard: LiveData<DashboardData> = _dashboard

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun loadDashboard() {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.getDashboard()
            _isLoading.value = false
            result.onSuccess { data ->
                _dashboard.value = data
            }.onFailure { e ->
                _error.value = e.message
            }
        }
    }
}