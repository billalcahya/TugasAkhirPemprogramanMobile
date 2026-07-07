package com.mobile.tugasrancangmoka.model

import com.google.gson.annotations.SerializedName

data class ReportResponse(
    val status: String,
    val data: ReportData?
)

data class ReportData(
    @SerializedName("total_revenue") val totalRevenue: Double,
    @SerializedName("gross_profit") val grossProfit: Double,
    @SerializedName("sales_trend") val salesTrend: List<SalesTrendData>
)

data class SalesTrendData(
    val label: String, // Tanggal atau Jam tergantung jenis laporan
    val revenue: Double
)