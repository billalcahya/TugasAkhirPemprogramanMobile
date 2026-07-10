package com.mobile.tugasrancangmoka.model

import com.google.gson.annotations.SerializedName
data class UpdateStockRequest(
    @SerializedName("current_stock") val currentStock: Int // Wajib 'current_stock' agar dibaca oleh backend
)
