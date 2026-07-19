package com.mobile.tugasrancangmoka.model

import com.google.gson.annotations.SerializedName

data class InventoryResponse(
    val status: String,
    val data: List<InventoryItem>?
)

data class InventoryItem(
    val id: Int,
    @SerializedName("product_id") val productId: Int,
    @SerializedName("buy_price") val buyPrice: Double?,
    @SerializedName("sell_price") val sellPrice: Double?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("is_active") val isActive: Boolean?,
    @SerializedName("current_stock") val stock: Int?,
    @SerializedName("initial_stock") val initialStock: Int? = null,
    @SerializedName("min_stock") val minStock: Int? = null,
    val unit: String? = null,
    @SerializedName("products") val nestedProduct: NestedProduct?
) {
    val name: String
        get() = nestedProduct?.name ?: "Unknown Product"
}

data class NestedProduct(
    val name: String,
    @SerializedName("initial_stock") val initialStock: Int? = null,
    @SerializedName("min_stock") val minStock: Int? = null
)

data class SingleInventoryResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: InventoryItem
)