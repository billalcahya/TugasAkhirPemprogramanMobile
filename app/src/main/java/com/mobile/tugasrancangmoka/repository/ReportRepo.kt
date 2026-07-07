package com.mobile.tugasrancangmoka.repository

import com.mobile.tugasrancangmoka.api.ApiService
import com.mobile.tugasrancangmoka.model.ReportResponse
import retrofit2.Response

class ReportRepo(private val apiService: ApiService) {
    suspend fun getDailyReport(date: String): Response<ReportResponse> = apiService.getDailyReport(date)
    suspend fun getMonthlyReport(month: String): Response<ReportResponse> = apiService.getMonthlyReport(month)
}