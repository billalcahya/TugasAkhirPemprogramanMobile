package com.mobile.tugasrancangmoka.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mobile.tugasrancangmoka.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository(application)

    // Loading state
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    // Login result
    private val _loginResult = MutableLiveData<Result<String>>()
    val loginResult: LiveData<Result<String>> = _loginResult

    // Logout result
    private val _logoutResult = MutableLiveData<Result<String>>()
    val logoutResult: LiveData<Result<String>> = _logoutResult

    fun login(email: String, password: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.login(email, password)
            _loginResult.value = result
            _isLoading.value = false
        }
    }

    fun logout() {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.logout()
            _logoutResult.value = result
            _isLoading.value = false
        }
    }
}
