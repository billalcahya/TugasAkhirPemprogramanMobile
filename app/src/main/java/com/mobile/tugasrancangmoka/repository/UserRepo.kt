package com.mobile.tugasrancangmoka.repository

import com.mobile.tugasrancangmoka.api.ApiService
import com.mobile.tugasrancangmoka.model.UserListResponse
import com.mobile.tugasrancangmoka.model.UserResponse // Import class yang baru disatukan tadi
import retrofit2.Response

class UserRepo(private val apiService: ApiService) {

    suspend fun getUsers(): Response<UserListResponse> {
        return apiService.getUsers()
    }

    suspend fun addUser(userMap: Map<String, String>): Response<UserResponse> {
        return apiService.addUser(userMap)
    }

    suspend fun updateUser(id: Int, userMap: Map<String, String>): Response<UserResponse> {
        return apiService.updateUser(id, userMap)
    }

    suspend fun deleteUser(id: Int): Response<UserResponse> = apiService.deleteUser(id)
}
