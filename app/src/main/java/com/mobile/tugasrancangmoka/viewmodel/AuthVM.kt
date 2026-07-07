package com.mobile.tugasrancangmoka.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.tugasrancangmoka.model.LoginRequest
import com.mobile.tugasrancangmoka.model.LoginResponse
import com.mobile.tugasrancangmoka.repository.AuthRepo
import kotlinx.coroutines.launch
import retrofit2.Response

sealed class LoginResult {
    object Loading : LoginResult()
    data class Success(val response: LoginResponse) : LoginResult()
    data class Error(val message: String) : LoginResult()
}

class AuthVM(private val repository: AuthRepo) : ViewModel() {

    private val _loginState = MutableLiveData<LoginResult>()
    val loginState: LiveData<LoginResult> = _loginState

    fun loginUser(request: LoginRequest) {
        _loginState.value = LoginResult.Loading
        viewModelScope.launch {
            try {
                val response = repository.login(request)
                if (response.isSuccessful && response.body() != null) {
                    _loginState.value = LoginResult.Success(response.body()!!)
                } else {
                    _loginState.value = LoginResult.Error("Email atau password salah")
                }
            } catch (e: Exception) {
                _loginState.value = LoginResult.Error("Koneksi gagal: ${e.message}")
            }
        }
    }
}