package com.mobile.tugasrancangmoka.model

import com.google.gson.annotations.SerializedName

data class CheckoutResponse(
    val status: String,
    val message: String?,
    val data: CheckoutData?
)

data class CheckoutData(
    @SerializedName("transactionId") val transactionId: Int,
    @SerializedName("transactionCode") val transactionCode: String,
    @SerializedName("grandTotal") val grandTotal: Double,
    @SerializedName("changeAmount") val changeAmount: Double
)