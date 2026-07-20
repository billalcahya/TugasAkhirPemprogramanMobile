package com.mobile.tugasrancangmoka.model

import com.google.gson.annotations.SerializedName

data class Product(
    val id: Int?,
    @SerializedName("category_id") val categoryId: Int?,
    val name: String,
    @SerializedName("sell_price") val sellPrice: Double,
    @SerializedName("cost_price") val costPrice: Double,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("initial_stock") val initialStock: Int?,
    @SerializedName("min_stock") val minStock: Int?,
    val unit: String?,
    val stock: Int?
)

data class SingleProductResponse(
    val status: String,
    val message: String?,
    val data: Product?
)