package com.mobile.tugasrancangmoka.repository

import com.mobile.tugasrancangmoka.api.ApiService
import com.mobile.tugasrancangmoka.model.InventoryResponse
import com.mobile.tugasrancangmoka.model.SingleInventoryResponse
import com.mobile.tugasrancangmoka.model.UpdateInventoryRequest
import com.mobile.tugasrancangmoka.model.UpdateStockRequest
import retrofit2.Response

class InventoryRepo(private val apiService: ApiService) {
    suspend fun getProducts(categoryId: Int?, search: String?): Response<InventoryResponse> {
        return apiService.getInventory(categoryId, search)
    }

//    suspend fun updateStock(id: Int, stock: Int): Response<com.mobile.tugasrancangmoka.model.SingleInventoryResponse> {
//        return apiService.updateStock(id, UpdateStockRequest(stock))
//    }

    suspend fun updateStock(id: Int, stock: Int): Response<SingleInventoryResponse> {
        return apiService.updateStock(id, UpdateInventoryRequest(currentStock = stock))
    }
}
