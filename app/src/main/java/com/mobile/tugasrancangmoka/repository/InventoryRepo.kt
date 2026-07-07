package com.mobile.tugasrancangmoka.repository

import com.mobile.tugasrancangmoka.api.ApiService
import com.mobile.tugasrancangmoka.model.Product
import com.mobile.tugasrancangmoka.model.UpdateStockRequest
import retrofit2.Response

class InventoryRepo(private val apiService: ApiService) {
    suspend fun getProducts(categoryId: Int?, search: String?): Response<List<Product>> {
        return apiService.getProducts(categoryId, search)
    }

    suspend fun updateStock(id: Int, stock: Int): Response<Product> {
        return apiService.updateStock(id, UpdateStockRequest(stock))
    }
}
