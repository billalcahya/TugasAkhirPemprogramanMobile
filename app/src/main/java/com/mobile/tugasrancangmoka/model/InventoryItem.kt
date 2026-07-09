package com.mobile.tugasrancangmoka.model

import com.google.gson.annotations.SerializedName

data class InventoryResponse(
    val status: String,
    val data: List<InventoryItem>?
)

data class InventoryItem(
    val id: Int,
    @SerializedName("product_id") val productId: Int,
    @SerializedName("buy_price") val buyPrice: Double,
    @SerializedName("sell_price") val sellPrice: Double,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("is_active") val isActive: Boolean,
    @SerializedName("current_stock") val stock: Int, // Menangkap current_stock dari /inventory
    @SerializedName("products") val nestedProduct: NestedProduct?
) {
    val name: String
        get() = nestedProduct?.name ?: "Unknown Product"
}

data class NestedProduct(
    val name: String
)