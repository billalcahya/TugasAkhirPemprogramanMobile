package com.mobile.tugasrancangmoka.repository

import android.content.Context
import com.mobile.tugasrancangmoka.api.ApiClient
import com.mobile.tugasrancangmoka.api.InventoryItem
import com.mobile.tugasrancangmoka.api.UpdateStockRequest

class InventoryRepository(private val context: Context) {

    private val api = ApiClient.getService(context)

    suspend fun getInventory(): Result<List<InventoryItem>> {
        return try {
            val response = api.getInventory()
            if (response.isSuccessful && response.body()?.status == "success") {
                val data = response.body()?.data ?: emptyList()
                Result.success(data)
            } else {
                val msg = response.body()?.message ?: "Gagal memuat inventori"
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Gagal terhubung ke server: ${e.message}"))
        }
    }

    suspend fun updateStock(productId: Int, newStock: Int, notes: String): Result<InventoryItem> {
        return try {
            val response = api.updateStock(productId, UpdateStockRequest(newStock, notes))
            if (response.isSuccessful && response.body()?.status == "success") {
                val data = response.body()?.data
                    ?: return Result.failure(Exception("Data stok tidak ditemukan"))
                Result.success(data)
            } else {
                val msg = response.body()?.message ?: "Gagal update stok"
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Gagal terhubung ke server: ${e.message}"))
        }
    }
}