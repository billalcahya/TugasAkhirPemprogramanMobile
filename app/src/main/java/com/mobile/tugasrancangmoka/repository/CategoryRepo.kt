package com.mobile.tugasrancangmoka.repository

import com.mobile.tugasrancangmoka.api.ApiService
import com.mobile.tugasrancangmoka.model.Category
import com.mobile.tugasrancangmoka.model.CategoryAddResponse
import com.mobile.tugasrancangmoka.model.CategoryRequest
import retrofit2.Response

class CategoryRepo(val apiService: ApiService) {
    suspend fun getCategories(): Response<List<Category>> { // Diubah di sini
        return apiService.getCategories()
    }

    suspend fun addCategory(name: String): Response<CategoryAddResponse> {
        return apiService.addCategory(CategoryRequest(name))
    }

    suspend fun updateCategory(id: Int, name: String): Response<CategoryAddResponse> {
        return apiService.updateCategory(id, CategoryRequest(name))
    }

    suspend fun deleteCategory(id: Int): Response<CategoryAddResponse> {
        return apiService.deleteCategory(id)
    }
}
