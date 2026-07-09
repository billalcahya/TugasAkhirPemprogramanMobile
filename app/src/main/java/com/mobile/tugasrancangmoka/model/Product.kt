package com.mobile.tugasrancangmoka.model

import com.google.gson.annotations.SerializedName

data class Product(
    val id: Int,
    @SerializedName("category_id") val categoryId: Int,
    val name: String, // Kembalikan ke String biasa
    @SerializedName("buy_price") val buyPrice: Double,
    @SerializedName("sell_price") val sellPrice: Double,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("is_active") val isActive: Boolean,
    val stock: Int
)

data class ProductResponse(
    val status: String,
    val data: List<Product>?
)

data class SingleProductResponse(
    val status: String,
    val data: Product?
)
