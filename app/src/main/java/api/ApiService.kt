package com.mobile.tugasrancangmoka.api

import retrofit2.Response
import retrofit2.http.*

// ==================== REQUEST MODELS ====================

data class LoginRequest(
    val email: String,
    val password: String
)

data class CreateTransactionRequest(
    val items: List<CartItemRequest>,
    val discountAmount: Double = 0.0,
    val paymentMethod: String,
    val paymentAmount: Double
)

data class CartItemRequest(
    val productId: Int,
    val quantity: Int
)

data class UpdateStockRequest(
    val currentStock: Int,
    val notes: String = ""
)

data class UpdateQuantityRequest(
    val quantity: Int
)

// ==================== RESPONSE MODELS ====================

data class ApiResponse<T>(
    val status: String,
    val message: String,
    val data: T?
)

data class AuthData(
    val token: String,
    val expiresIn: Int?,
    val user: User?
)

data class User(
    val id: Int,
    val email: String,
    val name: String?,
    val full_name: String?,
    val role: String?
)

data class Product(
    val id: Int,
    val name: String,
    val sell_price: Double,
    val cost_price: Double?,
    val category_id: Int?,
    val image_url: String?,
    val is_active: Boolean?,
    val category: Category?
)

data class Category(
    val id: Int,
    val name: String,
    val color_hex: String?,
    val icon_name: String?,
    val is_active: Boolean?
)

data class InventoryItem(
    val id: Int,
    val product_id: Int,
    val current_stock: Int,
    val min_stock: Int,
    val unit: String?,
    val product: Product?
)

data class Transaction(
    val id: Int,
    val transaction_code: String?,
    val user_id: Int?,
    val total_amount: Double?,
    val discount_amount: Double?,
    val grand_total: Double?,
    val payment_method: String?,
    val payment_amount: Double?,
    val change_amount: Double?,
    val status: String?,
    val created_at: String?,
    val details: List<TransactionDetail>?,
    val user: User?
)

data class TransactionDetail(
    val id: Int,
    val product_id: Int?,
    val product_name: String?,
    val sell_price: Double?,
    val quantity: Int?,
    val subtotal: Double?,
    val product: Product?
)

data class DashboardData(
    val totalRevenue: Double?,
    val totalTransactions: Int?,
    val topProducts: List<Product>?,
    val lowStockAlerts: List<InventoryItem>?
)

data class CheckoutResponse(
    val transactionId: Int?,
    val transactionCode: String?,
    val grandTotal: Double?,
    val changeAmount: Double?
)

// ==================== API INTERFACE ====================

interface ApiService {

    // --- Auth ---
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<AuthData>>

    @POST("auth/logout")
    suspend fun logout(): Response<ApiResponse<Any>>

    // --- Dashboard ---
    @GET("dashboard")
    suspend fun getDashboard(): Response<ApiResponse<DashboardData>>

    // --- Products ---
    @GET("products")
    suspend fun getProducts(): Response<ApiResponse<List<Product>>>

    @GET("products/{id}")
    suspend fun getProduct(@Path("id") id: Int): Response<ApiResponse<Product>>

    // --- Categories ---
    @GET("categories")
    suspend fun getCategories(): Response<ApiResponse<List<Category>>>

    // --- Inventory ---
    @GET("inventory")
    suspend fun getInventory(): Response<ApiResponse<List<InventoryItem>>>

    @PUT("inventory/{productId}")
    suspend fun updateStock(
        @Path("productId") productId: Int,
        @Body request: UpdateStockRequest
    ): Response<ApiResponse<InventoryItem>>

    // --- Transactions ---
    @GET("transactions")
    suspend fun getTransactions(): Response<ApiResponse<List<Transaction>>>

    @GET("transactions/{id}")
    suspend fun getTransaction(@Path("id") id: Int): Response<ApiResponse<Transaction>>

    @POST("transactions")
    suspend fun checkout(@Body request: CreateTransactionRequest): Response<ApiResponse<CheckoutResponse>>

    // --- Reports ---
    @GET("reports/daily")
    suspend fun getDailyReport(@Query("date") date: String): Response<ApiResponse<Any>>

    @GET("reports/monthly")
    suspend fun getMonthlyReport(@Query("month") month: String): Response<ApiResponse<Any>>
}
