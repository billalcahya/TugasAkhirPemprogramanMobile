package com.mobile.tugasrancangmoka.model

import com.google.gson.annotations.SerializedName

data class CheckoutRequest(
    @SerializedName("items") val items: List<CheckoutItem>,
    @SerializedName("discountAmount") val discountAmount: Double,
    @SerializedName("paymentMethod") val paymentMethod: String,
    @SerializedName("paymentAmount") val paymentAmount: Double
)

data class CheckoutItem(
    @SerializedName("productId") val productId: Int,
    @SerializedName("quantity") val quantity: Int
)