package com.mobile.tugasrancangmoka.model

import com.google.gson.annotations.SerializedName

data class Category(
    val id: Int,
    val name: String,
    @SerializedName("color_hex") val colorHex: String,
    @SerializedName("is_active") val isActive: Boolean
)

data class CategoryResponse(
    val status: String,
    val data: List<Category>?
)
