package com.mobile.tugasrancangmoka.model

import com.google.gson.annotations.SerializedName

data class Product(
    val id: Int?, // Dibuat nullable karena saat POST, id biasanya di-generate oleh server
    @SerializedName("category_id") val categoryId: Int,
    val name: String,
    @SerializedName("sell_price") val sellPrice: Double,
    @SerializedName("cost_price") val costPrice: Double, // Menyesuaikan "cost_price" dari Postman (pengganti buyPrice)
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("initial_stock") val initialStock: Int?, // Menyesuaikan "initial_stock" dari Postman
    @SerializedName("min_stock") val minStock: Int?, // Menyesuaikan "min_stock" dari Postman
    val unit: String?, // Menyesuaikan "unit" dari Postman
    val stock: Int? // Tetap dipertahankan jika server mengembalikan field stock saat GET
)

data class ProductResponse(
    val status: String,
    val message: String?, // Tambahkan opsional untuk menangkap pesan error/sukses dari server
    val data: List<Product>?
)

data class SingleProductResponse(
    val status: String,
    val message: String?,
    val data: Product?
)