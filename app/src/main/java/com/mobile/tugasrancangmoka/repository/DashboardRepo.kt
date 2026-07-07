package com.mobile.tugasrancangmoka.repository

import com.mobile.tugasrancangmoka.api.ApiService
import com.mobile.tugasrancangmoka.model.DashboardResponse
import retrofit2.Response

class DashboardRepo(private val apiService: ApiService) {
    suspend fun getDashboardData(): Response<DashboardResponse> = apiService.getDashboardData()
}