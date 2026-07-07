package com.mobile.tugasrancangmoka.model

import com.google.gson.annotations.SerializedName

data class UpdateStockRequest(
    @SerializedName("stock") val stock: Int
)
