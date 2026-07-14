package com.mobile.tugasrancangmoka.api

import com.mobile.tugasrancangmoka.model.Category
import com.mobile.tugasrancangmoka.model.CategoryAddResponse
import com.mobile.tugasrancangmoka.model.CategoryRequest
import com.mobile.tugasrancangmoka.model.CategoryResponse
import com.mobile.tugasrancangmoka.model.CategoryListResponse
import com.mobile.tugasrancangmoka.model.CheckoutRequest
import com.mobile.tugasrancangmoka.model.CheckoutResponse
import com.mobile.tugasrancangmoka.model.DashboardResponse
import com.mobile.tugasrancangmoka.model.InventoryResponse
import com.mobile.tugasrancangmoka.model.LoginRequest
import com.mobile.tugasrancangmoka.model.LoginResponse
import com.mobile.tugasrancangmoka.model.Product
import com.mobile.tugasrancangmoka.model.ProductResponse
import com.mobile.tugasrancangmoka.model.ReportResponse
import com.mobile.tugasrancangmoka.model.SingleProductResponse
import com.mobile.tugasrancangmoka.model.TransactionDetailResponse
import com.mobile.tugasrancangmoka.model.TransactionResponse
import com.mobile.tugasrancangmoka.model.User
import com.mobile.tugasrancangmoka.model.UserListResponse
import com.mobile.tugasrancangmoka.model.UserResponse
import com.mobile.tugasrancangmoka.model.VoidRequest
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @GET("categories")
    suspend fun getCategories(): Response<List<Category>>

    @POST("categories")
    suspend fun addCategory(
        @Body request: CategoryRequest
    ): Response<CategoryAddResponse>

    @PUT("categories/{id}")
    suspend fun updateCategory(
        @Path("id") id: Int,
        @Body request: CategoryRequest
    ): Response<CategoryAddResponse>

    @DELETE("categories/{id}")
    suspend fun deleteCategory(
        @Path("id") id: Int
    ): Response<CategoryAddResponse>

    @GET("products")
    suspend fun getProducts(
        @Query("category_id") categoryId: Int?,
        @Query("search") search: String?
    ): Response<List<Product>>

    @POST("products")
    suspend fun addProduct(@Body product: Product): Response<SingleProductResponse>

    @Multipart
    @POST("products")
    suspend fun addProductMultipart(
        @Part productData: MultipartBody.Part,
        @Part productImage: MultipartBody.Part?
    ): Response<SingleProductResponse>

    @PUT("products/{id}")
    suspend fun updateProduct(
        @Path("id") id: Int,
        @Body product: Product
    ): Response<SingleProductResponse>

    @DELETE("products/{id}")
    suspend fun deleteProduct(@Path("id") id: Int): Response<SingleProductResponse>


    @POST("transactions")
    suspend fun checkout(
        @Body request: CheckoutRequest
    ): Response<CheckoutResponse>

    @PUT("inventory/{id}") // Diubah sesuai rute router.put('/:id') pada inventoryRoutes
    suspend fun updateStock(
        @Path("id") id: Int,
        @Body request: com.mobile.tugasrancangmoka.model.UpdateStockRequest
    ): Response<com.mobile.tugasrancangmoka.model.SingleInventoryResponse> // Mengembalikan SingleInventoryResponse atau sesuaikan dengan pembungkus object InventoryItem

    @GET("inventory")
    suspend fun getInventory(
        @Query("category") categoryId: Int? = null,
        @Query("search") search: String? = null
    ): Response<InventoryResponse>

    @GET("transactions")
    suspend fun getTransactions(
        @Query("status") status: String? = null
    ): Response<TransactionResponse>

    @GET("transactions/{id}")
    suspend fun getTransactionDetail(
        @Path("id") id: Int
    ): Response<TransactionDetailResponse>

    @PATCH("transactions/{id}/void")
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

    @GET("profile/all")
    suspend fun getUsers(): Response<UserListResponse>

    @POST("profile")
    suspend fun addUser(@Body userData: Map<String, String>): Response<UserResponse>

    @PUT("profile/{id}")
    suspend fun updateUser(
        @Path("id") id: Int,
        @Body userData: Map<String, String>
    ): Response<UserResponse>

    @DELETE("profile/{id}")
    suspend fun deleteUser(@Path("id") id: Int): Response<UserResponse>
}