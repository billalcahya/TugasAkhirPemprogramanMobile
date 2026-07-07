package com.mobile.tugasrancangmoka.model

import com.google.gson.annotations.SerializedName

data class DashboardResponse(
    val status: String,
    val data: DashboardData?
)

data class DashboardData(
    @SerializedName("total_revenue_today") val totalRevenueToday: Double,
    @SerializedName("total_transactions_today") val totalTransactionsToday: Int,
    @SerializedName("last_7_days_sales") val last7DaysSales: List<SalesBarData>,
    @SerializedName("top_products") val topProducts: List<TopProduct>,
    @SerializedName("low_stock_alerts") val lowStockAlerts: List<LowStockAlert>
)

data class SalesBarData(
    val date: String, // format YYYY-MM-DD
    val total: Double
)

data class TopProduct(
    @SerializedName("product_name") val productName: String,
    val quantity: Int
)

data class LowStockAlert(
    @SerializedName("product_name") val productName: String,
    @SerializedName("current_stock") val currentStock: Int
)