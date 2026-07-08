package com.mobile.tugasrancangmoka.repository

import android.content.Context
import com.mobile.tugasrancangmoka.api.ApiClient
import com.mobile.tugasrancangmoka.api.Category
import com.mobile.tugasrancangmoka.api.Product

class ProductRepository(private val context: Context) {

    private val api = ApiClient.getService(context)

    suspend fun getProducts(): Result<List<Product>> {
        return try {
            val response = api.getProducts()
            if (response.isSuccessful && response.body()?.status == "success") {
                val data = response.body()?.data ?: emptyList()
                Result.success(data)
            } else {
                val msg = response.body()?.message ?: "Gagal memuat produk"
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Gagal terhubung ke server: ${e.message}"))
        }
    }

    suspend fun getProduct(id: Int): Result<Product> {
        return try {
            val response = api.getProduct(id)
            if (response.isSuccessful && response.body()?.status == "success") {
                val data = response.body()?.data
                    ?: return Result.failure(Exception("Produk tidak ditemukan"))
                Result.success(data)
            } else {
                val msg = response.body()?.message ?: "Gagal memuat produk"
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Gagal terhubung ke server: ${e.message}"))
        }
    }

    suspend fun getCategories(): Result<List<Category>> {
        return try {
            val response = api.getCategories()
            if (response.isSuccessful && response.body()?.status == "success") {
                val data = response.body()?.data ?: emptyList()
                Result.success(data)
            } else {
                val msg = response.body()?.message ?: "Gagal memuat kategori"
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Gagal terhubung ke server: ${e.message}"))
        }
    }
}