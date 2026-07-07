package com.mobile.tugasrancangmoka.api

import com.mobile.tugasrancangmoka.model.Category
import com.mobile.tugasrancangmoka.model.CheckoutRequest
import com.mobile.tugasrancangmoka.model.CheckoutResponse
import com.mobile.tugasrancangmoka.model.DashboardResponse
import com.mobile.tugasrancangmoka.model.LoginRequest
import com.mobile.tugasrancangmoka.model.LoginResponse
import com.mobile.tugasrancangmoka.model.Product
import com.mobile.tugasrancangmoka.model.ReportResponse
import com.mobile.tugasrancangmoka.model.TransactionDetailResponse
import com.mobile.tugasrancangmoka.model.TransactionResponse
import com.mobile.tugasrancangmoka.model.VoidRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @GET("categories")
    suspend fun getCategories(): Response<List<Category>>

    @GET("products")
    suspend fun getProducts(
        @Query("category") categoryId: Int? = null,
        @Query("search") search: String? = null
    ): Response<List<Product>>

    @POST("transactions")
    suspend fun checkout(
        @Body request: CheckoutRequest
    ): Response<CheckoutResponse>

    @PUT("products/{id}/stock")
    suspend fun updateStock(
        @Path("id") id: Int,
        @Body request: com.mobile.tugasrancangmoka.model.UpdateStockRequest
    ): Response<Product>

    // Tambahkan baris ini di dalam interface ApiService.kt
    @GET("transactions")
    suspend fun getTransactions(
        @Query("status") status: String? = null
    ): Response<TransactionResponse>

    @GET("transactions/{id}")
    suspend fun getTransactionDetail(
        @Path("id") id: Int
    ): Response<TransactionDetailResponse>

    @PUT("transactions/{id}/void")
    suspend fun voidTransaction(
        @Path("id") id: Int,
        @Body request: VoidRequest
    ): Response<CheckoutResponse>

    @GET("dashboard")
    suspend fun getDashboardData(): Response<DashboardResponse>

    @GET("reports/daily")
    suspend fun getDailyReport(
        @Query("date") date: String // format: YYYY-MM-DD
    ): Response<ReportResponse>

    @GET("reports/monthly")
    suspend fun getMonthlyReport(
        @Query("month") month: String // format: YYYY-MM
    ): Response<ReportResponse>
}