package com.mobile.tugasrancangmoka.model

import com.google.gson.annotations.SerializedName

data class ReportResponse(
    val status: String,
    val message: String?,
    val data: List<ReportData>
)

data class ReportData(
    val date: String?,
    val month: String?,
    @SerializedName("total_transactions") val totalTransactions: Int,
    @SerializedName("total_revenue") val totalRevenue: Double,
    @SerializedName("total_cost") val totalCost: Double,
    @SerializedName("gross_profit") val grossProfit: Double
)