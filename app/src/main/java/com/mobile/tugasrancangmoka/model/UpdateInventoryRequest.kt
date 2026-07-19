package com.mobile.tugasrancangmoka.model

import com.google.gson.annotations.SerializedName

data class UpdateInventoryRequest(
    @SerializedName("current_stock") val currentStock: Int,
    @SerializedName("min_stock") val minStock: Int,
    val unit: String? = "pcs"
)