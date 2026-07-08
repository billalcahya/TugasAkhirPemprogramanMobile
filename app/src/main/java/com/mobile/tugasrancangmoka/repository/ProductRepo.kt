package com.mobile.tugasrancangmoka.repository

import com.mobile.tugasrancangmoka.api.ApiService
import com.mobile.tugasrancangmoka.model.Product
import retrofit2.Response

class ProductRepo(private val apiService: ApiService) {
    suspend fun getProducts(categoryId: Int?, search: String?): Response<com.mobile.tugasrancangmoka.model.ProductResponse> {
        return apiService.getProducts(categoryId, search)
    }
}