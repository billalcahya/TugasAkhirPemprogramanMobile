package com.mobile.tugasrancangmoka.model

import com.google.gson.annotations.SerializedName

data class TransactionResponse(
    val status: String,
    val data: List<TransactionRecord>?
)

data class TransactionDetailResponse(
    val status: String,
    val data: List<TransactionRecord>?
)

data class TransactionRecord(
    val id: Int,
    @SerializedName("transaction_code") val transactionCode: String,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("total_amount") val totalAmount: Double,
    @SerializedName("discount_amount") val discountAmount: Double,
    @SerializedName("grand_total") val grandTotal: Double,
    @SerializedName("payment_method") val paymentMethod: String,
    @SerializedName("payment_amount") val paymentAmount: Double,
    @SerializedName("change_amount") val changeAmount: Double,
    val status: String, // 'completed' atau 'voided'
    @SerializedName("void_reason") val voidReason: String?,
    @SerializedName("created_at") val createdAt: String,
    val items: List<TransactionDetailItem>?
)

data class TransactionDetailItem(
    val id: Int,
    @SerializedName("product_id") val productId: Int,
    @SerializedName("product_name") val productName: String,
    @SerializedName("sell_price") val sellPrice: Double,
    val quantity: Int,
    val subtotal: Double
)

data class VoidRequest(
    @SerializedName("void_reason") val voidReason: String
)