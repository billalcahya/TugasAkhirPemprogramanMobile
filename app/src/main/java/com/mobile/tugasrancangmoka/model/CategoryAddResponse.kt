package com.mobile.tugasrancangmoka.model

import com.google.gson.annotations.SerializedName

data class CategoryAddResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: Category
)