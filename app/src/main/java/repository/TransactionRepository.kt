package com.mobile.tugasrancangmoka.repository

import android.content.Context
import com.mobile.tugasrancangmoka.api.ApiClient
import com.mobile.tugasrancangmoka.api.CheckoutResponse
import com.mobile.tugasrancangmoka.api.CreateTransactionRequest
import com.mobile.tugasrancangmoka.api.Transaction

class TransactionRepository(private val context: Context) {

    private val api = ApiClient.getService(context)

    suspend fun getTransactions(): Result<List<Transaction>> {
        return try {
            val response = api.getTransactions()
            if (response.isSuccessful && response.body()?.status == "success") {
                val data = response.body()?.data ?: emptyList()
                Result.success(data)
            } else {
                val msg = response.body()?.message ?: "Gagal memuat transaksi"
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Gagal terhubung ke server: ${e.message}"))
        }
    }

    suspend fun getTransaction(id: Int): Result<Transaction> {
        return try {
            val response = api.getTransaction(id)
            if (response.isSuccessful && response.body()?.status == "success") {
                val data = response.body()?.data
                    ?: return Result.failure(Exception("Transaksi tidak ditemukan"))
                Result.success(data)
            } else {
                val msg = response.body()?.message ?: "Gagal memuat transaksi"
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Gagal terhubung ke server: ${e.message}"))
        }
    }

    suspend fun checkout(request: CreateTransactionRequest): Result<CheckoutResponse> {
        return try {
            val response = api.checkout(request)
            if (response.isSuccessful && response.body()?.status == "success") {
                val data = response.body()?.data
                    ?: return Result.failure(Exception("Data checkout tidak ditemukan"))
                Result.success(data)
            } else {
                val msg = response.body()?.message ?: "Checkout gagal"
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Gagal terhubung ke server: ${e.message}"))
        }
    }
}