package com.mobile.tugasrancangmoka.repository

import com.mobile.tugasrancangmoka.api.ApiService
import com.mobile.tugasrancangmoka.model.LoginRequest
import com.mobile.tugasrancangmoka.model.LoginResponse
import retrofit2.Response

class AuthRepo(private val apiService: ApiService) {
    suspend fun login(request: LoginRequest): Response<LoginResponse> {
        return apiService.login(request)
    }
}
