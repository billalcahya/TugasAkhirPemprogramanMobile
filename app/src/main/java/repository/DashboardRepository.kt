package com.mobile.tugasrancangmoka.repository

import android.content.Context
import com.mobile.tugasrancangmoka.api.ApiClient
import com.mobile.tugasrancangmoka.api.DashboardData

class DashboardRepository(private val context: Context) {

    private val api = ApiClient.getService(context)

    suspend fun getDashboard(): Result<DashboardData> {
        return try {
            val response = api.getDashboard()
            if (response.isSuccessful && response.body()?.status == "success") {
                val data = response.body()?.data
                    ?: return Result.failure(Exception("Data dashboard tidak ditemukan"))
                Result.success(data)
            } else {
                val msg = response.body()?.message ?: "Gagal memuat dashboard"
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Gagal terhubung ke server: ${e.message}"))
        }
    }
}