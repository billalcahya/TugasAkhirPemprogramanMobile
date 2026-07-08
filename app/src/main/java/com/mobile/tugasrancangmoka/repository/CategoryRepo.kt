package com.mobile.tugasrancangmoka.repository

import com.mobile.tugasrancangmoka.api.ApiService
import com.mobile.tugasrancangmoka.model.Category
import retrofit2.Response

class CategoryRepo(private val apiService: ApiService) {
    suspend fun getCategories(): Response<List<Category>> {
        return apiService.getCategories()
    }
}